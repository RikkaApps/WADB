#include "netlink.h"

#include <stdint.h>
#include <errno.h>
#include <string.h>

#include <asm/types.h>
#include <sys/socket.h>
#include <linux/netlink.h>
#include <linux/if_addr.h>
#include <linux/rtnetlink.h>
#include <net/if.h>
#include <net/if_arp.h>
#include <arpa/inet.h>

#include "unique_fd.h"

#ifdef ANDROID
#include <android/log.h>
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, "wadb_netlink", __VA_ARGS__)
#else
#include <stdio.h>
#define ALOGE(...) fprintf(stderr, "\n[ERROR] wadb_netlink: " __VA_ARGS__)
#endif

namespace wadb::netlink {
    namespace {
        ssize_t send_get_link_msg(int fd) {
            struct Request {
                nlmsghdr nh;
                ifinfomsg ifi;
            };

            Request req{};
            req.nh.nlmsg_len = sizeof(req);
            req.nh.nlmsg_type = RTM_GETLINK;
            req.nh.nlmsg_flags = NLM_F_REQUEST | NLM_F_DUMP;
            req.nh.nlmsg_seq = 0;
            req.ifi.ifi_family = AF_INET;
            req.ifi.ifi_type = ARPHRD_NETROM;

            sockaddr_nl sa{};
            sa.nl_family = AF_NETLINK;
            iovec iov{};
            iov.iov_base = &req;
            iov.iov_len = sizeof(req);

            msghdr msg{};
            msg.msg_name = &sa;
            msg.msg_namelen = sizeof(sa);
            msg.msg_iov = &iov;
            msg.msg_iovlen = 1;

            return sendmsg(fd, &msg, 0);
        }

        ssize_t send_get_addr_msg(int fd, uint if_index) {
            struct Request {
                nlmsghdr nh;
                ifaddrmsg ifa;
            };

            Request req{};
            req.nh.nlmsg_len = sizeof(req);
            req.nh.nlmsg_type = RTM_GETADDR;
            req.nh.nlmsg_flags = NLM_F_REQUEST | NLM_F_DUMP;
            req.nh.nlmsg_seq = 1;
            req.ifa.ifa_family = AF_INET;
            req.ifa.ifa_index = if_index;

            sockaddr_nl sa{};
            sa.nl_family = AF_NETLINK;
            iovec iov{};
            iov.iov_base = &req;
            iov.iov_len = sizeof(req);

            msghdr msg{};
            msg.msg_name = &sa;
            msg.msg_namelen = sizeof(sa);
            msg.msg_iov = &iov;
            msg.msg_iovlen = 1;

            return sendmsg(fd, &msg, 0);
        }

        ssize_t recv_netlink_msg(int fd, void *rcvbuf, size_t rcvbuf_len) {
            sockaddr_nl sa{};
            sa.nl_family = AF_NETLINK;
            iovec iov{};
            iov.iov_base = rcvbuf;
            iov.iov_len = rcvbuf_len;

            msghdr msg{};
            msg.msg_name = &sa;
            msg.msg_namelen = sizeof(sa);
            msg.msg_iov = &iov;
            msg.msg_iovlen = 1;

            return recvmsg(fd, &msg, 0);
        }

        int parse_netlink_getlink_response_for_ifidx(const void *buf, size_t len, const std::string &if_name, uint &outidx) {
            uint16_t last_nlmsg_type = NLMSG_DONE;

            for (auto *nh = reinterpret_cast<const nlmsghdr *>(buf);
                 NLMSG_OK(nh, len);
                 nh = NLMSG_NEXT(nh, len)) {
                last_nlmsg_type = nh->nlmsg_type;

                if (nh->nlmsg_type == NLMSG_ERROR) {
                    return -1;
                } else if (outidx == 0 && nh->nlmsg_type == RTM_NEWLINK) {
                    auto ifi = reinterpret_cast<const ifinfomsg *>(NLMSG_DATA(nh));
                    auto ifi_len = IFLA_PAYLOAD(nh);

                    for (auto rta = reinterpret_cast<const rtattr *>(IFLA_RTA(ifi));
                         RTA_OK(rta, ifi_len);
                         rta = RTA_NEXT(rta, ifi_len)) {
                        if (rta->rta_type == IFLA_IFNAME) {
                            std::string_view rta_if_name{reinterpret_cast<const char *>(RTA_DATA(rta))};
                            if (rta_if_name == if_name) {
                                outidx = ifi->ifi_index;

                                // we cannot stop here with NLMSG_DONE,
                                // since we need to purge remaining response
                                return last_nlmsg_type;
                            }
                        }
                    }

                }
            }

            return last_nlmsg_type;
        }

        int parse_netlink_getaddr_response_for_ips(const void *buf, size_t len, uint if_index, std::vector<std::string> &ips) {
            uint16_t last_nlmsg_type = NLMSG_DONE;

            for (auto *nh = reinterpret_cast<const nlmsghdr *>(buf);
                 NLMSG_OK(nh, len);
                 nh = NLMSG_NEXT(nh, len)) {
                last_nlmsg_type = nh->nlmsg_type;

                if (nh->nlmsg_type == NLMSG_ERROR) {
                    return NLMSG_ERROR;
                } else if (nh->nlmsg_type == RTM_NEWADDR) {
                    auto ifa = reinterpret_cast<const ifaddrmsg *>(NLMSG_DATA(nh));
                    auto ifa_len = IFA_PAYLOAD(nh);

                    // double check for kernel without NETLINK_GET_STRICT_CHK
                    if (ifa->ifa_index == if_index) {
                        for (auto rta = reinterpret_cast<const rtattr *>(IFA_RTA(ifa));
                             RTA_OK(rta, ifa_len);
                             rta = RTA_NEXT(rta, ifa_len)) {
                            if (rta->rta_type == IFA_ADDRESS) {
                                std::string ip_str_buf(64, '\0');
                                inet_ntop(ifa->ifa_family, RTA_DATA(rta), ip_str_buf.data(), ip_str_buf.size());
                                ips.emplace_back(ip_str_buf.data());
                            }
                        }
                    }
                }
            }

            return last_nlmsg_type;
        }
    }

    int get_interface_ips(const std::string &if_name, std::vector<std::string> &ips) {
        hx::tux::unique_fd fd{socket(AF_NETLINK, SOCK_RAW|SOCK_CLOEXEC, NETLINK_ROUTE)};
        if (fd == -1) {
            ALOGE("socket(AF_NETLINK) failed with errno=%d", errno);
        }
        constexpr uint sndbuf_size{/*32 KB*/ 32 * (1u << 10)};
        if (auto ret = setsockopt(fd.get(), SOL_SOCKET, SO_SNDBUF, &sndbuf_size, sizeof(sndbuf_size));
            ret < 0) {
            ALOGE("setsockopt(SO_SNDBUF) failed with ret=%d, errno=%d", ret, errno);
            return ret;
        }
        constexpr uint rcvbuf_size{/*32 KB*/ 32 * (1u << 10)};
        if (auto ret = setsockopt(fd.get(), SOL_SOCKET, SO_RCVBUF, &rcvbuf_size, sizeof(rcvbuf_size));
            ret < 0) {
            ALOGE("setsockopt(SO_RCVBUF) failed with ret=%d, errno=%d", ret, errno);
            return ret;
        }
        std::vector<char> rcvbuf(static_cast<size_t>(rcvbuf_size));
        int enabled = 1;
        if (auto ret = setsockopt(3, SOL_NETLINK, NETLINK_GET_STRICT_CHK, &enabled, 4);
            ret < 0) {
            ALOGE("setsockopt(NETLINK_GET_STRICT_CHK) failed with ret=%d, errno=%d", ret, errno);
            // unsupported on old kernel
        }

        // to filter the result of RTM_GETADDR, we need if_index instead of if_name
        uint target_if_index{};

        // both if_indextoname(3) and if_nametoindex(3) won't work on android (permission denied),
        // and if_nameindex(3) requires SDK >= 24 (we are 23 now),
        // so we send a RTM_GETLINK request and parse the mapping between if_index and if_name by ourselves.
        if (auto ret = send_get_link_msg(fd.get());
            ret < 0) {
            ALOGE("send_get_link_msg failed with ret=%zd, errno=%d", ret, errno);
            return ret;
        }

        while (true) {
            auto len = recv_netlink_msg(fd.get(), rcvbuf.data(), rcvbuf_size);

            if (len < 0) {
                ALOGE("recv_netlink_msg failed with ret=%zd, errno=%d", len, errno);
                break;
            }

            if (len == 0) {
                // eof
                break;
            }

            auto last_netlink_msg_type = parse_netlink_getlink_response_for_ifidx(rcvbuf.data(), len, if_name, target_if_index);

            if (last_netlink_msg_type == NLMSG_DONE) {
                break;
            }

            if (last_netlink_msg_type == NLMSG_ERROR) {
                ALOGE("parse_netlink_getlink_response_for_ifidx: NLMSG_ERROR");
                return -1;
            }
        }

        if (target_if_index == 0) {
            ALOGE("if_index not found for %s", if_name.c_str());
            return -1;
        }

        // now we can filter the result with the if_index we got
        if (auto ret = send_get_addr_msg(fd.get(), target_if_index);
            ret < 0) {
            ALOGE("send_get_addr_msg failed with ret=%zd, errno=%d", ret, errno);
            return ret;
        }

        while (true) {
            auto len = recv_netlink_msg(fd.get(), rcvbuf.data(), rcvbuf_size);

            if (len < 0) {
                ALOGE("recv_netlink_msg failed with ret=%zd, errno=%d", len, errno);
                break;
            }

            if (len == 0) {
                // eof
                break;
            }

            auto last_netlink_msg_type = parse_netlink_getaddr_response_for_ips(rcvbuf.data(), len, target_if_index, ips);

            if (last_netlink_msg_type == NLMSG_DONE) {
                break;
            }

            if (last_netlink_msg_type == NLMSG_ERROR) {
                ALOGE("parse_netlink_getaddr_response_for_ips: NLMSG_ERROR");
                return -1;
            }
        }

        return 0;
    }
}

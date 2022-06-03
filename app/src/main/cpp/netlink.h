#pragma once

#include <string>
#include <list>
#include <sys/socket.h>

namespace wadb::netlink {
    struct InterfaceIPPair {
        InterfaceIPPair() = default;
        InterfaceIPPair(uint idx, uint8_t family, std::string interface, std::string ip) :
                idx{idx},
                family{family},
                interface{std::move(interface)},
                ip{std::move(ip)} {
        }

        uint idx{0};
        uint8_t family{AF_INET};
        std::string interface{};
        std::string ip{};
    };

    int get_interface_ips(bool include_ipv6, std::list<InterfaceIPPair> &ips);
}
#include <vector>
#include <string>
#include <iostream>

#include "netlink.h"

int main(int argc, char *argv[]) {
    std::list<wadb::netlink::InterfaceIPPair> ips{};
    auto ret = wadb::netlink::get_interface_ips(false, ips);

    for (const auto &info : ips) {
        std::cout << info.idx << ": [" << info.interface << "] " << info.ip << std::endl;
    }

    return ret;
}

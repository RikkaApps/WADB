#include <vector>
#include <string>
#include <iostream>

#include "netlink.h"

int main(int argc, char *argv[]) {
    if (argc < 2) {
        printf("Usage: %s <interface>\n", argv[0]);
        return 22;
    }

    std::vector<std::string> ips{};
    auto ret = wadb::netlink::get_interface_ips(argv[1], ips);

    for (const auto &ip : ips) {
        std::cout << ip << std::endl;
    }

    return ret;
}

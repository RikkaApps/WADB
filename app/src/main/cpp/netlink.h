#pragma once

#include <vector>
#include <string>

namespace wadb::netlink {
    int get_interface_ips(const std::string &if_name, std::vector<std::string> &ips);
}
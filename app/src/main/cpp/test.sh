#!/bin/bash

set -e

clang++ -std=c++17 -g3 -Og \
    netlink_test.cpp \
    netlink.cpp \
    -o netlink_test

time ./netlink_test "$@"

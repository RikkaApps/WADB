cmake_minimum_required(VERSION 3.4.1)

project("wadb")

set(CMAKE_CXX_STANDARD 17)
set(LIBRARY_NAME "wadb")

include_directories(.)

add_library(${LIBRARY_NAME} SHARED
        wadb-jni.cpp
        netlink.cpp
        unique_fd.cpp)

target_link_libraries(${LIBRARY_NAME}
        log)

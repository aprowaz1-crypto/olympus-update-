cmake_minimum_required(VERSION 3.22.1)
project(olympus_port)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(RPCS3_SRC_DIR "${CMAKE_SOURCE_DIR}/../../../../third_party/rpcs3")

add_library(olympus_port SHARED
    native-lib.cpp
)

if(EXISTS "${RPCS3_SRC_DIR}")
    target_compile_definitions(olympus_port PRIVATE OLYMPUS_HAS_RPCS3_SOURCE=1)
endif()

find_library(log-lib log)

target_link_libraries(olympus_port
    ${log-lib}
)

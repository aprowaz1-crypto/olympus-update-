cmake_minimum_required(VERSION 3.22.1)
project(helios3_port)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(RPCS3_SRC_DIR "${CMAKE_SOURCE_DIR}/../../../../third_party/rpcs3")

add_library(helios3_port SHARED
    native-lib.cpp
    android_runtime.cpp
)

if(EXISTS "${RPCS3_SRC_DIR}")
    target_compile_definitions(helios3_port PRIVATE HELIOS3_HAS_RPCS3_SOURCE=1)
endif()

find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(helios3_port
    ${log-lib}
    ${android-lib}
)

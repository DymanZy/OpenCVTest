cmake_minimum_required(VERSION 3.4.1)

set(NATIVE_CPP_SOURCE   src/main/cpp/native-cpp.cpp)

add_library(native-cpp SHARED ${NATIVE_CPP_SOURCE})

include_directories(src/main/cpp/include/)
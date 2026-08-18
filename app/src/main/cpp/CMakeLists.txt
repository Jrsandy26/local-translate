cmake_minimum_required(VERSION 3.10.2)

project(whisper_android)

# Disable warnings for 3rd party code
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -DNDEBUG -Wall -Wextra -Wno-unused-parameter -Wno-unused-function")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -DNDEBUG -Wall -Wextra -Wno-unused-parameter -Wno-unused-function")

# Enable ARM NEON
if (ANDROID_ABI STREQUAL "armeabi-v7a")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -mfpu=neon")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mfpu=neon")
endif()

add_library(
        whisper_jni
        SHARED
        whisper_jni.cpp
        whisper.cpp
        ggml.c
        ggml-alloc.c
        ggml-backend.c
        ggml-quants.c
)

find_library(
        log-lib
        log
)

target_link_libraries(
        whisper_jni
        ${log-lib}
)

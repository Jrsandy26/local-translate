with open("app/src/main/cpp/CMakeLists.txt", "r") as f:
    data = f.read()

import re

data = data.replace('set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -mfpu=neon")', 'set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -mfloat-abi=softfp -mfpu=neon")')
data = data.replace('set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mfpu=neon")', 'set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mfloat-abi=softfp -mfpu=neon")')

# Also fix the Aarch64 check to not use -mfpu=neon for arm64
replacement = """if (${CMAKE_SYSTEM_PROCESSOR} MATCHES "arm" AND NOT ${CMAKE_SYSTEM_PROCESSOR} MATCHES "aarch64")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -mfpu=neon")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mfpu=neon")
endif()"""

data = re.sub(r'if \(\$\{CMAKE_SYSTEM_PROCESSOR\} MATCHES "arm" OR \$\{CMAKE_SYSTEM_PROCESSOR\} MATCHES "aarch64"\)\n    set\(CMAKE_C_FLAGS "\$\{CMAKE_C_FLAGS\} -mfpu=neon"\)\n    set\(CMAKE_CXX_FLAGS "\$\{CMAKE_CXX_FLAGS\} -mfpu=neon"\)\nendif\(\)', replacement, data)

with open("app/src/main/cpp/CMakeLists.txt", "w") as f:
    f.write(data)

#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_helios3_launcher_NativeBridge_nativeGetCoreStatus(JNIEnv* env, jobject /* this */) {
#ifdef HELIOS3_HAS_RPCS3_SOURCE
    std::string status = "Native RPCS3 port layer is active. Upstream core source is present for Android integration.";
#else
    std::string status = "Native Android port layer is active. Import the upstream RPCS3 source tree to continue the real port.";
#endif
    return env->NewStringUTF(status.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_helios3_launcher_NativeBridge_nativeGetBuildInfo(JNIEnv* env, jobject /* this */) {
#if defined(__aarch64__)
    const char* arch = "arm64-v8a";
#elif defined(__x86_64__)
    const char* arch = "x86_64";
#else
    const char* arch = "unknown";
#endif

    std::string info = std::string("NDK bridge ready on ") + arch + ". Vulkan, audio, input, and memory mapping hooks belong here for the Android RPCS3 port.";
    return env->NewStringUTF(info.c_str());
}

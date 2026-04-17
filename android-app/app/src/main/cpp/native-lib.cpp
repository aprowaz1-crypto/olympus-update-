#include <jni.h>
#include <string>

namespace {
#ifdef HELIOS3_RPCS3_GIT_SHA
constexpr const char* k_rpcs3_git = HELIOS3_RPCS3_GIT_SHA;
#else
constexpr const char* k_rpcs3_git = "bootstrap";
#endif

std::string g_core_status = "Native RPCS3 bootstrap ready for Android core start.";
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_helios3_launcher_NativeBridge_nativeGetCoreStatus(JNIEnv* env, jobject /* this */) {
    return env->NewStringUTF(g_core_status.c_str());
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

#ifdef HELIOS3_HAS_RPCS3_SOURCE
    std::string info = std::string("Upstream RPCS3 source imported • ") + k_rpcs3_git + " • " + arch;
#else
    std::string info = std::string("Android bootstrap core • ") + arch;
#endif
    return env->NewStringUTF(info.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_helios3_launcher_NativeBridge_nativeStartCore(
    JNIEnv* env,
    jobject /* this */,
    jboolean firmwareInstalled,
    jstring driverMode,
    jstring renderer,
    jstring gameTitle) {
    const char* driver_chars = driverMode ? env->GetStringUTFChars(driverMode, nullptr) : "System default";
    const char* renderer_chars = renderer ? env->GetStringUTFChars(renderer, nullptr) : "Vulkan";
    const char* game_chars = gameTitle ? env->GetStringUTFChars(gameTitle, nullptr) : "Selected game";

    if (!firmwareInstalled) {
        g_core_status = "PS3 firmware is still required before the native core can boot.";
    } else {
        g_core_status = std::string("Launching ") + game_chars + " • " + renderer_chars + " • " + driver_chars;
    }

    if (driverMode) {
        env->ReleaseStringUTFChars(driverMode, driver_chars);
    }
    if (renderer) {
        env->ReleaseStringUTFChars(renderer, renderer_chars);
    }
    if (gameTitle) {
        env->ReleaseStringUTFChars(gameTitle, game_chars);
    }

    return env->NewStringUTF(g_core_status.c_str());
}

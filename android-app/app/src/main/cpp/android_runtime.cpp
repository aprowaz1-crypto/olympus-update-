#include <jni.h>
#include <android/log.h>
#include <android/native_window_jni.h>
#include <mutex>

namespace {
std::mutex g_window_mutex;
ANativeWindow* g_window = nullptr;

void release_window_locked() {
    if (g_window) {
        ANativeWindow_release(g_window);
        g_window = nullptr;
    }
}
}

extern "C" JNIEXPORT void JNICALL
Java_com_olympus_launcher_NativeBridge_nativeSetSurface(JNIEnv* env, jobject /* this */, jobject surface, jint width, jint height) {
    std::lock_guard<std::mutex> lock(g_window_mutex);
    release_window_locked();
    if (surface) {
        g_window = ANativeWindow_fromSurface(env, surface);
        __android_log_print(ANDROID_LOG_INFO, "OlympusPort", "Surface attached: %dx%d", width, height);
    } else {
        __android_log_print(ANDROID_LOG_INFO, "OlympusPort", "Surface cleared during update");
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_olympus_launcher_NativeBridge_nativeClearSurface(JNIEnv* /* env */, jobject /* this */) {
    std::lock_guard<std::mutex> lock(g_window_mutex);
    release_window_locked();
    __android_log_print(ANDROID_LOG_INFO, "OlympusPort", "Surface released");
}

extern "C" JNIEXPORT void JNICALL
Java_com_olympus_launcher_NativeBridge_nativeOnGamepadState(JNIEnv* env, jobject /* this */, jfloatArray axes, jintArray buttons) {
    const auto axis_count = axes ? env->GetArrayLength(axes) : 0;
    const auto button_count = buttons ? env->GetArrayLength(buttons) : 0;
    __android_log_print(ANDROID_LOG_INFO, "OlympusPort", "Gamepad update: axes=%d buttons=%d", axis_count, button_count);
}

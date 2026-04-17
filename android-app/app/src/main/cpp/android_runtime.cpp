#include <jni.h>
#include <android/log.h>
#include <android/native_window_jni.h>
#include <algorithm>
#include <cstdint>
#include <cstring>
#include <mutex>

namespace {
std::mutex g_window_mutex;
ANativeWindow* g_window = nullptr;
float g_last_axes[6] = {0.f, 0.f, 0.f, 0.f, 0.f, 0.f};
int g_last_buttons[4] = {0, 0, 0, 0};

uint32_t pack_rgb(uint8_t r, uint8_t g, uint8_t b) {
    return 0xff000000u | (static_cast<uint32_t>(r) << 16) | (static_cast<uint32_t>(g) << 8) | b;
}

void release_window_locked() {
    if (g_window) {
        ANativeWindow_release(g_window);
        g_window = nullptr;
    }
}

void draw_preview_locked() {
    if (!g_window) {
        return;
    }

    ANativeWindow_setBuffersGeometry(g_window, 0, 0, WINDOW_FORMAT_RGBA_8888);
    ANativeWindow_Buffer buffer;
    if (ANativeWindow_lock(g_window, &buffer, nullptr) != 0) {
        return;
    }

    auto* pixels = static_cast<uint32_t*>(buffer.bits);
    const int width = buffer.width;
    const int height = buffer.height;
    const float lx = std::clamp((g_last_axes[0] + 1.0f) * 0.5f, 0.0f, 1.0f);
    const float ly = std::clamp((g_last_axes[1] + 1.0f) * 0.5f, 0.0f, 1.0f);
    const bool any_button = g_last_buttons[0] || g_last_buttons[1] || g_last_buttons[2] || g_last_buttons[3];

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            const uint8_t r = static_cast<uint8_t>(20 + (180 * x) / std::max(1, width));
            const uint8_t g = static_cast<uint8_t>(30 + (140 * y) / std::max(1, height));
            const uint8_t b = static_cast<uint8_t>(80 + (80 * (x + y)) / std::max(1, width + height));
            pixels[y * buffer.stride + x] = pack_rgb(r, g, b);
        }
    }

    const int bar_y = height / 2;
    const int bar_h = std::max(12, height / 12);
    const int lx_w = static_cast<int>(width * lx);
    const int ly_w = static_cast<int>(width * ly);

    for (int y = std::max(0, bar_y - bar_h); y < std::min(height, bar_y); ++y) {
        for (int x = 0; x < lx_w; ++x) {
            pixels[y * buffer.stride + x] = pack_rgb(95, 140, 255);
        }
    }

    for (int y = bar_y; y < std::min(height, bar_y + bar_h); ++y) {
        for (int x = 0; x < ly_w; ++x) {
            pixels[y * buffer.stride + x] = pack_rgb(124, 199, 255);
        }
    }

    if (any_button) {
        for (int y = 0; y < std::min(height, bar_h); ++y) {
            for (int x = width / 2 - 40; x < std::min(width, width / 2 + 40); ++x) {
                if (x >= 0) {
                    pixels[y * buffer.stride + x] = pack_rgb(255, 210, 90);
                }
            }
        }
    }

    ANativeWindow_unlockAndPost(g_window);
}
}

extern "C" JNIEXPORT void JNICALL
Java_com_helios3_launcher_NativeBridge_nativeSetSurface(JNIEnv* env, jobject /* this */, jobject surface, jint width, jint height) {
    std::lock_guard<std::mutex> lock(g_window_mutex);
    release_window_locked();
    if (surface) {
        g_window = ANativeWindow_fromSurface(env, surface);
        draw_preview_locked();
        __android_log_print(ANDROID_LOG_INFO, "Helios3Port", "Surface attached: %dx%d", width, height);
    } else {
        __android_log_print(ANDROID_LOG_INFO, "Helios3Port", "Surface cleared during update");
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_helios3_launcher_NativeBridge_nativeClearSurface(JNIEnv* /* env */, jobject /* this */) {
    std::lock_guard<std::mutex> lock(g_window_mutex);
    release_window_locked();
    __android_log_print(ANDROID_LOG_INFO, "Helios3Port", "Surface released");
}

extern "C" JNIEXPORT void JNICALL
Java_com_helios3_launcher_NativeBridge_nativeOnGamepadState(JNIEnv* env, jobject /* this */, jfloatArray axes, jintArray buttons) {
    std::lock_guard<std::mutex> lock(g_window_mutex);

    if (axes) {
        const auto axis_count = std::min(static_cast<int>(env->GetArrayLength(axes)), 6);
        jfloat* axis_data = env->GetFloatArrayElements(axes, nullptr);
        for (int i = 0; i < axis_count; ++i) {
            g_last_axes[i] = axis_data[i];
        }
        env->ReleaseFloatArrayElements(axes, axis_data, JNI_ABORT);
    }

    if (buttons) {
        const auto button_count = std::min(static_cast<int>(env->GetArrayLength(buttons)), 4);
        jint* button_data = env->GetIntArrayElements(buttons, nullptr);
        for (int i = 0; i < button_count; ++i) {
            g_last_buttons[i] = button_data[i];
        }
        env->ReleaseIntArrayElements(buttons, button_data, JNI_ABORT);
    }

    draw_preview_locked();
    __android_log_print(ANDROID_LOG_INFO, "Helios3Port", "Gamepad state refreshed in native preview");
}

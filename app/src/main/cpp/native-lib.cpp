#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "DegoogledAndroidAuto"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_degoogled_androidauto_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Degoogled Android Auto Native Library";
    return env->NewStringUTF(hello.c_str());
}
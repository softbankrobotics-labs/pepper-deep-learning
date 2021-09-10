#include <jni.h>
#include <string>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include "yolo.h"

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved)
{
}

extern "C" JNIEXPORT void JNICALL
Java_com_softbankrobotics_pepper_1neural_1network_1ncnn_Yolo_00024Companion_init(JNIEnv* env, jobject pThis, jobject assetManager, jstring param, jstring bin) {
    if(Yolo::detector == nullptr){
        AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
        const char *nParam = env->GetStringUTFChars(param, nullptr);
        const char *nBin = env->GetStringUTFChars(bin, nullptr);
        Yolo::detector = new Yolo(mgr, nParam, nBin);
        env->ReleaseStringUTFChars(param, nParam);
        env->ReleaseStringUTFChars(bin, nBin);
    }
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_softbankrobotics_pepper_1neural_1network_1ncnn_Yolo_00024Companion_detect(JNIEnv* env, jobject pThis, jobject image, jdouble threshold, jdouble nms_threshold) {
    // FIXME: check detector is not null

    auto result = Yolo::detector->detect(env,image);

    auto box_cls = env->FindClass("com/softbankrobotics/pepper_neural_network_ncnn/Box");
    auto cid = env->GetMethodID(box_cls, "<init>", "(FFFFIF)V");
    jobjectArray ret = env->NewObjectArray( result.size(), box_cls, nullptr);
    int i = 0;
    for(auto& box:result){
        env->PushLocalFrame(1);
        jobject obj = env->NewObject(box_cls, cid,box.x1,box.y1,box.x2,box.y2,box.label,box.score);
        obj = env->PopLocalFrame(obj);
        env->SetObjectArrayElement( ret, i++, obj);
    }
    return ret;
}
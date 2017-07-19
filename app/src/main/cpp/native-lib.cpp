
/**
#include <jni.h>
#include <string>
/**/
//extern "C" jstring Java_com_aufthesis_encryption_MainActivity_stringFromJNI(JNIEnv *env, jobject /* this */)
/**
{
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
/**/
/**
#define JNICLASSNAME "org/cocos2dx/cpp/AppActivity"

extern "C" void setClipboardText(const std::string& text, JNIEnv *env){
    JniMethodInfo methodInfo;
    if(JniHelper::getStaticMethodInfo(methodInfo , JNICLASSNAME , "setClipboardText", "(Ljava/lang/String;)V")){
        jstring str = methodInfo.env->NewStringUTF(text.c_str());
        methodInfo.env->CallStaticVoidMethod(methodInfo.classID , methodInfo.methodID , str);
        methodInfo.env->DeleteLocalRef(str);
        methodInfo.env->DeleteLocalRef(methodInfo.classID);
    }
}
extern "C" std::string getClipboardText(JNIEnv *env){
    std::string text = "N/A";
    if(JniHelper::getStaticMethodInfo(methodInfo , JNICLASSNAME , "getClipboardText", "()Ljava/lang/String;")){
        jstring jstr = (jstring)env->CallStaticObjectMethod (methodInfo.classID , methodInfo.methodID);
        const char* ptr = env->GetStringUTFChars(jstr, NULL);
        text = std::string(ptr);
        env->ReleaseStringUTFChars(jstr, ptr);
        env->DeleteLocalRef(methodInfo.classID);
    }
    return text;
}
 /**/
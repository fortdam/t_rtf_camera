#include <jni.h>

jstring test_func(JNIEnv *env, jobject *thiz) {
	return (*env)->NewStringUTF(env, "test from Tang");
}

static const char *classPathName = "com/example/t_rtf_camera/NativeProcessActivity";


static JNINativeMethod methods[] = {
  {"testJNI", "()Ljava/lang/String;", (void*)test_func },
};

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    clazz = (*env)->FindClass(env, className);
    if (clazz == 0) {
        //LOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if ((*env)->RegisterNatives(env, clazz, gMethods, numMethods) < 0) {
        //LOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Register native methods for all classes we know about.
 *
 * returns JNI_TRUE on success.
 */
static int registerNatives(JNIEnv* env)
{
  if (!registerNativeMethods(env, classPathName,
                 methods, sizeof(methods) / sizeof(methods[0]))) {
    return JNI_FALSE;
  }

  return JNI_TRUE;
}

typedef union {
    JNIEnv* env;
    void* venv;
} UnionJNIEnvToVoid;

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    jint result = -1;
    JNIEnv* env = 0;

    if ((*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_4) != JNI_OK) {
        //LOGE("ERROR: GetEnv failed");
        goto bail;
    }

    if (registerNatives(env) != JNI_TRUE) {
        //LOGE("ERROR: registerNatives failed");
        goto bail;
    }

    result = JNI_VERSION_1_4;

bail:
    return result;
}

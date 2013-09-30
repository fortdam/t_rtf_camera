    LOCAL_PATH := $(call my-dir)

    include $(CLEAR_VARS)

    LOCAL_MODULE    := image_process
    LOCAL_SRC_FILES := image_process.c

    include $(BUILD_SHARED_LIBRARY)
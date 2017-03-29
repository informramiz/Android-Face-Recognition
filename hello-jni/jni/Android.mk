LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include D:/Android/OpenCV-2.4.3.2-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES := hello-jni.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl

LOCAL_MODULE    := hello-jni    

include $(BUILD_SHARED_LIBRARY)

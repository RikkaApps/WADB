#include <jni.h>

#include <string>
#include <string_view>
#include <vector>
#include <assert.h>

#include "netlink.h"

namespace wadb {
    struct JniGlobal {
        JavaVM *vm;

        jmethodID m_List_add;
    };

    JniGlobal &jni_global() {
        static auto *p = new JniGlobal{};
        return *p;
    }

    JNIEnv *ensure_jni_env_for_current_thread() {
        auto vm = jni_global().vm;
        assert(vm != nullptr);
        JNIEnv *env{};
        vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);
        if (env == nullptr) {
            auto ret = vm->AttachCurrentThread(&env, nullptr);
            assert(ret == 0);
        }
        return env;
    }

    namespace jni_methods {
        void initializeNative(JNIEnv *env, jobject,
                              jobject methodListAdd) {
            jni_global().m_List_add = env->FromReflectedMethod(methodListAdd);
        }

        jint getInterfaceIps(JNIEnv *env, jobject,
                             jstring ifName, jobject outList) {
            auto chars_ifName = env->GetStringUTFChars(ifName, nullptr);
            std::string s_if_name{chars_ifName, static_cast<size_t>(env->GetStringUTFLength(ifName))};
            env->ReleaseStringUTFChars(ifName, chars_ifName);

            auto m_List_add = jni_global().m_List_add;
            assert(m_List_add != nullptr);

            auto append_to_java_list = [env, m_List_add, outList] (const std::string &s) {
                auto js = env->NewStringUTF(s.c_str());
                env->CallBooleanMethod(outList, m_List_add, js);
            };

            std::vector<std::string> ips{};
            auto ret = netlink::get_interface_ips(s_if_name, ips);

            for (const auto &ip : ips) {
                append_to_java_list(ip);
            }

            return ret;
        }
    }

    namespace {
        jint jni_on_load(JavaVM *vm, void *reserved) {
            jni_global().vm = vm;
            auto env = ensure_jni_env_for_current_thread();
            jclass jc_LibWADB = env->FindClass("moe/haruue/wadb/util/LibWADB");
            assert(jc_LibWADB != nullptr);
            std::vector<JNINativeMethod> methods{};
            /*initializeNative*/ {
                auto &m = methods.emplace_back();
                m.name = "initializeNative";
                m.signature = "(Ljava/lang/reflect/Method;)V";
                m.fnPtr = reinterpret_cast<void *>(jni_methods::initializeNative);
            }
            /*getInterfaceIps*/ {
                auto &m = methods.emplace_back();
                m.name = "getInterfaceIps";
                m.signature = "(Ljava/lang/String;Ljava/util/List;)I";
                m.fnPtr = reinterpret_cast<void *>(jni_methods::getInterfaceIps);
            }
            auto ret = env->RegisterNatives(jc_LibWADB, methods.data(), methods.size());
            if (ret < 0) {
                return JNI_ERR;
            }
            return JNI_VERSION_1_6;
        }
    }
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    return wadb::jni_on_load(vm, reserved);
}

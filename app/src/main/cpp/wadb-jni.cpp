#include <jni.h>

#include <string>
#include <string_view>
#include <vector>
#include <assert.h>

#include "netlink.h"

namespace wadb {
    struct JniGlobal {
        JavaVM *vm;

        jclass c_LibWADB;
        jmethodID m_ipsListAdd;
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
                              jobject method_ipsListAdd) {
            jni_global().m_ipsListAdd = env->FromReflectedMethod(method_ipsListAdd);
        }

        jint getInterfaceIps(JNIEnv *env, jobject,
                             jboolean j_include_ipv6, jobject outList) {
            auto c_LibWADB = jni_global().c_LibWADB;
            auto m_ipsListAdd = jni_global().m_ipsListAdd;
            assert(m_ipsListAdd != nullptr);

            auto append_to_java_list = [env, c_LibWADB, m_ipsListAdd, outList] (const auto &info) {
                auto js_if_name = env->NewStringUTF(info.interface.c_str());
                auto js_ip = env->NewStringUTF(info.ip.c_str());
                env->CallStaticVoidMethod(c_LibWADB, m_ipsListAdd, outList, info.idx, info.family, js_if_name, js_ip);
            };

            std::list<wadb::netlink::InterfaceIPPair> ips{};
            auto ret = netlink::get_interface_ips(j_include_ipv6 == JNI_TRUE, ips);

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
            jni_global().c_LibWADB = (jclass) env->NewGlobalRef(env->FindClass("moe/haruue/wadb/util/LibWADB"));
            assert(jni_global().c_LibWADB != nullptr);
            std::vector<JNINativeMethod> methods{};
            /*initializeNative*/ {
                auto &m = methods.emplace_back();
                m.name = "initializeNative";
                m.signature = "(Ljava/lang/reflect/Method;)V";
                m.fnPtr = reinterpret_cast<void *>(jni_methods::initializeNative);
            }
            /*getInterfaceIps*/ {
                auto &m = methods.emplace_back();
                m.name = "nativeGetInterfaceIps";
                m.signature = "(ZLjava/util/List;)I";
                m.fnPtr = reinterpret_cast<void *>(jni_methods::getInterfaceIps);
            }
            auto ret = env->RegisterNatives(jni_global().c_LibWADB, methods.data(), methods.size());
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

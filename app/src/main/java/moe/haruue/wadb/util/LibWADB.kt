package moe.haruue.wadb.util

import java.lang.reflect.Method

object LibWADB {
    init {
        System.loadLibrary("wadb")

        initializeNative(java.util.List::class.java.getMethod("add", java.lang.Object::class.java))
    }

    external fun initializeNative(methodListAdd: Method)
    external fun getInterfaceIps(ifName: String, outList: java.util.List<String>): Int
}
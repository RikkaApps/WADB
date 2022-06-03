package moe.haruue.wadb.util

import java.lang.IllegalStateException
import java.lang.reflect.Method

object LibWADB {
    init {
        System.loadLibrary("wadb")

        val method_ipsListAdd = let {
            for (method in LibWADB::class.java.declaredMethods) {
                val jnc = method.getAnnotation(JNC::class.java)
                if (jnc != null && jnc.id == JNC.ID_ipsListAdd) {
                    return@let method
                }
            }
            return@let null
        } ?: throw IllegalStateException("method ipsListAdd() not found")

        initializeNative(method_ipsListAdd)
    }

    private external fun initializeNative(method_ipsListAdd: Method)
    private external fun nativeGetInterfaceIps(includeIPv6: Boolean, outList: MutableList<InterfaceIPPair>): Int

    @JvmStatic
    fun getInterfaceIps(includeIPv6: Boolean): List<InterfaceIPPair> {
        val list = mutableListOf<InterfaceIPPair>()
        val ret = nativeGetInterfaceIps(includeIPv6, list)
        if (ret != 0) {
            throw IllegalStateException("failed to use netlink socket for interface ips: $ret")
        }
        list.removeAll { it.interfaceName == "lo" || it.interfaceName.contains("rmnet") }
        list.sort()
        return list
    }

    @JvmStatic
    @JNC(JNC.ID_ipsListAdd)
    fun ipsListAdd(outList: MutableList<InterfaceIPPair>, idx: Int, family: Byte, interfaceName: String, ip: String) {
        outList.add(InterfaceIPPair(idx, family.toInt(), interfaceName, ip))
    }

    data class InterfaceIPPair(
        val idx: Int,
        val family: Int,
        val interfaceName: String,
        val ip: String
    ) : Comparable<InterfaceIPPair> {
        private val interfaceNameOrder: Int = when {
            interfaceName.startsWith("wlan") -> 0
            interfaceName.startsWith("eth") -> 1
            else -> 99
        }

        override fun compareTo(other: InterfaceIPPair): Int {
            if (family != other.family) {
                return family.compareTo(other.family)
            }
            if (interfaceNameOrder != other.interfaceNameOrder) {
                return interfaceNameOrder.compareTo(other.interfaceNameOrder)
            }
            if (interfaceName != other.interfaceName) {
                return interfaceName.compareTo(interfaceName)
            }
            return ip.compareTo(other.ip)
        }
    }

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    private annotation class JNC(
        val id: Int,
    ) {
        companion object {
            const val ID_ipsListAdd = 1
        }
    }
}
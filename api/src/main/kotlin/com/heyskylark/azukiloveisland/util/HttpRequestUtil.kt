package com.heyskylark.azukiloveisland.util

import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder

import org.springframework.web.context.request.ServletRequestAttributes

@Component("httpRequestUtil")
class HttpRequestUtil {
    companion object {
        private val IP_HEADER_CANDIDATES = listOf(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        )
    }

    fun getClientIpAddressIfServletRequestExist(): String {
        if (RequestContextHolder.getRequestAttributes() == null) {
            return "0.0.0.0"
        }

        val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)?.request
            ?: throw RuntimeException("RequestContextHolder does not exist.")

        for (header in IP_HEADER_CANDIDATES) {
            val ipList = request.getHeader(header)
            if (ipList != null && ipList.isNotEmpty() && !"unknown".equals(ipList, ignoreCase = true)) {
                return ipList.split(",").toTypedArray()[0]
            }
        }

        return request.remoteAddr
    }
}
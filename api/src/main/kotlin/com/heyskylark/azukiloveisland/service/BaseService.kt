package com.heyskylark.azukiloveisland.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

abstract class BaseService {
    internal val LOG: Logger = LogManager.getLogger(this.javaClass)
}

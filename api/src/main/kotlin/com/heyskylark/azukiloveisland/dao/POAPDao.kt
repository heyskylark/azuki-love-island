package com.heyskylark.azukiloveisland.dao

import com.heyskylark.azukiloveisland.model.poap.SeasonPOAP
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository("poapDao")
interface POAPDao : CrudRepository<SeasonPOAP, Int>

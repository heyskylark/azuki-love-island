package com.heyskylark.azukiloveisland.service

import com.heyskylark.azukiloveisland.client.AzukiClient
import com.heyskylark.azukiloveisland.model.azuki.AzukiInfo
import com.heyskylark.azukiloveisland.model.azuki.BackgroundTrait
import com.heyskylark.azukiloveisland.serialization.ErrorResponse
import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import com.heyskylark.azukiloveisland.service.errorcode.Web3ErrorCodes
import java.net.URL
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction

/**
 * Service to gather info about Azuki from IPFS and the Ethereum blockchain.
 */
@Component("azukiWeb3Service")
class AzukiWeb3Service(
    private val web3j: Web3j,
    private val azukiClient: AzukiClient,
    @Value("\${azuki.contract.address}")
    private val azukiContractAddress: String
) : BaseService() {
    fun fetchAzukiInfo(azukiId: Long): ServiceResponse<AzukiInfo> {
        // TODO: Cache this
        val metadataResponse = azukiClient.getMetadata(azukiId)
        val metadata = if (metadataResponse.isSuccess()) {
            metadataResponse.getSuccessValue() ?: run {
                LOG.error("The metadata fetch returned successful but with no metadata")
                return ServiceResponse.errorResponse(Web3ErrorCodes.METADATA_MISSING)
            }
        } else {
            val errorResponse = metadataResponse as ErrorResponse

            LOG.error("There was an error while trying to fetch the metadata: ${errorResponse.errorCode.message}")
            return ServiceResponse.errorResponse(errorResponse.errorCode)
        }

        val backgroundTrait = metadata.attributes.firstOrNull { it.traitType == "Background" }
            ?: run {
                LOG.error("Could not find the background trait in the metadata")
                return ServiceResponse.errorResponse(Web3ErrorCodes.METADATA_MISSING)
            }

        return ServiceResponse.successResponse(
            AzukiInfo(
                azukiId = azukiId,
                azukiImageUrl = getAzukiImageUrl(azukiId),
                ownerAddress = getAzukiOwner(azukiId).toString(),
                backgroundTrait = BackgroundTrait.fromText(backgroundTrait.value)
            )
        )
    }

    private fun getAzukiImageUrl(azukiId: Long): URL {
        return URL("https://ikzttp.mypinata.cloud/ipfs/QmYDvPAXtiJg7s8JdRBSLWdgSphQdac8j1YuQNNxcGE1hg/$azukiId.png")
    }

    private fun getAzukiOwner(azukiId: Long): Address {
        val addressType = TypeReference.makeTypeReference("address")
        val ownerOfFunction = org.web3j.abi.datatypes.Function(
            "ownerOf",
            listOf(Uint256(azukiId)),
            listOf(addressType)
        )
        val encodedFunction = FunctionEncoder.encode(ownerOfFunction)
        val transaction = Transaction.createEthCallTransaction(
            "0x0c9A1A471079995d034E7ddDa01aae6FDa66Bc7d",
            azukiContractAddress,
            encodedFunction
        )

        val request = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST)
        try {
            val response = request.send()
            response.error?.let {
                throw RuntimeException(it.message)
            }

            val responseData = FunctionReturnDecoder
                .decode(response.value, listOf(TypeReference.makeTypeReference("address")))

            return responseData.firstOrNull() as? Address
                ?: throw RuntimeException("Owner address was not returned for Azuki: $azukiId")
        } catch (e: Exception) {
            LOG.error("There was a problem getting the owner address of Azuki: $azukiId", e)
            throw e
        }
    }
}

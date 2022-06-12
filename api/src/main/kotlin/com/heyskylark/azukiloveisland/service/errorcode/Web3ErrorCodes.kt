package com.heyskylark.azukiloveisland.service.errorcode

enum class Web3ErrorCodes(
    override val message: String,
    override val code: String,
    override val type: ErrorType
) : ErrorCode {
    METADATA_MISSING(
        message = "Metadata is missing",
        code = "metadataMissing",
        type = ErrorType.INTERNAL_SERVER_ERROR
    ),
    INVALID_NFT_ID(
        message = "The given NFT ID is invalid",
        code = "invalidNFTId",
        type = ErrorType.BAD_REQUEST
    )
    ;
}

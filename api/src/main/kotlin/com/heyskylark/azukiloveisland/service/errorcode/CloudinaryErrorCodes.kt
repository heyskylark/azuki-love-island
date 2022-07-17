package com.heyskylark.azukiloveisland.service.errorcode

enum class CloudinaryErrorCodes(
    override val message: String,
    override val code: String,
    override val type: ErrorType
) : ErrorCode {
    IMAGE_FILE_TOO_LARGE(
        code = "imageFileTooLarge",
        message = "An image file can only have a max size of 10 MB.",
        type = ErrorType.BAD_REQUEST
    )
    ;
}

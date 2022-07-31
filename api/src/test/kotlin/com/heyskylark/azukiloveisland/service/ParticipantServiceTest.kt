package com.heyskylark.azukiloveisland.service

import com.heyskylark.azukiloveisland.dao.InitialBracketDao
import com.heyskylark.azukiloveisland.dao.ParticipantDao
import com.heyskylark.azukiloveisland.dto.ParticipantSubmissionDto
import com.heyskylark.azukiloveisland.factory.AzukiInfoFactory.Companion.DEFAULT_OWNER_ADDRESS
import com.heyskylark.azukiloveisland.factory.AzukiInfoFactory.Companion.createAzukiInfo
import com.heyskylark.azukiloveisland.factory.ParticipantFactory.Companion.createParticipant
import com.heyskylark.azukiloveisland.factory.SeasonFactory.Companion.createSeason
import com.heyskylark.azukiloveisland.model.Participant
import com.heyskylark.azukiloveisland.model.azuki.Gender
import com.heyskylark.azukiloveisland.serialization.ErrorResponse
import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import com.heyskylark.azukiloveisland.service.errorcode.ParticipantErrorCodes
import com.heyskylark.azukiloveisland.service.errorcode.Web3ErrorCodes
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ParticipantServiceTest {
    companion object {
        private const val DEFAULT_AZUKI_ID = 4434L
        private const val DEFAULT_TWITTER_HANDLE = "heyskylark"
        private const val DEFAULT_QUOTE = "This is a valid quote."
        private const val DEFAULT_BIO = "This is a valid bio."
        private const val DEFAULT_HOBBIES = "hobby1,hobby2, hobby3,    hobby4"
        private val DEFAULT_HOBBIES_LIST = setOf("hobby1", "hobby2", "hobby3", "hobby4")
    }

    @MockK lateinit var azukiWeb3Service: AzukiWeb3Service
    @MockK lateinit var seasonService: SeasonService
    @MockK lateinit var participantDao: ParticipantDao
    @MockK lateinit var initialBracketDao: InitialBracketDao

    private lateinit var participantService: ParticipantService

    @BeforeEach
    fun setUp() {
        participantService = ParticipantService(
            azukiWeb3Service = azukiWeb3Service,
            seasonService = seasonService,
            participantDao = participantDao,
            initialBracketDao = initialBracketDao
        )
    }

    @Test
    fun `user successfully submits with a valid submission`() {
        val testDto = ParticipantSubmissionDto(
            azukiId = DEFAULT_AZUKI_ID,
            twitterHandle = DEFAULT_TWITTER_HANDLE,
            quote = DEFAULT_QUOTE,
            bio = DEFAULT_BIO,
            hobbies = DEFAULT_HOBBIES
        )
        val testSeason = createSeason(submissionActive = true)
        val testAzuki = createAzukiInfo(DEFAULT_AZUKI_ID)

        every { seasonService.getRawLatestSeason() } returns testSeason

        every {
            azukiWeb3Service.fetchAzukiInfo(DEFAULT_AZUKI_ID)
        } returns ServiceResponse.successResponse(testAzuki)

        every {
            participantDao.findByAzukiIdAndSeasonNumber(DEFAULT_AZUKI_ID, testSeason.seasonNumber)
        } returns null

        every {
            participantDao.findByOwnerAddressAndSeasonNumber(DEFAULT_OWNER_ADDRESS, testSeason.seasonNumber)
        } returns null

        every {
            participantDao.findBySeasonNumberAndTwitterHandleIgnoreCase(testSeason.seasonNumber, testDto.twitterHandle)
        } returns null

        every { participantDao.save(ofType(Participant::class)) } returnsArgument 0

        val response = participantService.submitParticipant(testDto)

        assertThat(response.isSuccess()).isTrue

        val responseValue = response.getSuccessValue()

        assertThat(responseValue).isNotNull
        assertThat(responseValue!!.twitterHandle).isEqualTo(DEFAULT_TWITTER_HANDLE)
        assertThat(responseValue.azukiId).isEqualTo(DEFAULT_AZUKI_ID)
        assertThat(responseValue.quote).isEqualTo(DEFAULT_QUOTE)
        assertThat(responseValue.bio).isEqualTo(DEFAULT_BIO)
        assertThat(responseValue.hobbies?.size).isEqualTo(4)
        assertThat(responseValue.hobbies).isEqualTo(DEFAULT_HOBBIES_LIST)
        assertThat(responseValue.backgroundTrait).isEqualTo(testAzuki.backgroundTrait)
        assertThat(responseValue.gender).isEqualTo(Gender.UNDETERMINED)
        assertThat(responseValue.seasonNumber).isEqualTo(testSeason.seasonNumber)
        assertThat(responseValue.imageUrl).isEqualTo(testAzuki.azukiImageUrl)
        assertThat(responseValue.submitted).isFalse
        assertThat(responseValue.validated).isFalse
    }

    @Test
    fun `user submits with no active season returning a season not active error`() {
        val testDto = ParticipantSubmissionDto(
            azukiId = DEFAULT_AZUKI_ID,
            twitterHandle = DEFAULT_TWITTER_HANDLE,
            quote = DEFAULT_QUOTE,
            bio = DEFAULT_BIO,
            hobbies = DEFAULT_HOBBIES
        )

        every { seasonService.getRawLatestSeason() } returns null

        val response = participantService.submitParticipant(testDto)

        assertThat(response.isSuccess()).isFalse

        val errorResponse = response as ErrorResponse

        assertThat(errorResponse.errorCode).isEqualTo(ParticipantErrorCodes.SEASON_SUBMISSIONS_ARE_NOT_ACTIVE)


    }

    @Test
    fun `user submits to an inactive season returning a season not active error`() {
        val testDto = ParticipantSubmissionDto(
            azukiId = DEFAULT_AZUKI_ID,
            twitterHandle = DEFAULT_TWITTER_HANDLE,
            quote = DEFAULT_QUOTE,
            bio = DEFAULT_BIO,
            hobbies = DEFAULT_HOBBIES
        )
        val testSeason = createSeason(submissionActive = false)

        every { seasonService.getRawLatestSeason() } returns testSeason

        val response = participantService.submitParticipant(testDto)

        assertThat(response.isSuccess()).isFalse

        val errorResponse = response as ErrorResponse

        assertThat(errorResponse.errorCode).isEqualTo(ParticipantErrorCodes.SEASON_SUBMISSIONS_ARE_NOT_ACTIVE)
    }

    @Test
    fun `user submits with invalid azuki id returning an invalid NFT id error`() {
        val testDto = ParticipantSubmissionDto(
            azukiId = 10001,
            twitterHandle = DEFAULT_TWITTER_HANDLE,
            quote = DEFAULT_QUOTE,
            bio = DEFAULT_BIO,
            hobbies = DEFAULT_HOBBIES
        )
        val testSeason = createSeason(submissionActive = true)

        every { seasonService.getRawLatestSeason() } returns testSeason

        val response = participantService.submitParticipant(testDto)

        assertThat(response.isSuccess()).isFalse

        val errorResponse = response as ErrorResponse

        assertThat(errorResponse.errorCode).isEqualTo(Web3ErrorCodes.INVALID_NFT_ID)
    }

    @Test
    fun `user submits with an invalid twitter handle returning an invalid twitter handle error`() {
        val testDto = ParticipantSubmissionDto(
            azukiId = DEFAULT_AZUKI_ID,
            twitterHandle = "@heyskylark",
            quote = DEFAULT_QUOTE,
            bio = DEFAULT_BIO,
            hobbies = DEFAULT_HOBBIES
        )
        val testSeason = createSeason(submissionActive = true)
        val testAzuki = createAzukiInfo(DEFAULT_AZUKI_ID)

        every { seasonService.getRawLatestSeason() } returns testSeason

        every {
            azukiWeb3Service.fetchAzukiInfo(DEFAULT_AZUKI_ID)
        } returns ServiceResponse.successResponse(testAzuki)

        val response = participantService.submitParticipant(testDto)

        assertThat(response.isSuccess()).isFalse

        val errorResponse = response as ErrorResponse

        assertThat(errorResponse.errorCode).isEqualTo(ParticipantErrorCodes.INVALID_TWITTER_HANDLE)
    }

    @Test
    fun `user submits with a blank quote returning an invalid quote error`() {
        val testDto = ParticipantSubmissionDto(
            azukiId = DEFAULT_AZUKI_ID,
            twitterHandle = DEFAULT_TWITTER_HANDLE,
            quote = "",
            bio = DEFAULT_BIO,
            hobbies = DEFAULT_HOBBIES
        )
        val testSeason = createSeason(submissionActive = true)
        val testAzuki = createAzukiInfo(DEFAULT_AZUKI_ID)

        every { seasonService.getRawLatestSeason() } returns testSeason

        every {
            azukiWeb3Service.fetchAzukiInfo(DEFAULT_AZUKI_ID)
        } returns ServiceResponse.successResponse(testAzuki)

        val response = participantService.submitParticipant(testDto)

        assertThat(response.isSuccess()).isFalse

        val errorResponse = response as ErrorResponse

        assertThat(errorResponse.errorCode).isEqualTo(ParticipantErrorCodes.INVALID_QUOTE_ERROR)
    }

    @Test
    fun `user submits a quote that is longer than 100 chars returning an invalid quote error`() {
        val testDto = ParticipantSubmissionDto(
            azukiId = DEFAULT_AZUKI_ID,
            twitterHandle = DEFAULT_TWITTER_HANDLE,
            quote = "This is a test quote that will be too long, just shy over 100 characters leading to a failure response.",
            bio = DEFAULT_BIO,
            hobbies = DEFAULT_HOBBIES
        )
        val testSeason = createSeason(submissionActive = true)
        val testAzuki = createAzukiInfo(DEFAULT_AZUKI_ID)

        every { seasonService.getRawLatestSeason() } returns testSeason

        every {
            azukiWeb3Service.fetchAzukiInfo(DEFAULT_AZUKI_ID)
        } returns ServiceResponse.successResponse(testAzuki)

        val response = participantService.submitParticipant(testDto)

        assertThat(response.isSuccess()).isFalse

        val errorResponse = response as ErrorResponse

        assertThat(errorResponse.errorCode).isEqualTo(ParticipantErrorCodes.INVALID_QUOTE_ERROR)
    }

    @Test
    fun `user submits bio that is longer than 200 chars returning a bio too long error`() {
        val testDto = ParticipantSubmissionDto(
            azukiId = DEFAULT_AZUKI_ID,
            twitterHandle = DEFAULT_TWITTER_HANDLE,
            quote = DEFAULT_QUOTE,
            bio = "This is a test bio that will be too long, just shy over 200 characters leading to a failure response. Bios are much longer so this string will go on for quite some time until I pass the threshold, now.",
            hobbies = DEFAULT_HOBBIES
        )
        val testSeason = createSeason(submissionActive = true)
        val testAzuki = createAzukiInfo(DEFAULT_AZUKI_ID)

        every { seasonService.getRawLatestSeason() } returns testSeason

        every {
            azukiWeb3Service.fetchAzukiInfo(DEFAULT_AZUKI_ID)
        } returns ServiceResponse.successResponse(testAzuki)

        val response = participantService.submitParticipant(testDto)

        assertThat(response.isSuccess()).isFalse

        val errorResponse = response as ErrorResponse

        assertThat(errorResponse.errorCode).isEqualTo(ParticipantErrorCodes.BIO_TOO_LONG_ERROR)
    }

    @Test
    fun `user submits more than five hobbies returning a hobbies too long error`() {
        val testDto = ParticipantSubmissionDto(
            azukiId = DEFAULT_AZUKI_ID,
            twitterHandle = DEFAULT_TWITTER_HANDLE,
            quote = DEFAULT_QUOTE,
            bio = DEFAULT_BIO,
            hobbies = "hobby1, hobby2, hobby3, hobby4, hobby5, hobby6"
        )
        val testSeason = createSeason(submissionActive = true)
        val testAzuki = createAzukiInfo(DEFAULT_AZUKI_ID)

        every { seasonService.getRawLatestSeason() } returns testSeason

        every {
            azukiWeb3Service.fetchAzukiInfo(DEFAULT_AZUKI_ID)
        } returns ServiceResponse.successResponse(testAzuki)

        val response = participantService.submitParticipant(testDto)

        assertThat(response.isSuccess()).isFalse

        val errorResponse = response as ErrorResponse

        assertThat(errorResponse.errorCode).isEqualTo(ParticipantErrorCodes.HOBBIES_TOO_LONG_ERROR)
    }

    @Test
    fun `user submits with an azuki that is already submitted returning an azuki id already exists error`() {
        val testDto = ParticipantSubmissionDto(
            azukiId = DEFAULT_AZUKI_ID,
            twitterHandle = DEFAULT_TWITTER_HANDLE,
            quote = DEFAULT_QUOTE,
            bio = DEFAULT_BIO,
            hobbies = DEFAULT_HOBBIES
        )
        val testSeason = createSeason(submissionActive = true)
        val testAzuki = createAzukiInfo(DEFAULT_AZUKI_ID)
        val testParticipant = createParticipant("test-participant", testSeason.seasonNumber, false)

        every { seasonService.getRawLatestSeason() } returns testSeason

        every {
            azukiWeb3Service.fetchAzukiInfo(DEFAULT_AZUKI_ID)
        } returns ServiceResponse.successResponse(testAzuki)

        every {
            participantDao.findByAzukiIdAndSeasonNumber(DEFAULT_AZUKI_ID, testSeason.seasonNumber)
        } returns testParticipant

        val response = participantService.submitParticipant(testDto)

        assertThat(response.isSuccess()).isFalse

        val errorResponse = response as ErrorResponse

        assertThat(errorResponse.errorCode).isEqualTo(ParticipantErrorCodes.AZUKI_ID_ALREADY_EXISTS)
    }

    @Test
    fun `user submits with a wallet that is already submitted returning an owner address exists error`() {
        val testDto = ParticipantSubmissionDto(
            azukiId = DEFAULT_AZUKI_ID,
            twitterHandle = DEFAULT_TWITTER_HANDLE,
            quote = DEFAULT_QUOTE,
            bio = DEFAULT_BIO,
            hobbies = DEFAULT_HOBBIES
        )
        val testSeason = createSeason(submissionActive = true)
        val testAzuki = createAzukiInfo(DEFAULT_AZUKI_ID)
        val testParticipant = createParticipant("test-participant", testSeason.seasonNumber, false)

        every { seasonService.getRawLatestSeason() } returns testSeason

        every {
            azukiWeb3Service.fetchAzukiInfo(DEFAULT_AZUKI_ID)
        } returns ServiceResponse.successResponse(testAzuki)

        every {
            participantDao.findByAzukiIdAndSeasonNumber(DEFAULT_AZUKI_ID, testSeason.seasonNumber)
        } returns null

        every {
            participantDao.findByOwnerAddressAndSeasonNumber(DEFAULT_OWNER_ADDRESS, testSeason.seasonNumber)
        } returns testParticipant

        val response = participantService.submitParticipant(testDto)

        assertThat(response.isSuccess()).isFalse

        val errorResponse = response as ErrorResponse

        assertThat(errorResponse.errorCode).isEqualTo(ParticipantErrorCodes.OWNER_ADDRESS_EXISTS)
    }

    @Test
    fun `user submits with a twitter handle that is already submitted returning a twitter handle exists error`() {
        val testDto = ParticipantSubmissionDto(
            azukiId = DEFAULT_AZUKI_ID,
            twitterHandle = DEFAULT_TWITTER_HANDLE,
            quote = DEFAULT_QUOTE,
            bio = DEFAULT_BIO,
            hobbies = DEFAULT_HOBBIES
        )
        val testSeason = createSeason(submissionActive = true)
        val testAzuki = createAzukiInfo(DEFAULT_AZUKI_ID)
        val testParticipant = createParticipant("test-participant", testSeason.seasonNumber, false)

        every { seasonService.getRawLatestSeason() } returns testSeason

        every {
            azukiWeb3Service.fetchAzukiInfo(DEFAULT_AZUKI_ID)
        } returns ServiceResponse.successResponse(testAzuki)

        every {
            participantDao.findByAzukiIdAndSeasonNumber(DEFAULT_AZUKI_ID, testSeason.seasonNumber)
        } returns null

        every {
            participantDao.findByOwnerAddressAndSeasonNumber(DEFAULT_OWNER_ADDRESS, testSeason.seasonNumber)
        } returns null

        every {
            participantDao.findBySeasonNumberAndTwitterHandleIgnoreCase(testSeason.seasonNumber, testDto.twitterHandle)
        } returns testParticipant

        val response = participantService.submitParticipant(testDto)

        assertThat(response.isSuccess()).isFalse

        val errorResponse = response as ErrorResponse

        assertThat(errorResponse.errorCode).isEqualTo(ParticipantErrorCodes.TWITTER_HANDLE_EXISTS)
    }
}
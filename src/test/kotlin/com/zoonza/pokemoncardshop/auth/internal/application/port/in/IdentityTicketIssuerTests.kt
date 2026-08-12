package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IdentityTicketPurpose
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketStore
import com.zoonza.pokemoncardshop.auth.internal.application.service.IdentityTicketService
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountRepository
import com.zoonza.pokemoncardshop.auth.test.fake.verifiedExternalIdentityFixture
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class IdentityTicketIssuerTests {

    private val identityTicketStore = mockk<IdentityTicketStore>()
    private val externalAccountRepository = mockk<ExternalAccountRepository>()
    private val identityTicketIssuer: IdentityTicketIssuer = IdentityTicketService(
        identityTicketStore = identityTicketStore,
        externalAccountRepository = externalAccountRepository,
    )

    @Test
    fun `가입하지 않은 연동 계정에는 회원가입 티켓을 발급한다`() {
        val identity = verifiedExternalIdentityFixture().copy(subject = "new-subject")
        every {
            externalAccountRepository.existsByProviderAndSubject(identity.provider, identity.subject)
        } returns false
        every {
            identityTicketStore.issue(identity, IdentityTicketPurpose.SIGNUP.ttl)
        } returns "signup-ticket"

        val result = identityTicketIssuer.issue(identity)

        result.purpose shouldBe IdentityTicketPurpose.SIGNUP
        result.ticket shouldBe "signup-ticket"
        verify(exactly = 1) {
            externalAccountRepository.existsByProviderAndSubject(identity.provider, identity.subject)
            identityTicketStore.issue(identity, IdentityTicketPurpose.SIGNUP.ttl)
        }
    }

    @Test
    fun `가입한 연동 계정에는 로그인 티켓을 발급한다`() {
        val identity = verifiedExternalIdentityFixture().copy(subject = "registered-subject")
        every {
            externalAccountRepository.existsByProviderAndSubject(identity.provider, identity.subject)
        } returns true
        every {
            identityTicketStore.issue(identity, IdentityTicketPurpose.LOGIN.ttl)
        } returns "login-ticket"

        val result = identityTicketIssuer.issue(identity)

        result.purpose shouldBe IdentityTicketPurpose.LOGIN
        result.ticket shouldBe "login-ticket"
        verify(exactly = 1) {
            externalAccountRepository.existsByProviderAndSubject(identity.provider, identity.subject)
            identityTicketStore.issue(identity, IdentityTicketPurpose.LOGIN.ttl)
        }
    }
}

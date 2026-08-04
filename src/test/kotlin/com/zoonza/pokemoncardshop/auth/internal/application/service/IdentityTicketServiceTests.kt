package com.zoonza.pokemoncardshop.auth.internal.application.service

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IdentityTicketPurpose
import com.zoonza.pokemoncardshop.auth.internal.application.dto.VerifiedExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketStore
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentityRepository
import com.zoonza.pokemoncardshop.auth.internal.domain.IdentityProvider
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class IdentityTicketServiceTests {

    private val identityTicketStore = mockk<IdentityTicketStore>()
    private val externalIdentityRepository = mockk<ExternalIdentityRepository>()
    private val service = IdentityTicketService(identityTicketStore, externalIdentityRepository)

    @Test
    fun `가입하지 않은 연동 계정에는 회원가입 티켓을 발급한다`() {
        val identity = VerifiedExternalIdentity(IdentityProvider.GOOGLE, "new-subject")

        every {
            externalIdentityRepository.existsByProviderAndSubject(identity.provider, identity.identifier)
        } returns false

        every { identityTicketStore.issue(identity, IdentityTicketPurpose.SIGNUP.ttl) } returns "signup-ticket"

        val result = service.issue(identity)

        result.purpose shouldBe IdentityTicketPurpose.SIGNUP
        result.ticket shouldBe "signup-ticket"

        verify(exactly = 1) {
            externalIdentityRepository.existsByProviderAndSubject(identity.provider, identity.identifier)
            identityTicketStore.issue(identity, IdentityTicketPurpose.SIGNUP.ttl)
        }
    }

    @Test
    fun `가입한 연동 계정에는 로그인 티켓을 발급한다`() {
        val identity = VerifiedExternalIdentity(IdentityProvider.GOOGLE, "registered-subject")

        every {
            externalIdentityRepository.existsByProviderAndSubject(identity.provider, identity.identifier)
        } returns true

        every { identityTicketStore.issue(identity, IdentityTicketPurpose.LOGIN.ttl) } returns "login-ticket"

        val result = service.issue(identity)

        result.purpose shouldBe IdentityTicketPurpose.LOGIN
        result.ticket shouldBe "login-ticket"

        verify(exactly = 1) {
            externalIdentityRepository.existsByProviderAndSubject(identity.provider, identity.identifier)
            identityTicketStore.issue(identity, IdentityTicketPurpose.LOGIN.ttl)
        }
    }
}

package pt.unl.fct.di.apresentacoes.services.security

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.di.apresentacoes.api.dto.AuthResponseDTO
import pt.unl.fct.di.apresentacoes.api.dto.ForgotPasswordDTO
import pt.unl.fct.di.apresentacoes.api.dto.LoginDTO
import pt.unl.fct.di.apresentacoes.domain.UserDAO
import pt.unl.fct.di.apresentacoes.presistence.UserRepository
import pt.unl.fct.di.apresentacoes.security.JwtProperties
import pt.unl.fct.di.apresentacoes.services.mail.MailService
import pt.unl.fct.di.apresentacoes.services.passwordGeneration.PasswordGenerationService
import java.util.*

@Service
class AuthenticationService (
    private val autheManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailsService,
    private val tokenService: TokenService,
    private val jwtProperties: JwtProperties,
    val users: UserRepository,
    val mailService: MailService,
    val passwordGenerationService: PasswordGenerationService
){

    fun authentication(authRequest: LoginDTO): AuthResponseDTO {
        autheManager.authenticate(
            UsernamePasswordAuthenticationToken(
                authRequest.email,
                authRequest.password
            ))

        val user = userDetailsService.loadUserByUsername(authRequest.email)
        val accessToken = tokenService.generate(
            userDetails = user,
            expriationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration)
        )

        return AuthResponseDTO(accessToken)
    }

    @Transactional
    fun forgotPassword(forgotPasswordDTO: ForgotPasswordDTO): UserDAO {
        val user = users.findOneByEmail(forgotPasswordDTO.email)
            .orElseThrow { ForgotPasswordUnprocessable("There is no account with the given email.") }

        val newPassword = passwordGenerationService.generateRandomPassword()
        user.password = BCryptPasswordEncoder().encode(newPassword)
        mailService.sendForgotPasswordMail(forgotPasswordDTO.email, newPassword)
        return users.save(user)
    }

}

class ForgotPasswordUnprocessable(message: String) : RuntimeException(message)
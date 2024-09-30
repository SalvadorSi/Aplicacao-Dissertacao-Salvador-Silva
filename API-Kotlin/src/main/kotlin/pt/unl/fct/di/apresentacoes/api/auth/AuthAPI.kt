package pt.unl.fct.di.apresentacoes.api.auth

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import pt.unl.fct.di.apresentacoes.api.dto.AuthResponseDTO
import pt.unl.fct.di.apresentacoes.api.dto.ForgotPasswordDTO
import pt.unl.fct.di.apresentacoes.api.dto.FullUserDTO
import pt.unl.fct.di.apresentacoes.api.dto.LoginDTO

@RequestMapping("/api/auth")
interface AuthAPI {

    @PostMapping("/login")
    fun login(@RequestBody loginDTO: LoginDTO): AuthResponseDTO

    @PostMapping("/forgotMyPassword")
    fun forgotMyPassword(@RequestBody forgotPasswordDTO: ForgotPasswordDTO): FullUserDTO
}
package pt.unl.fct.di.apresentacoes.services

import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.di.apresentacoes.api.dto.ChangePasswordDTO
import pt.unl.fct.di.apresentacoes.api.dto.ChangeRoleDTO
import pt.unl.fct.di.apresentacoes.api.dto.CreateUserDTO
import pt.unl.fct.di.apresentacoes.api.dto.FullUserDTO
import pt.unl.fct.di.apresentacoes.domain.UnavailabilityDAO
import pt.unl.fct.di.apresentacoes.domain.UserDAO
import pt.unl.fct.di.apresentacoes.domain.UserRoles
import pt.unl.fct.di.apresentacoes.presistence.PresentationRepository
import pt.unl.fct.di.apresentacoes.presistence.UnavailabilityRepository
import pt.unl.fct.di.apresentacoes.presistence.UserRepository
import pt.unl.fct.di.apresentacoes.services.mail.MailService
import pt.unl.fct.di.apresentacoes.services.passwordGeneration.PasswordGenerationService

private const val USER_NOT_FOUND = "This user does not exist."
private const val USER_EXISTS = "This user already exists."

@Service
class UserService(val users: UserRepository,
                  val presentations: PresentationRepository,
                  val unavailabilities: UnavailabilityRepository,
                  val mailService: MailService,
                  val passwordGenerationService: PasswordGenerationService
) {

    @Transactional
    fun addUser(user: CreateUserDTO): UserDAO {
        if(users.existsByEmail(user.email) || users.existsByName(user.name))
            throw UserAlreadyExists()

        val generatedPassword = passwordGenerationService.generateRandomPassword()
        val encodedPassword = BCryptPasswordEncoder().encode(generatedPassword)

        val userDAO = UserDAO(user.id,user.name,user.email, UserRoles.USER,encodedPassword)
        val unavailabilityDAO = UnavailabilityDAO(0,mutableListOf(),userDAO)
        unavailabilities.save(unavailabilityDAO)
        userDAO.unavailability = unavailabilityDAO
        mailService.sendRegisteredMail(user.email, generatedPassword)
        return users.save(userDAO)
    }

    fun getOneUser(uid:Long): UserDAO {
        return users.findById(uid)
            .orElseThrow { UserNotFound() }
    }

    fun getAllUsers(): Iterable<UserDAO> = users.findAll()

    @Transactional
    fun deleteOneUser(uid:Long) {
        val user = users.findById(uid)
            .orElseThrow { UserNotFound() }

        if(user.role == UserRoles.ADMIN)
            throw UserUnprocessable("You can not delete an admin.")

        for(presentation in user.optional1Presentations){
            presentation.optionalParticipant1 = null
            presentations.save(presentation)
        }
        for(presentation in user.optional1Presentations){
            presentation.optionalParticipant2 = null
            presentations.save(presentation)
        }
        users.deleteById(uid)
    }

    @Transactional
    fun changeRole(changeRoleDTO: ChangeRoleDTO): UserDAO{
        val user = users.findById(changeRoleDTO.userID)
            .orElseThrow { UserNotFound() }
        if(user.role == UserRoles.ADMIN){
            throw UserUnprocessable("You can not change the role of an admin.")
        }
        user.role = changeRoleDTO.newRole
        return users.save(user)
    }

    fun getOneByEmail(email: String): UserDAO =
        users
            .findOneByEmail(email).orElse(null)//.orElseThrow { UserNotFound() }

    fun getOneById(uid: Long): UserDAO =
        users
            .findById(uid).orElse(null)//.orElseThrow { UserNotFound() }

    @Transactional
    fun changePassword(email: String, changePasswordDTO: ChangePasswordDTO): UserDAO {
        val user = users
            .findOneByEmail(email)
            .orElseThrow { UserNotFound() }

        if (BCryptPasswordEncoder().matches(changePasswordDTO.oldPassword, user.password)) {
            user.password = BCryptPasswordEncoder().encode(changePasswordDTO.newPassword)
            return users.save(user)
        } else {
            throw UserConflict()
        }
    }
}

class UserAlreadyExists(message: String? = USER_EXISTS) : Exception(message)
class UserNotFound(message: String? = USER_NOT_FOUND) : RuntimeException(message)
class UserConflict(message: String? = USER_EXISTS) : RuntimeException(message)
class UserUnprocessable(message: String) : RuntimeException(message)


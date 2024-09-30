package pt.unl.fct.di.apresentacoes.services.mail

import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

private const val REGISTRATION_SUBJECT = "Registered to Presentations App"
private const val REGISTRATION_TEXT = "You have been registered in the Presentations Application!\n\n" +
        "This application helps to schedule presentations for all the dissertation preparation documents.\n\n" +
        "Your username is your email and your password is: "
private const val REGISTRATION_TEXT_END = "We highly recommend you change your password for security reasons. " +
        "To do so, log in to the app and navigate to the user section on the left-hand side of the page. " +
        "Then, click on \"Change my password\" and select a new, secure password."
private const val FORGOT_PASSWORD_SUBJECT = "Forgot my password - Presentations App"
private const val FORGOT_PASSWORD_TEXT ="You have requested a new password for the presentations application.\n\n" +
        "Your new password is: "

private const val END_MAIL = "Best Regards,\n" +
        "Salvador Silva"

@Service
class MailService(private val mailSender: JavaMailSender) {

    @Async
    fun sendRegisteredMail(email: String, password: String){
        sendEmail(email, REGISTRATION_SUBJECT, "$REGISTRATION_TEXT$password.\n\n$REGISTRATION_TEXT_END\n\n$END_MAIL")
    }

    @Async
    fun sendForgotPasswordMail(email: String, newPassword: String){
        sendEmail(email, FORGOT_PASSWORD_SUBJECT, "$FORGOT_PASSWORD_TEXT$newPassword.\n\n$END_MAIL")
    }

    private fun sendEmail(email: String, subject: String, text: String){
        try{
            val message = SimpleMailMessage()
            message.from = "apresentacoesfct@gmail.com"
            message.setTo(email)
            message.subject = subject
            message.text = text
            mailSender.send(message)
        } catch (e: MailException){
            println("Something went wrong.")
        }
    }
}
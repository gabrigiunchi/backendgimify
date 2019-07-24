package com.gabrigiunchi.backendtesi.service

import com.sendgrid.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.IOException


@Service
class MailService {

    private val logger = LoggerFactory.getLogger(MailService::class.java)

    @Value("\${application.email.sendgrid.apikey}")
    private var apiKey = ""

    fun sendEmail(to: String, subject: String, content: String, type: String = "text/plain") {
        if (to.isNotEmpty() && this.apiKey.isNotEmpty()) {
            this.logger.info("Sending email to $to")

            try {
                val request = Request()
                request.method = Method.POST
                request.endpoint = "mail/send"
                request.body = Mail(Email("no-reply@gimify.it"), subject, Email(to), Content(type, content)).build()
                SendGrid(this.apiKey).api(request)
                this.logger.info("Email sent to $to")
            } catch (ex: IOException) {
                this.logger.error(ex.toString())
            }
        }
    }
}
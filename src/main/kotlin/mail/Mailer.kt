/*
 * Copyright (C) 2016 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.hska.lieferant.mail

import de.hska.lieferant.config.logger
import de.hska.lieferant.config.MailAddressProps
import de.hska.lieferant.entity.Lieferant
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.stereotype.Component
import javax.mail.Message.RecipientType.TO
import javax.mail.internet.InternetAddress

/**
 * Mail-Client.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@Component
class Mailer(private val mailSender: JavaMailSender, private val props: MailAddressProps) {
    /**
     * Email senden, dass es einen neuen Lieferantn gibt.
     * @param neuerLieferant Das Objekt des neuen Lieferantn.
     */
    fun send(neuerLieferant: Lieferant) {
        val preparator = MimeMessagePreparator {
            with(it) {
                setFrom(InternetAddress(props.from))
                setRecipient(TO, InternetAddress(props.sales))
                subject = "Neuer Lieferant ${neuerLieferant.id}"
                val body = "<b>Neuer Lieferant:</b> \"${neuerLieferant.nachname}\""
                logger.trace("Mail-Body: {}", body)
                setText(body)
            }
        }

        // TODO Retry siehe https://github.com/reactor/reactor-addons/blob/...
        //      ...master/reactor-extra/src/main/java/reactor/retry/Retry.java
        try {
            mailSender.send(preparator)
        } catch (e: MailException) {
            logger.error("Fehler beim Senden der Email bzgl.: $neuerLieferant", e)
        }
    }

    private companion object {
        val logger = logger()
    }
}

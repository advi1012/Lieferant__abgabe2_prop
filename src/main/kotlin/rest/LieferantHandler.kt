/*
 * Copyright (C) 2017 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package de.hska.lieferant.rest

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import de.hska.lieferant.config.security.UsernameExistsException
import de.hska.lieferant.entity.Lieferant
import de.hska.lieferant.Router.Companion.idPathVar
import de.hska.lieferant.config.logger
import de.hska.lieferant.rest.util.LieferantPatcher
import de.hska.lieferant.rest.util.PatchOperation
import de.hska.lieferant.rest.util.LieferantConstraintViolation
import de.hska.lieferant.rest.util.ifMatch
import de.hska.lieferant.rest.util.ifNoneMatch
import de.hska.lieferant.rest.util.itemLinks
import de.hska.lieferant.rest.util.singleLinks
import de.hska.lieferant.service.EmailExistsException
import de.hska.lieferant.service.InvalidAccountException
import de.hska.lieferant.service.InvalidVersionException
import de.hska.lieferant.service.LieferantService
import java.net.URI
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_MODIFIED
import org.springframework.http.HttpStatus.PRECONDITION_FAILED
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToFlux
import org.springframework.web.reactive.function.server.bodyToMono
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse
        .badRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.Mono
import reactor.core.publisher.onErrorResume
import reactor.core.publisher.toMono
import javax.validation.ConstraintViolationException

/**
 * Eine Handler-Function wird von der Router-Function [de.hska.lieferant.Router.router]
 * aufgerufen, nimmt einen Request entgegen und erstellt den Response.
 *
 * [Klassendiagramm](../../../../docs/images/LieferantHandler.png)
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 *
 * @constructor Einen LieferantHandler mit einem injizierten [LieferantService]
 *      erzeugen.
 */
@Component
@Suppress("TooManyFunctions")
class LieferantHandler(private val service: LieferantService) {
    /**
     * Suche anhand der Lieferant-ID
     * @param request Der eingehende Request
     * @return Ein Mono-Objekt mit dem Statuscode 200 und dem gefundenen
     *      Lieferantn einschließlich HATEOAS-Links, oder aber Statuscode 204.
     */
    fun findById(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable(idPathVar)

        return service.findById(id)
            .flatMap { lieferantToOK(it, request) }
            .switchIfEmpty(notFound().build())
    }

    private fun lieferantToOK(lieferant: Lieferant, request: ServerRequest): Mono<ServerResponse> {
        val version = lieferant.version
        return if (version == request.ifNoneMatch()?.toIntOrNull()) {
            status(NOT_MODIFIED).build()
        } else {
            lieferant._links = request.uri().singleLinks()

            // Entity Tag, um Aenderungen an der angeforderten
            // Ressource erkennen zu koennen.
            // Client: GET-Requests mit Header "If-None-Match"
            //         ggf. Response mit Statuscode NOT MODIFIED (s.o.)
            ok().eTag("\"$version\"").body(lieferant.toMono())
        }
    }

    /**
     * Suche mit diversen Suchkriterien als Query-Parameter. Es wird
     * `Mono<List<Lieferant>>` statt `Flux<Lieferant>` zurückgeliefert, damit
     * auch der Statuscode 204 möglich ist.
     * @param request Der eingehende Request mit den Query-Parametern.
     * @return Ein Mono-Objekt mit dem Statuscode 200 und einer Liste mit den
     *      gefundenen Lieferantn einschließlich HATEOAS-Links, oder aber
     *      Statuscode 204.
     */
    fun find(request: ServerRequest): Mono<ServerResponse> {
        val queryParams = request.queryParams()

        // https://stackoverflow.com/questions/45903813/...
        //     ...webflux-functional-how-to-detect-an-empty-flux-and-return-404
        val lieferantn = service.find(queryParams)
            .map {
                if (it.id != null) {
                    it.links = request.uri().itemLinks(it.id)
                }
                it
            }
            .collectList()

        return lieferantn.flatMap {
            if (it.isEmpty())
                notFound().build()
            else ok().body(it.toMono())
        }
    }

    /**
     * Einen neuen Lieferant-Datensatz anlegen.
     * @param request Der eingehende Request mit dem Lieferant-Datensatz im Body.
     * @return Response mit Statuscode 201 einschließlich Location-Header oder
     *      Statuscode 400 falls Constraints verletzt sind oder der
     *      JSON-Datensatz syntaktisch nicht korrekt ist.
     */
    fun create(request: ServerRequest) = request.bodyToMono<Lieferant>()
        .flatMap(service::create)
        .flatMap {
            logger.trace("Lieferant abgespeichert: {}", it)
            val location = URI("${request.uri()}${it.id}")
            created(location).build()
        }
        .onErrorResume(ConstraintViolationException::class) { constraintViolationEx ->
            val violations = constraintViolationEx.constraintViolations
            if (violations.isEmpty()) {
                badRequest().build()
            } else {
                val lieferantViolations = violations.map {
                    LieferantConstraintViolation(
                        property = it.propertyPath.toString().replace("create.lieferant.", ""),
                        message = it.message)
                }
                logger.trace("lieferantViolations [create]: {}", lieferantViolations)
                badRequest().body(lieferantViolations.toMono())
            }
        }
        .onErrorResume(InvalidAccountException::class) {
            val msg = it.message ?: ""
            badRequest().body(msg.toMono())
        }
        .onErrorResume(EmailExistsException::class) {
            val msg = it.message ?: ""
            badRequest().body(msg.toMono())
        }
        .onErrorResume(UsernameExistsException::class) {
            val msg = it.message ?: ""
            badRequest().body(msg.toMono())
        }
        .onErrorResume(DecodingException::class) {
            val msg = it.message ?: ""
            badRequest().body(msg.toMono())
        }

    /**
     * Einen vorhandenen Lieferant-Datensatz überschreiben.
     * @param request Der eingehende Request mit dem neuen Lieferant-Datensatz im
     *      Body.
     * @return Response mit Statuscode 204 oder Statuscode 400, falls
     *      Constraints verletzt sind oder der JSON-Datensatz syntaktisch nicht
     *      korrekt ist.
     */
    fun update(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable(idPathVar)
        val version = request.ifMatch()
            ?: return status(PRECONDITION_FAILED).body("Versionsnummer fehlt".toMono())

        return request.bodyToMono<Lieferant>()
            .flatMap { service.update(it, id, version) }
            .flatMap {
                logger.trace("Lieferant aktualisiert: {}", it)
                noContent().eTag("\"${it.version}\"").build()
            }
            .switchIfEmpty(notFound().build())
            .onErrorResume(ConstraintViolationException::class) { constraintViolationEx ->
                val violations = constraintViolationEx.constraintViolations
                if (violations.isEmpty()) {
                    badRequest().build()
                } else {
                    val lieferantViolations = violations.map {
                        LieferantConstraintViolation(
                            property = it.propertyPath.toString().replace("update.lieferant.", ""),
                            message = it.message)
                    }
                    logger.trace("lieferantViolations [update]: {}", lieferantViolations)
                    badRequest().body(lieferantViolations.toMono())
                }
            }
            .onErrorResume(EmailExistsException::class) {
                logger.trace("EmailExistsException: {}", it.message)
                badRequest().syncBody(it.message ?: "")
            }
            .onErrorResume(InvalidVersionException::class) {
                logger.trace("InvalidVersionException: {}", it.message)
                status(PRECONDITION_FAILED).syncBody(it.message ?: "")
            }
            .onErrorResume(DecodingException::class) {
                logger.trace("DecodingException")
                handleDecodingException(it)
            }
    }

    /**
     * Einen vorhandenen Lieferant-Datensatz durch PATCH aktualisieren.
     * @param request Der eingehende Request mit dem PATCH-Datensatz im Body.
     * @return Response mit Statuscode 204 oder Statuscode 400, falls
     *      Constraints verletzt sind oder der JSON-Datensatz syntaktisch nicht
     *      korrekt ist.
     */
    fun patch(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable(idPathVar)
        val version = request.ifMatch()
            ?: return status(PRECONDITION_FAILED).body("Versionsnummer fehlt".toMono())
        logger.trace("Versionsnummer $version")

        return request.bodyToFlux<PatchOperation>()
            // Die einzelnen Patch-Operationen als Liste in einem Mono
            .collectList()
            .flatMap { patchOps ->
                service.findById(id)
                    .flatMap {
                        val patchedLieferant = LieferantPatcher.patch(it, patchOps)
                        logger.trace("Lieferant mit Patch-Ops: {}", patchedLieferant)
                        service.update(patchedLieferant, id, version)
                    }
                    .flatMap {
                        noContent().eTag("\"${it.version}\"").build()
                    }
                    .switchIfEmpty(notFound().build())
                    .onErrorResume(ConstraintViolationException::class) { constraintViolationEx ->
                        val violations = constraintViolationEx.constraintViolations
                        if (violations.isEmpty()) {
                            badRequest().build()
                        } else {
                            val lieferantViolations = violations.map {
                                LieferantConstraintViolation(
                                    property = it.propertyPath.toString().replace("patch.lieferant.", ""),
                                    message = it.message)
                            }
                            logger.trace("lieferantViolations [patch]: {}", lieferantViolations)
                            badRequest().body(lieferantViolations.toMono())
                        }
                    }
                    .onErrorResume(EmailExistsException::class) {
                        badRequest().syncBody(it.message ?: "")
                    }
                    .onErrorResume(InvalidVersionException::class) {
                        val msg = it.message ?: ""
                        status(PRECONDITION_FAILED).body(msg.toMono())
                    }
                    .onErrorResume(DecodingException::class) {
                        handleDecodingException(it)
                    }
            }
    }

    /**
     * Einen vorhandenen Lieferantn anhand seiner ID löschen.
     * @param request Der eingehende Request mit der ID als Pfad-Parameter.
     * @return Response mit Statuscode 204.
     */
    fun deleteById(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable(idPathVar)
        return service.deleteById(id)
                .flatMap { noContent().build() }
                .switchIfEmpty(notFound().build())
    }

    /**
     * Einen vorhandenen Lieferantn anhand seiner Emailadresse löschen.
     * @param request Der eingehende Request mit der Emailadresse als
     *      Query-Parameter.
     * @return Response mit Statuscode 204.
     */
    fun deleteByEmail(request: ServerRequest): Mono<ServerResponse> {
        val email = request.queryParam("email")
        return if (email.isPresent) {
            service.deleteByEmail(email.get())
                    .flatMap { noContent().build() }
        } else {
            noContent().build()
        }
    }

    private fun handleDecodingException(e: DecodingException): Mono<ServerResponse> {
        logger.debug(e.message)
        val exception = e.cause
        return when (exception) {
            is JsonParseException -> {
                logger.debug(exception.message)
                badRequest().syncBody(exception.message ?: "")
            }
            is InvalidFormatException -> {
                logger.debug("${exception.message}")
                badRequest().syncBody(exception.message ?: "")
            }
            else -> status(INTERNAL_SERVER_ERROR).build()
        }
    }

    private companion object {
        val logger = logger()
    }
}

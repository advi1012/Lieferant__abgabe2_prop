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
package de.hska.lieferant.service

import de.hska.lieferant.config.logger
import de.hska.lieferant.config.security.CustomUserDetails
import de.hska.lieferant.config.security.CustomUserDetailsService
import de.hska.lieferant.db.LieferantRepository
import de.hska.lieferant.db.CriteriaUtil.getCriteria
import de.hska.lieferant.db.update
import de.hska.lieferant.entity.Lieferant
import de.hska.lieferant.mail.Mailer
import java.time.Duration.ofMillis
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Lazy
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.query.Query
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MultiValueMap
import org.springframework.validation.annotation.Validated
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.util.UUID.randomUUID
import javax.validation.Valid

@Suppress("TooManyFunctions")
/**
 * Anwendungslogik für Lieferantn.
 *
 * [Klassendiagramm](../../../../docs/images/LieferantService.png)
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@Component
@Validated
// TODO https://jira.spring.io/browse/SPR-14235
// https://stackoverflow.com/questions/49731127/caching-in-spring-5-webflux#answer-49731328
@CacheConfig(cacheNames = ["lieferant_id"])
class LieferantService(
    // Annotation im zugehoerigen Parameter des Java-Konstruktors
    private val mongoTemplate: ReactiveMongoTemplate,
    private val repo: LieferantRepository,
    @param:Lazy private val userService: CustomUserDetailsService,
    @param:Lazy private val mailer: Mailer
) {

    /**
     * Einen Lieferantn anhand seiner ID suchen.
     *
     * @param id Die Id des gesuchten Lieferantn.
     * @return Der gefundene Lieferant oder ein leeres Mono-Objekt.
     */
    @Cacheable(key = "#id")
    fun findById(id: String) = repo.findById(id).timeout(timeoutShort)

    /**
     * Lieferantn anhand von Suchkriterien ermitteln.
     *
     * @param queryParams Suchkriterien.
     * @return Gefundene Lieferantn.
     */
    @Suppress("ReturnCount")
    fun find(queryParams: MultiValueMap<String, String>): Flux<Lieferant> {
        if (queryParams.isEmpty()) {
            return mongoTemplate.findAll()
        }

        val criteria = getCriteria(queryParams)
        if (criteria.contains(null)) {
            return Flux.empty()
        }

        val query = Query()
        criteria.filterNotNull()
                .forEach { query.addCriteria(it) }
        logger.debug("{}", query)
        // http://www.baeldung.com/spring-data-mongodb-tutorial
        return mongoTemplate.find<Lieferant>(query).timeout(timeoutLong)
    }

    /**
     * Einen neuen Lieferantn anlegen.
     *
     * @param lieferant Das Objekt des neu anzulegenden Lieferantn.
     * @return Der neu angelegte Lieferant mit generierter ID.
     * @throws InvalidAccountException falls die Benutzerkennung nicht korrekt
     *      ist.
     * @throws EmailExistsException falls die Emailadresse bereits existiert.
     */
    @Transactional
    fun create(@Valid lieferant: Lieferant): Mono<Lieferant> {
        // CustomUserDetails nicht @NotNull: nicht in der Mongo-Collection gespeichert
        lieferant.user ?: throw InvalidAccountException()

        val email = lieferant.email
        return repo.findByEmail(email)
                .timeout(timeoutShort)
                .map<Lieferant> { throw EmailExistsException(email) }
                .switchIfEmpty(lieferant.toMono())
                .flatMap(::createUser)
                .flatMap { create(lieferant, it) }
                .doOnSuccess(mailer::send)
    }

    private fun createUser(lieferant: Lieferant): Mono<CustomUserDetails>? {
        val lieferantUser = lieferant.user ?: throw InvalidAccountException()
        val user = CustomUserDetails(
                id = null,
                username = lieferantUser.username,
                password = lieferantUser.password,
                authorities = listOf(SimpleGrantedAuthority("ROLE_LIEFERANT")))
        logger.trace("User wird angelegt: {}", user)
        return userService.create(user)
                .timeout(timeoutShort)
    }

    private fun create(lieferant: Lieferant, user: CustomUserDetails): Mono<Lieferant> {
        val neuerLieferant = lieferant.copy(
                email = lieferant.email.toLowerCase(),
                username = user.username,
                id = randomUUID().toString())
        neuerLieferant.user = user
        logger.trace("Lieferant mit user: {}", lieferant)
        return mongoTemplate.save(neuerLieferant).timeout(timeoutShort)
    }

    /**
     * Einen vorhandenen Lieferantn aktualisieren.
     *
     * @param lieferant Das Objekt mit den neuen Daten.
     * @param id ID des Lieferantn.
     * @param version Versionsnummer.
     * @return Der aktualisierte Lieferant oder ein leeres Mono-Objekt, falls
     *      es keinen Lieferantn mit der angegebenen ID gibt.
     * @throws InvalidVersionException falls die Versionsnummer nicht korrekt
     *      ist.
     * @throws EmailExistsException falls die Emailadresse bereits existiert.
     */
    @CacheEvict(key = "#id")
    @Transactional
    fun update(@Valid lieferant: Lieferant, id: String, version: String) =
        repo.findById(id)
                .timeout(timeoutShort)
                .flatMap { lieferantDb ->
                    logger.trace("update: lieferantDb={}, version={}", lieferantDb, version)
                    checkVersion(lieferantDb, version)
                    checkEmail(lieferantDb, lieferant.email)
                            .switchIfEmpty(lieferantDb.toMono())
                            .flatMap { update(lieferantDb, lieferant) }
                }

    private fun checkVersion(lieferantDb: Lieferant, versionStr: String) {
        // Gibt es eine neuere Version in der DB?
        val version = versionStr.toIntOrNull() ?: throw InvalidVersionException(versionStr)
        val versionDb = lieferantDb.version ?: 0
        if (version < versionDb) {
            throw InvalidVersionException(versionStr)
        }
    }

    private fun checkEmail(lieferantDb: Lieferant, neueEmail: String): Mono<Lieferant> {
        // Hat sich die Emailadresse ueberhaupt geaendert?
        if (lieferantDb.email == neueEmail) {
            logger.trace("Email nicht geaendert: {}", lieferantDb)
            return Mono.empty()
        }
        logger.trace("Email geaendert: {} -> {}", neueEmail, lieferantDb)

        // Gibt es die neue Emailadresse bei einem existierenden Lieferantn?
        return repo.findByEmail(neueEmail)
                .timeout(timeoutShort)
                .map<Lieferant> {
                    logger.trace("Neue Email existiert bereits: {}", neueEmail)
                    throw EmailExistsException(neueEmail)
                }
    }

    private fun update(lieferantDb: Lieferant, lieferant: Lieferant): Mono<Lieferant> {
        lieferantDb.update(lieferant)
        logger.trace("Abspeichern des geaenderten Lieferantn: {}", lieferantDb)
        return mongoTemplate.save(lieferantDb).timeout(timeoutShort)
    }

    /**
     * Einen vorhandenen Lieferantn in der DB löschen.
     *
     * @param id Die ID des zu löschenden Lieferantn.
     * @return true falls es zur ID ein Lieferantnobjekt gab, das gelöscht
     *      wurde; false sonst.
     */
    // erfordert zusaetzliche Konfiguration in SecurityConfig
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CacheEvict(key = "#id")
    @Transactional
    fun deleteById(id: String) = repo.findById(id)
            .timeout(timeoutShort)
            // EmptyResultDataAccessException bei delete(), falls es zur
            // gegebenen ID kein Objekt gibt
            // http://docs.spring.io/spring/docs/current/javadoc-api/org/...
            // ...springframework/dao/EmptyResultDataAccessException.html
            .delayUntil { repo.deleteById(id).timeout(timeoutShort) }

    /**
     * Einen vorhandenen Lieferantn löschen.
     *
     * @param email Die Email des zu löschenden Lieferantn.
     * @return true falls es zur Email ein Lieferantnobjekt gab, das gelöscht
     *      wurde; false sonst.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    fun deleteByEmail(email: String) = repo.deleteByEmail(email)

    @Suppress("MagicNumber")
    companion object {
        private val logger = logger()
        private val timeoutShort = ofMillis(500)
        private val timeoutLong = ofMillis(2000)
    }
}

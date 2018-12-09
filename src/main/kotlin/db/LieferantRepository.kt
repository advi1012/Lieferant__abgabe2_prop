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
@file:Suppress("unused", "FunctionName")

package de.hska.lieferant.db

import de.hska.lieferant.entity.Lieferant
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

// Queries werden durch Namenskonventionen deklariert wie bei Ruby-on-Rails.
// Von ReactiveCrudRepository sind u.a. folgende Interfaces abgeleitet:
// * ReactiveMongoRepository
// * ReactiveJpaRepository

/**
 * Interface für ein _Repository_ gemäß DDD für _MongoDB_.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
interface LieferantRepository : ReactiveCrudRepository<Lieferant, String> {
    /**
     * Suche nach einem Lieferantn mit der gegebenen Emailadresse.
     * @param email Die Emailadresse des gesuchten Lieferantn.
     * @return Der gefundene Lieferant oder empty.
     */
    // SELECT * FROM lieferant WHERE email = ...
    fun findByEmail(email: String): Mono<Lieferant>

    /**
     * Suche nach Lieferantn mit dem gegebenen Nachnamen ohne Unterscheidung
     * zwischen Gross- und Kleinschreibung.
     * @param nachname Der gemeinsame Nachname der gesuchten Lieferantn.
     * @return Die gefundenen Lieferantn als Flux.
     */
    // SELECT * FROM lieferant WHERE nachname LIKE ...
    fun findByNachnameIgnoreCase(nachname: String): Flux<Lieferant>

    /**
     * Suche nach Lieferantn mit einem Nachnamen, der den gegebenen Teilstring
     * ohne Unterscheidung zwischen Gross- und Kleinschreibung enthält.
     * @param nachname Der gemeinsame Teilstring im Nachname der gesuchten
     *      Lieferantn.
     * @return Die gefundenen Lieferantn als Flux.
     */
    // SELECT * FROM lieferant WHERE nachname ... LIKE ...
    fun findByNachnameContainingIgnoreCase(nachname: String): Flux<Lieferant>

    /**
     * Suche nach Lieferantn mit dem gegebenen Nachnamen und sortiert nach der
     * Emailadresse.
     * @param nachname Der gemeinsame Nachname der gesuchten Lieferantn.
     * @return Die gefundenen Lieferantn als Flux mit Sortierung gemäß
     *      ihrer Emailadresse.
     */
    // SELECT * FROM lieferant WHERE nachname = ... ORDER BY email ASC
    fun findByNachnameOrderByEmailAsc(nachname: String): Flux<Lieferant>

    /**
     * Suche nach Lieferantn mit der gegebenen Postleitzahl.
     * @param plz Die gemeinsame Postleitzahl der gesuchten Lieferantn.
     * @return Die gefundenen Lieferantn als Flux.
     */
    // https://stackoverflow.com/questions/51646917/sonarqube-how-to-suppress-a-warning-in-kotlin-code#answer-51659699
    // SELECT * FROM lieferant JOIN adresse ON ... WHERE plz = ...
    @Suppress("FunctionNaming", "kotlin:S100")
    fun findByAdresse_Plz(plz: String): Flux<Lieferant>

    /**
     * Suche nach Lieferantn mit einem bestimmten Präfix für deren Nachnamen.
     * @param prefix Der Präfix für den Nachnamen.
     * @return Die gefundenen Lieferantn als Flux.
     */
    fun findByNachnameStartingWithIgnoreCase(prefix: String): Flux<Lieferant>

    /**
     * Suche nach Lieferantn mit einem bestimmten Präfix für deren Emailadresse.
     * @param prefix Der Präfix für den Nachnamen.
     * @return Die gefundenen Lieferantn als Flux.
     */
    fun findByEmailStartingWithIgnoreCase(prefix: String): Flux<Lieferant>

    /**
     * Lieferant mit einer bestimmten Emailadresse in der DB löschen.
     * @param email Die Emailadresse.
     * @return Die Anzahl der gelöschten Lieferantn.
     */
    fun deleteByEmail(email: String): Mono<Long>
}

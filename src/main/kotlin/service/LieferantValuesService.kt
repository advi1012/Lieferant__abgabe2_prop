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

import de.hska.lieferant.db.LieferantRepository
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Anwendungslogik für Werte zu Lieferantn (für "Software Engineering").
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@Component
class LieferantValuesService(private val repo: LieferantRepository) {
    /**
     * Die Anzahl aller Lieferantn ermitteln.
     * @return Die Anzahl aller Lieferantn.
     */
    fun anzahlLieferantn() = repo.count()

    /**
     * Nachnamen anhand eines Präfix ermitteln. Projektionen in Spring Data
     * würden "nullable" Properties in den Data-Klassen erfordern.
     *
     * @param prefix Präfix für Nachnamen
     * @return Gefundene Nachnamen
     */
    fun findNachnamenByPrefix(prefix: String) =
        repo.findByNachnameStartingWithIgnoreCase(prefix)
                .timeout(TIMEOUT_SHORT)
                .map { it.nachname }
                .distinct()

    /**
     * Emailadressen anhand eines Präfix ermitteln.
     * @param prefix Präfix für Emailadressen.
     * @return Gefundene Emailadressen.
     */
    fun findEmailsByPrefix(prefix: String) =
        repo.findByEmailStartingWithIgnoreCase(prefix)
                .timeout(TIMEOUT_SHORT)
                .map { it.email }

    /**
     * Version zur Lieferant-ID ermitteln.
     * @param id Lieferant-ID.
     * @return Versionsnummer.
     */
    fun findVersionById(id: String) =
        repo.findById(id)
                .timeout(TIMEOUT_SHORT)
                .map { it.version }

    private companion object {
        @Suppress("MagicNumber")
        val TIMEOUT_SHORT = Duration.ofMillis(500)
    }
}

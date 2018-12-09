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
import de.hska.lieferant.db.LieferantRepository
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.io.InputStream
import java.time.Duration

/**
 * Anwendungslogik für multimediale Daten zu Lieferantn.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@Component
class LieferantMultimediaService(private val repo: LieferantRepository, private val gridFsTemplate: GridFsTemplate) {
    /**
     * Multimediale Datei (Bild oder Video) zu einem Lieferantn mit gegebener ID
     * ermitteln.
     * @param lieferantId Lieferant-ID
     * @return Multimediale Datei, falls sie existiert. Sonst empty().
     */
    fun findMedia(lieferantId: String) =
        repo.existsById(lieferantId)
                .timeout(timeout)
                .flatMap {
                    val file = if (it)
                        gridFsTemplate.getResource(lieferantId)
                    else null
                    Mono.justOrEmpty(file)
                }

    /**
     * Multimediale Daten aus einem Inputstream werden persistent mit der
     * gegebenen Lieferantn-ID als Dateiname abgespeichert. Der Inputstream wird
     * am Ende geschlossen.
     *
     * @param inputStream Inputstream mit multimedialen Daten.
     * @param lieferantId Lieferant-ID
     * @param contentType MIME-Type, z.B. image/png
     * @return ID der neuangelegten multimedialen Datei
     */
    @Transactional
    fun save(inputStream: InputStream, lieferantId: String, contentType: String) =
        repo.existsById(lieferantId)
                .timeout(timeout)
                .flatMap {
                    // TODO MIME-Type ueberpruefen
                    logger.warn("TODO: MIME-Type ueberpruefen")
                    val idGridFS = if (it) {
                        // ggf. multimediale Datei loeschen
                        val criteria = Criteria.where("filename")
                                .isEqualTo(lieferantId)
                        val query = Query(criteria)
                        gridFsTemplate.delete(query)

                        // store() schliesst auch den Inputstream
                        gridFsTemplate
                                .store(inputStream, lieferantId, contentType)
                                .toHexString()
                    } else {
                        null
                    }

                    logger.trace("ID GridFS: {}", idGridFS)
                    idGridFS.toString().toMono()
                }

    private companion object {
        val logger = logger()
        @Suppress("MagicNumber", "HasPlatformType")
        val timeout = Duration.ofMillis(500)
    }
}

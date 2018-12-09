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
package de.hska.lieferant.entity

import com.fasterxml.jackson.annotation.JsonValue
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

/**
 * Enum für Lieferzeit. Dazu kann auf der Clientseite z.B. ein Dropdown-Menü
 * realisiert werden.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 *
 * @property value Der interne Wert
 */
enum class LieferzeitType(val value: String) {
    /**
     * _Lang_ mit dem internen Wert `L` für z.B. das Mapping in einem
     * JSON-Datensatz oder das Abspeichern in einer DB.
     */
    LANG("L"),
    /**
     * _MITTELLANG_ mit dem internen Wert `ML` für z.B. das Mapping in einem
     * JSON-Datensatz oder das Abspeichern in einer DB.
     */
    MITTELLANG("ML"),
    /**
     * _KURZ_ mit dem internen Wert `K` für z.B. das Mapping in einem
     * JSON-Datensatz oder das Abspeichern in einer DB.
     */
    KURZ("K");

    /**
     * Einen enum-Wert als String mit dem internen Wert ausgeben. Dieser Wert
     * wird durch Jackson in einem JSON-Datensatz verwendet.
     * [https://github.com/FasterXML/jackson-databind/wiki]
     * @return Der interne Wert.
     */
    @JsonValue
    override fun toString() = value

    /**
     * Konvertierungsklasse für MongoDB, um einen String einzulesen und
     * ein Enum-Objekt von FamilienstandType zu erzeugen.
     * Wegen @ReadingConverter ist kein Lambda-Ausdruck möglich.
     * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
     */
    @ReadingConverter
    class ReadConverter : Converter<String, LieferzeitType> {
        /**
         * Konvertierung eines Strings in einen LieferzeitType.
         * @param value Der zu konvertierende String.
         * @return Der passende FamilienstandType oder null.
         */
        override fun convert(value: String) = LieferzeitType.build(value)
    }

    /**
     * Konvertierungsklasse für MongoDB, um LieferzeitType in einen
     * String zu konvertieren.
     * Wegen @WritingConverter ist kein Lambda-Ausdruck möglich.
     * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
     */
    @WritingConverter
    class WriteConverter : Converter<LieferzeitType, String> {
        /**
         * Konvertierung eines LieferzeitType in einen String für JSON oder für
         * MongoDB.
         * @param lieferzeit Zu konvertierender LieferzeitType
         * @return Der passende String
         */
        override fun convert(lieferzeit: LieferzeitType) =
            lieferzeit.value
    }

    companion object {
        private val nameCache = HashMap<String, LieferzeitType>().apply {
            enumValues<LieferzeitType>().forEach {
                put(it.value, it)
                put(it.value.toLowerCase(), it)
                put(it.name, it)
                put(it.name.toLowerCase(), it)
            }
        }

        /**
         * Konvertierung eines Strings in einen Enum-Wert
         * @param value Der String, zu dem ein passender Enum-Wert ermittelt
         *      werden soll. Keine Unterscheidung zwischen Groß- und Kleinschreibung.
         * @return Passender Enum-Wert oder null.
         */
        fun build(value: String?): LieferzeitType? = nameCache[value]
    }
}

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
package de.hska.lieferant.rest.util

import de.hska.lieferant.entity.InteresseType
import de.hska.lieferant.entity.Lieferant

/**
 * Singleton-Klasse, um PATCH-Operationen auf Lieferant-Objekte anzuwenden.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
object LieferantPatcher {
    /**
     * PATCH-Operationen werden auf ein Lieferant-Objekt angewandt.
     * @param lieferant Das zu modifizierende Lieferant-Objekt.
     * @param operations Die anzuwendenden Operationen.
     * @return Ein Lieferant-Objekt mit den modifizierten Properties.
     */
    fun patch(lieferant: Lieferant, operations: List<PatchOperation>): Lieferant {
        val replaceOps = operations.filter { "replace" == it.op }
        var lieferantUpdated = replaceOps(lieferant, replaceOps)

        val addOps = operations.filter { "add" == it.op }
        lieferantUpdated = addInteressen(lieferantUpdated, addOps)

        val removeOps = operations.filter { "remove" == it.op }
        return removeInteressen(lieferantUpdated, removeOps)
    }

    private fun replaceOps(lieferant: Lieferant, ops: Collection<PatchOperation>): Lieferant {
        var lieferantUpdated = lieferant
        ops.forEach { (_, path, value) ->
            when (path) {
                "/nachname" -> {
                    lieferantUpdated = replaceNachname(lieferantUpdated, value)
                }
                "/email" -> {
                    lieferantUpdated = replaceEmail(lieferantUpdated, value)
                }
            }
        }
        return lieferantUpdated
    }

    private fun replaceNachname(lieferant: Lieferant, nachname: String) = lieferant.copy(nachname = nachname)

    private fun replaceEmail(lieferant: Lieferant, email: String) = lieferant.copy(email = email)

    private fun addInteressen(lieferant: Lieferant, ops: Collection<PatchOperation>):
            Lieferant {
        if (ops.isEmpty()) {
            return lieferant
        }
        var lieferantUpdated = lieferant
        ops.filter { "/interessen" == it.path }
                .forEach { lieferantUpdated = addInteresse(it, lieferantUpdated) }
        return lieferantUpdated
    }

    private fun addInteresse(op: PatchOperation, lieferant: Lieferant): Lieferant {
        val interesseStr = op.value
        val interesse = InteresseType.build(interesseStr)
                ?: throw InvalidInteresseException(interesseStr)
        val interessen = if (lieferant.interessen == null)
            mutableListOf()
        else lieferant.interessen.toMutableList()
        interessen.add(interesse)
        return lieferant.copy(interessen = interessen)
    }

    private fun removeInteressen(lieferant: Lieferant, ops: List<PatchOperation>): Lieferant {
        if (ops.isEmpty()) {
            return lieferant
        }
        var lieferantUpdated = lieferant
        ops.filter { "/interessen" == it.path }
                .forEach { lieferantUpdated = removeInteresse(it, lieferant) }
        return lieferantUpdated
    }

    private fun removeInteresse(op: PatchOperation, lieferant: Lieferant): Lieferant {
        val interesseStr = op.value
        val interesse = InteresseType.build(interesseStr)
                ?: throw InvalidInteresseException(interesseStr)
        val interessen = lieferant.interessen?.filter { it != interesse }
        return lieferant.copy(interessen = interessen)
    }
}

/**
 * Exception, falls bei einer PATCH-Operation ein ungültiger Wert für ein
 * Interesse verwendet wird.
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
class InvalidInteresseException(value: String)
    : RuntimeException("$value ist kein gueltiges Interesse")

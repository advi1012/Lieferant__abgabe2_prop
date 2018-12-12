package de.hska.lieferant.entity

import java.math.BigDecimal
import java.util.Currency

/**
 * Geldbetrag und Währungseinheit für eine Umsatzangabe.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 *
 * @property skonto Der Skonto als unveränderliches Pflichtfeld.
 * @property rabatt Der Rabatt als unveränderliches Pflichtfeld.
 * @property bonus Der Bonus als unveränderliches Pflichtfeld.
 * @property waehrung Die Währungseinheit als unveränderliches Pflichtfeld.
 * @constructor Erzeugt ein Objekt mit Skonto, Rabatt, Bonus und Währung
 */
data class Kondition(val skonto: BigDecimal, val rabatt: BigDecimal, val bonus: BigDecimal, val waehrung: Currency)

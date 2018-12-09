/*
 * Copyright (C) 2018 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package de.hska.lieferant.config.dev

import de.hska.lieferant.entity.*
import reactor.core.publisher.Flux
import java.math.BigDecimal
import java.net.URL
import java.time.LocalDate
import java.util.Currency

/**
 * Testdaten für Lieferantn
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@SuppressWarnings("MagicNumber")
val lieferantn = Flux.just(
        Lieferant(
                id = "00000000-0000-0000-0000-000000000000",
                nachname = "Meier",
                email = "admin@hska.de",
                kategorie = 0,
                newsletter = true,
                geburtsdatum = LocalDate.of(2018, 1, 31),
                umsatz = Umsatz(BigDecimal("0"),
                        Currency.getInstance("EUR")),
                kondition = Kondition(BigDecimal("3"), BigDecimal("5"),BigDecimal("0"),
                        Currency.getInstance("EUR")),
                homepage = URL("https://www.hska.de"),
                geschlecht = GeschlechtType.build("W"),
                lieferzeit = LieferzeitType.build("L"),
                familienstand = FamilienstandType.build("VH"),
                interessen = listOf(InteresseType.build("L")!!),
                adresse = Adresse("00000", "Aachen"),
                username = "admin"
        ),
        Lieferant(
                id = "00000000-0000-0000-0000-000000000001",
                nachname = "Fischer",
                email = "alpha@hska.edu",
                kategorie = 1,
                newsletter = true,
                geburtsdatum = LocalDate.of(2018, 1, 1),
                umsatz = Umsatz(BigDecimal("10"),
                        Currency.getInstance("USD")),
                kondition = Kondition(BigDecimal("2"), BigDecimal("10"),BigDecimal("3"),
                        Currency.getInstance("USD")),
                homepage = URL("https://www.hska.edu"),
                geschlecht = GeschlechtType.build("M"),
                lieferzeit = LieferzeitType.build("ML"),
                familienstand = FamilienstandType.build("L"),
                interessen = listOf(
                        InteresseType.build("S")!!,
                        InteresseType.build("L")!!),
                adresse = Adresse("11111", "Augsburg"),
                username = "alpha1"
        ),
        Lieferant(
                id = "00000000-0000-0000-0000-000000000002",
                nachname = "Wagner",
                email = "alpha@hska.ch",
                kategorie = 2,
                newsletter = true,
                geburtsdatum = LocalDate.of(2018, 1, 2),
                umsatz = Umsatz(BigDecimal("20"),
                        Currency.getInstance("CHF")),
                kondition = Kondition(BigDecimal("6"), BigDecimal("15"),BigDecimal("5"),
                        Currency.getInstance("CHF")),
                homepage = URL("https://www.hska.ch"),
                geschlecht = GeschlechtType.build("W"),
                lieferzeit = LieferzeitType.build("K"),
                familienstand = FamilienstandType.build("G"),
                interessen = listOf(
                        InteresseType.build("S")!!,
                        InteresseType.build("R")!!),
                adresse = Adresse("22222", "Aalen"),
                username = "alpha2"
        ),
        Lieferant(
                id = "00000000-0000-0000-0000-000000000003",
                nachname = "Koch",
                email = "alpha@hska.uk",
                kategorie = 3,
                newsletter = true,
                geburtsdatum = LocalDate.of(2018, 1, 3),
                umsatz = Umsatz(BigDecimal("30"),
                        Currency.getInstance("GBP")),
                kondition = Kondition(BigDecimal("3"), BigDecimal("5"),BigDecimal("10"),
                        Currency.getInstance("GBP")),
                homepage = URL("https://www.hska.uk"),
                geschlecht = GeschlechtType.build("M"),
                lieferzeit = LieferzeitType.build("K"),
                familienstand = FamilienstandType.build("VW"),
                interessen = listOf(
                        InteresseType.build("L")!!,
                        InteresseType.build("R")!!),
                adresse = Adresse("33333", "Ahlen"),
                username = "alpha3"
        ),
        Lieferant(
                id = "00000000-0000-0000-0000-000000000004",
                nachname = "Bauer",
                email = "delta@hska.jp",
                kategorie = 4,
                newsletter = true,
                geburtsdatum = LocalDate.of(2018, 1, 4),
                umsatz = Umsatz(BigDecimal("40"),
                        Currency.getInstance("JPY")),
                kondition = Kondition(BigDecimal("2"), BigDecimal("20"),BigDecimal("5"),
                        Currency.getInstance("JPY")),
                homepage = URL("https://www.hska.jp"),
                geschlecht = GeschlechtType.build("W"),
                lieferzeit = null,
                familienstand = FamilienstandType.build("VH"),
                interessen = null,
                adresse = Adresse("44444", "Dortmund"),
                username = "delta"
        ),
        Lieferant(
                id = "00000000-0000-0000-0000-000000000005",
                nachname = "Schwarz",
                email = "epsilon@hska.cn",
                kategorie = 5,
                newsletter = true,
                geburtsdatum = LocalDate.of(2018, 1, 5),
                umsatz = null,
                kondition = null,
                homepage = URL("https://www.hska.cn"),
                geschlecht = GeschlechtType.build("M"),
                lieferzeit = LieferzeitType.build("K"),
                familienstand = FamilienstandType.build("L"),
                interessen = null,
                adresse = Adresse("55555", "Essen"),
                username = "epsilon"
        ),
        Lieferant(
                id = "00000000-0000-0000-0000-000000000006",
                nachname = "Winfried",
                email = "phi@hska.cn",
                kategorie = 6,
                newsletter = true,
                geburtsdatum = LocalDate.of(2018, 1, 6),
                umsatz = null,
                kondition = null,
                homepage = URL("https://www.hska.cn"),
                geschlecht = GeschlechtType.build("M"),
                lieferzeit = LieferzeitType.build("ML"),
                familienstand = FamilienstandType.build("L"),
                interessen = null,
                adresse = Adresse("66666", "Freiburg"),
                username = "phi"
        )
)

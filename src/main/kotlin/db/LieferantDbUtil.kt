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
package de.hska.lieferant.db

import de.hska.lieferant.db.LieferantDbUtil.updateAdresse
import de.hska.lieferant.db.LieferantDbUtil.updateEmail
import de.hska.lieferant.db.LieferantDbUtil.updateFamilienstand
import de.hska.lieferant.db.LieferantDbUtil.updateGeburtsdatum
import de.hska.lieferant.db.LieferantDbUtil.updateGeschlecht
import de.hska.lieferant.db.LieferantDbUtil.updateHomepage
import de.hska.lieferant.db.LieferantDbUtil.updateInteressen
import de.hska.lieferant.db.LieferantDbUtil.updateKategorie
import de.hska.lieferant.db.LieferantDbUtil.updateNachname
import de.hska.lieferant.db.LieferantDbUtil.updateNewsletter
import de.hska.lieferant.db.LieferantDbUtil.updateUmsatz
import de.hska.lieferant.entity.Adresse
import de.hska.lieferant.entity.FamilienstandType
import de.hska.lieferant.entity.GeschlechtType
import de.hska.lieferant.entity.InteresseType
import de.hska.lieferant.entity.Lieferant
import de.hska.lieferant.entity.Umsatz
import org.springframework.util.ReflectionUtils.findField
import org.springframework.util.ReflectionUtils.makeAccessible
import org.springframework.util.ReflectionUtils.setField
import java.net.URL
import java.time.LocalDate

// Hilfsfunktionalitaet fuer PUT und PATCH:
// Um eine Aenderung zu machen, muss ein Datensatz zuerst aus der DB ausgelesen
// werden. Danach ist er in einem Cache von Spring Data.
// Wenn man nun ein *NEUES* Objekt als Kopie im Hauptspeicher erstellt, das die
// zu aendernden Werte und die ID des in der DB vorhandenen Objekts enthaelt,
// dann gibt es im Cache *2* Objekte mit *GLEICHER ID*.
// Durch die Extension-Function koennen Properties bei einem Lieferant-Objekt
// geaendert werden, das aus der DB ausgelesen wurde und *IMMUTABLE* (var) ist.
// Folglich gibt es im Cache nur *1* Objekt mit der zugehoerigen ID.

/**
 * Extension Function für [Lieferant], um ein _immutable_ Objekt im Cache von
 * _Spring Data MongoDB_ modifizieren zu können.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
fun Lieferant.update(other: Lieferant) {
    // alle Werte bis auf ID und account setzen

    updateNachname(this, other.nachname)
    updateEmail(this, other.email)
    updateKategorie(this, other.kategorie)
    updateNewsletter(this, other.newsletter)
    updateGeburtsdatum(this, other.geburtsdatum)
    updateUmsatz(this, other.umsatz)
    updateHomepage(this, other.homepage)
    updateGeschlecht(this, other.geschlecht)
    updateFamilienstand(this, other.familienstand)
    updateInteressen(this, other.interessen)
    updateAdresse(this, other.adresse)
}

/**
 * Singleton-Klasse, um die Extension Function [Lieferant.update] zu realisieren.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@Suppress("TooManyFunctions")
object LieferantDbUtil {
    private
    val clazz = Lieferant::class.java

    private
    val nachnameProp = findField(clazz, "nachname")!!

    private
    val emailProp = findField(clazz, "email")!!

    private
    val kategorieProp = findField(clazz, "kategorie")!!

    private
    val newsletterProp = findField(clazz, "newsletter")!!

    private
    val geburtsdatumProp = findField(clazz, "geburtsdatum")!!

    private
    val umsatzProp = findField(clazz, "umsatz")!!

    private
    val homepageProp = findField(clazz, "homepage")!!

    private
    val geschlechtProp = findField(clazz, "geschlecht")!!

    private
    val familienstandProp = findField(clazz, "familienstand")!!

    private
    val interessenProp = findField(clazz, "interessen")!!

    private
    val adresseProp = findField(clazz, "adresse")!!

    init {
        makeAccessible(nachnameProp)
        makeAccessible(emailProp)
        makeAccessible(kategorieProp)
        makeAccessible(newsletterProp)
        makeAccessible(geburtsdatumProp)
        makeAccessible(umsatzProp)
        makeAccessible(homepageProp)
        makeAccessible(geschlechtProp)
        makeAccessible(familienstandProp)
        makeAccessible(interessenProp)
        makeAccessible(adresseProp)
    }

    /**
     * Den Nachnamen eines immutable Lieferantn überschreiben.
     * @param lieferant Lieferant-Objekt mit dem zu überschreibenden Nachnamen.
     * @param nachname Der neue Nachname.
     */
    fun updateNachname(lieferant: Lieferant, nachname: String) =
            setField(nachnameProp, lieferant, nachname)

    /**
     * Die Emailadresse eines immutable Lieferantn überschreiben.
     * @param lieferant Lieferant-Objekt mit der zu überschreibenden Emailadresse.
     * @param email Die neue Emailadresse.
     */
    fun updateEmail(lieferant: Lieferant, email: String) =
            setField(emailProp, lieferant, email)

    /**
     * Die Kategorie eines immutable Lieferantn überschreiben.
     * @param lieferant Lieferant-Objekt mit der zu überschreibenden Kategorie.
     * @param kategorie Die neue Kategorie.
     */
    fun updateKategorie(lieferant: Lieferant, kategorie: Int) =
            setField(kategorieProp, lieferant, kategorie)

    /**
     * Das Flag für Newsletter eines immutable Lieferantn überschreiben.
     * @param lieferant Lieferant-Objekt mit dem zu überschreibenden Flag.
     * @param newsletter Das neue Flag für Newsletter.
     */
    fun updateNewsletter(lieferant: Lieferant, newsletter: Boolean) =
            setField(newsletterProp, lieferant, newsletter)

    /**
     * Das Geburtsdatum eines immutable Lieferantn überschreiben.
     * @param lieferant Lieferant-Objekt mit dem zu überschreibenden Geburtsdatum.
     * @param geburtsdatum Das neue Geburtsdatum.
     */
    fun updateGeburtsdatum(lieferant: Lieferant, geburtsdatum: LocalDate?) =
            setField(geburtsdatumProp, lieferant, geburtsdatum)

    /**
     * Den Umsatz eines immutable Lieferantn überschreiben.
     * @param lieferant Lieferant-Objekt mit dem zu überschreibenden Umsatz.
     * @param umsatz Der neue Umsatz.
     */
    fun updateUmsatz(lieferant: Lieferant, umsatz: Umsatz?) =
            setField(umsatzProp, lieferant, umsatz)

    /**
     * Die URL für die Homepage eines immutable Lieferantn überschreiben.
     * @param lieferant Lieferant-Objekt mit der zu überschreibenden Homepage.
     * @param homepage Die neue URL.
     */
    fun updateHomepage(lieferant: Lieferant, homepage: URL?) =
            setField(homepageProp, lieferant, homepage)

    /**
     * Das Geschlecht eines immutable Lieferantn überschreiben.
     * @param lieferant Lieferant-Objekt mit dem zu überschreibenden Geschlecht.
     * @param geschlecht Das neue Geschlecht.
     */
    fun updateGeschlecht(lieferant: Lieferant, geschlecht: GeschlechtType?) =
            setField(geschlechtProp, lieferant, geschlecht)

    /**
     * Den Familienstand eines immutable Lieferantn überschreiben.
     * @param lieferant Lieferant-Objekt mit dem zu überschreibenden Familienstand.
     * @param familienstand Der neue Familienstand.
     */
    fun updateFamilienstand(lieferant: Lieferant, familienstand: FamilienstandType?) =
            setField(familienstandProp, lieferant, familienstand)

    /**
     * Die Interessen eines immutable Lieferantn überschreiben.
     * @param lieferant Lieferant-Objekt mit den zu überschreibenden Interessen.
     * @param interessen Die neuen Interessen.
     */
    fun updateInteressen(lieferant: Lieferant, interessen: List<InteresseType>?) =
            setField(interessenProp, lieferant, interessen)

    /**
     * Die Adresse eines immutable Lieferantn überschreiben.
     * @param lieferant Lieferant-Objekt mit der zu überschreibenden Adresse.
     * @param adresse Die neue Adresse.
     */
    fun updateAdresse(lieferant: Lieferant, adresse: Adresse) =
            setField(adresseProp, lieferant, adresse)
}

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
package de.hska.lieferant.config

import de.hska.lieferant.Router.Companion.authPath
import de.hska.lieferant.Router.Companion.emailPath
import de.hska.lieferant.Router.Companion.multimediaPath
import de.hska.lieferant.Router.Companion.nachnamePath
import de.hska.lieferant.Router.Companion.versionPath
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.OPTIONS
import org.springframework.http.HttpMethod.PATCH
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.security.config.web.server.ServerHttpSecurity

/**
 * Security-Konfiguration.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
// https://github.com/spring-projects/spring-security/tree/master/samples
interface SecurityConfig {
    /**
     * Bean-Definition, um den Zugriffsschutz an der REST-Schnittstelle zu
     * konfigurieren.
     *
     * @param http Injiziertes Objekt von `ServerHttpSecurity` als
     *      Ausgangspunkt für die Konfiguration.
     * @return Objekt von `SecurityWebFilterChain`
     */
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity) =
        http.authorizeExchange()
                .pathMatchers(POST, lieferantPath).permitAll()
                .pathMatchers(GET, lieferantPath, lieferantIdPAth).hasRole(adminRolle)
                .pathMatchers(GET, multimediaIdPath).hasRole(lieferantRolle)
                .pathMatchers(GET, rollenPath).hasRole(lieferantRolle)
                .pathMatchers(GET, "$nachnamePath/*").hasRole(lieferantRolle)
                .pathMatchers(GET, "$emailPath/*").hasRole(lieferantRolle)
                .pathMatchers(GET, "$versionPath/*").hasRole(lieferantRolle)

                .pathMatchers(PUT, lieferantIdPAth, multimediaIdPath)
                .hasRole(lieferantRolle)
                .pathMatchers(PATCH, lieferantIdPAth).hasRole(adminRolle)
                .pathMatchers(DELETE, lieferantIdPAth).hasRole(adminRolle)
                .pathMatchers(OPTIONS).permitAll()

                .matchers(EndpointRequest.to("health")).permitAll()
                .matchers(EndpointRequest.toAnyEndpoint()).hasRole(endpointAdminRolle)

                .and()
                .httpBasic()

                .and()
                // keine generierte HTML-Seite fuer Login
                .formLogin().disable()
                .csrf().disable()
                // TODO Disable FrameOptions: Clickjacking
                .build()

    @Suppress("MayBeConstant")
    companion object {
        private val adminRolle = "ADMIN"
        private val lieferantRolle = "LIEFERANT"
        private val endpointAdminRolle = "ACTUATOR"

        private val lieferantPath = "/"
        private val lieferantIdPAth = "/*"
        private val multimediaIdPath = "$multimediaPath/*"
        private val rollenPath = "$authPath/rollen"
    }
}

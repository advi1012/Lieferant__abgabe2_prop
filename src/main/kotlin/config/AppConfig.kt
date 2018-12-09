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

import de.hska.lieferant.config.security.PasswordEncoder
import de.hska.lieferant.Router
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.security.config.annotation.web.reactive
        .EnableWebFluxSecurity
import org.springframework.transaction.annotation.EnableTransactionManagement

// @Configuration-Klassen als Einstiegspunkt zur Konfiguration
// Mit CGLIB werden @Configuration-Klassen verarbeitet

/**
 * Konfigurationsklasse für die Anwendung bzw. den Microservice.
 * Konfigurationsklassen werden mit _CGLIB_ verarbeitet.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@Configuration
@EnableMongoAuditing
@EnableTransactionManagement
@EnableWebFluxSecurity
// https://spring.io/blog/2015/06/15/cache-auto-configuration-in-spring-boot-1-3
@EnableCaching
class AppConfig :
        Router,
        DbConfig,
        PasswordEncoder,
        SecurityConfig,
        TransactionConfig

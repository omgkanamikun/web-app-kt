package com.github.omgkanamikun.application.model

/**
 * @author Vlad Kondratenko, email: omgkanamikun@gmail.com
 * @since 18/10/2022
 */
sealed interface Entity {
    data class Person(
        val firstName: String, val lastName: String, val dateOfBirth: String, val nationality: String
    ) : Entity

    data class Company(
        val name: String, val domain: String, val email: String
    ) : Entity
}
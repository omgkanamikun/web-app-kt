package com.github.omgkanamikun.application.util

import com.devskiller.jfairy.Fairy
import com.github.omgkanamikun.application.model.Entity

/**
 * @author Vlad Kondratenko, email: omgkanamikun@gmail.com
 * @since 17/10/2022
 */
fun randomNumberBounded() = (Math.random() * 100).toInt()

fun emptyCompany() = Entity.Company("empty", "empty", "empty")

fun emptyPerson() = Entity.Person("empty", "empty", "empty", "empty")

fun fairy() : Fairy = Fairy.create()


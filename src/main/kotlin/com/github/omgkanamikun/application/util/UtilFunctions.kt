package com.github.omgkanamikun.application.util

import com.devskiller.jfairy.Fairy
import com.github.omgkanamikun.application.handler.model.Entity
import java.util.*

/**
 * @author Vlad Kondratenko, email: omgkanamikun@gmail.com
 * @since 17/10/2022
 */
fun randomNumberBounded(bound: Int) = (Math.random() * bound).toInt()
fun generateId(): String = UUID.randomUUID().toString()
fun emptyCompany() = Entity.Company("empty", "empty", "empty", "empty")
fun emptyPerson() = Entity.Person("empty", "empty", "empty", "empty", "empty")
fun fairy(): Fairy = Fairy.create()

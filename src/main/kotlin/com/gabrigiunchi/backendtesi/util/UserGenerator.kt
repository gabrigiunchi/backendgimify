package com.gabrigiunchi.backendtesi.util

import java.util.*

object UserGenerator {

    private val names = listOf(
            "Marco", "Mattia", "Martina", "Giovanni", "Gabriele", "Antonella", "Fabio", "Francesco", "Alessio", "Davide", "Federico",
            "Sara", "Bianca", "Amedeo", "Sofia", "Chiara", "Beatrice", "Michele", "Michela", "Francesca", "Gabriella"
    )

    private val surnames = listOf(
            "Francisconi", "Bandini", "Giunchi", "Russo", "Ferrari", "Bianchi", "Colombo", "Greco", "Ricci", "Fontana", "Caruso", "Rinaldi",
            "Barbieri", " Rizzo", "Lombardi", "Costa", "Mancini", "Conti", "Sala", "Palmieri", "Bernardi"
    )

    fun generateRandomUser(): SimpleUser {
        val random = Random()
        val name = names[random.nextInt(names.size)]
        val surname = surnames[random.nextInt(surnames.size)]
        val username = "${name.substring(0, 3)}${surname.substring(0, 3)}".toLowerCase()
        val email = "$name.$surname@mail.com"
        return SimpleUser(name, surname, username, email)
    }
}
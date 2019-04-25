package com.gabrigiunchi.backendtesi

import com.gabrigiunchi.backendtesi.config.DBInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BackendtesiApplication : CommandLineRunner {

    @Autowired
    lateinit var dbInitializer: DBInitializer

    @Value("\${application.initDB}")
    private var initDB = false


    override fun run(vararg args: String?) {
        if (this.initDB) {
            this.dbInitializer.initDB()
        } else {
            this.dbInitializer.initUsers()
        }
    }
}


fun main(args: Array<String>) {
    runApplication<BackendtesiApplication>(*args)
}


package com.gabrigiunchi.backendtesi

import com.gabrigiunchi.backendtesi.config.AppInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BackendtesiApplication : CommandLineRunner {

    @Autowired
    lateinit var appInitializer: AppInitializer

    override fun run(vararg args: String?) {
        this.appInitializer.initApp()
    }
}


fun main(args: Array<String>) {
    runApplication<BackendtesiApplication>(*args)
}


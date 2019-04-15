package com.gabrigiunchi.backendtesi

import com.gabrigiunchi.backendtesi.config.DBInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BackendtesiApplication : CommandLineRunner {

	@Autowired
	lateinit var dbInitializer: DBInitializer


	override fun run(vararg args: String?) {
		this.dbInitializer.initDB()
	}
}


fun main(args: Array<String>) {
	runApplication<BackendtesiApplication>(*args)
}


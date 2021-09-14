package br.com.talk

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@EnableFeignClients
@SpringBootApplication(scanBasePackages = ["br.com.talk"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

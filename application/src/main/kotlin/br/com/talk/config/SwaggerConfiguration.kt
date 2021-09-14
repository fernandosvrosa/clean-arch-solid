package br.com.talk.config;

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import springfox.documentation.builders.PathSelectors.regex
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@EnableSwagger2
@Configuration
@Profile("dev", "staging")
class SwaggerConfiguration {
    
    @Bean
    fun swaggerSpringPlugin(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
            .groupName("Nome do projeto")
            .select()
            .paths(regex("/api/.*"))
            .build()
            .apiInfo(apiInfo())
        
    }
    
    private fun apiInfo(): ApiInfo {
        return ApiInfo(
            "Talk Nome do projeto",
            "Descrition do projeto",
            "0.1",
            "",
            Contact("Ontools", "", "fernandosvrosa@gmail.com"),
            "Site", "http://site.com.br/",
            listOf()
        )
    }
    
}

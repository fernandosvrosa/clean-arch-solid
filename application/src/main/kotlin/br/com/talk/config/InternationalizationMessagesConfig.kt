package br.com.talk.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import java.util.*

@Configuration
class InternationalizationMessagesConfig : WebMvcConfigurer {

    @Bean
    fun localeResolver() = SessionLocaleResolver().apply {
        setDefaultLocale(Locale.forLanguageTag("en-US"))
    }

    @Bean
    fun localeInterceptor() = LocaleChangeInterceptor().apply {
        this.paramName = "lang"
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(localeInterceptor())
    }
}
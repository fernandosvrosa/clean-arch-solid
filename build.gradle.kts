import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.3.4.RELEASE" apply false
    id("io.spring.dependency-management") version "1.0.10.RELEASE" apply false
    id("org.jetbrains.kotlin.plugin.jpa") version "1.4.30" apply false
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.spring") version "1.5.10"
    id("org.sonarqube") version "2.7" apply true
}

allprojects {
    group = "br.com.talk"
    version = "1.0"

    repositories {
        jcenter()
        mavenCentral()
    }
}

dependencies {
    // faz com que a configuração dos arquivos do projeto raiz dependa de cada subprojeto
    subprojects.forEach {
        archives(it)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlin.plugin.jpa")
    id("kotlin-jpa")
    kotlin("jvm")
    kotlin("plugin.spring")
    id("jacoco")
    id ("io.swagger.core.v3.swagger-gradle-plugin") version "2.1.9"
}

repositories {
    mavenLocal()
    mavenCentral()
}

extra["springCloudVersion"] = "Hoxton.SR8"

dependencies {
    implementation(project(":domain"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("io.springfox:springfox-swagger2:3.0.0")
    implementation("io.springfox:springfox-swagger-ui:3.0.0")
    implementation("io.springfox:springfox-boot-starter:3.0.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.4.0")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.mockito:mockito-inline:3.3.3")
    testImplementation("org.mockito:mockito-core:3.5.10")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.25.1")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-noarg:1.4.10")
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

jacoco {
    toolVersion = "0.8.5"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    group = "Reporting"
    reports {
        xml.isEnabled = true
        csv.isEnabled = false
        html.isEnabled = true
        html.destination = file("$buildDir/reports/coverage")
    }
}
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                minimum = "0.0".toBigDecimal()
                value = "COVEREDRATIO"
                counter = "LINE"
            }
        }
    }
}
sonarqube {
    val exclusions = listOf(
        "**/config/*.kt",
        "**/domain/*.kt"
    )

    properties {
        property("sonar.jacoco.reportPath", "$buildDir/jacoco/test.exec")
        property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.coverage.exclusions", exclusions)
    }
}



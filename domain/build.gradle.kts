import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm")
    id("jacoco")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("javax.inject:javax.inject:1")
    implementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.6.0")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.mockito:mockito-inline:3.3.3")
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
        "**/model/*.kt",
        "**/exception/*.kt"
    )
    properties {
        property("sonar.jacoco.reportPath", "$buildDir/jacoco/test.exec")
        property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.coverage.exclusions", exclusions)
    }
}



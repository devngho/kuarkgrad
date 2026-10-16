plugins {
    kotlin("multiplatform") version "2.4.21"
    id("io.kotest") version "6.2.6"
    id("com.google.devtools.ksp") version "2.3.12"
}

group = "io.github.devngho"
version = "0.1.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(25)

    jvm {}

    sourceSets {
        applyDefaultHierarchyTemplate()

        commonMain {
            dependencies {
                implementation("com.ionspin.kotlin:bignum:0.3.10")
            }
        }

        commonTest {
            dependencies {
                implementation("io.kotest:kotest-assertions-core:6.2.6")
                implementation("io.kotest:kotest-framework-engine:6.2.6")
            }
        }

        jvmTest {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5:6.2.6")
            }
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}
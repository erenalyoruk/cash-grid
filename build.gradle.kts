plugins {
    java

    id("com.diffplug.spotless") version "8.2.1"

    id("org.springframework.boot") version "4.0.2" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "com.erenalyoruk"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    spotless {
        format("misc") {
            target("**/*.gradle.kts", "**/*.md", "**/*.yml", "**/*.yaml", "**/*.properties")
            targetExclude(
                "**/build/**",
                "**/generated/**",
                "**/out/**"
            )
            trimTrailingWhitespace()
            endWithNewline()
            indentWithSpaces(4)
        }
    }

    plugins.withId("java") {
        spotless {
            java {
                target("src/**/*.java")
                googleJavaFormat("1.34.1")
                    .aosp()
                    .reflowLongStrings()

                formatAnnotations()

                importOrder()
                removeUnusedImports()
            }
        }
    }

    tasks.matching { it.name == "check" }.configureEach {
        dependsOn("spotlessCheck")
    }
}

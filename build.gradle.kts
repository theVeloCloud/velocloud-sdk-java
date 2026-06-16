plugins {
    id("com.gradleup.shadow") version "9.0.0"
    id("java-library")
    `maven-publish`
}

group = "de.snenjih.velocloud"
version = "3.0.7"

repositories {
    mavenLocal()
    mavenCentral()

    maven {
        name = "velocloud-snapshots"
        url = uri("https://repo.snenjih.de/snapshots")
    }
    maven {
        name = "velocloud-releases"
        url = uri("https://repo.snenjih.de/releases")
    }
}

tasks.shadowJar {
    archiveClassifier.set(null)
    mergeServiceFiles()
}

dependencies {
    api("io.grpc:grpc-services:1.81.0")
    api("io.grpc:grpc-netty-shaded:1.81.0")
    api("com.zaxxer:HikariCP:5.1.0")
    api("com.mysql:mysql-connector-j:9.3.0")

    api("de.snenjih.velocloud:proto:$version")
    api("de.snenjih.velocloud:shared:$version")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.3.0")

    compileOnly("com.google.code.gson:gson:2.13.2")
    compileOnly("org.jetbrains:annotations:26.0.2-1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

tasks.publish {
    dependsOn(tasks.shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.named("shadowJar")) {
                classifier = null
            }

            pom {
                description.set("VeloCloud Java client SDK")
                url.set("https://github.com/theVeloCloud/velocloud-sdk-java")

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        name.set("Mirco Lindenau")
                        email.set("mirco.lindenau@gmx.de")
                    }
                }
                scm {
                    url.set("https://github.com/theVeloCloud/velocloud-sdk-java")
                    connection.set("scm:git:https://github.com/theVeloCloud/velocloud-sdk-java.git")
                    developerConnection.set("scm:git:https://github.com/theVeloCloud/velocloud-sdk-java.git")
                }
            }
        }
    }

    repositories {
        maven {
            name = "reposilite"
            url = uri(
                if (version.toString().endsWith("-SNAPSHOT"))
                    "https://repo.snenjih.de/snapshots"
                else
                    "https://repo.snenjih.de/releases"
            )
            credentials {
                username = System.getenv("REPOSILITE_USER") ?: ""
                password = System.getenv("REPOSILITE_SECRET") ?: ""
            }
        }
    }
}

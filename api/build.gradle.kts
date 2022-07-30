import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.0"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	war
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
}

group = "com.heyskylark"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	// Crypto
	implementation("com.squareup.okhttp3:okhttp:4.9.0")
	implementation ("org.web3j:core:5.0.0")

	implementation("com.cloudinary:cloudinary-http44:1.32.2")

	implementation("org.apache.httpcomponents:httpclient:4.5.13")

	implementation("com.twitter:twitter-api-java-sdk:1.1.4")

	implementation("commons-io:commons-io:2.11.0")
	implementation("org.apache.logging.log4j:log4j-core:2.17.2")
	implementation("org.apache.commons:commons-lang3:3.12.0")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
	
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.mockk:mockk:1.12.4")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

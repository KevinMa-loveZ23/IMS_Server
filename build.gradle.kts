import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.2"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "1.9.22"
	kotlin("plugin.spring") version "1.9.22"

	kotlin("plugin.serialization") version "1.9.22"
}

group = "xyz.kein-thema"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.security:spring-security-test")

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")

	testImplementation("org.projectlombok:lombok")


	implementation("io.jsonwebtoken:jjwt-api:0.12.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

	implementation("jakarta.mail:jakarta.mail-api")
//	"jakarta.mail:jakarta.mail-api:2.1.3"
	runtimeOnly("org.eclipse.angus:angus-mail")
//	"org.eclipse.angus:angus-mail:2.0.2"

	implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

	implementation("io.minio:minio:8.5.10")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// 清除现有的lib目录
//task clearJar(type: Delete) {
//	delete "$buildDir/libs/lib"
//}

//tasks.named<Delete>("clearJar") {
//	delete("${layout.buildDirectory}/libs/lib")
//}

// 将依赖包复制到lib目录
//task copyJar(type: Copy, dependsOn: 'clearJar') {
//	from configurations.compileClasspath
//			into "$buildDir/libs/lib"
//}

//tasks.named<Copy>("copyJar") {
//	dependsOn("clearJar")
//	from(configurations.compileClasspath)
//		.into("${layout.buildDirectory}/libs/lib")
//}

//bootJar {
//	excludes = ["*.jar", "application.yml"]
//	dependsOn clearJar
//			dependsOn copyJar
//			// 指定依赖包的路径
//			manifest {
//				attributes "Manifest-Version": 1.0,
//				'Class-Path': configurations.compileClasspath.files.collect { "lib/$it.name" }.join(' ')
//			}
//}

//tasks.bootJar {
//	excludes.add("*.jar")
//	excludes.add("application.yml")
//	dependsOn("clearJar")
//	dependsOn("copyJar")
//	manifest {
//		attributes["Manifest-Version"] = 1.0
//		attributes["Class-Path"] = configurations.compileClasspath
//			.files.collect { "lib/$it.name" }.join(' ')
//	}
//}
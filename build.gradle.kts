import java.text.SimpleDateFormat
import java.util.Date

plugins {
	id("idea")
	id("java-library")
	id("maven-publish")
	id("net.neoforged.gradle.userdev") version "7.0.+"
	id("io.github.themrmilchmann.curseforge-publish") version "0.6.1"
}

val username: String? = System.getenv("MAVEN_USERNAME")
val password: String? = System.getenv("MAVEN_PASSWORD")
val token: String? = System.getenv("CURSEFORGE_TOKEN")

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

println("Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}")

minecraft.accessTransformers.file(rootProject.file("src/main/resources/META-INF/accesstransformer.cfg"))

runs {
	configureEach {
		workingDirectory(project.file("run"))

		systemProperty("neoforge.logging.markers", "REGISTRIES")
		systemProperty("neoforge.logging.console.level", "debug")
		systemProperty("neoforge.enabledGameTestNamespaces", "xbackpack")

		modSource(project.sourceSets.main.get())
	}

	create("client") {
		systemProperty("neoforge.enabledGameTestNamespaces", "xbackpack")
	}

	create("server") {
		systemProperty("neoforge.enabledGameTestNamespaces", "xbackpack")
		arguments("--nogui")
	}
}

sourceSets.main.configure {
	resources.srcDir("src/generated/resources")
}

repositories {
	maven {
		name = "Jared's maven"
		url = uri("https://maven.blamejared.com/")
	}
	maven {
		name = "ModMaven"
		url = uri("https://modmaven.dev/")
	}
}

dependencies {
	implementation("net.neoforged:neoforge:${property("NeoForgeVersion")}")

	compileOnly("mezz.jei:jei-${property("MinecraftVersion")}-common-api:${property("JeiVersion")}")
	compileOnly("mezz.jei:jei-${property("MinecraftVersion")}-neoforge-api:${property("JeiVersion")}")
	runtimeOnly("mezz.jei:jei-${property("MinecraftVersion")}-neoforge:${property("JeiVersion")}")
}

java {
	withSourcesJar()
}

val ver = "${property("MinecraftVersion")}-${property("ModVersion")}"

curseforge {
	apiToken = token ?: ""
	publications {
		register("curseForge") {
			projectId = "557199"

			artifacts.register("main") {
				displayName = "XBackpack-$ver"
				from(tasks.named("jar"))
			}
		}
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			groupId = "net.luis"
			artifactId = "XBackpack"
			version = ver
			artifact(tasks.jar)
			artifact(tasks.named("sourcesJar"))
		}
	}
	repositories {
		if (username != null && password != null) {
			maven {
				url = uri("https://maven.luis-st.net/forge/")
				credentials {
					this.username = username
					this.password = password
				}
			}
		} else {
			logger.error("No credentials provided. Publishing to maven.luis-st.net not possible.")
		}
	}
}

val resourceTargets = listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")
val replaceProperties = mapOf(
	"MinecraftVersion" to property("MinecraftVersion"),
	"MinecraftVersionRange" to property("MinecraftVersionRange"),
	"NeoForgeVersion" to property("NeoForgeVersion"),
	"NeoForgeVersionRange" to property("NeoForgeVersionRange"),
	"ModVersion" to property("ModVersion")
)

tasks.processResources {
	inputs.properties(replaceProperties)

	filesMatching(resourceTargets) {
		expand(replaceProperties)
	}
}

tasks.jar {
	archiveFileName.set("XBackpack-$ver.jar")
	manifest {
		attributes(
			mapOf(
				"Specification-Title" to "XBackpack",
				"Specification-Version" to ver.split("-")[1],
				"Implementation-Title" to project.name,
				"Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ").format(Date()),
				"Mod-Author" to "Luis-st",
				"Mod-ID" to "xbackpack"
			)
		)
	}
}

tasks.named<Jar>("sourcesJar") {
	outputs.upToDateWhen { false }
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
}

idea {
	module {
		isDownloadSources = true
		isDownloadJavadoc = true
	}
}

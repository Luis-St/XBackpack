rootProject.name = "XBackpack"

pluginManagement {
	plugins {
		id("net.luis.lm") version "1.1.0"
	}
	
	repositories {
		gradlePluginPortal()
		maven {
			name = "NeoForged"
			url = uri("https://maven.neoforged.net/releases/")
		}
		maven {
			url = uri("https://maven.luis-st.net/plugins/")
		}
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

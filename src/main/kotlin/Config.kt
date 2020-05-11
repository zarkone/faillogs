package org.zarkone.faillogs

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.File
import java.lang.IllegalArgumentException


private val configFile = File(System.getProperty("user.home") + "/.config/faillogs/config.json")

fun writeConfigFile(configToFile: ConfigMap) {
    configToFile.validateUsername()
    val configString = Json(JsonConfiguration(encodeDefaults = false)).stringify(ConfigMap.serializer(), configToFile)

    configFile.writeText(configString)
}

@Serializable
class ConfigMap(var githubUser: String, var githubToken: String = "") {
    fun merge(o: ConfigMap): ConfigMap {
        return ConfigMap(
                githubUser = 
                if (o.githubUser.isNotBlank()) { o.githubUser } 
                else { githubUser },
        
                githubToken =
                if (o.githubToken.isNotBlank()) { o.githubToken }
                else { githubToken }
        )
    }

    fun validateUsername() {
        when {
            githubUser.isBlank() -> {
                throw IllegalArgumentException("Github Username must be provided.")
            }
        }
    }

    fun validateToken() {
        when {
            githubToken.isBlank() -> {
                throw IllegalArgumentException("Github Token must be provided.")
            }
        }
    }

    fun validate() {
        validateUsername()
        validateToken()
    }

}

class Config {
    private var configMap: ConfigMap


    val githubToken: String
        get() = configMap.githubToken
    val githubUser: String
        get() = configMap.githubUser

    init {
        configMap = fromConfigFile().merge(fromEnv())
        configMap.validate()
    }

    private fun fromEnv(): ConfigMap {
        return ConfigMap(
                githubUser = System.getenv("GITHUB_USER") ?: "",
                githubToken = System.getenv("GITHUB_TOKEN") ?: ""
        )
    }

    private fun fromConfigFile(): ConfigMap {
        if (!configFile.exists()) {
            return ConfigMap(githubUser = "")
        }

        return Json.parse(
                ConfigMap.serializer(),
                string = configFile.readText()
        )
    }
}
package org.zarkone.faillogs

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.lang.IllegalArgumentException

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

    fun validate(): Boolean {
        when {
            githubUser.isBlank() -> {
                throw IllegalArgumentException("Github Username must be provided. Set ENV['GITHUB_USER'] or do `faillogs login`")
            }
            githubToken.isBlank() -> {
                throw IllegalArgumentException("Github Token must be provided. Set ENV['GITHUB_TOKEN'] or do `faillogs login`")
            }
        }

        return true
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
        val configFile = File(System.getProperty("user.home") + "/.config/faillogs/config.json")

        if (!configFile.exists()) {
            return ConfigMap(githubUser = "")
        }

        return Json.parse(
                ConfigMap.serializer(),
                string = configFile.readText()
        )
    }
}
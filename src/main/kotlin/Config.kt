package org.zarkone.faillogs

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.File
import java.lang.IllegalArgumentException

@Serializable
class ConfigMap(var githubUser: String, var githubToken: String = "") {
    fun merge(o: ConfigMap): ConfigMap {
        if (o.githubUser.isNotBlank()) {
            githubUser = o.githubUser
        }

        if (o.githubToken.isNotBlank()) {
            githubToken = o.githubToken
        }
        return this
    }

    fun validate() {
        when {
            githubUser.isBlank() -> {
                throw IllegalArgumentException("Github Username must be provided. Set ENV['GITHUB_USER'] or do `faillogs login`")
            }
            githubToken.isBlank() -> {
                throw IllegalArgumentException("Github Token must be provided. Set ENV['GITHUB_TOKEN'] or do `faillogs login`")
            }
        }
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
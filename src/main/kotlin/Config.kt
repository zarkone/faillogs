package org.zarkone.faillogs

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.File
import java.lang.IllegalArgumentException

@Serializable
data class ConfigMap(var githubUser: String, var githubToken: String = "")

class Config {
    var githubToken: String
    var githubUser: String

    init {
        val fromFile = fromConfigFile()
        val fromEnv = fromEnv()

        when {
            !fromEnv.githubUser.isBlank() -> { githubUser = fromEnv.githubUser }
            fromFile.githubUser.isBlank() -> {
                throw IllegalArgumentException("Github Username must be provided. Set ENV['GITHUB_USER'] or do `faillogs login`")
            }
            else -> { githubUser = fromFile.githubUser }
        }

        when {
            !fromEnv.githubToken.isBlank() -> { githubToken = fromEnv.githubToken }
            fromFile.githubToken.isBlank() -> {
                throw IllegalArgumentException("Github Token must be provided. Set ENV['GITHUB_TOKEN'] or do `faillogs login`")
            }
            else -> { githubToken = fromFile.githubToken }
        }
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

        val configText = configFile.readText()

        return Json(JsonConfiguration()).parse(ConfigMap.serializer(), configText)

    }
}
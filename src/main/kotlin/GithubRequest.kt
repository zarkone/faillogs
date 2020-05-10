package org.zarkone.faillogs

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.requests.DownloadRequest
import com.github.kittinunf.fuel.httpDownload
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.util.encodeBase64ToString

import com.github.kittinunf.result.Result
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import sun.jvm.hotspot.memory.HeapBlock
import java.io.File
import javax.security.auth.login.Configuration


@Serializable
data class Runs(val total_count: Int, val workflow_runs: List<WorkflowRun>)

@Serializable
data class WorkflowRun(val logs_url: String, val conclusion: String)

class GithubResponse {

}

class GithubRequest(config: Config, repo: String) {
    var repo = repo
    var config = config

    fun getFailedRuns(): List<WorkflowRun>? {
        var url = "https://api.github.com/repos/${repo}/actions/runs"
        val (request, response, result)  = url.httpGet().responseString()

        when(result) {
            is Result.Failure -> {
                println(result.getException())
            }
            is Result.Success -> {
                var runs = result.value
                var jsonConf = JsonConfiguration(ignoreUnknownKeys = true, prettyPrint = true)

                val json = Json(jsonConf)
                var o = json.parse(Runs.serializer(), runs)
                return o.workflow_runs.filter { it.conclusion != "success" }
            }

        }
        return null
    }

    fun getLastFailedLog(): String? {
        var failed = getFailedRuns()
        if (failed != null) {
            val logsUrl = failed.first().logs_url
            println(logsUrl)
            val numberOfBytes = 3276800
            val zip = File.createTempFile(numberOfBytes.toString(), null)

            var manager = FuelManager.instance

            val auth = "zarkone:${config.githubToken}"
            val encodedAuth = auth.encodeBase64ToString()
            val authHeader = "Basic $encodedAuth"

            logsUrl.httpDownload()
                .fileDestination{ _, _ -> zip }
                .authentication()
                .basic("zarkone", config.githubToken)
                .response()


            println(zip)
        }
        else {
            return null

        }

        return null
    }
}
package org.zarkone.faillogs

import com.github.kittinunf.fuel.httpGet

import com.github.kittinunf.result.Result
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration


@Serializable
data class Runs(val total_count: Int, val workflow_runs: List<WorkflowRun>)

@Serializable
data class WorkflowRun(val logs_url: String, val conclusion: String)

class GithubResponse {

}

class GithubRequest(config: Config, repo: String) {
    var repo = repo

    fun getFailureRuns(): List<WorkflowRun>? {
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
}
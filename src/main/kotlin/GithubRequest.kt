package org.zarkone.faillogs

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.FuelJson
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import org.json.JSONArray
import org.json.JSONObject

class GithubResponse {

}
fun arrayToList(array: JSONArray): MutableList<JSONObject> {
    var o = mutableListOf<JSONObject>()

    array.filterIsInstance<JSONObject>().forEach {
        o.add(it)
    }

    return o
}
class GithubRequest(config: Config, repo: String) {
    var repo = repo

    fun getFailureRuns(): List<Any>? {
        var url = "https://api.github.com/repos/${repo}/actions/runs"
        val (request, response, result)  = url.httpGet().responseJson()

        when(result) {
            is Result.Failure -> {
                println(result.getException())
            }
            is Result.Success -> {
                var runs = arrayToList(result.value.obj().get("workflow_runs") as JSONArray)

                return runs.filter{
                    it.get("conclusion") != "success"
                }
            }

        }
        return null
    }
}
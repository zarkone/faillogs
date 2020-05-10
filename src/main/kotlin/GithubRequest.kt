package org.zarkone.faillogs

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.httpDownload
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.util.encodeBase64ToString
import com.github.kittinunf.result.Result
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


@Serializable
data class Runs(val total_count: Int, val workflow_runs: List<WorkflowRun>)

@Serializable
data class WorkflowRun(val logs_url: String, val conclusion: String, val jobs_url: String)

@Serializable
data class JobStep(val name: String, val status: String, val conclusion: String, val number: Int)

@Serializable
data class Job(val steps: List<JobStep>, val name: String, val conclusion: String)

@Serializable
data class Jobs(val total_count: Int, val jobs: List<Job>)

class GithubRequest(private val config: Config, private val repo: String) {

    @OptIn(UnstableDefault::class)
    fun getLastFailedRun(): WorkflowRun? {
        val url = "https://api.github.com/repos/${repo}/actions/runs"
        val (_, _, result)  = url.httpGet().responseString()

        when(result) {
            is Result.Failure -> {
                println(result.getException())
            }
            is Result.Success -> {
                val runs = result.value
                val jsonConf = JsonConfiguration(ignoreUnknownKeys = true)

                val json = Json(jsonConf)
                val o = json.parse(Runs.serializer(), runs)
                return o.workflow_runs.filter { it.conclusion == "failure" }.first()
            }

        }
        return null
    }

    @OptIn(UnstableDefault::class)
    fun getFailedStepFilename(jobUrl: String): String? {
        val (_, _, result) = jobUrl.httpGet().responseString()
        when (result) {
            is Result.Failure -> println(result.getException())
            is Result.Success -> {
                val steps = result.value
                val jsonConf = JsonConfiguration(ignoreUnknownKeys = true, prettyPrint = true)
                val json = Json(jsonConf)
                val o = json.parse(Jobs.serializer(), steps)
                val lastFailedJob = o.jobs.filter { it.conclusion == "failure" }.first()
                val lastFailedStep = lastFailedJob.steps.filter{ it.conclusion == "failure"}.first()

                return "%s/%d_%s.txt".format(lastFailedJob.name, lastFailedStep.number, lastFailedStep.name)

            }
        }

        return null
    }

    data class LogFile(val name: String, val content: String)

    fun getLastFailedLog(): LogFile? {
        val failedRun = getLastFailedRun()
        if (failedRun != null) {
            val numberOfBytes = 3276800
            val stream = ByteArrayOutputStream(numberOfBytes)

            val (_, _, wrapped) = failedRun.logs_url.httpDownload()
                .streamDestination { _, _ -> Pair(stream, { ByteArrayInputStream(stream.toByteArray()) }) }
                .authentication()
                .basic("zarkone", config.githubToken)
                .response()

            val (data, _) = wrapped
            if (data != null) {
                val failedStepFilename = getFailedStepFilename(failedRun.jobs_url)
                val zipStream = ZipInputStream(ByteArrayInputStream(data))
                var ze: ZipEntry? = zipStream.nextEntry

                while (ze != null) {
                    if (ze.name == failedStepFilename) {
                        val byteArray = ByteArray(102400)
                        val size = zipStream.read(byteArray)

                        if (size != -1) {
                            return LogFile(name = ze.name, content = String(byteArray, 0, size))
                        }
                    }

                    ze = zipStream.nextEntry
                }
            }
        }
        else {
            return null
        }

        return null
    }
}
@file:JvmName("App")
package org.zarkone.faillogs

import kotlinx.serialization.json.Json

fun printHelp() {
    System.err.println("Usage:")
    System.err.println("faillogs username/repo")
}

fun login() {
    print("Github Username: ")
    val githubUsername = readLine() ?: ""
    val githubToken = String(System.console()?.readPassword("Github Token: ") ?: CharArray(0))
    val configMap = ConfigMap(githubUsername,githubToken)

    writeConfigFile(configMap)

}
fun main(args: Array<String>) {

    when {
        (args.isEmpty() || args[0] == "--help") -> {
            printHelp()
            return
        }

        args[0] == "login" -> {
            login()
            return
        }
    }


    val cmd = args[0]

    val repo = cmd
    val conf = Config()
    val githubRequest = GithubRequest(conf, repo)

    val name = githubRequest.getLastFailedLog()?.name
    val content = githubRequest.getLastFailedLog()?.content

    println("$name:")
    println(content)
}
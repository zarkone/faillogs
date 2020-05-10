@file:JvmName("App")
package org.zarkone.faillogs

fun main(args: Array<String>) {
    val repo = args[0]
    val conf = Config()
    val githubRequest = GithubRequest(conf, repo)

    val name = githubRequest.getLastFailedLog()?.name
    val content = githubRequest.getLastFailedLog()?.content

    println("$name:")
    println(content)
}
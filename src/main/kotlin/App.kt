@file:JvmName("App")
package org.zarkone.faillogs

fun printHelp() {
    System.err.println("Usage:")
    System.err.println("faillogs username/repo")
}
fun main(args: Array<String>) {

    when {
        (args.isEmpty() || args[0] == "--help") -> {
            printHelp()
            return
        }

        args[0] == "login" -> {
            // TODO: implement login
            throw UnsupportedOperationException("login: not implemented yet")
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
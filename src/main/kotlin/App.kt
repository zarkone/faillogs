@file:JvmName("App")
package org.zarkone.faillogs


fun main(args: Array<String>) {
    println("hello world.")
    var conf = Config()
    var githubRequest = GithubRequest(conf, "zarkone/literally.el")

    println(githubRequest.getFailureRuns())
}
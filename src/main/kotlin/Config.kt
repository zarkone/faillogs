package org.zarkone.faillogs

class Config {
    var githubToken: String
    var githubUser: String

    init {
        githubToken = System.getenv("GITHUB_TOKEN")
        githubUser =  System.getenv("GITHUB_USER")
    }
}
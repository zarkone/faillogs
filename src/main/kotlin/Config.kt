package org.zarkone.faillogs

class Config {
    val githubToken = System.getenv("GITHUB_TOKEN")
    val githubUser =  System.getenv("GITHUB_USER")
}
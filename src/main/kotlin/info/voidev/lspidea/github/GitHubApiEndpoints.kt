package info.voidev.lspidea.github

object GitHubApiEndpoints {

    private fun get(path: String) = "https://api.github.com/$path"

    fun releases(repoOwner: String, repo: String, perPage: Int) = get("repos/$repoOwner/$repo/releases?per_page=$perPage")
}

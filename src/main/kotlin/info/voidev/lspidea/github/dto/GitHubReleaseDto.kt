package info.voidev.lspidea.github.dto

data class GitHubReleaseDto(
    var tagName: String,
    var assets: List<GitHubAssetDto>,
)

data class GitHubAssetDto(
    var url: String,
    var name: String,
)

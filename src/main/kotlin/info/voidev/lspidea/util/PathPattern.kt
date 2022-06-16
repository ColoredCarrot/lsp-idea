package info.voidev.lspidea.util

import java.nio.file.FileSystem
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

data class PathPattern(val pattern: String) {

    @Transient
    private val cachedPathMatchers = ConcurrentHashMap<FileSystem, PathMatcher>()

    @Transient
    private val patternForPathMatcher =
        if (SYNTAX_SPECIFIER_REGEX.matcher(pattern).find()) pattern
        else DEFAULT_SYNTAX_SPECIFIER + pattern

    fun getMatcher(path: Path) = cachedPathMatchers.computeIfAbsent(path.fileSystem) { fs ->
        fs.getPathMatcher(patternForPathMatcher)
    }

    infix fun matches(path: Path) = getMatcher(path).matches(path)

    companion object {
        private val SYNTAX_SPECIFIER_REGEX = Pattern.compile("\\A[a-zA-Z][\\w-]*:")

        private const val DEFAULT_SYNTAX_SPECIFIER = "glob:"
    }
}

package info.voidev.lspidea.util

fun <T : Comparable<T>> MutableList<T>.reverseConsecutiveSequences() = reverseConsecutiveSequences(naturalOrder())

fun <T> MutableList<T>.reverseConsecutiveSequences(comparator: Comparator<T>) {
    var last: T? = null
    var begOfConsec = 0
    for ((i, e) in this.withIndex()) {
        if (last == null || comparator.compare(e, last) != 0) {
            // i is no longer part of the consecutive sequence
            if (last != null) {
                // Reverse the sequence
                subList(begOfConsec, i).reverse()
            }

            begOfConsec = i
        }
        last = e
    }
    // The very last sequence of elements might also be consecutive
    if (last != null) {
        subList(begOfConsec, size).reverse()
    }
}

inline fun <T> Sequence<T>.sortedBy(crossinline selector: (T) -> UInt) =
    sortedWith { a, b ->
        selector(a).compareTo(selector(b))
    }

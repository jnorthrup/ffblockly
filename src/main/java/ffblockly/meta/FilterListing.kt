package ffblockly.meta

import java.util.regex.Pattern

interface FilterListing {
    val timelineSupport: Boolean?
    val sliceThreading: Boolean?
    val commandSupport: Boolean?
    val filterName: String
    val shortDescription: String?
    val inputSignature: List<FilterSignature>?
    val returnSignature: List<FilterSignature>?
    val options: List<AvOption>?

    enum class Attr {
        timelineSupport, sliceThreading, commandSupport, filterName, inputSignature, returnSignature, shortDescription
    }

    enum class FilterSignature(private val c: Char) {
        Audio('A'), Video('V'), Bag('N'), SourceSink('|');

        companion object {
            fun valueOf(c: Char): FilterSignature? {
                for (filterSignature in values()) {
                    if (c == filterSignature.c) {
                        return filterSignature
                    }
                }
                return null
            }
        }

    }

    companion object {
        @JvmField
        val FILTERS_LIST_FORMAT = Pattern.compile("^\\s+(?<" + Attr.timelineSupport +
                ">[T.])(?<" + Attr.sliceThreading +
                ">[S.])+(?<" + Attr.commandSupport +
                ">[C.])\\s+(?<" + Attr.filterName +
                ">\\w+)\\s+(?<" + Attr.inputSignature +
                ">[AVN\\|]+)\\-\\>(?<" + Attr.returnSignature +
                ">[AVN\\|]+)\\s+(?<" + Attr.shortDescription +
                ">.*)$")
    }
}
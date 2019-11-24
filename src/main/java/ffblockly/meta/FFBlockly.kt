package ffblockly.meta

import java.util.*

/**
 */
interface FFBlockly {
    val model: Set<FFBlock?>?

    companion object {
        @JvmStatic
        fun colorForString(filterName: String): Int {
            return filterName.hashCode() % 360
        }

        @JvmField
        val typesMap: SortedMap<String, String> =
        sortedMapOf(
                ("string" to "String")
         ,  ("int" to "Number")
         ,  ("boolean" to "Boolean")
         ,  ("float" to "Number")
         ,  ("double" to "Number")
         ,  ("int64" to "Number")
         ,  ("int32" to "Number")
         ,  ("short" to "Number")
         ,  ("byte" to "Number")
        )
    }

}
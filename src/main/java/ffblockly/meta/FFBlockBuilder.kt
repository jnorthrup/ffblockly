package ffblockly.meta

import ffblockly.meta.FFBlockly.Companion.colorForString
import ffblockly.meta.FFBlocklyInputValueBuilder.*
import ffblockly.meta.FilterListing.FilterSignature
import java.text.MessageFormat
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors.*
import java.util.stream.Stream

interface FFBlockBuilder {
    companion object {   val inputDummy = object : FFBlocklyFieldValue {
        override val value: String?
            get() = null

        override val type: FFBFieldType
            get() = FFBFieldType.input_dummy

        override val text: String?
            get() = null

        override val min: String?
            get() = null

        override val max: String?
            get() = null

        override val checked: Boolean?
            get() = null

        override val options: List<List<String>>?
            get() = null

        override val name: String?
            get() = null

        override val tooltip: String?
            get() = null

        override val check: String?
            get() = null
    }
        @JvmStatic
        fun createFFBlock(filterListing: FilterListing): FFBlock? {
            return Scan.MYFACTORY.block(object : FFBlock {
                var loaded = false
                private var options: List<AvOption>? = null
                override val isInputsInline: Boolean
                    get() = true

                override val previousStatement: FilterSignature?
                    get() = if (filterListing.inputSignature!!.iterator().hasNext()) filterListing.inputSignature!![0] else null

                override val nextStatement: FilterSignature?
                    get() = if (filterListing.returnSignature!!.iterator().hasNext()) filterListing.returnSignature!![0] else null

                override val colour: Int
                    get() {
                        val inputSignature = filterListing.inputSignature
                        return if (inputSignature!!.isEmpty()) 0 else colorForString(inputSignature.iterator().next().toString())
                    }

                override val tooltip: String?
                    get() = filterListing.shortDescription

                override val helpUrl: String
                    get() = MessageFormat.format("https://ffmpeg.org/ffmpeg-filters.html#{0}", filterListing.filterName)

                override val type: String
                    get() = filterListing.filterName

                override val message0: String
                    get() {
                        val msg0 = StringBuilder(type)
                        val args0 = args0
                        var first = true
                        if (null != args0) for (i in args0.indices) {
                            val theArg = args0[i]
                            if (null != theArg.type) {
                                val type = theArg.type
                                if (type!!.ordinal < FFBFieldType.input_statement.ordinal) {
                                    msg0.append(if (first) "=" else ":").append(theArg.name).append("=%").append(i + 1)
                                    first = false
                                }
                            }
                        }
                        return msg0.toString()
                    }

                private val autoBean = Scan.MYFACTORY.fil(inputDummy)

                override val args0: List<FFBlocklyFieldValue>?
                    get() {
                        val c = intArrayOf(1)
                        val filterSignatureFFBlocklyFieldValueFunction = Function { filterSignature: FilterSignature -> getFfBlocklyFieldValue( c, filterSignature) }
                        val inputSignature = filterListing.inputSignature
                        val stream1 = inputSignature!! .drop(1)?.map  {filterSignature ->  filterSignatureFFBlocklyFieldValueFunction.apply(filterSignature  ) }
                        if (!loaded) {
                            loaded = true
                            options = filterListing.options
                        }
                         c[0] = 1

                      return   options?.  map(FFBlocklyInputValueBuilder.Companion::createFFBlocklyInputValue)?.let { ffBlocklyFieldValueStream ->

                            val fil = autoBean
                            val of = fil.`as`()
                            val concat1 = ffBlocklyFieldValueStream + of
                            val stream = filterListing.returnSignature!!
                            val map = stream.drop(1).map( filterSignatureFFBlocklyFieldValueFunction::apply)
                            val concat =  (concat1+ map)
                          (stream1 + concat).filterNotNull()
                        }
                    }
            }).`as`()
        }

        fun getFfBlocklyFieldValue(c: IntArray, filterSignature: FilterSignature): FFBlocklyFieldValue? {
            c[0]++
            return Scan.MYFACTORY.fil(object : FFBlocklyFieldValue {
                override val value: String?
                    get() = null

                override val type: FFBFieldType
                    get() = FFBFieldType.input_statement

                override val text: String?
                    get() = null

                override val min: String?
                    get() = null

                override val max: String?
                    get() = null

                override val checked: Boolean?
                    get() = null

                override val options: List<List<String>>?
                    get() = null

                override val name: String
                    get() = "Input" + filterSignature.name + c[0]++

                override val tooltip: String?
                    get() = null

                override val check: String?
                    get() = null
            }).`as`()
        }

        val ORDINAL = FFBFieldType.input_statement.ordinal
    }
}
package ffblockly.meta

import ffblockly.meta.FilterListing.FilterSignature
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.StringReader
import java.util.*
import java.util.regex.Matcher

interface FilterListingBuilder {
    companion object {
        fun getFilterSignatures(group: String?) = StringReader(group).let { stringReader ->
            generateSequence {
                stringReader.read().takeUnless { it == -1 }
            }.map { FilterSignature.valueOf((it and 0xffff).toChar())!! }.toList()
        }.takeUnless { it.isEmpty() }

        @JvmStatic
        fun createFilterListing(filterMatcher1: Matcher): FilterListing? {
            return Scan.MYFACTORY.listing(object : FilterListing {
                private var opts: List<AvOption>? = null
                override val timelineSupport: Boolean?
                    get() = if (java.lang.Boolean.valueOf("." != filterMatcher1.group(FilterListing.Attr.timelineSupport.ordinal + 1))) true else null

                override val sliceThreading: Boolean?
                    get() = if (java.lang.Boolean.valueOf("." != filterMatcher1.group(FilterListing.Attr.sliceThreading.ordinal + 1))) true else null

                override val commandSupport: Boolean?
                    get() = if (java.lang.Boolean.valueOf("." != filterMatcher1.group(FilterListing.Attr.commandSupport.ordinal + 1))) true else null

                override val filterName: String
                    get() = filterMatcher1.group(FilterListing.Attr.filterName.ordinal + 1)

                override val shortDescription: String?
                    get() = filterMatcher1.group(FilterListing.Attr.shortDescription.ordinal + 1)

                override val inputSignature: List<FilterSignature>?
                    get() {
                        val group = filterMatcher1.group(FilterListing.Attr.inputSignature.ordinal + 1)
                        return getFilterSignatures(group)
                    }

                override val returnSignature: List<FilterSignature>?
                    get() {
                        val group = filterMatcher1.group(FilterListing.Attr.returnSignature.ordinal + 1)
                        return getFilterSignatures(group)
                    }

                override val options: List<AvOption>?
                    get() {
                        if (null == opts) {
                            var process: Process? = null
                            try {
                                process = ProcessBuilder("ffmpeg", "-h", "filter=$filterName").start()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            try {
                                BufferedReader(InputStreamReader(process!!.inputStream)).use { bufferedReader ->
                                    var buf: String?
                                    var prev: AvOption? = null
                                    while (null != bufferedReader.readLine().also { buf = it }) {
                                        val optMatcher = AvOption.AV_OPT_PAT.matcher(buf)
                                        if (optMatcher.matches()) {
                                            val avOption = AvOptionBuilder().setOptMatcher(optMatcher).createAvOptionImpl()
                                            val type = avOption.type
                                            if (type != null && !type.isEmpty()) {
                                                prev = avOption
                                                opts = (opts ?: emptyList()) + (avOption)
                                            } else {
                                                if (prev == null) prev = avOption else {
                                                    prev.addChild(avOption)
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            } finally {
                                process!!.destroy()
                            }
                        }
                        return opts
                    }
            }).`as`()
        }
    }
}
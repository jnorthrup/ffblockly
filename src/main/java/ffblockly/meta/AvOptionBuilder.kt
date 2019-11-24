package ffblockly.meta

import ffblockly.meta.AvOption.AvOptionField
import ffblockly.meta.AvOption.AvTarget
import java.util.*
import java.util.regex.Matcher

class AvOptionBuilder {
    private var optMatcher: Matcher? = null
    fun setOptMatcher(optMatcher: Matcher?): AvOptionBuilder {
        this.optMatcher = optMatcher
        return this
    }

    fun createAvOptionImpl(): AvOption {
        val optMatcher1 = optMatcher
        return Scan.MYFACTORY.options(object : AvOption {
            override val optionName: String? by lazy { optMatcher1!!.group(AvOptionField.optionName.name) }

            override val type: String? by lazy { optMatcher1!!.group(AvOptionField.type.name) }

            override val targets: List<AvTarget>? by lazy {
                if (type == null) null
                else {
                    AvTarget.values().filter { '.' != optMatcher1!!.group(AvOptionField.targets.name)[it.ordinal] }
                }
            }


            override val description: String? by lazy { optMatcher1!!.group(AvOptionField.description.name) }

            override val range1: String? by lazy { optMatcher1!!.group(AvOptionField.range1.name) }

            override val range2: String? by lazy { optMatcher1!!.group(AvOptionField.range2.name) }

            override val def: String? by lazy { optMatcher1!!.group(AvOptionField.def.name) }

            override val encoding: Boolean? by lazy { if (null != type) if (targets!!.contains(AvTarget.Encoding)) true else null else null }

            override val decoding: Boolean? by lazy { if (null != type) if (targets!!.contains(AvTarget.Decoding)) true else null else null }

            override val filtering: Boolean? by lazy { if (null != type) if (targets!!.contains(AvTarget.Filtering)) true else null else null }

            override val video: Boolean? by lazy { if (null != type) if (targets!!.contains(AvTarget.Video)) true else null else null }

            override val audio: Boolean? by lazy { if (null != type) if (targets!!.contains(AvTarget.Audio)) true else null else null }

            override val subtitle: Boolean? by lazy { if (null != type) if (targets!!.contains(AvTarget.Subtitle)) true else null else null }

            override val export: Boolean? by lazy { if (null != type) if (targets!!.contains(AvTarget.Export)) true else null else null }

            override val readonly: Boolean? by lazy { if (null != type) if (targets!!.contains(AvTarget.Readonly)) true else null else null }
            override val children: List<AvOption>?
                get() = ch

            protected var ch : List<AvOption>?= null

            override fun addChild(o: AvOption) { ch = ( this.ch ?: emptyList ()) + o }
        }).`as`()
    }
}
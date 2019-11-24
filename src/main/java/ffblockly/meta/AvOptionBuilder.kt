package ffblockly.meta

import ffblockly.meta.AvOption.AvOptionField
import ffblockly.meta.AvOption.AvTarget
import ffblockly.meta.AvOption.AvTarget.*
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
                    values().filter { '.' != optMatcher1!!.group(AvOptionField.targets.name)[it.ordinal] }
                }
            }
            override val description: String? = optMatcher1!!.group(AvOptionField.description.name)

            override val range1: String? = optMatcher1!!.group(AvOptionField.range1.name)

            override val range2: String? = optMatcher1!!.group(AvOptionField.range2.name)

            override val def: String? = optMatcher1!!.group(AvOptionField.def.name)

            override val encoding: Boolean? = type?.let { Encoding in targets!! }?.takeUnless { it == false }

            override val decoding: Boolean? =  type?.let { Decoding in targets!! }?.takeUnless { it == false }

            override val filtering: Boolean? =  type?.let { Filtering in targets!! }?.takeUnless { it == false }

            override val video: Boolean? =  type?.let { Video in targets!! }?.takeUnless { it == false }

            override val audio: Boolean? =  type?.let { Audio in targets!! }?.takeUnless { it == false }

            override val subtitle: Boolean? =  type?.let { Subtitle in targets!! }?.takeUnless { it == false }

            override val export: Boolean? =  type?.let { Export in targets!! }?.takeUnless { it == false }

            override val readonly: Boolean? =  type?.let { Readonly in targets!! }?.takeUnless { it == false }
            override val children: List<AvOption>?
                get() = ch

            protected var ch: List<AvOption>? = null

            override fun addChild(o: AvOption) {
                ch = (this.ch ?: emptyList()) + o
            }
        }).`as`()
    }
}
package ffblockly.meta

import java.util.regex.Pattern

/**
 * Created by jim on 9/12/2016.
 */
interface AvOption {
    val optionName: String?
    val type: String?
    val targets: List<AvTarget>?
    val description: String?
    val range1: String?
    val range2: String?
    val def: String?
    val children: List<AvOption>?
    val encoding: Boolean?
    val decoding: Boolean?
    val filtering: Boolean?
    val video: Boolean?
    val audio: Boolean?
    val subtitle: Boolean?
    val export: Boolean?
    val readonly: Boolean?
    fun addChild(o: AvOption)
    enum class AvTarget {
        Encoding, Decoding, Filtering, Video, Audio, Subtitle, Export, Readonly
    }

    enum class AvOptionField {
        optionName, type, targets, description, range1, range2, def
    }

    companion object {
        val AV_OPT_PAT = Pattern.compile("^\\s+(?<" + AvOptionField.optionName + ">[\\w+-]+)(\\s+\\<(?<" + AvOptionField.type + ">\\w+)\\>)?\\s+(?<" + AvOptionField.targets + ">[\\w.]{8})(\\s+(?<" + AvOptionField.description + ">[^(]+)(\\s+\\(from\\s+(?<" + AvOptionField.range1 + ">.*)\\s+to\\s+(?<" + AvOptionField.range2 + ">[^)]+)\\))?(\\s+\\(default\\s+(?<" + AvOptionField.def + ">[^)]+)\\))?)?$")
    }
}
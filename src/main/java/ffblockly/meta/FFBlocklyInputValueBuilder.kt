package ffblockly.meta

import java.util.*

interface FFBlocklyInputValueBuilder {
    enum class FFBFieldType(val scanfor: String) {
        field_input("String"), field_number("Number"), field_checkbox("Boolean"), field_colour("color"), field_dropdown("sdfalsjkdhfjahsf"), input_statement("asdasdasdasds"), input_value("sgddfg"), input_dummy("asdasdasd");

    }

    companion object {
        fun createFFBlocklyInputValue(avOption: AvOption): FFBlocklyFieldValue? {
            return Scan.MYFACTORY.fil(object : FFBlocklyFieldValue {
                override val value: String?
                    get() = if (FFBFieldType.field_number == type) avOption.def else null

                override val text: String?
                    get() {
                        val def = avOption.def
                        return if (FFBFieldType.field_input == type) def ?: "" else null
                    }

                override val min: String?
                    get() = avOption.range1

                override val max: String?
                    get() = avOption.range2

                override val checked: Boolean?
                    get() = if (FFBFieldType.field_checkbox == type && avOption.def == "true") true else null

                override val type: FFBFieldType
                    get() {
                        val children = avOption.children
                        val b = null !=  children && ! children.isEmpty()
                        if ( b) return FFBFieldType.field_dropdown
                        for (FFBFFBFieldType in FFBFieldType.values()) {
                            val type = FFBlockly.typesMap.getOrDefault(avOption.type, avOption.type)
                            if ( FFBFFBFieldType.scanfor ==  type) return  FFBFFBFieldType
                        }
                        return FFBFieldType.field_input
                    }

                override val options: List<List<String ?>>?
                    get() =   avOption.children?.let {

                        listOf(listOf("", ""))+
                                avOption.children!!
                                        .map { Arrays.asList(it.optionName + "\t|\t" + if (null == it.description) "tbd" else it.description, it.optionName) }

                    }

                override val name: String?
                    get() = avOption.optionName

                override val tooltip: String?
                    get() = avOption.description

                override val check: String
                    get() = "String"
            }).`as`()
        }
    }
}
package ffblockly.meta

/**
 * Created by Jnorthrup on 9/13/2016.
 */
interface FFBlocklyFieldValue {
    val value: String?
    val type: FFBlocklyInputValueBuilder.FFBFieldType?
    val text: String?
    val min: String?
    val max: String?
    val checked: Boolean?
    val options: List<List<String?>?>?
    val name: String?
    val tooltip: String?
    val check: String?
}
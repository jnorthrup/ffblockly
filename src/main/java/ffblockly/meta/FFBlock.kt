package ffblockly.meta

/**
 * Created by Jnorthrup on 9/13/2016.
 */
interface FFBlock {
    val isInputsInline: Boolean?
    val previousStatement: FilterListing.FilterSignature?
    val nextStatement: FilterListing.FilterSignature?
    val colour: Int
    val tooltip: String?
    val helpUrl: String?
    val type: String?
    val message0: String?
    val args0: List<FFBlocklyFieldValue?>?
}
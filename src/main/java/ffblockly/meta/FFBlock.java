package ffblockly.meta;


import java.util.List;

/**
 * Created by Jnorthrup on 9/13/2016.
 */
public interface FFBlock {
    Boolean isInputsInline();

    ffblockly.meta.FilterListing.FilterSignature getPreviousStatement();

    ffblockly.meta.FilterListing.FilterSignature getNextStatement();

    int getColour();

    String getTooltip();


    String getHelpUrl();

    String getType();

    String getMessage0();

    List<ffblockly.meta.FFBlocklyFieldValue> getArgs0();

}

package ffblockly.meta;

import java.util.List;

/**
 * Created by Jnorthrup on 9/13/2016.
 */
public interface FFBlocklyFieldValue {
    String getValue();

    FFBlocklyInputValueBuilder.FFBFieldType getType();


    String getText();

    String getMin();

    String getMax();

    Boolean getChecked();

    List<List<String>>getOptions();

    String getName();

    String getTooltip();
    String getCheck();

}

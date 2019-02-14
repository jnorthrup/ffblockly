package ffblockly.meta;

import java.util.List;
import java.util.regex.Pattern;

import static ffblockly.meta.FilterListing.Attr.*;

public interface FilterListing {
    Pattern FILTERS_LIST_FORMAT = Pattern.compile("^\\s+(?<" +
            timelineSupport +
            ">[T.])(?<" +
            sliceThreading +
            ">[S.])+(?<" +
            commandSupport +
            ">[C.])\\s+(?<" +
            filterName +
            ">\\w+)\\s+(?<" +
            inputSignature +
            ">[AVN\\|]+)\\-\\>(?<" +
            returnSignature +
            ">[AVN\\|]+)\\s+(?<" +
            shortDescription +
            ">.*)$");

    Boolean getTimelineSupport();


    Boolean getSliceThreading();


    Boolean getCommandSupport();


    String getFilterName();


    String getShortDescription();


    List<FilterSignature> getInputSignature();


    List<FilterSignature> getReturnSignature();

    List<AvOption> getOptions();

    enum Attr {
        timelineSupport, sliceThreading, commandSupport, filterName, inputSignature, returnSignature, shortDescription
    }

    enum FilterSignature {
        Audio('A'), Video('V'), Bag('N'), SourceSink('|');
        private char c;
        FilterSignature(char c) {
            this.c = c;
        }
        static FilterSignature valueOf(char c) {
            for (FilterSignature filterSignature : values()) {
                if (c == filterSignature.c) {
                    return filterSignature;
                }
            }
            return null;
        }
    }
}
package ffblockly.meta;

import java.util.List;
import java.util.regex.Pattern;

import static ffblockly.meta.AvOption.AvOptionField.*;

/**
 * Created by jim on 9/12/2016.
 */
public interface AvOption {

    Pattern AV_OPT_PAT = Pattern.compile("^\\s+(?<" +
            name +
            ">[\\w+-]+)(\\s+\\<(?<" +
            type +
            ">\\w+)\\>)?\\s+(?<" +
            targets +
            ">[\\w.]{8})(\\s+(?<" +
            description +
            ">[^(]+)(\\s+\\(from\\s+(?<" +
            range1 +
            ">.*)\\s+to\\s+(?<" +
            range2 +
            ">[^)]+)\\))?(\\s+\\(default\\s+(?<" +
            def +
            ">[^)]+)\\))?)?$");

    String getName();

    String getType();

    List<AvTarget> getTargets();

    String getDescription();

    String getRange1();

    String getRange2();

    String getDef();

    List<AvOption> getChildren();

    Boolean getEncoding();

    Boolean getDecoding();

    Boolean getFiltering();

    Boolean getVideo();

    Boolean getAudio();

    Boolean getSubtitle();

    Boolean getExport();

    Boolean getReadonly();

    void addChild(AvOption o);

    enum AvTarget {
        Encoding, Decoding, Filtering, Video, Audio, Subtitle, Export, Readonly
    }

    enum AvOptionField {
        name, type, targets, description, range1, range2, def
    }
}

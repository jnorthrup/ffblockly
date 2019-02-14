package ffblockly.meta;


import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.lang.Math.abs;

/**
 */
public interface FFBlockly {
    Map<String, String> typesMap = new TreeMap<String, String>() {{
        put("string", "String");
        put("int", "Number");
        put("boolean", "Boolean");
        put("float", "Number");
        put("double", "Number");
        put("int64", "Number");
        put("int32", "Number");
        put("short", "Number");
        put("byte", "Number");
    }};

    static int colorForString(String filterName) {
        return  filterName.hashCode()% 360 ;
    }


    Set<FFBlock> getModel();
}

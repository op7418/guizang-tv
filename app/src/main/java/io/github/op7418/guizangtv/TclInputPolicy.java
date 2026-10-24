package io.github.op7418.guizangtv;

import java.util.Collection;
import java.util.LinkedHashMap;

/** Mappings observed on Smart TV Pro; never reuse them on another device profile. */
final class TclInputPolicy {
    private TclInputPolicy() { }
    static boolean supported(String model, String manufacturer, String brand) {
        return "Smart TV Pro".equals(model)
                && ("TCL".equalsIgnoreCase(manufacturer) || "TCL".equalsIgnoreCase(brand));
    }
    static String hdmi(int port) {
        return port >= 1 && port <= 4 ? "com.tcl.tvinput/.TvPassThroughService/HW" + (14 + port) : null;
    }
    static LinkedHashMap<String,String> registered(Collection<String> ids) {
        LinkedHashMap<String,String> result = new LinkedHashMap<>();
        for (int port=1;port<=4;port++) if (ids.contains(hdmi(port))) result.put("HDMI " + port, hdmi(port));
        String tv="com.tcl.tvinput/.TunerInputService/HW2", av="com.tcl.tvinput/.TvPassThroughService/HW3";
        if (ids.contains(tv)) result.put("电视节目 (TV)", tv);
        if (ids.contains(av)) result.put("复合视频 (AV)", av);
        return result;
    }
}

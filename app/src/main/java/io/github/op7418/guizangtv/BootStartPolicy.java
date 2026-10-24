package io.github.op7418.guizangtv;

final class BootStartPolicy {
    private BootStartPolicy() { }
    static boolean supported(String model, String manufacturer, String brand) {
        return "MiTV-MTSU0".equals(model) || TclInputPolicy.supported(model, manufacturer, brand);
    }
    static boolean acceptsAction(String action, boolean gate) {
        return (gate ? "android.intent.action.MAIN" : "android.intent.action.BOOT_COMPLETED")
                .equals(action);
    }
    static String skipReason(boolean supported, boolean enabled, int boot, int attempted, long elapsed) {
        if (!supported) return "当前设备尚未适配开机自动进入";
        if (!enabled) return "开机自动进入已关闭";
        if (boot < 0) return "无法确认本次启动，已跳过";
        if (boot == attempted) return "本次开机已尝试，未重复打开";
        if (elapsed < 0 || elapsed > 10 * 60_000L) return "启动通知过晚，已跳过以免打断使用";
        return null;
    }
}

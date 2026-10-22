package io.github.op7418.guizangtv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.FileReader;

/** Opt-in, once-per-full-boot launch. Never handles screen-on, HDMI or Home keys. */
public final class BootStart {
    private static final String PREFS = "boot-start";
    private static final String EXTRA = "guizang.boot-count";
    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
    static boolean enabled(Context c) { return prefs(c).getBoolean("enabled", false); }
    private static int bootCount(Context c) {
        int boot = Settings.Global.getInt(c.getContentResolver(), Settings.Global.BOOT_COUNT, -1);
        if (boot >= 0) return boot;
        // Android 6 has no boot_count setting. /proc/stat btime is stable for this boot
        // and changes on every reboot. elapsedRealtime/1000 collides when the stock
        // launcher calls the gate at the same ~27s mark on consecutive boots.
        long btime = bootTimeSeconds();
        if (btime > 0) return (int)(btime & 0x7fffffff);
        // Last resort: a changing token; the app still opens, only the status may not confirm.
        return (int)(System.nanoTime() & 0x7fffffff);
    }
    private static long bootTimeSeconds() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("btime ")) return Long.parseLong(line.substring(6).trim());
            }
        } catch (Exception ignored) { }
        return -1;
    }
    static void enter(Activity a) {
        int boot = bootCount(a);
        String reason = BootStartPolicy.skipReason(XiaomiControls.enabled(), enabled(a), boot, prefs(a).getInt("attempted", -1),
                SystemClock.elapsedRealtime());
        if (reason != null) { Log.i("GuiZangBoot", reason); return; }
        if (!prefs(a).edit().putInt("attempted", boot).putString("status", "系统已调用启动入口，等待界面确认").commit()) return;
        try {
            a.startActivity(new Intent(a, FrameHomeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(EXTRA, boot));
        } catch (RuntimeException error) {
            status(a, "启动失败：" + error.getClass().getSimpleName());
            Log.w("GuiZangBoot", "Launch rejected", error);
        }
    }
    private static boolean configured(Context c) {
        return c.getPackageName().equals(Settings.System.getString(c.getContentResolver(), "start_3rd_app"));
    }
    private static void status(Context c, String value) {
        prefs(c).edit().putString("status", value).apply();
        Log.i("GuiZangBoot", value);
    }
    static void confirm(Activity activity) {
        Intent intent = activity.getIntent();
        int boot = intent.getIntExtra(EXTRA, -1);
        if (boot < 0) return;
        intent.removeExtra(EXTRA);
        if (boot == bootCount(activity) && prefs(activity).getInt("attempted", -1) == boot && enabled(activity)) {
            status(activity, "已在本次开机后进入桌面");
        }
    }
    static void settings(Activity a, Runnable systemSettings) {
        new AlertDialog.Builder(a).setTitle("设置")
                .setItems(new String[]{"电视系统设置", "开机自动进入：" + (enabled(a) ? "已开启" : "已关闭")},
                        (d, i) -> { if (i == 0) systemSettings.run(); else show(a); })
                .setNegativeButton("返回", null).show();
    }
    static void show(Activity a) {
        if (!XiaomiControls.enabled()) {
            new AlertDialog.Builder(a).setTitle("开机自动进入（实验）")
                    .setMessage("目前仅适配 MiTV-MTSU0。其他机型请先使用普通应用入口。")
                    .setNegativeButton("返回", null).show();
            return;
        }
        String state = prefs(a).getString("status", "尚未进行完整重启测试");
        new AlertDialog.Builder(a).setTitle("开机自动进入（实验）")
                .setMessage("仅在完整开机后尝试一次，不接管 Home 键，同次开机的后续调用直接跳过。\n\n"
                        + "由已适配的小米系统调用启动入口；不需要悬浮窗权限。\n\n"
                        + "系统入口：" + (configured(a) ? "已配置" : "尚未配置，需电脑设置") + "\n上次状态：" + state)
                .setPositiveButton(enabled(a) ? "关闭自动进入" : "开启自动进入", (d, which) -> {
                    boolean value = !enabled(a);
                    prefs(a).edit().putBoolean("enabled", value).apply();
                    Toast.makeText(a, value ? (configured(a) ? "已开启，下次完整重启时尝试进入" : "开关已开启，还需按项目文档配置系统入口") : "已关闭开机自动进入", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("返回", null).show();
    }
    static void dump(Context c, java.io.PrintWriter out) {
        out.println("BootStart: enabled=" + enabled(c) + " configured=" + configured(c)
                + " boot=" + bootCount(c) + " attempted=" + prefs(c).getInt("attempted", -1)
                + " status=" + prefs(c).getString("status", "none"));
    }
}

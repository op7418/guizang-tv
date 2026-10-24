package io.github.op7418.guizangtv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/** Opt-in, once-per-full-boot launch. Never handles screen-on, HDMI or Home keys. */
public final class BootStart {
    private static final String PREFS = "boot-start";
    private static final String EXTRA = "guizang.boot-count";
    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
    static boolean enabled(Context c) { return prefs(c).getBoolean("enabled", false); }
    static boolean supported() {
        return BootStartPolicy.supported(Build.MODEL, Build.MANUFACTURER, Build.BRAND);
    }
    private static int bootCount(Context c) {
        return Settings.Global.getInt(c.getContentResolver(), Settings.Global.BOOT_COUNT, -1);
    }
    static synchronized void enter(Context a, Intent source, boolean gate) {
        if (source == null || !BootStartPolicy.acceptsAction(source.getAction(), gate)) {
            Log.i("GuiZangBoot", "Ignored unsupported startup action");
            return;
        }
        int boot = bootCount(a);
        String reason = BootStartPolicy.skipReason(supported(), enabled(a), boot, prefs(a).getInt("attempted", -1),
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
        if (intent == null) return;
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
    static AlertDialog show(Activity a) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(a).setTitle("开机自动进入（实验）")
                .setMessage("完整开机后尝试进入归藏电视桌面，每次开机最多一次；待机唤醒不会触发。\n\n当前状态："
                        + (enabled(a) ? "已开启" : "已关闭")
                        + "\n设备适配：" + (supported() ? "支持尝试，仍受系统限制" : "尚未适配")
                        + (XiaomiControls.enabled() ? "\n系统入口：" + (configured(a) ? "已配置" : "尚未配置，需电脑设置") : "")
                        + "\n最近结果：" + prefs(a).getString("status", "尚未尝试")
                        + "\n\n系统可能阻止后台打开界面。开启开关不会更改默认桌面或电视系统设置。")
                .setNegativeButton("返回", null);
        if (supported() || enabled(a)) {
            dialog.setPositiveButton(enabled(a) ? "关闭自动进入" : "开启自动进入", (d, which) -> {
                    boolean value = !enabled(a);
                    prefs(a).edit().putBoolean("enabled", value).apply();
                    Toast.makeText(a, value ? (XiaomiControls.enabled()&&!configured(a) ? "开关已开启，还需按项目文档配置系统入口" : "已开启，下次完整重启时尝试进入") : "已关闭开机自动进入", Toast.LENGTH_LONG).show();
                });
        }
        return dialog.show();
    }
    static void dump(Context c, java.io.PrintWriter out) {
        out.println("BootStart: enabled=" + enabled(c) + " supported=" + supported() + " configured=" + configured(c)
                + " boot=" + bootCount(c) + " attempted=" + prefs(c).getInt("attempted", -1)
                + " status=" + prefs(c).getString("status", "none"));
    }
}

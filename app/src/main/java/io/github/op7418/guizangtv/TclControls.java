package io.github.op7418.guizangtv;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/** TCL routes are opt-in by device profile and checked against the installed firmware. */
final class TclControls {
    private TclControls() { }
    static boolean enabled() { return TclInputPolicy.supported(Build.MODEL, Build.MANUFACTURER, Build.BRAND); }
    private static Intent explicit(String pkg, String activity) {
        return new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(pkg,activity));
    }
    private static boolean start(Context context, List<Intent> candidates) {
        return LaunchFallback.start(candidates, intent -> {
            ComponentName component=intent.getComponent();
            if (component!=null) {
                try {
                    ActivityInfo info=context.getPackageManager().getActivityInfo(component,0);
                    if (!info.enabled || !info.exported || (info.permission!=null
                            && context.checkSelfPermission(info.permission)!=PackageManager.PERMISSION_GRANTED))
                        throw new SecurityException("Entry is not accessible");
                } catch (PackageManager.NameNotFoundException absent) {
                    throw new android.content.ActivityNotFoundException("Entry is not installed");
                }
            }
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }, (intent,error) -> Log.w("GuiZangTcl", "Entry rejected: " + error.getClass().getSimpleName()));
    }
    static LinkedHashMap<String,String> inputs(Context context) {
        if (!enabled()) return new LinkedHashMap<>();
        try {
            TvInputManager manager=(TvInputManager)context.getSystemService(Context.TV_INPUT_SERVICE);
            List<String> ids=new ArrayList<>();
            if (manager!=null) for (TvInputInfo info:manager.getTvInputList()) ids.add(info.getId());
            return TclInputPolicy.registered(ids);
        } catch (RuntimeException unavailable) {
            Log.w("GuiZangTcl", "Input enumeration unavailable");
            return new LinkedHashMap<>();
        }
    }
    static boolean openInput(Context context,String id) {
        if (id==null || !inputs(context).containsValue(id)) return false;
        return start(context,Arrays.asList(
                explicit("com.tcl.tv","com.tcl.player.TVActivity").putExtra("input_id",id),
                explicit("com.tcl.tv","com.tcl.tv.TVActivity").putExtra("input_id",id)));
    }
    static boolean openHdmi(Context context,int port) { return openInput(context,TclInputPolicy.hdmi(port)); }
    static boolean openSettings(Context context) {
        return enabled() && start(context,Arrays.asList(
                explicit("com.tcl.settings","com.tcl.settings.ui.MainActivity"),
                new Intent(Settings.ACTION_SETTINGS)));
    }
    static boolean openStockHome(Context context) {
        return enabled() && start(context,Arrays.asList(
                explicit("com.tcl.cyberui","com.tcl.cyberui.MainActivity")));
    }
}

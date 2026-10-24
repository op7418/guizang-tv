package io.github.op7418.guizangtv;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import org.json.JSONArray;
import java.util.*;

/** Stable app/source identities; removing an entry never uninstalls an app. */
final class HomeItems {
    static final String[] DEFAULTS = {"hdmi:2", "hdmi:1", "com.xiaodianshi.tv.yst",
            "com.xiaomi.mitv.smartshare", "com.mitv.gallery", "com.mitv.tvhome"};
    final Context context;
    final LinkedHashMap<String,String> names = new LinkedHashMap<>();
    final List<String> order = new ArrayList<>();
    HomeItems(Context c) {
        context = c;
        String[] labels = {"PlayStation 5", "Apple TV", "哔哩哔哩", "小米投屏", "相册", "小米原桌面"};
        for (int i=0;i<DEFAULTS.length;i++) names.put(DEFAULTS[i], labels[i]);
        String[] defaults=DEFAULTS;
        if(TclControls.enabled()){
            names.clear();
            for(int port=1;port<=4;port++)names.put("hdmi:"+port,"HDMI "+port);
            names.put("com.tcl.cyberui","TCL 原桌面");
            defaults=new String[]{"hdmi:1","hdmi:2","com.tcl.cyberui"};
        }
        discover();
        try {
            JSONArray data = new JSONArray(c.getSharedPreferences("home-items",0).getString("order","[]"));
            for (int i=0;i<data.length() && order.size()<9;i++) {
                String id = data.getString(i);
                if (validId(id) && !order.contains(id)) order.add(id);
            }
        } catch (Exception ignored) { order.clear(); }
        if (order.isEmpty()) order.addAll(Arrays.asList(defaults));
    }
    void discover() {
        for (String category : new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER}) {
            for (ResolveInfo r : context.getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(category), 0)) {
                String id = r.activityInfo.packageName;
                if (!id.equals(context.getPackageName()) && r.activityInfo.enabled && r.activityInfo.exported)
                    names.putIfAbsent(id, XiaomiControls.label(id, r.loadLabel(context.getPackageManager()).toString()));
            }
        }
    }
    String name(String id) {
        String saved=context.getSharedPreferences("home-items",0).getString("name:"+id,null);
        return saved!=null&&!saved.isEmpty()?saved:names.getOrDefault(id,id);
    }
    static boolean validId(String id) { return id!=null&&(id.matches("hdmi:[1-4]") || id.matches("[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z0-9_]+)+")); }
    boolean save(List<String> next) {
        if (next.isEmpty() || next.size()>9 || new HashSet<>(next).size()!=next.size()) return false;
        for(String id : next) if(!validId(id)) return false;
        android.content.SharedPreferences.Editor e = context.getSharedPreferences("home-items",0).edit()
                .putString("order",new JSONArray(next).toString());
        for(String id:next) e.putString("name:"+id,name(id));
        if(!e.commit()) return false;
        order.clear(); order.addAll(next); return true;
    }
}

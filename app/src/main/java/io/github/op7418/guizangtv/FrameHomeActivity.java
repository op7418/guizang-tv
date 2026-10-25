package io.github.op7418.guizangtv;

import android.app.*;
import android.content.*;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

/** Native TV home and one grouped Settings entry. */
public final class FrameHomeActivity extends Activity implements HomeSurface.Actions {
    final Handler handler = new Handler(Looper.getMainLooper());
    HomeAppearance appearance;
    HomeItems items;
    FrameWallpapers wallpapers;
    HomeSettings settings;
    HomeWeather weather;
    HomeAssistant assistant;
    HomeSurface surface;
    private FrameLayout root;
    private ImageView photo;
    private AlertDialog library;
    private boolean resumed;
    private int selected;
    private final Runnable tick = new Runnable(){public void run(){
        String time=new SimpleDateFormat("HH:mm",Locale.CHINA).format(new Date());
        if(surface!=null)surface.clock.setText(time);
        if(settings!=null)settings.time(time);
        handler.postDelayed(this,60000-System.currentTimeMillis()%60000);
    }};
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        appearance=new HomeAppearance(this);items=new HomeItems(this);
        selected=getSharedPreferences("frame-home",0).getInt("selected",0);
        root=new FrameLayout(this);photo=new ImageView(this);
        photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(photo,new FrameLayout.LayoutParams(-1,-1));
        wallpapers=new FrameWallpapers(this,photo);
        weather=new HomeWeather(this,this::weatherChanged);
        settings=new HomeSettings(this);
        assistant=new HomeAssistant(this);
        wallpapers.changed=()->{if(surface!=null)surface.wallpaper(wallpaperDrawable());};
        setContentView(root);refreshHome();surface.focusApp();
    }
    void refreshHome(){
        int restore=Math.max(0,Math.min(items.order.size()-1,selected));
        selected=restore;
        if(surface!=null)root.removeView(surface);
        root.setBackgroundColor(appearance.light()?0xFFF0EFE9:0xFF11151B);
        photo.setVisibility(appearance.get("background")==0?View.VISIBLE:View.INVISIBLE);
        surface=new HomeSurface(this,appearance,items,wallpaperDrawable(),restore,false,this);
        root.addView(surface,new FrameLayout.LayoutParams(-1,-1));
        surface.apps.get(restore).requestFocus();
        selected=restore;
        handler.removeCallbacks(tick);tick.run();weatherChanged();updateRotation();
    }
    HomeSurface preview(){HomeSurface view=new HomeSurface(this,appearance,items,wallpaperDrawable(),selected,true,this);view.weather(weather.current());return view;}
    void weatherChanged(){if(surface!=null)surface.weather(weather.current());if(settings!=null)settings.weatherChanged();}
    Drawable wallpaperDrawable(){return photo.getDrawable();}
    void updateRotation(){if(weather!=null)weather.active(resumed&&!settings.showing()&&(assistant==null||!assistant.showing())&&(library==null||!library.isShowing()));if(resumed&&appearance.get("background")==0&&!settings.showing()&&(assistant==null||!assistant.showing())&&(library==null||!library.isShowing()))wallpapers.resume();else wallpapers.pause();}
    @Override public void selected(int index){selected=index;getSharedPreferences("frame-home",0).edit().putInt("selected",index).apply();}
    @Override public void open(String id){
        if(id.startsWith("hdmi:")){if(!XiaomiControls.openHdmi(this,Integer.parseInt(id.substring(5))))message("暂时无法切换信号源");return;}
        if("com.mitv.tvhome".equals(id)){stockHome();return;}
        Intent intent=getPackageManager().getLeanbackLaunchIntentForPackage(id);
        if(intent==null)intent=getPackageManager().getLaunchIntentForPackage(id);
        launch(intent,"应用暂时不可用");
    }
    @Override public void control(int index){
        if(index==0)showLibrary();
        else if(index==1){if(!XiaomiControls.showInputs(this))message("暂时无法打开信号源");}
        else if(index==2)settings.show();
        else if(index==3&&HomeAssistant.enabled(this))assistant.show();
    }
    void stockHome(){if(!XiaomiControls.startStockHome(this,new ComponentName("com.mitv.tvhome","com.mitv.tvhome.MainActivity")))message("暂时无法打开小米原桌面");}
    void systemSettings(){Intent i=XiaomiControls.settings(this,Settings.ACTION_SETTINGS);if(i==null)i=new Intent(Settings.ACTION_SETTINGS);launch(i,"暂时无法打开设置");}
    private void launch(Intent i,String error){if(i==null){message(error);return;}try{startActivity(i);}catch(RuntimeException unavailable){message(error);}}
    private void showLibrary(){
        items.discover();List<String> ids=new ArrayList<>(items.names.keySet());ids.removeIf(id->id.startsWith("hdmi:") || (getPackageManager().getLeanbackLaunchIntentForPackage(id)==null && getPackageManager().getLaunchIntentForPackage(id)==null));
        Collator collator=Collator.getInstance(Locale.CHINA);ids.sort((a,b)->collator.compare(items.name(a),items.name(b)));
        String[] labels=new String[ids.size()];for(int i=0;i<labels.length;i++)labels[i]=items.name(ids.get(i));
        library=new AlertDialog.Builder(this).setTitle("全部应用").setItems(labels,(d,i)->open(ids.get(i))).setNegativeButton("返回",null).create();
        library.setOnDismissListener(d->{surface.controls.get(0).requestFocus();updateRotation();});library.show();updateRotation();
    }
    @Override protected void onResume(){super.onResume();resumed=true;assistant.active(true);BootStart.confirm(this);handler.removeCallbacks(tick);tick.run();updateRotation();}
    @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);}
    @Override protected void onPause(){resumed=false;assistant.active(false);weather.active(false);handler.removeCallbacks(tick);wallpapers.pause();if(library!=null)library.dismiss();super.onPause();}
    @Override protected void onDestroy(){assistant.destroy();settings.close();weather.destroy();wallpapers.destroy();handler.removeCallbacksAndMessages(null);super.onDestroy();}
    @Override public void onBackPressed(){if(surface.controls.contains(getCurrentFocus()))surface.focusApp();else if(XiaomiControls.enabled())stockHome();else super.onBackPressed();}
    @Override public void dump(String prefix,java.io.FileDescriptor fd,java.io.PrintWriter out,String[] args){
        super.dump(prefix,fd,out,args);wallpapers.dump(out);BootStart.dump(this,out);weather.dump(out);
        out.println("HomeAppearance: "+appearance.prefs.getAll()+" items="+items.order+" settings="+settings.page());
    }
    void message(String value){Toast.makeText(this,value,Toast.LENGTH_LONG).show();}
    int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

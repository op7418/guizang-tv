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
    private ImageView photo, transition;
    private AlertDialog library, sources;
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
        root=new FrameLayout(this);photo=new ImageView(this);transition=new ImageView(this);
        photo.setScaleType(ImageView.ScaleType.CENTER_CROP);transition.setScaleType(ImageView.ScaleType.CENTER_CROP);transition.setAlpha(0);
        root.addView(photo,new FrameLayout.LayoutParams(-1,-1));root.addView(transition,new FrameLayout.LayoutParams(-1,-1));
        wallpapers=new FrameWallpapers(this,photo,transition);
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
        transition.setVisibility(photo.getVisibility());
        surface=new HomeSurface(this,appearance,items,wallpaperDrawable(),restore,false,this);
        root.addView(surface,new FrameLayout.LayoutParams(-1,-1));
        surface.apps.get(restore).requestFocus();
        selected=restore;
        handler.removeCallbacks(tick);tick.run();weatherChanged();updateRotation();
    }
    HomeSurface preview(){HomeSurface view=new HomeSurface(this,appearance,items,wallpaperDrawable(),selected,true,this);view.weather(weather.current());return view;}
    void weatherChanged(){if(surface!=null)surface.weather(weather.current());if(settings!=null)settings.weatherChanged();}
    Drawable wallpaperDrawable(){return photo.getDrawable();}
    void updateRotation(){boolean visible=resumed&&!settings.showing()&&(assistant==null||!assistant.showing())&&(library==null||!library.isShowing())&&(sources==null||!sources.isShowing());if(weather!=null)weather.active(visible);if(visible&&appearance.get("background")==0)wallpapers.resume();else wallpapers.pause();}
    @Override public void selected(int index){selected=index;getSharedPreferences("frame-home",0).edit().putInt("selected",index).apply();}
    @Override public void open(String id){
        if(id.startsWith("hdmi:")){int port=Integer.parseInt(id.substring(5));if(!(TclControls.enabled()?TclControls.openHdmi(this,port):XiaomiControls.openHdmi(this,port)))message("暂时无法切换信号源，请使用遥控器信源键");return;}
        if("com.mitv.tvhome".equals(id)||"com.tcl.cyberui".equals(id)){stockHome();return;}
        Intent intent=getPackageManager().getLeanbackLaunchIntentForPackage(id);
        if(intent==null)intent=getPackageManager().getLaunchIntentForPackage(id);
        launch(intent,"应用暂时不可用");
    }
    @Override public void control(int index){
        if(index==0)showLibrary();
        else if(index==1){if(TclControls.enabled())showTclInputs();else if(!XiaomiControls.showInputs(this))message("暂时无法打开信号源");}
        else if(index==2)settings.show();
        else if(index==3&&HomeAssistant.enabled(this))assistant.show();
    }
    void stockHome(){returnToHome();if(!(TclControls.enabled()?TclControls.openStockHome(this):XiaomiControls.startStockHome(this,new ComponentName("com.mitv.tvhome","com.mitv.tvhome.MainActivity"))))message("暂时无法打开原厂桌面");}
    void systemSettings(){
        returnToHome();
        boolean opened=TclControls.enabled()?TclControls.openSettings(this):LaunchFallback.start(
                Arrays.asList(XiaomiControls.settings(this,Settings.ACTION_SETTINGS),new Intent(Settings.ACTION_SETTINGS)),
                this::startActivity,(intent,error)->android.util.Log.w("GuiZangRoutes","Settings entry unavailable"));
        if(!opened)message("暂时无法打开设置");
    }
    private void launch(Intent i,String error){if(i==null){message(error);return;}try{startActivity(i);}catch(RuntimeException unavailable){message(error);}}
    private void showLibrary(){
        if(library!=null&&library.isShowing())return;
        items.discover();List<String> ids=new ArrayList<>(items.names.keySet());ids.removeIf(id->id.startsWith("hdmi:") || (getPackageManager().getLeanbackLaunchIntentForPackage(id)==null && getPackageManager().getLaunchIntentForPackage(id)==null));
        Collator collator=Collator.getInstance(Locale.CHINA);ids.sort((a,b)->collator.compare(items.name(a),items.name(b)));
        String[] labels=new String[ids.size()];for(int i=0;i<labels.length;i++)labels[i]=items.name(ids.get(i));
        library=new AlertDialog.Builder(this).setTitle("全部应用").setItems(labels,(d,i)->open(ids.get(i))).setNegativeButton("返回",null).create();
        library.setOnDismissListener(d->{if(library==d)library=null;if(!isFinishing()&&!isDestroyed())surface.controls.get(0).requestFocus();updateRotation();});library.show();updateRotation();
    }
    private void showTclInputs(){
        if(sources!=null&&sources.isShowing())return;
        LinkedHashMap<String,String> available=TclControls.inputs(this);
        if(available.isEmpty()){message("未发现已适配信源，请使用遥控器信源键");return;}
        String[] names=available.keySet().toArray(new String[0]);List<String> ids=new ArrayList<>(available.values());
        sources=new AlertDialog.Builder(this).setTitle("选择信号源").setItems(names,(d,i)->{
            if(!TclControls.openInput(this,ids.get(i)))message("暂时无法切换信号源，请使用遥控器信源键");
        }).setNegativeButton("返回",null).create();
        sources.setOnDismissListener(d->{if(sources==d)sources=null;if(!isFinishing()&&!isDestroyed())surface.controls.get(1).requestFocus();updateRotation();});
        sources.show();updateRotation();
    }
    private void closeLists(){if(library!=null)library.dismiss();if(sources!=null)sources.dismiss();}
    private void returnToHome(){
        settings.close();if(assistant!=null)assistant.close();closeLists();surface.focusApp();
        handler.post(()->{if(!isFinishing()&&!isDestroyed())surface.focusApp();});
    }
    private boolean isDefaultHome(){
        android.content.pm.ResolveInfo home=getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME),android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);
        return home!=null&&home.activityInfo!=null&&getPackageName().equals(home.activityInfo.packageName);
    }
    @Override protected void onResume(){super.onResume();resumed=true;assistant.active(true);BootStart.confirm(this);handler.removeCallbacks(tick);tick.run();updateRotation();}
    @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);if(Intent.ACTION_MAIN.equals(i.getAction())&&i.hasCategory(Intent.CATEGORY_HOME))returnToHome();}
    @Override protected void onPause(){resumed=false;assistant.active(false);weather.active(false);handler.removeCallbacks(tick);wallpapers.pause();closeLists();super.onPause();}
    @Override protected void onDestroy(){assistant.destroy();settings.close();closeLists();weather.destroy();wallpapers.destroy();handler.removeCallbacksAndMessages(null);super.onDestroy();}
    @Override public void onBackPressed(){if(isDefaultHome()||surface.controls.contains(getCurrentFocus()))surface.focusApp();else if(XiaomiControls.enabled())stockHome();else super.onBackPressed();}
    @Override public void dump(String prefix,java.io.FileDescriptor fd,java.io.PrintWriter out,String[] args){
        super.dump(prefix,fd,out,args);wallpapers.dump(out);BootStart.dump(this,out);weather.dump(out);
        out.println("HomeAppearance: "+appearance.prefs.getAll()+" items="+items.order+" settings="+settings.page());
    }
    void message(String value){Toast.makeText(this,value,Toast.LENGTH_LONG).show();}
    int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

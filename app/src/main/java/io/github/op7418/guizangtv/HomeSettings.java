package io.github.op7418.guizangtv;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Remote-first grouped settings. Focus browses; OK commits; Back returns one level. */
final class HomeSettings {
    private final FrameHomeActivity a;
    private Dialog dialog;
    private String page="overview";
    private final Map<String,Integer> remembered=new HashMap<>();
    private final List<View> choices=new ArrayList<>();
    private final Deque<List<String>> undo=new ArrayDeque<>();
    private LinearLayout options;
    private HomeSurface preview;
    private FrameLayout previewHost;
    private TextView status;
    private boolean rendering;
    private FrameLayout stage;
    private ImageView backdrop;
    private View content;
    private int backdropRequest;
    private int cityRequest;
    private boolean searching;
    private EditText cityInput;
    private String cityQuery="",cityError="";
    private List<HomeWeather.City> cityResults=new ArrayList<>();
    HomeSettings(FrameHomeActivity activity){a=activity;}
    boolean showing(){return dialog!=null&&dialog.isShowing();}
    String page(){return showing()?page:"closed";}
    void time(String time){if(preview!=null)preview.clock.setText(time);}
    void weatherChanged(){if(preview!=null)preview.weather(a.weather.current());if(showing()&&page.equals("weather")&&status!=null)status.setText(a.weather.city()+" · "+a.weather.status());}
    void show(){
        if(showing())return;
        page="overview";undo.clear();
        dialog=new Dialog(a){@Override public void onBackPressed(){back();}};
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setOnDismissListener(d->{a.wallpapers.cancelPreview();a.wallpapers.cancelSelection();undo.clear();a.updateRotation();a.surface.controls.get(2).requestFocus();});
        dialog.show();dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(-1,-1);
        dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        stage=new FrameLayout(a);stage.setBackgroundColor(0xFF121619);
        backdrop=new ImageView(a);backdrop.setScaleType(ImageView.ScaleType.CENTER_CROP);stage.addView(backdrop,new FrameLayout.LayoutParams(-1,-1));
        View shade=new View(a);shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{0xC2080C0F,0x7D080C0F}));stage.addView(shade,new FrameLayout.LayoutParams(-1,-1));
        dialog.setContentView(stage);render();a.updateRotation();
    }
    void close(){cityRequest++;searching=false;if(dialog!=null)dialog.dismiss();}
    private void back(){
        a.wallpapers.cancelPreview();a.wallpapers.cancelSelection();
        if(page.equals("overview")){close();return;}
        go(page.equals("city")?"weather":page.equals("wallpaper")||page.equals("rotation")||page.equals("fade")?"background":page.startsWith("item:")||page.equals("add")?"home":"overview");
    }
    private void go(String next){cityRequest++;searching=false;a.wallpapers.cancelPreview();a.wallpapers.cancelSelection();page=next;render();}
    private String title(){
        switch(page){case "overview":return "设置";case "background":return "背景";case "wallpaper":return "壁纸";case "rotation":return "轮播";case "fade":return "渐变时长";
            case "weather":return "时间与天气";case "city":return "选择城市";case "layout":return "布局";case "type":return "字体";case "focus":return "选中样式";case "home":return "首页内容";case "add":return "首页内容 / 添加";case "tv":return "电视与启动";case "experiments":return "实验性功能";
            default:return "首页内容 / "+a.items.name(page.substring(5));}
    }
    private void render(){
        rendering=true;choices.clear();
        boolean overview=page.equals("overview");
        LinearLayout panel=new LinearLayout(a);panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(a.dp(70),a.dp(overview?42:24),a.dp(70),a.dp(30));
        if(!overview){
            TextView back=label("‹  "+(page.equals("wallpaper")||page.equals("rotation")||page.equals("fade")?"背景":"设置"),14);
            back.setTextColor(0xBBFFFFFF);back.setFocusable(true);back.setOnClickListener(v->back());
            back.setPadding(a.dp(8),0,a.dp(10),0);
            back.setOnFocusChangeListener((v,f)->decorate(v,f));
            LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-2,a.dp(24));bp.leftMargin=-a.dp(8);panel.addView(back,bp);
        }
        TextView title=label(title(),28);title.setPadding(0,a.dp(6),0,0);
        panel.addView(title,new LinearLayout.LayoutParams(-1,a.dp(overview?64:68)));
        LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.HORIZONTAL);
        panel.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        ScrollView scroll=new ScrollView(a);scroll.setVerticalScrollBarEnabled(false);scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        options=new LinearLayout(a);options.setOrientation(LinearLayout.VERTICAL);options.setPadding(0,0,0,a.dp(12));
        scroll.addView(options,new ScrollView.LayoutParams(-1,-2));
        body.addView(scroll,new LinearLayout.LayoutParams(a.dp(overview?528:320),-1));
        LinearLayout right=new LinearLayout(a);right.setOrientation(LinearLayout.VERTICAL);right.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,-1,1);rp.leftMargin=a.dp(38);body.addView(right,rp);
        previewHost=new FrameLayout(a){@Override protected void onSizeChanged(int w,int h,int oldw,int oldh){super.onSizeChanged(w,h,oldw,oldh);scalePreview(w,h);}};
        previewHost.setBackground(WallpaperMaterial.rounded(0x40000000,a.dp(10)));previewHost.setClipToOutline(true);
        right.addView(previewHost,new LinearLayout.LayoutParams(-1,a.dp(260)));
        status=label("首页预览",14);status.setTextColor(0xBFFFFFFF);status.setPadding(0,a.dp(16),0,0);
        right.addView(status,new LinearLayout.LayoutParams(-1,a.dp(65)));
        if(overview)right.setVisibility(View.GONE);
        TextView footer=label(overview?"确定  打开选项":"确定  应用设置        返回  上一级",13);footer.setTextColor(0xA6FFFFFF);
        panel.addView(footer,new LinearLayout.LayoutParams(-1,a.dp(26)));
        switch(page){
            case "overview":
                entry("背景", "壁纸、轮播与纯色",()->go("background"));entry("布局","列表、Dock、图标与文字",()->go("layout"));
                entry("字体","字形与字号",()->go("type"));entry("选中样式","文字高亮与毛玻璃",()->go("focus"));
                entry("时间与天气","城市、温度与天气显示",()->go("weather"));entry("首页内容","添加、排序、隐藏",()->go("home"));entry("电视与启动","自动进入与小米系统",()->go("tv"));entry("实验性功能","Codex 对话 · 默认关闭",()->go("experiments"));break;
            case "background":
                row("背景","background",new String[]{"照片","浅色","深色"});
                if(a.appearance.get("background")==0){entry("壁纸",a.wallpapers.name(a.wallpapers.selected()),()->go("wallpaper"));entry("轮播",a.wallpapers.rotating()?"每 "+a.wallpapers.minutes()+" 分钟":"固定壁纸",()->go("rotation"));if(a.wallpapers.rotating())entry("渐变时长",WallpaperCycle.fadeLabel(a.wallpapers.fadeMs()),()->go("fade"));}
                else note("切换回照片背景后，可选择壁纸和轮播。原来的照片与轮播偏好会保留。");break;
            case "layout":
                row("首页布局","layout",new String[]{"纵向列表","横向 Dock"});row("应用显示","apps",new String[]{"文字","图标","图标＋文字"});row("右侧操作","controls",new String[]{"文字","图标","图标＋文字"});
                note("仅显示图标时，聚焦后会显示名称。");break;
            case "type":
                row("字体风格","font",new String[]{"轻盈","标准","衬线"});row("首页字号","size",new String[]{"小","标准","大"});note("使用电视本机字体，中文由系统回退显示。字体应用于首页名称和时钟；设置页保持清晰易读。Dock 使用适合卡片的字号。");break;
            case "focus":
                row("焦点表现","focus",new String[]{"文字高亮","毛玻璃"});
                if(a.appearance.get("focus")==1)row("底板质感","material",new String[]{"柔和","浓雾","清透"});else note("启用毛玻璃后可调整质感。");
                row("未选中项淡化","dim",new String[]{"轻","标准","明显"});break;
            case "rotation":
                option("固定壁纸",!a.wallpapers.rotating(),()->{if(a.wallpapers.rotation(false,a.wallpapers.minutes()))render();else failed();});
                for(int m:WallpaperCycle.MINUTES){final int value=m;option("每 "+m+" 分钟",a.wallpapers.rotating()&&a.wallpapers.minutes()==m,()->{
                    if(a.wallpapers.rotation(true,value))render();else failed();});}
                note("只在照片首页显示时计时；设置页、其他应用和纯色背景会暂停轮播。选择壁纸不会改变轮播模式。");break;
            case "fade":
                for(int duration=700;duration<=1500;duration+=100){final int value=duration;
                    option(WallpaperCycle.fadeLabel(value),a.wallpapers.fadeMs()==value,()->{
                        if(a.wallpapers.fadeDuration(value))render();else failed();});}
                break;
            case "wallpaper":gallery();break;
            case "home":
                note("保留 1–9 个入口。选择一项可调整位置或从首页隐藏。");
                for(int i=0;i<a.items.order.size();i++){String id=a.items.order.get(i);entry((i+1)+"  "+a.items.name(id),id.startsWith("hdmi:")?"HDMI "+id.substring(5):"应用",()->go("item:"+id));}
                entry("添加入口","应用或 HDMI 信号源",()->go("add"));if(!undo.isEmpty())entry("撤销上一步","恢复本次编辑前的顺序",this::undo);break;
            case "add":
                a.items.discover();for(String id:a.items.names.keySet())if(!a.items.order.contains(id))entry(a.items.name(id),"添加到首页末尾",()->{
                    if(a.items.order.size()>=9){a.message("首页最多保留 9 个入口");return;}List<String> next=new ArrayList<>(a.items.order);next.add(id);if(save(next))go("home");});break;
            case "weather":
                option("关闭天气",!a.weather.enabled(),()->{if(a.weather.enabled(false)){a.refreshHome();render();}else failed();});
                option("显示天气",a.weather.enabled(),()->{if(a.weather.enabled(true)){a.refreshHome();render();}else failed();});
                option("自动识别城市",a.weather.automatic(),()->{if(a.weather.autoCity()){a.refreshHome();render();}else failed();});
                entry("手动选择城市",a.weather.city(),()->go("city"));
                note("天气显示在时间右侧，每 30 分钟更新。自动城市根据网络出口识别；使用代理时可能不准确，可手动纠正。");
                note("数据：Open-Meteo · CC BY 4.0\n城市：IPWhois · 不申请精确定位");break;
            case "city":
                cityInput=new EditText(a);cityInput.setText(cityQuery);cityInput.setTextColor(0xF2FFFFFF);cityInput.setHintTextColor(0x99FFFFFF);cityInput.setTextSize(18);
                cityInput.setHint("城市名称或拼音");cityInput.setSingleLine(true);cityInput.setPadding(a.dp(14),0,a.dp(14),0);cityInput.setBackground(WallpaperMaterial.rounded(0x26FFFFFF,a.dp(10)));
                cityInput.setOnFocusChangeListener((v,focus)->v.setBackground(WallpaperMaterial.rounded(focus?0x45FFFFFF:0x26FFFFFF,a.dp(10))));
                options.addView(cityInput,new LinearLayout.LayoutParams(-1,a.dp(44)));
                entry("搜索城市",searching?"正在搜索…":"确认后查看匹配城市",this::searchCities);
                for(HomeWeather.City city:cityResults)entry(city.name,city.region,()->{if(a.weather.select(city)){a.refreshHome();go("weather");}else failed();});
                if(!cityError.isEmpty())note(cityError);else note("确认省份和国家后选择。返回不会更改当前城市。");break;
            case "experiments":
                heading("Codex 对话");
                option("关闭",!HomeAssistant.enabled(a),()->experiment(false));
                option("开启",HomeAssistant.enabled(a),()->experiment(true));
                note("开启后，首页显示 Codex 入口。需要同一网络内的电脑运行连接服务，并登录 Codex；使用相应账号额度。");
                note("当前支持文字对话；语音输入取决于系统输入法。尚未接入日程、米家或电脑控制。");
                note("关闭后隐藏入口并停止电视端连接，保留配对。已发送的回答可能继续在电脑完成；完全停用请在电脑停止连接服务。");break;
            case "tv":
                entry("开机自动进入",BootStart.enabled(a)?"已开启":"已关闭",()->BootStart.show(a));entry("电视系统设置","网络、声音、画面等",a::systemSettings);entry("小米原桌面","打开原来的电视首页",a::stockHome);break;
            default:
                String id=page.substring(5);int pos=a.items.order.indexOf(id);
                if(pos>0)entry("上移","与上一项交换位置",()->move(id,-1));if(pos>=0&&pos<a.items.order.size()-1)entry("下移","与下一项交换位置",()->move(id,1));
                if(a.items.order.size()>1)entry("从首页隐藏","应用仍保留在电视上",()->{List<String> next=new ArrayList<>(a.items.order);next.remove(id);if(save(next))go("home");});
                else note("至少保留一个首页入口。");break;
        }
        if(content!=null)stage.removeView(content);content=panel;stage.addView(panel,new FrameLayout.LayoutParams(-1,-1));
        refreshPreview();background(a.wallpaperDrawable());rendering=false;weatherChanged();
        if(!choices.isEmpty())choices.get(Math.min(remembered.getOrDefault(page,0),choices.size()-1)).requestFocus();
    }
    private void experiment(boolean enabled){
        if(!a.assistant.enabled(enabled)){failed();return;}
        a.refreshHome();render();
    }
    private void searchCities(){
        if(searching)return;cityQuery=cityInput.getText().toString().trim();
        ((android.view.inputmethod.InputMethodManager)a.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(cityInput.getWindowToken(),0);
        searching=true;cityError="";cityResults.clear();int request=++cityRequest;render();
        a.weather.search(cityQuery,(cities,error)->{if(request!=cityRequest||!showing()||!page.equals("city"))return;searching=false;cityResults=cities;cityError=error;render();});
    }
    private void gallery(){
        for(int i=0;i<a.wallpapers.count();i++){
            final int index=i;
            TextView v=button(a.wallpapers.name(i),()->{
                if(index==a.wallpapers.selected()){status.setText("已应用这张壁纸");return;}
                status.setText("正在应用壁纸…");String origin=page;
                a.wallpapers.select(index,()->{if(showing()&&page.equals(origin)){render();status.setText("已应用 · "+a.wallpapers.name(index));}});
            });
            android.graphics.drawable.Drawable thumb=a.wallpapers.thumbnail(i);
            if(thumb!=null){thumb=roundedThumb(thumb);thumb.setBounds(0,0,a.dp(48),a.dp(27));v.setCompoundDrawables(thumb,null,checkmark(i==a.wallpapers.selected()),null);v.setCompoundDrawablePadding(a.dp(14));}
            v.setOnFocusChangeListener((view,focus)->{
                decorate(view,focus);if(!focus)return;remembered.put(page,choices.indexOf(view));
                a.wallpapers.cancelSelection();
                status.setText(index==a.wallpapers.selected()?"已应用 · "+a.wallpapers.name(index):"预览 · 按确定应用 "+a.wallpapers.name(index));
                if(preview!=null){HomeSurface target=preview;a.wallpapers.preview(index,target.previewPhoto,()->{
                    if(preview!=target||!showing())return;
                    target.wallpaper(target.previewPhoto.getDrawable());background(target.previewPhoto.getDrawable());
                });}
            });
            options.addView(v,new LinearLayout.LayoutParams(-1,a.dp(46)));
        }
        note("上下浏览预览，按确定设为壁纸。");
    }
    private void row(String title,String key,String[] labels){
        heading(title);
        for(int i=0;i<labels.length;i++){
            final int value=i;
            TextView v=button(labels[i],()->{
                if(a.appearance.get(key)==value)return;
                if(a.appearance.set(key,value)){a.refreshHome();render();}else failed();
            });
            v.setCompoundDrawables(null,null,checkmark(a.appearance.get(key)==i),null);
            options.addView(v,new LinearLayout.LayoutParams(-1,a.dp(42)));
        }
    }
    private void option(String name,boolean selected,Runnable run){
        TextView v=button(name,run);v.setCompoundDrawables(null,null,checkmark(selected),null);
        options.addView(v,new LinearLayout.LayoutParams(-1,a.dp(44)));
    }
    private Drawable checkmark(boolean selected){
        if(!selected)return null;
        Drawable mark=a.getDrawable(R.drawable.mark_check).mutate();mark.setTint(0xDFFFFFFF);mark.setBounds(0,0,a.dp(16),a.dp(16));return mark;
    }
    private void heading(String s){TextView v=label(s,14);v.setTextColor(0xADFFFFFF);v.setPadding(a.dp(12),a.dp(12),0,a.dp(7));options.addView(v);}
    private void note(String s){TextView v=label(s,13);v.setTextColor(0xA6FFFFFF);v.setLineSpacing(a.dp(3),1);v.setPadding(a.dp(12),a.dp(14),a.dp(8),a.dp(12));options.addView(v);}
    private void entry(String name,String subtitle,Runnable run){
        TextView v=button(name+(subtitle.isEmpty()?"":"\n"+subtitle),run);
        if(!subtitle.isEmpty()){
            SpannableString text=new SpannableString(v.getText());
            text.setSpan(new AbsoluteSizeSpan(13,true),name.length()+1,text.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            v.setText(text);v.setLineSpacing(a.dp(3),1);
        }
        v.setTag(Boolean.TRUE);
        Drawable arrow=a.getDrawable(R.drawable.mark_caret_right).mutate();arrow.setTint(0xAAFFFFFF);arrow.setBounds(0,0,a.dp(14),a.dp(14));v.setCompoundDrawables(null,null,arrow,null);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,a.dp(subtitle.isEmpty()?44:56));lp.bottomMargin=a.dp(4);options.addView(v,lp);decorate(v,false);
    }
    private TextView button(String text,Runnable run){
        TextView v=label(text,19);v.setPadding(a.dp(14),a.dp(5),a.dp(16),a.dp(5));v.setGravity(Gravity.CENTER_VERTICAL);v.setId(View.generateViewId());v.setFocusable(true);v.setFocusableInTouchMode(true);v.setClickable(true);choices.add(v);
        v.setOnFocusChangeListener((view,focus)->{decorate(view,focus);if(focus&&!rendering)remembered.put(page,choices.indexOf(view));});
        v.setOnClickListener(view->run.run());v.setOnKeyListener((view,key,e)->(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER)&&e.getAction()==KeyEvent.ACTION_DOWN&&e.getRepeatCount()>0);return v;
    }
    private void decorate(View view,boolean focus){
        TextView v=(TextView)view;
        v.setBackground(WallpaperMaterial.rounded(focus?0xF5F5F4EF:0x00000000,a.dp(10)));
        v.setTextColor(focus?0xFF202321:0xEBFFFFFF);
        String value=v.getText().toString();int line=value.indexOf('\n');
        if(line>=0){SpannableString text=new SpannableString(value);
            text.setSpan(new AbsoluteSizeSpan(13,true),line+1,value.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new ForegroundColorSpan(focus?0xA6202321:0xA6FFFFFF),line+1,value.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);v.setText(text);}

        Drawable arrow=v.getCompoundDrawables()[2];if(arrow!=null)arrow.setTint(focus?0x99202321:0xAAFFFFFF);
    }
    private TextView label(String s,int size){TextView v=new TextView(a);v.setText(s);v.setTextSize(size);v.setTextColor(0xF2FFFFFF);v.setTypeface(android.os.Build.VERSION.SDK_INT>=28?Typeface.create(Typeface.create("sans-serif",Typeface.NORMAL),300,false):Typeface.create("sans-serif-light",Typeface.NORMAL));v.setIncludeFontPadding(false);return v;}
    private void background(Drawable source){
        int request=++backdropRequest;
        WallpaperMaterial.request(source,42,image->{if(request==backdropRequest&&showing()&&image!=null)backdrop.setImageBitmap(image);});
    }
    private Drawable roundedThumb(Drawable source){
        android.graphics.Bitmap image=android.graphics.Bitmap.createBitmap(a.dp(48),a.dp(27),android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas c=new android.graphics.Canvas(image);android.graphics.Path path=new android.graphics.Path();
        path.addRoundRect(0,0,image.getWidth(),image.getHeight(),a.dp(4),a.dp(4),android.graphics.Path.Direction.CW);c.clipPath(path);
        source.setBounds(0,0,image.getWidth(),image.getHeight());source.draw(c);return new android.graphics.drawable.BitmapDrawable(a.getResources(),image);
    }
    private void refreshPreview(){
        preview=a.preview();previewHost.removeAllViews();previewHost.addView(preview,new FrameLayout.LayoutParams(a.dp(960),a.dp(540)));
        preview.setPivotX(0);preview.setPivotY(0);scalePreview(previewHost.getWidth(),previewHost.getHeight());
        preview.clock.setText(new java.text.SimpleDateFormat("HH:mm",Locale.CHINA).format(new Date()));
    }
    private void scalePreview(int w,int h){if(preview!=null){float scale=Math.min(w/(float)a.dp(960),h/(float)a.dp(540));preview.setScaleX(scale);preview.setScaleY(scale);}}
    private boolean save(List<String> next){List<String> old=new ArrayList<>(a.items.order);if(!a.items.save(next)){failed();return false;}undo.push(old);a.refreshHome();return true;}
    private void move(String id,int direction){List<String> next=new ArrayList<>(a.items.order);int index=next.indexOf(id);Collections.swap(next,index,index+direction);if(save(next))render();}
    private void undo(){if(undo.isEmpty())return;if(a.items.save(undo.peek())){undo.pop();a.refreshHome();render();}else failed();}
    private void failed(){a.message("保存失败，保留原设置，请重试");}
}

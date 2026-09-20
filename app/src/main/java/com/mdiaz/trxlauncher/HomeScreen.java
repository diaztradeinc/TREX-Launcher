package com.mdiaz.trxlauncher;
import android.content.*;import android.content.pm.*;import android.graphics.Color;import android.graphics.drawable.Drawable;import android.view.*;import android.widget.*;import java.util.*;

final class HomeScreen extends ScrollView{
    private final MainActivity a;
    HomeScreen(MainActivity c){super(c);a=c;setFillViewport(true);build();}
    private void build(){
        LinearLayout body=Ui.col(a);body.setPadding(Ui.dp(a,24),Ui.dp(a,18),Ui.dp(a,24),Ui.dp(a,22));addView(body);
        int wp=a.getSharedPreferences("trx",0).getInt("wallpaper",0);
        if(wp>=0&&wp<Ui.WALLPAPER_IMAGES.length&&Ui.WALLPAPER_IMAGES[wp]!=0){
            body.setBackgroundResource(Ui.WALLPAPER_IMAGES[wp]);
            body.setForeground(new android.graphics.drawable.ColorDrawable(0xCC000000));
        }
        body.addView(Ui.text(a,"PRECISION COMMAND",11,Ui.RED,true));
        body.addView(Ui.text(a,"Focused on the road.",30,Color.WHITE,true));
        Space s=new Space(a);body.addView(s,new LinearLayout.LayoutParams(1,Ui.dp(a,16)));
        LinearLayout tiles=Ui.row(a);
        tiles.addView(navTile(),new LinearLayout.LayoutParams(0,Ui.dp(a,250),1));
        Space gap=new Space(a);tiles.addView(gap,new LinearLayout.LayoutParams(Ui.dp(a,12),1));
        tiles.addView(npTile(),new LinearLayout.LayoutParams(0,Ui.dp(a,250),1));
        body.addView(tiles);
        Space s2=new Space(a);body.addView(s2,new LinearLayout.LayoutParams(1,Ui.dp(a,16)));
        LinearLayout gauges=Ui.row(a);
        gauges.addView(gauge("RPM",ObdService.rpm,"RPM"),new LinearLayout.LayoutParams(0,Ui.dp(a,150),1));
        gauges.addView(gauge("BOOST","--","PSI"),new LinearLayout.LayoutParams(0,Ui.dp(a,150),1));
        gauges.addView(gauge("TEMP",ObdService.coolant,"°F"),new LinearLayout.LayoutParams(0,Ui.dp(a,150),1));
        gauges.addView(gauge("HP","--","HP"),new LinearLayout.LayoutParams(0,Ui.dp(a,150),1));
        body.addView(gauges);
        Space s3=new Space(a);body.addView(s3,new LinearLayout.LayoutParams(1,Ui.dp(a,16)));
        body.addView(quickApps());
    }
    private View quickApps(){
        LinearLayout card=Ui.col(a);card.setPadding(Ui.dp(a,16),Ui.dp(a,14),Ui.dp(a,16),Ui.dp(a,14));card.setBackground(Ui.bg(Ui.CARD,Ui.CARD_STROKE,20,a));
        LinearLayout hdr=Ui.row(a);
        hdr.addView(Ui.text(a,"QUICK APPS",11,0xff9ea2a9,true),new LinearLayout.LayoutParams(0,-1,1));
        Button add=Ui.button(a,"+ ADD",false);add.setOnClickListener(v->showPicker(card));hdr.addView(add,new LinearLayout.LayoutParams(Ui.dp(a,86),Ui.dp(a,44)));
        card.addView(hdr);
        LinearLayout row=Ui.row(a);row.setTag("quick_row");card.addView(row,new LinearLayout.LayoutParams(-1,Ui.dp(a,86)));
        renderQuickApps(row);
        return card;
    }
    private void renderQuickApps(LinearLayout row){
        row.removeAllViews();
        Set<String> picked=a.getSharedPreferences("trx",0).getStringSet("quick_apps",new HashSet<>());
        List<ResolveInfo> all=launchableApps();
        List<ResolveInfo> chosen=new ArrayList<>();
        for(ResolveInfo info:all){String pkg=info.activityInfo.packageName;if(picked.contains(pkg))chosen.add(info);}
        int shown=0;
        for(ResolveInfo info:chosen){if(shown++>=6)break;row.addView(appTile(info),new LinearLayout.LayoutParams(0,-1,1));}
        while(shown++<6){LinearLayout empty=Ui.col(a);empty.setGravity(android.view.Gravity.CENTER);empty.setBackground(Ui.bg(0x00000000,Ui.CARD_STROKE,14,a));TextView plus=Ui.text(a,"+",22,Ui.DIM_TEXT,true);plus.setGravity(android.view.Gravity.CENTER);empty.addView(plus);row.addView(empty,new LinearLayout.LayoutParams(0,-1,1));}
    }
    private View appTile(ResolveInfo info){
        LinearLayout tile=Ui.col(a);tile.setGravity(android.view.Gravity.CENTER);tile.setPadding(4,6,4,6);
        ImageView iv=new ImageView(a);iv.setImageDrawable(info.loadIcon(a.getPackageManager()));tile.addView(iv,new LinearLayout.LayoutParams(Ui.dp(a,44),Ui.dp(a,44)));
        TextView name=Ui.text(a,info.loadLabel(a.getPackageManager()).toString(),9,0xffa9adb4,false);name.setGravity(android.view.Gravity.CENTER);name.setMaxLines(1);name.setEllipsize(android.text.TextUtils.TruncateAt.END);tile.addView(name,new LinearLayout.LayoutParams(-1,-2));
        tile.setOnClickListener(v->{Intent i=a.getPackageManager().getLaunchIntentForPackage(info.activityInfo.packageName);if(i!=null)a.startActivity(i);});
        return tile;
    }
    private List<ResolveInfo> launchableApps(){
        List<ResolveInfo> apps=a.getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0);
        apps.sort(Comparator.comparing(x->x.loadLabel(a.getPackageManager()).toString().toLowerCase()));return apps;
    }
    private void showPicker(LinearLayout card){
        Set<String> picked=new HashSet<>(a.getSharedPreferences("trx",0).getStringSet("quick_apps",new HashSet<>()));
        android.app.AlertDialog.Builder dlg=new android.app.AlertDialog.Builder(a);
        dlg.setTitle("Choose Quick Apps (max 6)");
        LinearLayout list=Ui.col(a);list.setPadding(Ui.dp(a,20),Ui.dp(a,8),Ui.dp(a,20),Ui.dp(a,8));
        ScrollView sc=new ScrollView(a);sc.addView(list);
        for(ResolveInfo info:launchableApps()){
            String pkg=info.activityInfo.packageName;
            CheckBox cb=new CheckBox(a);cb.setText(info.loadLabel(a.getPackageManager()).toString());cb.setTextColor(Color.WHITE);cb.setChecked(picked.contains(pkg));
            cb.setOnCheckedChangeListener((b,on)->{if(on)picked.add(pkg);else picked.remove(pkg);});
            list.addView(cb,new LinearLayout.LayoutParams(-1,Ui.dp(a,52)));
        }
        sc.setLayoutParams(new LinearLayout.LayoutParams(-1,Ui.dp(a,420)));
        dlg.setView(sc);
        dlg.setNegativeButton("Cancel",null);
        dlg.setPositiveButton("Save",(d,w)->{
            if(picked.size()>6){android.widget.Toast.makeText(a,"Choose up to 6 apps",android.widget.Toast.LENGTH_SHORT).show();return;}
            a.getSharedPreferences("trx",0).edit().putStringSet("quick_apps",picked).apply();
            LinearLayout row=card.findViewWithTag("quick_row");if(row!=null)renderQuickApps(row);
        });
        dlg.show();
    }
    private View navTile(){
        FrameLayout card=new FrameLayout(a);
        card.setBackgroundResource(R.drawable.nav_map_bg);
        card.setForeground(Ui.bg(0x88000000,Ui.CARD_STROKE,22,a));
        LinearLayout overlay=Ui.col(a);overlay.setPadding(20,18,20,18);
        overlay.addView(Ui.text(a,"NAVIGATION  •  READY",12,Ui.GREEN_DOT,true));
        overlay.addView(Ui.text(a,"Where are we heading?",20,Color.WHITE,true));
        Space s=new Space(a);overlay.addView(s,new LinearLayout.LayoutParams(1,0,1));
        Button b=Ui.button(a,"OPEN MAP  ➤",true);b.setOnClickListener(v->a.openNavigation(null));overlay.addView(b,new LinearLayout.LayoutParams(-1,56));
        card.addView(overlay,new FrameLayout.LayoutParams(-1,-1));card.setOnClickListener(v->a.openNavigation(null));return card;
    }
    private View npTile(){
        LinearLayout c=Ui.col(a);c.setPadding(18,16,18,16);c.setBackground(Ui.bg(Ui.CARD,Ui.CARD_STROKE,20,a));
        c.addView(Ui.text(a,"NOW PLAYING  •  CONNECTED",12,Ui.GREEN_DOT,true));
        FrameLayout art=new FrameLayout(a);art.setBackground(Ui.bg(Ui.NAV_TILE_BG,Ui.CARD_STROKE,14,a));
        ImageView iv=new ImageView(a);iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if(MediaAccessService.albumArt!=null)iv.setImageBitmap(MediaAccessService.albumArt);else iv.setImageResource(R.drawable.music_art);
        art.addView(iv,new FrameLayout.LayoutParams(-1,-1));
        c.addView(art,new LinearLayout.LayoutParams(-1,Ui.dp(a,96)));
        TextView title=Ui.text(a,MediaAccessService.playing?MediaAccessService.title:"NOTHING PLAYING",16,Color.WHITE,true);title.setMaxLines(1);title.setEllipsize(android.text.TextUtils.TruncateAt.END);c.addView(title);
        TextView artist=Ui.text(a,MediaAccessService.playing?MediaAccessService.artist:"Pick a track to get started",12,Ui.SUB_TEXT,false);artist.setMaxLines(1);artist.setEllipsize(android.text.TextUtils.TruncateAt.END);c.addView(artist);
        Space s=new Space(a);c.addView(s,new LinearLayout.LayoutParams(1,0,1));
        Button play=Ui.button(a,MediaAccessService.playing?"❚❚  PAUSED":"▶  PLAY",false);play.setOnClickListener(v->MediaAccessService.playPause());c.addView(play,new LinearLayout.LayoutParams(-1,52));
        return c;
    }
    private View gauge(String label,String value,String unit){
        LinearLayout c=Ui.col(a);c.setGravity(android.view.Gravity.CENTER);c.setPadding(Ui.dp(a,6),Ui.dp(a,16),Ui.dp(a,6),Ui.dp(a,16));c.setBackground(Ui.bg(Ui.CARD,Ui.CARD_STROKE,18,a));
        TextView v=Ui.text(a,value,26,Color.WHITE,true);v.setGravity(android.view.Gravity.CENTER);c.addView(v);
        TextView l=Ui.text(a,label,11,0xff9ea2a9,true);l.setGravity(android.view.Gravity.CENTER);c.addView(l);
        TextView u=Ui.text(a,unit,10,Ui.DIM_TEXT,true);u.setGravity(android.view.Gravity.CENTER);c.addView(u);
        return c;
    }
}

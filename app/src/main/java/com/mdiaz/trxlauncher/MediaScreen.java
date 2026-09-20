package com.mdiaz.trxlauncher;
import android.graphics.Color;import android.graphics.drawable.GradientDrawable;import android.provider.Settings;import android.text.TextUtils;import android.widget.*;import java.util.List;
final class MediaScreen extends ScrollView{
    private final MainActivity a;MediaScreen(MainActivity c){super(c);a=c;setFillViewport(true);show(0);}
    private void show(int tab){removeAllViews();LinearLayout root=Ui.col(a);root.setPadding(24,18,24,22);addView(root);root.addView(Ui.text(a,"PRECISION MEDIA",11,Ui.RED,true));root.addView(Ui.text(a,"Your music, perfectly centered.",32,Color.WHITE,true));root.addView(Ui.text(a,"Large controls, sources, queue, and audio tuning.",14,Ui.SUB_TEXT,false));root.addView(Ui.tabs(a,new String[]{"Now Playing","Queue","Sources","Audio"},tab,this::show),new LinearLayout.LayoutParams(-1,76));
        if(!MediaAccessService.hasAccess(a)){LinearLayout p=Ui.col(a);p.setPadding(26,30,26,30);p.setBackground(Ui.bg(Ui.CARD,Ui.CARD_STROKE,22,a));p.addView(Ui.text(a,"ONE-TIME MEDIA ACCESS",21,Color.WHITE,true));p.addView(Ui.text(a,"Android requires Notification Access to read and control the active MediaSession. Once enabled, TRX Launcher will reconnect automatically and will not ask again.",15,0xffb7bac0,false));Button b=Ui.button(a,"ENABLE TRX MEDIA CONTROLS",true);b.setOnClickListener(v->a.requestMediaAccess());p.addView(b,new LinearLayout.LayoutParams(-1,70));root.addView(p);return;}
        if(tab==0)now(root);else if(tab==1)queue(root);else if(tab==2)sources(root);else audio(root);}
    private void now(LinearLayout r){LinearLayout c=Ui.col(a);c.setPadding(28,26,28,26);c.setBackground(Ui.bg(Ui.CARD,Ui.CARD_STROKE,22,a));
        FrameLayout art=new FrameLayout(a);art.setBackground(Ui.bg(Ui.NAV_TILE_BG,Ui.CARD_STROKE,18,a));
        ImageView iv=new ImageView(a);iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if(MediaAccessService.albumArt!=null){iv.setImageBitmap(MediaAccessService.albumArt);}
        else{iv.setImageResource(R.drawable.music_art);}
        art.addView(iv,new FrameLayout.LayoutParams(-1,-1));
        c.addView(art,new LinearLayout.LayoutParams(-1,Ui.dp(a,280)));
        c.addView(Ui.text(a,MediaAccessService.playing?MediaAccessService.artist.toUpperCase():"NOTHING PLAYING",13,0xffaaaeb5,true));
        TextView title=Ui.text(a,MediaAccessService.playing?MediaAccessService.title:"Pick a track to get started",34,Color.WHITE,true);title.setPadding(0,Ui.dp(a,4),0,Ui.dp(a,14));c.addView(title);
        Button controls=Ui.button(a,MediaAccessService.playing?"❚❚  PAUSED":"▶  PLAY",true);controls.setOnClickListener(v->MediaAccessService.playPause());c.addView(controls,new LinearLayout.LayoutParams(-1,84));
        r.addView(c,new LinearLayout.LayoutParams(-1,Ui.dp(a,470)));}
    private void queue(LinearLayout r){List<MediaAccessService.QueueEntry> q=MediaAccessService.queue;if(q==null||q.isEmpty()){r.addView(Ui.text(a,"The active media app controls and publishes its queue.",18,Color.WHITE,false));return;}
        for(MediaAccessService.QueueEntry e:q){LinearLayout row=Ui.row(a);row.setPadding(12,10,12,10);row.setBackground(Ui.bg(Ui.CARD,Ui.CARD_STROKE,14,a));
            ImageView iv=new ImageView(a);iv.setScaleType(ImageView.ScaleType.CENTER_CROP);iv.setImageBitmap(e.art);iv.setBackground(Ui.bg(Ui.NAV_TILE_BG,Ui.CARD_STROKE,10,a));row.addView(iv,new LinearLayout.LayoutParams(Ui.dp(a,48),Ui.dp(a,48)));
            LinearLayout info=Ui.col(a);info.setPadding(14,0,0,0);TextView t=Ui.text(a,e.title,15,Color.WHITE,true);t.setMaxLines(1);t.setEllipsize(TextUtils.TruncateAt.END);info.addView(t);TextView ar=Ui.text(a,e.artist.isEmpty()?"—":e.artist,12,Ui.SUB_TEXT,false);ar.setMaxLines(1);ar.setEllipsize(TextUtils.TruncateAt.END);info.addView(ar);row.addView(info,new LinearLayout.LayoutParams(0,-1,1));
            r.addView(row,new LinearLayout.LayoutParams(-1,-2));Ui.margins(row,0,0,0,8);}}
    private void sources(LinearLayout r){Button y=Ui.button(a,"Open YouTube Music",true);y.setOnClickListener(v->{var i=a.getPackageManager().getLaunchIntentForPackage("com.google.android.apps.youtube.music");if(i!=null)a.startActivity(i);});r.addView(y,new LinearLayout.LayoutParams(-1,80));}
    private void audio(LinearLayout r){r.addView(Ui.text(a,"Audio output follows the connected Android device. Hardware volume remains available from the vehicle controls.",17,Color.WHITE,false));}
}

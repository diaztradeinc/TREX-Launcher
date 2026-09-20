package com.mdiaz.trxlauncher;
import android.content.*;import android.graphics.Color;import android.provider.Settings;import android.view.*;import android.widget.*;
final class SettingsScreen extends ScrollView{
    private final MainActivity a;
    private LinearLayout content;
    private String currentPanel=null;

    private static final String[][] MENU={
        {"Appearance","\u2696","Theme, wallpaper, brightness"},
        {"Navigation","\u27A4","Map style, voice, route prefs"},
        {"Media","\u266B","Source, output, auto-resume"},
        {"OBD-II / OBDLink MX+","\u2691","Connection, PIDs, logging"},
        {"Home & Work","\u2302","Saved destinations"},
        {"Quick Launch","\u26A1","Customize shortcut bar"},
        {"Audio","\u266A","Volume, EQ, sound modes"},
        {"Permissions","\u26D4","Manage app permissions"},
        {"Backup & Reset","\u21BB","Backup, restore, reset"},
        {"About","\u2139","Version, vehicle, support"},
    };

    SettingsScreen(MainActivity c){super(c);a=c;setFillViewport(true);setBackgroundColor(Ui.BG);build();}

    private void build(){
        content=Ui.col(a);content.setPadding(Ui.dp(a,24),Ui.dp(a,18),Ui.dp(a,24),Ui.dp(a,24));
        addView(content);
        showMenu();
    }

    private void showMenu(){
        currentPanel=null;
        content.removeAllViews();
        LinearLayout hdr=Ui.row(a);
        TextView title=Ui.text(a,"SETTINGS",30,Color.WHITE,true);
        hdr.addView(title,new LinearLayout.LayoutParams(0,-1,1));
        Button close=Ui.button(a,"\u2715",false);
        close.setOnClickListener(v->a.showPage(0));
        hdr.addView(close,new LinearLayout.LayoutParams(Ui.dp(a,54),Ui.dp(a,54)));
        content.addView(hdr);
        Ui.margins(hdr,0,0,0,16);

        for(String[] item:MENU){
            LinearLayout row=Ui.row(a);
            row.setPadding(Ui.dp(a,16),Ui.dp(a,14),Ui.dp(a,16),Ui.dp(a,14));
            row.setBackground(Ui.bg(Ui.CARD,Ui.CARD_STROKE,16,a));
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView icon=Ui.text(a,item[1],22,Ui.RED,true);
            icon.setGravity(Gravity.CENTER);
            row.addView(icon,new LinearLayout.LayoutParams(Ui.dp(a,40),-1));

            LinearLayout info=Ui.col(a);info.setPadding(Ui.dp(a,14),0,0,0);
            info.addView(Ui.text(a,item[0],15,Color.WHITE,true));
            info.addView(Ui.text(a,item[2],12,Ui.SUB_TEXT,false));
            row.addView(info,new LinearLayout.LayoutParams(0,-1,1));

            TextView arrow=Ui.text(a,"\u203A",18,Ui.DIM_TEXT,true);
            row.addView(arrow);

            final String name=item[0];
            row.setOnClickListener(v->openPanel(name));
            content.addView(row,new LinearLayout.LayoutParams(-1,-2));
            Ui.margins(row,0,0,0,8);
        }
    }

    private void openPanel(String name){
        currentPanel=name;
        content.removeAllViews();

        LinearLayout hdr=Ui.row(a);
        Button back=Ui.button(a,"\u2039 BACK",false);
        back.setOnClickListener(v->showMenu());
        hdr.addView(back,new LinearLayout.LayoutParams(Ui.dp(a,100),Ui.dp(a,54)));
        TextView pt=Ui.text(a,name,18,Color.WHITE,true);
        pt.setGravity(Gravity.CENTER);
        hdr.addView(pt,new LinearLayout.LayoutParams(0,-1,1));
        Button save=Ui.button(a,"SAVE",true);
        save.setOnClickListener(v->{a.getSharedPreferences("trx",0).edit().apply();showMenu();});
        hdr.addView(save,new LinearLayout.LayoutParams(Ui.dp(a,80),Ui.dp(a,54)));
        content.addView(hdr);
        Ui.margins(hdr,0,0,0,16);

        ScrollView body=new ScrollView(a);
        LinearLayout panel=Ui.col(a);
        body.addView(panel);
        content.addView(body,new LinearLayout.LayoutParams(-1,0,1));

        if(name.equals("Appearance"))appearance(panel);
        else if(name.equals("Navigation"))navigation(panel);
        else if(name.equals("Media"))media(panel);
        else if(name.equals("OBD-II / OBDLink MX+"))obd(panel);
        else if(name.equals("Home & Work"))homeWork(panel);
        else if(name.equals("Quick Launch"))quickLaunch(panel);
        else if(name.equals("Audio"))audio(panel);
        else if(name.equals("Permissions"))permissions(panel);
        else if(name.equals("Backup & Reset"))backup(panel);
        else if(name.equals("About"))about(panel);
    }

    private void appearance(LinearLayout p){
        android.content.SharedPreferences sp=a.getSharedPreferences("trx",0);
        LinearLayout themeCard=card(p,"THEME");
        LinearLayout swatches=Ui.row(a);swatches.setPadding(0,Ui.dp(a,8),0,0);
        for(int i=0;i<Ui.THEME_NAMES.length;i++){
            final int t=i;
            LinearLayout sw=Ui.col(a);sw.setGravity(Gravity.CENTER);
            sw.setPadding(Ui.dp(a,8),Ui.dp(a,10),Ui.dp(a,8),Ui.dp(a,10));
            int curTheme=sp.getInt("theme",0);
            sw.setBackground(Ui.bg(curTheme==t?Ui.RED:Ui.CARD2,curTheme==t?0:Ui.CARD_STROKE,14,a));
            TextView dot=Ui.text(a,"\u25CF",20,Ui.THEME_ACCENTS[t],true);
            dot.setGravity(Gravity.CENTER);
            sw.addView(dot);
            TextView nm=Ui.text(a,Ui.THEME_NAMES[i],10,Color.WHITE,true);
            nm.setGravity(Gravity.CENTER);
            sw.addView(nm);
            sw.setOnClickListener(v->{sp.edit().putInt("theme",t).apply();Ui.applyTheme(a);a.recreate();});
            swatches.addView(sw,new LinearLayout.LayoutParams(0,-2,1));
        }
        themeCard.addView(swatches);

        LinearLayout wpCard=card(p,"WALLPAPER");
        String[] wpNames={"TRX Hero","Carbon","Solid Dark","Desert"};
        LinearLayout wpRow=Ui.row(a);wpRow.setPadding(0,Ui.dp(a,8),0,0);
        for(int i=0;i<wpNames.length;i++){
            final int w=i;
            int cur=sp.getInt("wallpaper",0);
            Button b=Ui.button(a,wpNames[i],cur==i);
            b.setOnClickListener(v->{sp.edit().putInt("wallpaper",w).apply();a.recreate();});
            wpRow.addView(b,new LinearLayout.LayoutParams(0,Ui.dp(a,48),1));
            if(i<wpNames.length-1)Ui.margins(b,0,0,6,0);
        }
        wpCard.addView(wpRow);

        LinearLayout brightCard=card(p,"BRIGHTNESS");
        boolean autoBright=sp.getBoolean("autoBright",true);
        toggleRow(brightCard,"Auto Brightness",autoBright,(b,checked)->{
            sp.edit().putBoolean("autoBright",checked).apply();
            openPanel("Appearance");
        });
        if(!autoBright){
            android.widget.SeekBar sb=new android.widget.SeekBar(a);
            sb.setMax(100);sb.setProgress(sp.getInt("brightness",80));
            sb.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener(){
                public void onProgressChanged(android.widget.SeekBar s,int val,boolean f){sp.edit().putInt("brightness",val).apply();}
                public void onStartTrackingTouch(android.widget.SeekBar s){}
                public void onStopTrackingTouch(android.widget.SeekBar s){}
            });
            brightCard.addView(sb,new LinearLayout.LayoutParams(-1,Ui.dp(a,44)));
            Ui.margins(sb,0,8,0,0);
        }
    }

    private void navigation(LinearLayout p){
        android.content.SharedPreferences sp=a.getSharedPreferences("trx",0);
        chipCard(p,"MAP TYPE",new String[]{"Default","Satellite","Terrain","Dark"},sp.getInt("navMapStyle",0),v->sp.edit().putInt("navMapStyle",(int)v.getTag()).apply());
        chipCard(p,"ROUTE PREF",new String[]{"Fastest","Shortest","No Tolls","No Highways"},sp.getInt("navRoutePref",0),v->sp.edit().putInt("navRoutePref",(int)v.getTag()).apply());
        LinearLayout optCard=card(p,"OPTIONS");
        toggleRow(optCard,"Voice Guidance",sp.getBoolean("navVoice",true),(b,checked)->sp.edit().putBoolean("navVoice",checked).apply());
        toggleRow(optCard,"Speed Limit Alerts",sp.getBoolean("navSpeedAlert",true),(b,checked)->sp.edit().putBoolean("navSpeedAlert",checked).apply());
        toggleRow(optCard,"Traffic Layer",sp.getBoolean("navTraffic",true),(b,checked)->sp.edit().putBoolean("navTraffic",checked).apply());
        toggleRow(optCard,"Night Mode",sp.getBoolean("navNight",true),(b,checked)->sp.edit().putBoolean("navNight",checked).apply());
        chipCard(p,"UNITS",new String[]{"Miles","Kilometers"},sp.getInt("navUnits",0),v->sp.edit().putInt("navUnits",(int)v.getTag()).apply());
    }

    private void media(LinearLayout p){
        android.content.SharedPreferences sp=a.getSharedPreferences("trx",0);
        chipCard(p,"DEFAULT SOURCE",new String[]{"YouTube Music","Spotify","Pandora","Radio"},sp.getInt("mediaSrc",0),v->sp.edit().putInt("mediaSrc",(int)v.getTag()).apply());
        chipCard(p,"OUTPUT",new String[]{"Vehicle","Bluetooth","Phone"},sp.getInt("mediaOut",0),v->sp.edit().putInt("mediaOut",(int)v.getTag()).apply());
        LinearLayout card=card(p,"OPTIONS");
        toggleRow(card,"Auto-resume on Connect",sp.getBoolean("mediaResume",true),(b,checked)->sp.edit().putBoolean("mediaResume",checked).apply());
    }

    private void obd(LinearLayout p){
        android.content.SharedPreferences sp=a.getSharedPreferences("trx",0);
        LinearLayout statusCard=card(p,"STATUS");
        TextView st=Ui.text(a,"\u2715 DISCONNECTED — Pair via Bluetooth",14,Ui.RED,true);
        st.setPadding(0,Ui.dp(a,8),0,0);
        statusCard.addView(st);
        Button pair=Ui.button(a,"OPEN BLUETOOTH SETTINGS",true);
        pair.setOnClickListener(v->{try{a.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));}catch(Throwable ignored){}});
        statusCard.addView(pair,new LinearLayout.LayoutParams(-1,Ui.dp(a,52)));
        Ui.margins(pair,0,10,0,0);
        chipCard(p,"UNITS",new String[]{"Imperial (\u00b0F, mph)","Metric (\u00b0C, km/h)"},sp.getInt("obdUnits",0),v->sp.edit().putInt("obdUnits",(int)v.getTag()).apply());
        LinearLayout optCard=card(p,"OPTIONS");
        toggleRow(optCard,"Data Logging",sp.getBoolean("obdLog",false),(b,checked)->sp.edit().putBoolean("obdLog",checked).apply());
        toggleRow(optCard,"Auto-reconnect",sp.getBoolean("obdRecon",true),(b,checked)->sp.edit().putBoolean("obdRecon",checked).apply());
        LinearLayout pidCard=card(p,"SUPPORTED PIDs");
        TextView pids=Ui.text(a,"RPM \u00B7 Coolant \u00B7 Intake \u00B7 Battery \u00B7 Engine Load \u00B7 Trans Temp \u00B7 Throttle \u00B7 MAF",13,Ui.SUB_TEXT,false);
        pids.setPadding(0,Ui.dp(a,8),0,0);
        pidCard.addView(pids);
    }

    private void homeWork(LinearLayout p){
        android.content.SharedPreferences sp=a.getSharedPreferences("trx",0);
        LinearLayout hc=card(p,"HOME ADDRESS");
        EditText home=new EditText(a);home.setText(sp.getString("homeAddr","123 Main St, Plainsboro, NJ"));
        home.setTextColor(Color.WHITE);home.setHintTextColor(Ui.SUB_TEXT);
        home.setBackground(Ui.bg(Ui.CARD2,Ui.CARD_STROKE,12,a));home.setPadding(Ui.dp(a,16),0,Ui.dp(a,16),0);
        hc.addView(home,new LinearLayout.LayoutParams(-1,Ui.dp(a,52)));
        Ui.margins(home,0,8,0,0);

        LinearLayout wc=card(p,"WORK ADDRESS");
        EditText work=new EditText(a);work.setText(sp.getString("workAddr","500 Tech Way, Princeton, NJ"));
        work.setTextColor(Color.WHITE);work.setHintTextColor(Ui.SUB_TEXT);
        work.setBackground(Ui.bg(Ui.CARD2,Ui.CARD_STROKE,12,a));work.setPadding(Ui.dp(a,16),0,Ui.dp(a,16),0);
        wc.addView(work,new LinearLayout.LayoutParams(-1,Ui.dp(a,52)));
        Ui.margins(work,0,8,0,0);

        Button save=Ui.button(a,"SAVE ADDRESSES",true);
        save.setOnClickListener(v->{
            sp.edit().putString("homeAddr",home.getText().toString()).putString("workAddr",work.getText().toString()).apply();
            android.widget.Toast.makeText(a,"Addresses saved",android.widget.Toast.LENGTH_SHORT).show();
        });
        p.addView(save,new LinearLayout.LayoutParams(-1,Ui.dp(a,52)));
        Ui.margins(save,0,12,0,0);
    }

    private void quickLaunch(LinearLayout p){
        android.content.SharedPreferences sp=a.getSharedPreferences("trx",0);
        String[] all={"Navigation","Media","Phone","Performance","Weather","Camera","Settings","Apps"};
        LinearLayout card=card(p,"SHORTCUT BAR");
        for(String name:all){
            boolean on=sp.getBoolean("ql_"+name,name.equals("Navigation")||name.equals("Media")||name.equals("Phone")||name.equals("Performance"));
            LinearLayout row=Ui.row(a);row.setPadding(0,Ui.dp(a,10),0,Ui.dp(a,10));
            row.addView(Ui.text(a,name,14,Color.WHITE,false),new LinearLayout.LayoutParams(0,-1,1));
            android.widget.ToggleButton tg=new android.widget.ToggleButton(a);
            tg.setTextOn("ON");tg.setTextOff("OFF");tg.setText(on?"ON":"OFF");
            tg.setChecked(on);tg.setTextColor(Color.WHITE);tg.setTextSize(11);
            tg.setBackground(Ui.bg(on?Ui.RED:Ui.CARD2,on?0:Ui.CARD_STROKE,10,a));
            tg.setPadding(Ui.dp(a,16),0,Ui.dp(a,16),0);
            tg.setOnCheckedChangeListener((b,val)->sp.edit().putBoolean("ql_"+name,val).apply());
            row.addView(tg,new LinearLayout.LayoutParams(Ui.dp(a,72),Ui.dp(a,36)));
            card.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
    }

    private void audio(LinearLayout p){
        android.content.SharedPreferences sp=a.getSharedPreferences("trx",0);
        LinearLayout volCard=card(p,"VOLUME");
        SeekBarWithLabel vol=new SeekBarWithLabel(a,"VOL",0,100,sp.getInt("vol",45),v->sp.edit().putInt("vol",v).apply());
        volCard.addView(vol);

        LinearLayout eqCard=card(p,"EQUALIZER");
        eqCard.addView(new SeekBarWithLabel(a,"BASS",0,10,sp.getInt("bass",5),v->sp.edit().putInt("bass",v).apply()));
        eqCard.addView(new SeekBarWithLabel(a,"TREBLE",0,10,sp.getInt("treble",3),v->sp.edit().putInt("treble",v).apply()));
        eqCard.addView(new SeekBarWithLabel(a,"BAL",-10,10,sp.getInt("bal",0),v->sp.edit().putInt("bal",v).apply()));

        chipCard(p,"SOUND MODE",new String[]{"Auto","Surround","Stereo","Party"},sp.getInt("soundMode",0),v->sp.edit().putInt("soundMode",(int)v.getTag()).apply());
    }

    private void permissions(LinearLayout p){
        LinearLayout card=card(p,"GRANTED");
        String[][] perms={{"Location","GPS navigation, live traffic"},{"Bluetooth","OBDLink MX+ vehicle data"},{"Notifications","Media controls"},{"Phone","Hands-free calling"},{"Microphone","Voice search"}};
        for(String[] perm:perms){
            LinearLayout row=Ui.row(a);row.setPadding(0,Ui.dp(a,10),0,Ui.dp(a,10));
            row.addView(Ui.text(a,perm[0],14,Color.WHITE,true),new LinearLayout.LayoutParams(0,-1,1));
            row.addView(Ui.text(a,perm[1],11,Ui.SUB_TEXT,false),new LinearLayout.LayoutParams(0,-1,1));
            TextView badge=Ui.text(a,"GRANTED",11,Ui.GREEN_DOT,true);
            row.addView(badge);
            card.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
        Button btn=Ui.button(a,"OPEN ANDROID PERMISSIONS",false);
        btn.setOnClickListener(v->a.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+a.getPackageName()))));
        p.addView(btn,new LinearLayout.LayoutParams(-1,Ui.dp(a,52)));
        Ui.margins(btn,0,12,0,0);
    }

    private void backup(LinearLayout p){
        android.content.SharedPreferences sp=a.getSharedPreferences("trx",0);
        LinearLayout card=card(p,"BACKUP");
        toggleRow(card,"Auto-backup to Google Drive",sp.getBoolean("backup",false),(b,checked)->sp.edit().putBoolean("backup",checked).apply());
        Button bu=Ui.button(a,"BACK UP NOW",false);
        bu.setOnClickListener(v->android.widget.Toast.makeText(a,"Backup started",android.widget.Toast.LENGTH_SHORT).show());
        card.addView(bu,new LinearLayout.LayoutParams(-1,Ui.dp(a,52)));
        Ui.margins(bu,0,10,0,0);

        LinearLayout resetCard=card(p,"RESET");
        Button reset=Ui.button(a,"RESET TO DEFAULTS",true);
        reset.setOnClickListener(v->new android.app.AlertDialog.Builder(a)
            .setTitle("Reset TRX Launcher?")
            .setMessage("This clears launcher preferences, favorites and paired in-app choices. Android Bluetooth pairings are not removed.")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Reset",(d,w)->{sp.edit().clear().apply();a.recreate();})
            .show());
        resetCard.addView(reset,new LinearLayout.LayoutParams(-1,Ui.dp(a,52)));
    }

    private void about(LinearLayout p){
        LinearLayout card=card(p,"ABOUT");
        String[][] info={{"App","TRX Launcher"},{"Version","6.0 Redline"},{"Build","2026.09.20"},{"Vehicle","RAM 1500 TRX"},{"Display","Uconnect 5 (12\")"},{"Engine","6.2L SC V8"},{"Output","702 HP / 650 lb-ft"}};
        for(String[] row:info){
            LinearLayout r=Ui.row(a);r.setPadding(0,Ui.dp(a,8),0,Ui.dp(a,8));
            r.addView(Ui.text(a,row[0],13,Ui.SUB_TEXT,true),new LinearLayout.LayoutParams(0,-1,1));
            r.addView(Ui.text(a,row[1],13,Color.WHITE,false),new LinearLayout.LayoutParams(0,-1,1));
            card.addView(r,new LinearLayout.LayoutParams(-1,-2));
        }
    }

    private LinearLayout card(LinearLayout parent,String title){
        LinearLayout c=Ui.col(a);
        c.setPadding(Ui.dp(a,16),Ui.dp(a,14),Ui.dp(a,16),Ui.dp(a,14));
        c.setBackground(Ui.bg(Ui.CARD,Ui.CARD_STROKE,18,a));
        c.addView(Ui.text(a,title,11,Ui.SUB_TEXT,true));
        parent.addView(c,new LinearLayout.LayoutParams(-1,-2));
        Ui.margins(c,0,0,0,10);
        return c;
    }

    private void chipCard(LinearLayout parent,String title,String[] labels,int active,View.OnClickListener onClick){
        LinearLayout c=card(parent,title);
        LinearLayout row=Ui.row(a);row.setPadding(0,Ui.dp(a,8),0,0);
        for(int i=0;i<labels.length;i++){
            Button b=Ui.button(a,labels[i],i==active);
            b.setTag(i);b.setOnClickListener(onClick);
            row.addView(b,new LinearLayout.LayoutParams(0,Ui.dp(a,48),1));
            if(i<labels.length-1)Ui.margins(b,0,0,6,0);
        }
        c.addView(row);
    }

    private void toggleRow(LinearLayout parent,String label,boolean on,android.widget.ToggleButton.OnCheckedChangeListener listener){
        LinearLayout row=Ui.row(a);row.setPadding(0,Ui.dp(a,10),0,Ui.dp(a,10));
        row.addView(Ui.text(a,label,14,Color.WHITE,false),new LinearLayout.LayoutParams(0,-1,1));
        android.widget.ToggleButton tg=new android.widget.ToggleButton(a);
        tg.setTextOn("ON");tg.setTextOff("OFF");tg.setText(on?"ON":"OFF");
        tg.setChecked(on);tg.setTextColor(Color.WHITE);tg.setTextSize(11);
        tg.setBackground(Ui.bg(on?Ui.RED:Ui.CARD2,on?0:Ui.CARD_STROKE,10,a));
        tg.setPadding(Ui.dp(a,16),0,Ui.dp(a,16),0);
        tg.setOnCheckedChangeListener(listener);
        row.addView(tg,new LinearLayout.LayoutParams(Ui.dp(a,72),Ui.dp(a,36)));
        parent.addView(row,new LinearLayout.LayoutParams(-1,-2));
    }

    private final class SeekBarWithLabel extends LinearLayout{
        SeekBarWithLabel(MainActivity ctx,String label,int min,int max,int val,java.util.function.IntConsumer onChange){
            super(ctx);setOrientation(LinearLayout.HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setPadding(0,Ui.dp(a,8),0,Ui.dp(a,8));
            addView(Ui.text(a,label,12,Ui.SUB_TEXT,true),new LinearLayout.LayoutParams(Ui.dp(a,60),-1));
            android.widget.SeekBar sb=new android.widget.SeekBar(a);
            sb.setMax(max-min);sb.setProgress(val-min);
            sb.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener(){
                public void onProgressChanged(android.widget.SeekBar s,int v,boolean f){onChange.accept(v+min);}
                public void onStartTrackingTouch(android.widget.SeekBar s){}
                public void onStopTrackingTouch(android.widget.SeekBar s){}
            });
            addView(sb,new LinearLayout.LayoutParams(0,-1,1));
            addView(Ui.text(a,String.valueOf(val),12,Color.WHITE,true),new LinearLayout.LayoutParams(Ui.dp(a,36),-1));
        }
    }
}

package com.mdiaz.trxlauncher;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

final class Ui {
    static int BG=0xff050506, CARD=0xff111214, CARD2=0xff17191c;
    static int RED=0xffe1192d;
    static int ACCENT=0xffe1192d;
    static int HEADER_BG=0xff090a0c;
    static int HEADER_STROKE=0xff351116;
    static int NAV_BAR_BG=0xff090a0b;
    static int CARD_STROKE=0xff3d4148;
    static int SUB_TEXT=0xffa5a8ae;
    static int DIM_TEXT=0xff6b6e74;
    static int GREEN_DOT=0xff68d391;
    static int NAV_TILE_BG=0xff1a1c1f;
    static int GAUGE_BG=0xff1c1e22;
    static int DIVIDER=0xff2a2d33;

    static final String[] THEME_NAMES={"Red","Baja","Stealth","Blue","Orange"};
    static final int[] THEME_ACCENTS={0xffe1192d,0xffd99a2b,0xffaeb6c0,0xff2b7de1,0xffe16a1f};

    static final int[][] THEME_PALETTE={
        {0xff050506,0xff111214,0xff17191c,0xffe1192d,0xff090a0c,0xff351116,0xff090a0b,0xff3d4148,0xff1a1c1f,0xff1c1e22},
        {0xff0a0805,0xff1a1510,0xff221c14,0xffd99a2b,0xff0c0905,0xff3d2e10,0xff0a0805,0xff4a3d28,0xff1c1810,0xff241e14},
        {0xff070809,0xff101214,0xff161819,0xffaeb6c0,0xff08090a,0xff2a2d33,0xff070809,0xff3a3d43,0xff121416,0xff1a1c1e},
        {0xff040608,0xff0e1218,0xff141820,0xff2b7de1,0xff050709,0xff0c2a52,0xff040608,0xff2a3d55,0xff0e1218,0xff101820},
        {0xff080503,0xff181210,0xff1e1614,0xffe16a1f,0xff0a0604,0xff3d1e0a,0xff080503,0xff4a2e1d,0xff181210,0xff1e1614},
    };

    static final int[] HERO_IMAGES={R.drawable.hero_red,R.drawable.theme_baja_bg,R.drawable.theme_stealth_bg,R.drawable.theme_blue_bg,R.drawable.theme_orange_bg};
    static final int[] WALLPAPER_IMAGES={R.drawable.hero_red,R.drawable.carbon_bg,0,R.drawable.theme_baja_bg};

    static void applyTheme(Context c){
        int t=c.getSharedPreferences("trx",0).getInt("theme",0);
        if(t<0||t>=THEME_PALETTE.length)t=0;
        int[] p=THEME_PALETTE[t];
        BG=p[0];CARD=p[1];CARD2=p[2];RED=p[3];ACCENT=p[3];HEADER_BG=p[4];HEADER_STROKE=p[5];
        NAV_BAR_BG=p[6];CARD_STROKE=p[7];NAV_TILE_BG=p[8];GAUGE_BG=p[9];
    }

    static int dp(Context c,int v){return Math.round(v*c.getResources().getDisplayMetrics().density);}
    static GradientDrawable bg(int color,int stroke,int radius,Context c){
        GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(c,radius));
        if(stroke!=0)g.setStroke(dp(c,1),stroke);return g;
    }
    static TextView text(Context c,String s,float size,int color,boolean bold){
        TextView v=new TextView(c);v.setText(s);v.setTextSize(size);v.setTextColor(color);
        v.setGravity(Gravity.CENTER_VERTICAL);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;
    }
    static Button button(Context c,String s,boolean selected){
        Button b=new Button(c);b.setText(s);b.setTextSize(13);b.setTextColor(Color.WHITE);b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackground(bg(selected?RED:CARD2,selected?0:0xff3d4148,14,c));return b;
    }
    static LinearLayout row(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    static LinearLayout col(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);return l;}
    static LinearLayout tabs(Context c,String[] names,int active,java.util.function.IntConsumer onSelect){
        LinearLayout bar=row(c);bar.setPadding(dp(c,6),dp(c,6),dp(c,6),dp(c,6));bar.setBackground(bg(CARD,0xff383b40,18,c));
        for(int i=0;i<names.length;i++){final int index=i;Button b=button(c,names[i],i==active);bar.addView(b,new LinearLayout.LayoutParams(0,dp(c,58),1));b.setOnClickListener(v->onSelect.accept(index));}
        return bar;
    }
    static LinearLayout metric(Context c,String label,String value,String unit){
        LinearLayout box=col(c);box.setPadding(dp(c,18),dp(c,14),dp(c,18),dp(c,14));box.setBackground(bg(CARD,0xff383b40,18,c));
        box.addView(text(c,label,11,0xff9ea2a9,true));LinearLayout line=row(c);line.addView(text(c,value,30,Color.WHITE,false));line.addView(text(c,"  "+unit,12,0xffaaaeb5,true));box.addView(line);return box;
    }
    static void margins(View v,int l,int t,int r,int b){if(v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams m)m.setMargins(dp(v.getContext(),l),dp(v.getContext(),t),dp(v.getContext(),r),dp(v.getContext(),b));}
    private Ui(){}
}

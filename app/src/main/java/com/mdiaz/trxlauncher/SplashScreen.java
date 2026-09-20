package com.mdiaz.trxlauncher;
import android.content.Intent;import android.graphics.Color;import android.graphics.drawable.GradientDrawable;import android.os.Handler;import android.os.Looper;import android.provider.Settings;import android.view.View;import android.view.animation.AlphaAnimation;import android.view.animation.Animation;import android.view.animation.LinearInterpolator;import android.view.animation.TranslateAnimation;import android.widget.*;
final class SplashScreen extends ScrollView{
 SplashScreen(MainActivity a){super(a);setFillViewport(true);setBackgroundColor(Ui.BG);

  FrameLayout root=new FrameLayout(a);addView(root,new FrameLayout.LayoutParams(-1,-1));

  ImageView bg=new ImageView(a);bg.setImageResource(R.drawable.splash_hero);bg.setScaleType(ImageView.ScaleType.CENTER_CROP);
  GradientDrawable dim=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0x00000000,0x00000000,0xCC050506,0xFF050506});
  bg.setForeground(dim);
  root.addView(bg,new FrameLayout.LayoutParams(-1,-1));

  TranslateAnimation slideBg=new TranslateAnimation(0,0,0,-60);
  slideBg.setDuration(8000);slideBg.setRepeatCount(Animation.INFINITE);slideBg.setRepeatMode(Animation.REVERSE);
  slideBg.setInterpolator(new LinearInterpolator());
  bg.startAnimation(slideBg);

  LinearLayout content=Ui.col(a);content.setGravity(android.view.Gravity.BOTTOM|android.view.Gravity.CENTER_HORIZONTAL);
  content.setPadding(Ui.dp(a,32),0,Ui.dp(a,32),Ui.dp(a,40));
  root.addView(content,new FrameLayout.LayoutParams(-1,-1));

  TextView ram=Ui.text(a,"RAM",64,Ui.RED,true);ram.setGravity(android.view.Gravity.CENTER);
  AlphaAnimation fadeIn=new AlphaAnimation(0,1);fadeIn.setDuration(800);fadeIn.setStartOffset(200);
  ram.startAnimation(fadeIn);content.addView(ram);

  TextView trx=Ui.text(a,"TRX LAUNCHER",26,Color.WHITE,true);trx.setGravity(android.view.Gravity.CENTER);
  AlphaAnimation fadeIn2=new AlphaAnimation(0,1);fadeIn2.setDuration(800);fadeIn2.setStartOffset(600);
  trx.startAnimation(fadeIn2);content.addView(trx);

  TextView sub=Ui.text(a,"Command your truck",16,Ui.SUB_TEXT,false);sub.setGravity(android.view.Gravity.CENTER);
  AlphaAnimation fadeIn3=new AlphaAnimation(0,1);fadeIn3.setDuration(800);fadeIn3.setStartOffset(1000);
  sub.startAnimation(fadeIn3);content.addView(sub);

  Space s=new Space(a);content.addView(s,new LinearLayout.LayoutParams(1,Ui.dp(a,24)));

  content.addView(perm(a,"Location","GPS navigation, live traffic, and 0-60 timing.",a.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)==android.content.pm.PackageManager.PERMISSION_GRANTED));
  content.addView(perm(a,"Bluetooth","Connect to OBDLink MX+ for live vehicle data.",a.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT)==android.content.pm.PackageManager.PERMISSION_GRANTED));
  content.addView(perm(a,"Media Access","Read and control the active music app.",MediaAccessService.hasAccess(a)));

  Button setDefault=Ui.button(a,"SET AS DEFAULT LAUNCHER",true);setDefault.setOnClickListener(v->{try{a.startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));}catch(Throwable ignored){}});
  content.addView(setDefault,new LinearLayout.LayoutParams(-1,Ui.dp(a,64)));Ui.margins(setDefault,0,24,0,0);

  Button cont=Ui.button(a,"CONTINUE",false);cont.setOnClickListener(v->{a.getSharedPreferences("trx",0).edit().putBoolean("setup_done",true).apply();a.recreate();});
  content.addView(cont,new LinearLayout.LayoutParams(-1,Ui.dp(a,64)));Ui.margins(cont,0,10,0,0);

  ProgressBar bar=new ProgressBar(a,null,android.R.attr.progressBarStyleHorizontal);
  bar.setMax(100);bar.setProgress(0);
  GradientDrawable barBg=new GradientDrawable();barBg.setColor(0x00000000);barBg.setCornerRadius(Ui.dp(a,3));
  bar.setBackground(barBg);bar.getProgressDrawable().setColorFilter(Ui.RED,android.graphics.PorterDuff.Mode.SRC_IN);
  content.addView(bar,new LinearLayout.LayoutParams(Ui.dp(a,180),Ui.dp(a,4)));Ui.margins(bar,0,20,0,0);

  final Handler h=new Handler(Looper.getMainLooper());
  h.postDelayed(new Runnable(){int p=0;public void run(){p+=2;bar.setProgress(Math.min(p,100));if(p<100)h.postDelayed(this,40);}},1200);
 }
 private View perm(MainActivity a,String title,String desc,boolean granted){LinearLayout c=Ui.col(a);c.setPadding(20,16,20,16);c.setBackground(Ui.bg(Ui.CARD,Ui.CARD_STROKE,18,a));c.addView(Ui.text(a,title+(granted?"   •   READY":""),15,granted?Ui.GREEN_DOT:Color.WHITE,true));c.addView(Ui.text(a,desc,13,Ui.SUB_TEXT,false));Ui.margins(c,0,0,0,10);return c;}
}

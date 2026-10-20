package com.vivascapes.wallpaper;
import android.service.wallpaper.WallpaperService;
import android.view.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.hardware.*;
import java.util.*;
public class VivaWallpaper extends WallpaperService {
 @Override public Engine onCreateEngine(){return new World();}
 class World extends Engine implements SensorEventListener {
  Handler h=new Handler(Looper.getMainLooper());
  Paint p=new Paint(3); Random rng=new Random(19);
  float[][] stars=new float[90][3], sparks=new float[28][4];
  int w=1,ht=1; boolean visible=false; long tick=0;float shift=0, accel=0;
  SharedPreferences pref=getSharedPreferences("viva",0);
  SensorManager sm=(SensorManager)getSystemService(SENSOR_SERVICE);
  Sensor sensor=sm==null?null:sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
  Runnable update=()->draw();
  World(){for(int i=0;i<stars.length;i++){stars[i]=new float[]{rng.nextFloat(),rng.nextFloat()*.6f,rng.nextFloat()*7f};}}
  void paint(Canvas c,int color){c.drawColor(color);}
  void rect(Canvas c,float a,float b,float d,float e,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRect(a,b,d,e,p);}
  void oval(Canvas c,float x,float y,float rx,float ry,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawOval(x-rx,y-ry,x+rx,y+ry,p);}
  void line(Canvas c,float x,float y,float xx,float yy,int color,float sw){p.setColor(color);p.setStrokeWidth(sw);c.drawLine(x,y,xx,yy,p);}
  int col(int a,int b,float f){return Color.rgb((int)(Color.red(a)*(1-f)+Color.red(b)*f),(int)(Color.green(a)*(1-f)+Color.green(b)*f),(int)(Color.blue(a)*(1-f)+Color.blue(b)*f));}
  @Override public void onSurfaceChanged(SurfaceHolder sh,int format,int width,int height){super.onSurfaceChanged(sh,format,width,height);w=width;ht=height;draw();}
  @Override public void onVisibilityChanged(boolean v){visible=v;h.removeCallbacks(update);if(v){if(sm!=null&&sensor!=null)sm.registerListener(this,sensor,SensorManager.SENSOR_DELAY_UI);draw();}else if(sm!=null)sm.unregisterListener(this);}
  @Override public void onDestroy(){visible=false;h.removeCallbacks(update);if(sm!=null)sm.unregisterListener(this);super.onDestroy();}
  @Override public void onTouchEvent(MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){for(int i=0;i<sparks.length;i++){float a=(float)(i*Math.PI*2/sparks.length);sparks[i]=new float[]{e.getX()/w,e.getY()/ht,(float)Math.cos(a), (float)Math.sin(a)};}tick=SystemClock.uptimeMillis();}super.onTouchEvent(e);}
  @Override public void onSensorChanged(SensorEvent e){if(pref.getBoolean("tilt",true))accel=accel*.91f+(e.values[0]/10)*.09f;}
  @Override public void onAccuracyChanged(Sensor s,int a){}
  void draw(){
   h.removeCallbacks(update);if(!visible||!getSurfaceHolder().getSurface().isValid())return;
   Canvas c=null;try{c=getSurfaceHolder().lockCanvas();if(c!=null)render(c);}catch(Exception ignored){}finally{if(c!=null)try{getSurfaceHolder().unlockCanvasAndPost(c);}catch(Exception ignored){}}
   if(visible)h.postDelayed(update,45);
  }
  void render(Canvas c){
   long now=SystemClock.uptimeMillis();float t=now/1000f;
   Calendar cal=Calendar.getInstance();float hour=cal.get(Calendar.HOUR_OF_DAY)+cal.get(Calendar.MINUTE)/60f;
   int mode=pref.getInt("mode",0);int scene=pref.getInt("scene",0);
   float night=mode==3?1:mode==1?0:mode==2?.45f:hour<6||hour>=20?1:hour>=7&&hour<17?0:hour<7?(7-hour):hour<20?(hour-17)/3f:1;
   int sky=col(0xff65b8e3,scene==1?0xff07142d:0xff170c32,night);
   c.drawColor(sky); c.save(); c.scale(w/400f,ht/850f);
   float par=accel*8; c.translate(par,0);
   for(int y=0;y<510;y+=6){float f=y/510f;rect(c,0,y,400,y+6,col(sky,col(0xffe1ac79,0xff18123d,night),f*.72f));}
   if(night>.1f){for(float[] s:stars){float glow=(float)(.6+.4*Math.sin(t*1.8+s[2]));oval(c,s[0]*400,s[1]*530,1.1f*glow*night,1.1f*glow*night,0xfff5e3cc);}oval(c,310,150,30,30,0xffe6ddcb);}else{oval(c,315,145,36,36,0xffffe1a0);}
   Path mountains=new Path();mountains.moveTo(0,410);mountains.lineTo(60,300);mountains.lineTo(105,365);mountains.lineTo(195,270);mountains.lineTo(267,370);mountains.lineTo(350,290);mountains.lineTo(400,345);mountains.lineTo(400,560);mountains.close();p.setColor(col(0xff7b99a5,0xff27344e,night));c.drawPath(mountains,p);
   int water=col(0xff377ea3,0xff0b234e,night);rect(c,0,478,400,850,water);
   for(int i=0;i<105;i++){float x=(i*117%415)-12, y=493+(i*53%356);float wave=(float)Math.sin(t*1.8+i*1.7);int shimmer=col(0xff75d4e0,0xffffc067,night*.65f);line(c,x+wave*4,y,x+15+wave*4,y,shimmer,i%5==0?1.5f:.7f);}
   int stone=col(0xffc6a28a,0xff62536b,night),roof=col(0xffa85c42,0xff4e284e,night);
   // Distant illuminated cliffside houses
   for(int i=0;i<18;i++){float x=200+i*12,y=450-(float)Math.sin(i*.34)*24-(i%5)*7;rect(c,x,y,x+20,492,stone);rect(c,x-3,y-9,x+23,y,roof);if(night>.24f)oval(c,x+10,y+17,2,4,0xffffc266);}
   // Main fantasy village
   for(int i=0;i<7;i++){float x=-36+i*38,y=295-i%3*26;rect(c,x,y,x+51,650,stone);rect(c,x-5,y-12,x+56,y,roof);
    for(int yy=(int)y+25;yy<625;yy+=42){for(int xx=(int)x+9;xx<x+47;xx+=22){rect(c,xx,yy,xx+10,yy+17,night>.22?0xffffc075:0xff526a82);}}
   }
   // Pointed tower
   rect(c,155,253,192,475,stone);Path peak=new Path();peak.moveTo(149,255);peak.lineTo(174,195);peak.lineTo(198,255);peak.close();p.setColor(roof);c.drawPath(peak,p);
   rect(c,166,293,180,321,night>.22?0xffffda91:0xff51739a);
   // Cobblestone path, warm lanterns, pumpkins
   Path walkway=new Path();walkway.moveTo(0,650);walkway.lineTo(185,598);walkway.lineTo(212,850);walkway.lineTo(0,850);walkway.close();p.setColor(col(0xffaea49b,0xff4b3a53,night));c.drawPath(walkway,p);
   for(int i=0;i<5;i++){float x=28+i*39,y=652+i*30;line(c,x,y,x,y+49,0xff342d3b,4);oval(c,x,y,6+night*3,10+night*3,night>.2?0xffffae42:0xffa78c66);}
   if(scene==0){for(int i=0;i<15;i++){float x=(i*47)%213,y=670+i*13;oval(c,x,y,9,8,0xffd6732d);rect(c,x-1,y-11,x+2,y-5,0xff43683b);if(night>.25){oval(c,x-3,y-1,1,2,0xffffea94);oval(c,x+3,y-1,1,2,0xffffea94);}}}
   for(int i=0;i<20;i++){float x=(i*43+t*(i%2==0?.5f:-.5f))%420;float y=(i*71+t*(i%3+1)*2)%800;oval(c,x,y,1.7f,1.7f,scene==0?0xffcf76fd:0xffffcb73);}
   float age=(now-tick)/1000f;if(age<1.5f){for(float[] s:sparks){float x=s[0]*400+s[2]*age*88, y=s[1]*850+s[3]*age*88;oval(c,x,y,2,2,0xffdfa7ff);}}
   c.restore();
  }
 }
}
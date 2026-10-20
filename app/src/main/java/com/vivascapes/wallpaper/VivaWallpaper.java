package com.vivascapes.wallpaper;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import java.io.File;
import java.util.Calendar;

/**
 * Battery-conscious image wallpaper. Actual illustrated image is the scene.
 * No animated vector landscape, sensors, touch effects, network or audio.
 */
public final class VivaWallpaper extends WallpaperService {
  @Override public Engine onCreateEngine(){return new Scene();}
  private final class Scene extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener{
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Paint imagePaint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final Paint shimmerPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final SharedPreferences prefs=getSharedPreferences(PackStore.PREFS,0);
    private final PowerManager power=(PowerManager)getSystemService(POWER_SERVICE);
    private final Runnable update=this::drawFrame;
    private boolean visible=false,closed=false;
    private int width=0,height=0;
    private Bitmap picture=null;
    private String loadedKey="";
    private int loadedPhase=-1;
    private long lastMillis=0;
    Scene(){prefs.registerOnSharedPreferenceChangeListener(this);}
    @Override public void onCreate(SurfaceHolder holder){
      super.onCreate(holder);setTouchEventsEnabled(false);setOffsetNotificationsEnabled(false);
    }
    @Override public void onSurfaceChanged(SurfaceHolder holder,int format,int w,int h){
      super.onSurfaceChanged(holder,format,w,h);width=w;height=h;refresh();
    }
    @Override public void onVisibilityChanged(boolean show){
      visible=show;handler.removeCallbacks(update);
      if(show)refresh();else release();
    }
    @Override public void onSharedPreferenceChanged(SharedPreferences s,String k){
      if(k!=null){release();refresh();}
    }
    @Override public void onSurfaceDestroyed(SurfaceHolder holder){
      handler.removeCallbacks(update);super.onSurfaceDestroyed(holder);
    }
    @Override public void onDestroy(){
      closed=true;visible=false;handler.removeCallbacks(update);
      prefs.unregisterOnSharedPreferenceChangeListener(this);release();super.onDestroy();
    }
    private void release(){if(picture!=null){picture.recycle();picture=null;}loadedKey="";loadedPhase=-1;}
    private void refresh(){handler.removeCallbacks(update);if(visible&&!closed&&width>0&&height>0)handler.post(update);}
    private int phase(){
      int mode=prefs.getInt(PackStore.MODE,0);if(mode>0)return mode-1;
      int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
      return hour>=7&&hour<17?0:hour>=17&&hour<20?1:2;
    }
    private void loadSceneIfNeeded(){
      int phase=phase();
      // Keep the decoded bitmap cached: no ZIP scans or bitmap metadata decoding per frame.
      if(picture!=null && loadedPhase==phase)return;
      PackStore.Pack pack=PackStore.selected(VivaWallpaper.this);
      if(pack==null){release();return;}
      String key=pack.id+":"+phase;
      if(picture!=null&&key.equals(loadedKey))return;
      File target=phase==0?pack.day:phase==1?pack.dusk:pack.night;
      BitmapFactory.Options bounds=new BitmapFactory.Options();
      bounds.inJustDecodeBounds=true;BitmapFactory.decodeFile(target.getAbsolutePath(),bounds);
      if(bounds.outWidth<1||bounds.outHeight<1)return;
      int sample=1;
      // Limit decoded pixel count for energy and memory, keeping phone display quality.
      while((long)(bounds.outWidth/sample)*(bounds.outHeight/sample)>3000000L&&sample<16)sample*=2;
      BitmapFactory.Options opts=new BitmapFactory.Options();
      opts.inSampleSize=sample;opts.inScaled=false;opts.inPreferredConfig=Bitmap.Config.RGB_565;
      Bitmap next=null;
      try{next=BitmapFactory.decodeFile(target.getAbsolutePath(),opts);}catch(OutOfMemoryError e){android.util.Log.w("VivaScapes","Out of memory while decoding",e);}
      if(next!=null){release();picture=next;loadedKey=key;loadedPhase=phase;}
    }
    private void drawFrame(){
      if(!visible||closed||width<=0||height<=0)return;
      try{loadSceneIfNeeded();}catch(Exception e){android.util.Log.w("VivaScapes","Image load error",e);}
      Canvas c=null;
      try{
        SurfaceHolder h=getSurfaceHolder();
        if(!h.getSurface().isValid())return;
        c=h.lockCanvas();
        if(c!=null){
          c.drawColor(0xff111120);
          if(picture!=null&&!picture.isRecycled()){
            float scale=Math.max(width/(float)picture.getWidth(),height/(float)picture.getHeight());
            float x=(width-picture.getWidth()*scale)*.5f;
            float y=(height-picture.getHeight()*scale)*.5f;
            c.drawBitmap(picture,null,new RectF(x,y,x+picture.getWidth()*scale,y+picture.getHeight()*scale),imagePaint);
            if(power==null||!power.isPowerSaveMode())drawSubtleWater(c);
          }
        }
      }catch(Exception e){android.util.Log.w("VivaScapes","Frame skipped",e);}
      finally{if(c!=null)try{getSurfaceHolder().unlockCanvasAndPost(c);}catch(Exception ignored){}}
      // ~2 FPS visible; in Android power-save mode render only every 60s for time-of-day changes.
      if(visible&&!closed)handler.postDelayed(update,(power!=null&&power.isPowerSaveMode())?60000:500);
    }
    private void drawSubtleWater(Canvas c){
      float t=SystemClock.uptimeMillis()/1000f;
      // A few translucent moving glints over the original lake illustration.
      // These are light shifts, not artificial stars, dots, boats or gameplay.
      c.save();c.clipRect(width*.47f,height*.37f,width*.95f,height*.72f);
      for(int i=0;i<7;i++){
        float f=i/7f;
        float y=height*(.42f+.037f*i);
        float x=width*(.58f+.13f*(float)Math.sin(t*.25f+i*1.27f));
        float r=width*(.014f+.007f*(float)Math.sin(t*.4f+i));
        int alpha=8+(int)(8*(.5f+.5f*(float)Math.sin(t*.55f+i)));
        shimmerPaint.setColor(Color.argb(alpha,248,226,177));
        shimmerPaint.setStrokeWidth(Math.max(1,width/650f));
        c.drawLine(x-r,y,x+r,y,shimmerPaint);
      }
      c.restore();
    }
  }
}
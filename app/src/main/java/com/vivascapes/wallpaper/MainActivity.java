package com.vivascapes.wallpaper;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
public class MainActivity extends Activity {
 SharedPreferences pref;
 int bg=Color.rgb(12,13,29), card=Color.rgb(29,28,55);
 TextView label(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextColor(color);v.setTextSize(size);v.setPadding(24,18,24,18);return v;}
 public void onCreate(Bundle b){super.onCreate(b);pref=getSharedPreferences("viva",0);show();}
 void show(){
 ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(bg);
 LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(16,28,16,22);scroll.addView(root);setContentView(scroll);
 TextView brand=label("✦ VivaScapes",32,Color.WHITE);brand.setTypeface(null,Typeface.BOLD);root.addView(brand);
 root.addView(label("Tu mundo, siempre vivo. Fondos que cambian con la hora de tu Samsung.",15,0xffbbb2d7));
 root.addView(label("ESCOGE TU MUNDO",14,0xffcfafff));
 String[] names={"🎃  Halloween Encantado","🌙  Lago de Luna","✨  Bosque Mágico"};
 for(int i=0;i<names.length;i++){final int k=i;Button btn=new Button(this);btn.setAllCaps(false);btn.setText(names[i]+(pref.getInt("scene",0)==i?"   ✓":""));btn.setTextColor(Color.WHITE);btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(pref.getInt("scene",0)==i?0xff7853bb:card));root.addView(btn,new LinearLayout.LayoutParams(-1,92));btn.setOnClickListener(v->{pref.edit().putInt("scene",k).apply();show();});}
 root.addView(label("CICLO DE LUZ",14,0xffcfafff));
 String[] modes={"Automático • reloj local","Día","Atardecer","Noche"};
 Spinner spinner=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,modes);spinner.setAdapter(adapter);spinner.setSelection(pref.getInt("mode",0));root.addView(spinner);
 spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){pref.edit().putInt("mode",pos).apply();}});
 Switch tilt=new Switch(this);tilt.setText("Profundidad al inclinar teléfono");tilt.setTextColor(Color.WHITE);tilt.setPadding(18,25,18,25);tilt.setChecked(pref.getBoolean("tilt",true));root.addView(tilt);tilt.setOnCheckedChangeListener((b,c)->pref.edit().putBoolean("tilt",c).apply());
 Button apply=new Button(this);apply.setAllCaps(false);apply.setText("✦  Activar fondo animado");apply.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff9462e5));apply.setTextColor(Color.WHITE);root.addView(apply,new LinearLayout.LayoutParams(-1,120));
 apply.setOnClickListener(v->{try {Intent intent=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(this,VivaWallpaper.class));startActivity(intent);}catch(Exception e){Toast.makeText(this,"No se pudo abrir fondos: "+e.getMessage(),Toast.LENGTH_LONG).show();}});
 root.addView(label("Versión inicial · Paisajes animados dibujados en Android. Próximamente importación de paquetes .vwp.",12,0xffa2a0b6));
 }
}
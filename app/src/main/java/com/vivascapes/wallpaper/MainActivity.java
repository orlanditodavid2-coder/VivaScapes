package com.vivascapes.wallpaper;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends Activity{
  private static final int OPEN_PACK=100;
  private final int bg=0xff111120,card=0xff242238,violet=0xff9d77e6;
  private LinearLayout page;
  private SharedPreferences prefs;
  private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
  private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
  private GradientDrawable shape(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(18));return d;}
  private Button button(String s,int color){
    Button b=new Button(this);b.setText(s);b.setTextSize(14);b.setAllCaps(false);b.setTextColor(-1);
    b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));return b;
  }
  private void addSpace(int h){View v=new View(this);page.addView(v,new LinearLayout.LayoutParams(1,dp(h)));}
  @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences(PackStore.PREFS,0);render();}
  @Override public void onResume(){super.onResume();if(prefs!=null)render();}
  private void render(){
    ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(bg);
    page=new LinearLayout(this);page.setOrientation(1);page.setPadding(dp(20),dp(24),dp(20),dp(40));scroll.addView(page);setContentView(scroll);
    TextView title=text("✦  VivaScapes",30,0xffffffff);title.setTypeface(null,1);page.addView(title);
    addSpace(8);page.addView(text("Fondos de verdad, con movimiento suave y mínimo consumo.",14,0xffc9c4d8));addSpace(25);
    PackStore.Pack selected=PackStore.selected(this);
    if(selected!=null){
      LinearLayout hero=new LinearLayout(this);hero.setOrientation(1);hero.setPadding(dp(12),dp(12),dp(12),dp(14));hero.setBackground(shape(card));
      ImageView preview=new ImageView(this);preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
      BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=4; o.inPreferredConfig=Bitmap.Config.RGB_565;
      java.io.File image=prefs.getInt(PackStore.MODE,0)==1?selected.day:prefs.getInt(PackStore.MODE,0)==2?selected.dusk:selected.night;
      Bitmap thumbnail=BitmapFactory.decodeFile(image.getAbsolutePath(),o);
      if(thumbnail!=null)preview.setImageBitmap(thumbnail);
      hero.addView(preview,new LinearLayout.LayoutParams(-1,dp(330)));
      TextView selectedName=text(selected.title,22,-1);selectedName.setPadding(dp(6),dp(12),0,0);hero.addView(selectedName);
      TextView detail=text(selected.description,13,0xffc4bbd2);detail.setPadding(dp(6),dp(4),0,0);hero.addView(detail);page.addView(hero);addSpace(14);
      Button activate=button("Activar como fondo de pantalla",0xff8455ce);
      page.addView(activate,new LinearLayout.LayoutParams(-1,dp(56)));
      activate.setOnClickListener(v->{Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(this,VivaWallpaper.class));
        try{startActivity(i);}catch(Exception e){Toast.makeText(this,"Abre Fondos de pantalla y busca VivaScapes",Toast.LENGTH_LONG).show();}
      });
    }else{
      TextView welcome=text("Tu colección está vacía.\n\nImporta el paquete .vwp de Halloween para activar la imagen original que creamos.",17,0xffdfd6f2);
      welcome.setPadding(dp(20),dp(22),dp(20),dp(22));welcome.setBackground(shape(card));page.addView(welcome);
    }
    addSpace(22);
    page.addView(text("Colección de fondos",19,-1));addSpace(10);
    List<PackStore.Pack> packs=PackStore.list(this);
    for(PackStore.Pack p:packs){
      Button tile=button((selected!=null&&selected.id.equals(p.id)?"✓   ":"") + p.title,selected!=null&&selected.id.equals(p.id)?0xff5c478d:0xff353044);
      page.addView(tile,new LinearLayout.LayoutParams(-1,dp(49)));
      tile.setOnClickListener(v->{PackStore.select(this,p.id);render();});
    }
    Button importButton=button("＋  Agregar un nuevo wallpaper (.vwp)",0xff5e4087);
    page.addView(importButton,new LinearLayout.LayoutParams(-1,dp(56)));
    importButton.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
      i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");
      i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivityForResult(i,OPEN_PACK);
    });
    addSpace(22);
    page.addView(text("Horario automático",19,-1));addSpace(7);
    page.addView(text("Tu fondo cambiará de día a tarde y noche con la hora del Samsung.",13,0xffc8bfd1));addSpace(8);
    String[] names={"Automático (recomendado)","Siempre de día","Siempre atardecer","Siempre de noche"};
    RadioGroup group=new RadioGroup(this);group.setOrientation(RadioGroup.VERTICAL);
    int current=prefs.getInt(PackStore.MODE,0);
    for(int n=0;n<4;n++){final int value=n;RadioButton radio=new RadioButton(this);
      radio.setText(names[n]);radio.setTextColor(-1);radio.setTextSize(14);radio.setPadding(dp(8),dp(10),0,dp(10));
      radio.setButtonTintList(android.content.res.ColorStateList.valueOf(0xffb895ff));
      radio.setChecked(current==n);group.addView(radio);radio.setOnClickListener(v->{prefs.edit().putInt(PackStore.MODE,value).apply();});
    }
    page.addView(group);addSpace(16);
    page.addView(text("Ahorro de batería",19,-1));
    addSpace(5);
    page.addView(text("Sin sensores, sin efectos táctiles y sin redibujar cuando la pantalla está apagada. Animación ambiental lenta y pausada en modo ahorro de energía.",13,0xffc9bfda));
    addSpace(20);
    page.addView(text("VivaScapes 1.1 · Samsung · Sin anuncios",12,0xff9790a6));
  }
  @Override protected void onActivityResult(int request,int result,Intent data){
    super.onActivityResult(request,result,data);
    if(request==OPEN_PACK&&result==RESULT_OK&&data!=null&&data.getData()!=null){
      try(InputStream stream=getContentResolver().openInputStream(data.getData())){
        PackStore.Pack pack=PackStore.importZip(this,stream);
        Toast.makeText(this,pack.title+" agregado correctamente",Toast.LENGTH_LONG).show();
        render();
      }catch(Exception e){Toast.makeText(this,"No se pudo importar: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }
  }
}
package com.vivascapes.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;

public final class PackStore {
  public static final String PREFS="viva", SELECTED="selected_pack", MODE="mode";
  private PackStore(){}
  public static class Pack {
    public final String id,title,description;
    public final File day,dusk,night;
    Pack(String id,JSONObject j,File dir)throws Exception{
      this.id=id;title=j.optString("title",id);
      description=j.optString("description","Paisaje vivo");
      day=image(dir,j.optString("day"));dusk=image(dir,j.optString("dusk"));night=image(dir,j.optString("night"));
    }
  }
  private static File image(File folder,String name)throws Exception{
    if(!name.matches("[A-Za-z0-9_-]{1,60}\\.(webp|png|jpg|jpeg)"))throw new IOException("Nombre de imagen incorrecto");
    File f=new File(folder,name);
    if(!f.isFile()||f.length()>12000000||f.length()==0)throw new IOException("No existe "+name);
    BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;
    BitmapFactory.decodeFile(f.getAbsolutePath(),o);
    if(o.outWidth<200||o.outHeight<400||o.outWidth>6000||o.outHeight>10000)throw new IOException("Imagen inválida");
    return f;
  }
  private static File root(Context c){File d=new File(c.getFilesDir(),"wallpapers");if(!d.exists())d.mkdirs();return d;}
  public static List<Pack> list(Context c){
    ArrayList<Pack> result=new ArrayList<>(); File[] dirs=root(c).listFiles();
    if(dirs!=null)for(File d:dirs)if(d.isDirectory()&&!d.getName().startsWith("_")){
      try{byte[] buf=java.nio.file.Files.readAllBytes(new File(d,"scene.json").toPath());
        if(buf.length<=16384)result.add(new Pack(d.getName(),new JSONObject(new String(buf,StandardCharsets.UTF_8)),d));
      }catch(Exception ignored){}
    }
    result.sort((a,b)->a.title.compareToIgnoreCase(b.title));return result;
  }
  public static Pack selected(Context c){
    String id=c.getSharedPreferences(PREFS,0).getString(SELECTED,"");
    for(Pack p:list(c))if(p.id.equals(id))return p;
    List<Pack> all=list(c);return all.isEmpty()?null:all.get(0);
  }
  public static void select(Context c,String id){
    c.getSharedPreferences(PREFS,0).edit().putString(SELECTED,id).apply();
  }
  public static Pack importZip(Context c,InputStream input)throws Exception{
    File base=root(c),temp=new File(base,"_temporary_"+System.nanoTime());
    if(!temp.mkdirs())throw new IOException("No se pudo crear carpeta");
    try{
      long total=0;int entries=0;
      try(ZipInputStream zip=new ZipInputStream(input)){
        ZipEntry entry;byte[] buf=new byte[16384];
        while((entry=zip.getNextEntry())!=null){
          if(entry.isDirectory())continue;
          String name=entry.getName();
          if(!name.equals("scene.json")&&!name.matches("[A-Za-z0-9_-]{1,60}\\.(webp|png|jpg|jpeg)"))
            throw new IOException("Archivo inesperado en el paquete");
          if(++entries>7)throw new IOException("Demasiados archivos");
          File f=new File(temp,name);if(f.exists())throw new IOException("Archivo duplicado");
          long individual=0;try(FileOutputStream out=new FileOutputStream(f)){
            int n;while((n=zip.read(buf))!=-1){total+=n;individual+=n;
              if(total>35000000||individual>12000000||(name.equals("scene.json")&&individual>16384))
                throw new IOException("Paquete demasiado grande");
              out.write(buf,0,n);
            }
          }
          zip.closeEntry();
        }
      }
      File manifest=new File(temp,"scene.json");
      if(!manifest.isFile())throw new IOException("Falta scene.json");
      JSONObject j=new JSONObject(new String(java.nio.file.Files.readAllBytes(manifest.toPath()),StandardCharsets.UTF_8));
      String id=j.optString("id","");
      if(!id.matches("[a-z0-9][a-z0-9_-]{2,40}")||!"vivascapes.pack".equals(j.optString("format"))||j.optInt("version")!=1)
        throw new IOException("Paquete incompatible");
      Pack validated=new Pack(id,j,temp);
      File target=new File(base,id),backup=new File(base,"_backup_"+System.nanoTime());
      if(target.exists()&&!target.renameTo(backup))throw new IOException("No se pudo actualizar el fondo");
      if(!temp.renameTo(target)){if(backup.exists())backup.renameTo(target);throw new IOException("No se pudo guardar el fondo");}
      delete(backup);
      select(c,id);
      return new Pack(id,j,target);
    }finally{delete(temp);}
  }
  private static void delete(File file){
    if(file.isDirectory()){File[] fs=file.listFiles();if(fs!=null)for(File f:fs)delete(f);}
    if(file.exists())file.delete();
  }
}
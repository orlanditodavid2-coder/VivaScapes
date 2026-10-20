# VivaScapes 1.1 – Samsung
Fondo de pantalla real con tus ilustraciones y animación de bajo consumo. 

## Instalar y usar
1. En [Actions](../../actions), descarga el artefacto `VivaScapes-Samsung-APK` del último build exitoso.
2. Extrae el archivo ZIP e instala `app-debug.apk` en tu Samsung.
3. Descarga una sola vez el paquete `VivaScapes_Halloween_Encantado.vwp` del chat de ChatGPT en el que creamos las ilustraciones (o `VivaScapes_Lago_de_Luna.vwp`).
4. En VivaScapes pulsa **Agregar un nuevo wallpaper (.vwp)**, elige el paquete en Descargas y luego **Activar como fondo de pantalla**.

Después puedes añadir todos los paquetes `.vwp` que creemos en el futuro sin volver a instalar la app. Los paquetes incluyen `scene.json`, `day.webp`, `dusk.webp` y `night.webp`.

## Ahorro de energía
- Solo **2 cuadros por segundo** cuando el wallpaper está visible, sin acelerómetro, sin toques, sin conexión de red.
- En modo ahorro de energía, 1 actualización por minuto y sin efectos.
- Al ocultarse el fondo, se cancela la animación y se libera la memoria de la imagen.
- Solo se decodifica la ilustración necesaria para la franja horaria actual.
- En automático: día 07:00–16:59, tarde 17:00–19:59 y noche 20:00–06:59, según hora local del dispositivo.

**Limitación actual:** el APK inicial no incluye los paquetes ilustrados porque se compilan por separado; deben importarse una sola vez. La versión 1.1 se debe probar en un Samsung real para validar consumo. 

# App Chofer (versión real, para Android) — cómo generar el .apk

## Antes que nada: por qué este paso es necesario

Yo puedo escribir todo el código de la app, pero **no puedo compilar el
archivo .apk final aquí mismo** — el servidor de Google donde vive el
"Android SDK" (la pieza necesaria para construir apps de Android) está
bloqueado en este entorno y no hay forma de evitarlo desde aquí.

Para resolverlo, dejé listo un sistema que hace esa compilación **automáticamente
y gratis** usando los servidores de GitHub (una plataforma gratuita y muy
usada, propiedad de Microsoft). Tú NO vas a programar ni usar la línea de
comandos — solo vas a **subir esta carpeta a un sitio web y esperar unos
minutos**. Son 4 pasos, uno de ellos es esperar.

## Paso 1 — Crea una cuenta gratis en GitHub

1. Ve a **https://github.com/signup**
2. Crea una cuenta gratuita (correo, usuario, contraseña).

Si ya tienes una cuenta de GitHub, sáltate este paso.

## Paso 2 — Crea un repositorio nuevo

1. Ya adentro de GitHub, ve a **https://github.com/new**
2. En "Repository name" pon algo como `app-chofer`.
3. Puedes dejarlo como **Private** (privado) — así nadie más lo ve.
4. NO marques ninguna casilla adicional ("Add a README", etc.).
5. Dale clic a **Create repository**.

## Paso 3 — Sube esta carpeta

1. En la página del repositorio recién creado, busca el link que dice
   **"uploading an existing file"** (o el botón **Add file → Upload files**).
2. En tu computadora, entra a la carpeta `app-chofer-nativa` (la que te
   compartí) y selecciona **TODO su contenido** (todos los archivos y
   carpetas de adentro, no la carpeta en sí) y arrástralos a la página de
   GitHub.
   - Nota: no vas a ver una carpeta llamada `node_modules` — no viene
     incluida a propósito (para que pese menos); GitHub la genera sola
     durante la compilación, es normal que no esté.
3. Abajo, dale clic a **Commit changes** (déjalo con el mensaje que ya
   trae por default).
4. Espera a que termine de subir (puede tardar uno o dos minutos según tu
   internet, son varios archivos).

## Paso 4 — Espera a que se compile y descarga el .apk

1. En la parte de arriba del repositorio, dale clic a la pestaña **Actions**.
2. Vas a ver una ejecución en curso (un círculo amarillo girando) llamada
   "Generar APK de App Chofer". Dale clic para entrar.
3. Espera unos 5–10 minutos. Cuando el círculo se ponga **verde con un ✔**,
   ya está listo.
4. Baja hasta la sección **Artifacts** (al final de esa misma página) y
   dale clic a **app-chofer-apk** para descargarlo — se baja un archivo
   `.zip`.
5. Descomprime ese `.zip`: adentro está el archivo **`app-debug.apk`**.
   Ese es el instalador de la app.

## Paso 5 — Instálalo en el celular del chofer

1. Manda el archivo `app-debug.apk` al celular del chofer (por WhatsApp,
   Google Drive, USB, como prefieras) y ábrelo desde el celular.
2. Android probablemente muestre un aviso de "Instalar apps desconocidas"
   la primera vez — es normal, es porque no viene de la tienda de Google
   Play (esto pasa con cualquier app instalada así). Actívalo solo para
   esta instalación y continúa.
3. Al abrir la app por primera vez, va a pedir permiso de ubicación —
   hay que aceptarlo (puede pedirlo en dos pasos: uno normal, y luego
   otro que dice algo como "Permitir todo el tiempo" — hay que aceptar
   ambos para que funcione correctamente en segundo plano).
4. A partir de ahí, la app funciona exactamente igual que la página web
   que ya usaba el chofer (mismo usuario, mismas rutas) — la diferencia es
   que ahora, cuando el chofer le da "Iniciar ruta", va a seguir mandando
   su ubicación aunque se le apague la pantalla o se cambie a WhatsApp o
   Waze. Vas a ver una notificación fija en su celular que dice
   "Compartiendo tu ubicación" mientras la ruta está en curso — es
   normal y es justo lo que Android exige mostrar para permitir que esto
   funcione (así trabajan también Uber, Didi, apps de reparto, etc.), no
   se puede quitar esa notificación sin apagar el rastreo.

## Cosas importantes que debes saber

- **Cada vez que yo actualice el código de esta app en el futuro**, te voy
  a mandar de nuevo esta misma carpeta actualizada. Solo repites el Paso 3
  (subir los archivos nuevos al mismo repositorio, sobrescribiendo) y el
  Paso 4 (esperar y descargar el nuevo .apk) — no hace falta repetir los
  pasos 1 y 2.
- Este primer .apk es una **versión de prueba** (se llama "debug" en
  Android). Funciona perfectamente para probarla con uno o dos choferes.
  Si más adelante quieres subirla oficialmente a la Google Play Store para
  que cualquier chofer la instale desde ahí sin este proceso, eso es un
  paso aparte (requiere una cuenta de desarrollador de Google, tiene un
  costo único de $25 USD) — dime si llegado el momento quieres que lo
  preparemos.
- Como no puedo instalar Android aquí, **no pude probar el .apk yo mismo
  antes de dártelo** — sí revisé el código a fondo comparándolo con la
  documentación oficial de Capacitor para minimizar errores, pero por
  favor pruébalo con un chofer de confianza antes de repartirlo a todos.
  Si algo no funciona como se espera, cuéntame exactamente qué pasó
  (o mándame captura de pantalla) y lo corrijo.
- La app para los **papás** (Portal de Padres) sigue siendo la versión
  web/PWA que ya tienen — no necesita este mismo proceso, porque el
  problema que resolvimos aquí (que se corte el rastreo si el celular se
  bloquea) es específico del lado del **chofer**, que es quien tiene que
  traer la app abierta todo el tiempo mientras maneja. Los papás nada más
  reciben la actualización, no la mandan.

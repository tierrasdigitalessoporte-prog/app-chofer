import type { CapacitorConfig } from '@capacitor/cli';

// La app no trae páginas propias: abre directamente la página real de
// "App Chofer" que ya vive en el hosting (transporte-escolar.tierras-
// digitales.com), como si fuera un navegador dedicado nada más para esa
// página. Así, cualquier cambio que se le haga a esa página en el
// servidor se ve en la app sin tener que volver a generar el .apk.
const config: CapacitorConfig = {
  appId: 'com.tierrasdigitales.transporteescolar.chofer',
  appName: 'App Chofer',
  webDir: 'www',
  server: {
    url: 'https://transporte-escolar.tierras-digitales.com/transporte-escolar/chofer/',
    cleartext: false
  }
};

export default config;

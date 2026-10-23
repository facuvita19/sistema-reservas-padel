import type { CapacitorConfig } from '@capacitor/cli';

const desarrollo = process.env.CAPACITOR_W2_DEV === '1';
const dispositivo = process.env.CAPACITOR_W2_DEVICE_HOST?.trim();
const servidorLocal = dispositivo ? `http://${dispositivo}:4321/w2/` : 'http://10.0.2.2:4321/w2/';
const config: CapacitorConfig = {
  appId: 'ar.com.padelasflores.w2',
  appName: 'Plataforma Deportiva W2',
  webDir: 'dist',
  backgroundColor: '#070809',
  loggingBehavior: desarrollo ? 'debug' : 'none',
  android: {
    backgroundColor: '#070809',
    allowMixedContent: false,
  },
  plugins: {
    SystemBars: { insetsHandling: 'disable' },
  },
  server: desarrollo ? {
    url: servidorLocal,
    cleartext: true,
    allowNavigation: [dispositivo || '10.0.2.2'],
  } : {
    hostname: 'localhost',
    androidScheme: 'https',
  },
};
export default config;

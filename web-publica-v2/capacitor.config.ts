import type { CapacitorConfig } from '@capacitor/cli';

const desarrollo = process.env.CAPACITOR_W2_DEV === '1';
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
  server: desarrollo ? {
    url: 'http://10.0.2.2:4321/w2/',
    cleartext: true,
    allowNavigation: ['10.0.2.2'],
  } : {
    hostname: 'localhost',
    androidScheme: 'https',
  },
};
export default config;

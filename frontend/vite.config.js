import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { viteSingleFile } from "vite-plugin-singlefile"
// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue(), viteSingleFile()],
  base: './',
  resolve: {
    alias: {
      '@': '/src',
    }
  },
  server: {
    port: 9090,
    proxy: {
      '/machine': {
        target: 'http://localhost:9084',
        changeOrigin: true,
        secure: false
      },
      '/api': {
        target: 'http://localhost:9084',
        changeOrigin: true,
        secure: false
      }
    },
  },
  build: {
    outDir: '../app/src/main/assets',
    assetsDir: 'static',
    emptyOutDir: true,
    target: 'es2015'
  }
})
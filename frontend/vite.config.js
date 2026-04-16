import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { viteSingleFile } from "vite-plugin-singlefile"
// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue(), viteSingleFile()],
  base: './',
  server: {
    port: 9090
  },
  build: {
    outDir: '../app/src/main/assets',
    assetsDir: 'static',
    emptyOutDir: true,
    target: 'es2015'
  }
})
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
        // target: 'http://localhost:9084',
        target: 'https://yjycs.uat2.bysjy.com.cn',
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
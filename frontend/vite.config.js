import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
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
      }
    },
  },
  build: {
    outDir: '../app/src/main/assets',
    assetsDir: 'static',
    emptyOutDir: true,
    target: 'es2015',
    minify: 'terser',
    rollupOptions: {
      output: {
        format: 'umd',
        name: 'AndroidOneApp'
      }
    }
  }
})

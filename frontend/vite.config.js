import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: 5173, // Chạy frontend ở port 3000
    allowedHosts: ['viperine-tearingly-brandee.ngrok-free.dev'],
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // Proxy API calls sang Spring Boot
        changeOrigin: true,
      }
    }
  }
})
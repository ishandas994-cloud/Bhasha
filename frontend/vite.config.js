// frontend/vite.config.js
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Proxies /api/* to the Spring Boot backend during development, so
    // runCode.js can just call fetch("/api/run") with a relative path
    // instead of hardcoding http://localhost:8080 - and it means CORS
    // only has to matter if you ever call the backend from a different
    // dev origin (WebConfig.java handles that case too, as a backup).
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
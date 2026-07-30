import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { VitePWA } from "vite-plugin-pwa";

const isDemoBuild = process.env.VITE_DEMO_BUILD === "true";

export default defineConfig({
  plugins: [
    react(),
    ...(!isDemoBuild
      ? [
          VitePWA({
            registerType: "autoUpdate",
            includeAssets: ["icon.svg", "manifest.json"],
            manifest: {
              name: "SGE - Sistema de Gestao Escolar",
              short_name: "SGE",
              description: "Gestao escolar: financeiro, academico, comunicacao e saude.",
              theme_color: "#0c2d57",
              background_color: "#f1f5f9",
              display: "standalone",
              start_url: "/",
              lang: "pt-BR",
              icons: [
                {
                  src: "/icon.svg",
                  sizes: "any",
                  type: "image/svg+xml",
                  purpose: "any",
                },
                {
                  src: "/icon.svg",
                  sizes: "512x512",
                  type: "image/svg+xml",
                  purpose: "maskable",
                },
              ],
            },
            workbox: {
              globPatterns: ["**/*.{js,css,html,ico,svg,woff2}"],
              navigateFallback: "/index.html",
            },
          }),
        ]
      : []),
  ],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: "dist",
    emptyOutDir: true,
  },
});

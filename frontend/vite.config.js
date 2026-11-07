import { defineConfig, loadEnv } from "vite";
import { fileURLToPath } from "node:url";
import react from "@vitejs/plugin-react";

const envDir = fileURLToPath(new URL("..", import.meta.url));

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, envDir, "");
  const target =
    env.GATEWAY_URL || `http://localhost:${env.GATEWAY_PORT || 8080}`;
  return {
    envDir,
    plugins: [react()],
    server: {
      port: Number(env.FRONTEND_PORT || 5173),
      strictPort: true,
      proxy: { "/api": { target, changeOrigin: true } },
    },
    preview: {
      port: Number(env.FRONTEND_PREVIEW_PORT || 4173),
      proxy: { "/api": { target, changeOrigin: true } },
    },
  };
});

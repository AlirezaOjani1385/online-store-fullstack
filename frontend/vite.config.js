import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Backend already whitelists http://localhost:5173 in its CORS config
// (see SecurityConfig.java), so we keep Vite on its default port.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
  },
});

import React from "react";
import ReactDOM from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import { router } from "./router";
import "./index.css";
import { useAuthStore } from "./store/authStore";

useAuthStore.getState().hydrateFromStorage();

if (import.meta.env.VITE_DEMO_BUILD === "true" && "serviceWorker" in navigator) {
  void navigator.serviceWorker.getRegistrations().then((registrations) => {
    registrations.forEach((registration) => void registration.unregister());
  });
  if ("caches" in window) {
    void caches.keys().then((keys) => keys.forEach((key) => void caches.delete(key)));
  }
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
);

/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        brand: {
          blue: "#0c2d57",
          "blue-deep": "#051830",
          "blue-mid": "#1a5088",
          "blue-bright": "#2d6cb5",
          yellow: "#f5d547",
          "yellow-bright": "#fff59d",
          "yellow-soft": "#fffde7",
          "yellow-poster": "#ffeb3b",
          "yellow-sun": "#fff176",
          "yellow-cream": "#fff9c4",
          "sky": "#42bff7",
          "sky-mid": "#28a7e8",
          "sky-deep": "#1590d1",
          "sky-light": "#7ad4fb",
        },
      },
    },
  },
  plugins: [],
};

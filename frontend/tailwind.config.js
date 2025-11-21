/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}", // Quét tất cả file trong thư mục src
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'sans-serif'], // Ưu tiên font Inter
      },
      animation: {
        'spin-slow': 'spin 3s linear infinite', // Animation xoay chậm nếu cần
      }
    },
  },
  plugins: [],
}
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        'SB': ['MBP','Helvetica Neue','Helvetica','Arial','sans']
      },
      colors: {
        'azukired':'#c03541',
      }
    },
  },
  plugins: [],
}

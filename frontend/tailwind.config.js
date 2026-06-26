/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#fdf4f3',
          100: '#fce8e6',
          200: '#f9d5d1',
          300: '#f3b5ad',
          400: '#ea8a7d',
          500: '#dc6356',
          600: '#c8473a',
          700: '#a8392f',
          800: '#8c332c',
          900: '#742f2a',
        },
        sage: {
          50: '#f4f7f4',
          100: '#e3ebe3',
          200: '#c8d7c9',
          300: '#a0bba2',
          400: '#759a78',
          500: '#567d59',
          600: '#426445',
          700: '#365039',
          800: '#2d4130',
          900: '#263628',
        },
      },
      fontFamily: {
        display: ['Georgia', 'serif'],
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
};

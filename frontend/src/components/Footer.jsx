export default function Footer() {
  return (
    <footer className="mt-auto border-t border-stone-200 bg-white py-8">
      <div className="mx-auto max-w-7xl px-4 text-center text-sm text-stone-500 sm:px-6 lg:px-8">
        <p className="font-display text-lg text-brand-700">Paint & Petals</p>
        <p className="mt-2">Artisan paints, art supplies & floral arrangements from independent creators.</p>
        <p className="mt-4">&copy; {new Date().getFullYear()} Paint and Petals. All rights reserved.</p>
      </div>
    </footer>
  );
}

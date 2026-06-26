import { Link } from 'react-router-dom';

export default function Home() {
  return (
    <div>
      <section className="relative overflow-hidden bg-gradient-to-br from-brand-50 via-white to-sage-50">
        <div className="mx-auto max-w-7xl px-4 py-24 sm:px-6 lg:px-8">
          <div className="max-w-2xl">
            <p className="text-sm font-semibold uppercase tracking-wider text-brand-600">Multi-Vendor Marketplace</p>
            <h1 className="mt-4 font-display text-5xl font-bold leading-tight text-stone-900 sm:text-6xl">
              Where artisan paints meet fresh petals
            </h1>
            <p className="mt-6 text-lg text-stone-600">
              Discover handcrafted paints, premium art supplies, and bespoke floral arrangements from independent artists and florists.
            </p>
            <div className="mt-8 flex flex-wrap gap-4">
              <Link to="/catalog" className="btn-primary">Browse Collection</Link>
              <Link to="/vendor/register" className="btn-secondary">Become a Vendor</Link>
            </div>
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <div className="grid gap-8 md:grid-cols-3">
          {[
            { title: 'Paints', desc: 'Hand-mixed pigments and artisan watercolor sets.', slug: 'paints', color: 'bg-brand-100' },
            { title: 'Petals', desc: 'Seasonal bouquets and custom floral arrangements.', slug: 'petals', color: 'bg-sage-100' },
            { title: 'Art Supplies', desc: 'Brushes, canvases, and studio essentials.', slug: 'art-supplies', color: 'bg-amber-100' },
          ].map((cat) => (
            <Link key={cat.slug} to={`/catalog?category=${cat.slug}`} className="card group transition hover:shadow-md">
              <div className={`mb-4 h-32 rounded-lg ${cat.color}`} />
              <h3 className="font-display text-xl font-semibold group-hover:text-brand-600">{cat.title}</h3>
              <p className="mt-2 text-sm text-stone-600">{cat.desc}</p>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
}

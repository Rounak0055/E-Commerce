import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import api from '../api/axios';

export default function Catalog() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);

  const category = searchParams.get('category') || '';
  const search = searchParams.get('search') || '';
  const sort = searchParams.get('sort') || 'newest';
  const page = searchParams.get('page') || '0';

  useEffect(() => {
    api.get('/categories').then((res) => setCategories(res.data));
  }, []);

  useEffect(() => {
    setLoading(true);
    api.get('/products', { params: { category, search, sort, page, size: 12 } })
      .then((res) => {
        setProducts(res.data.content || []);
        setTotalPages(res.data.totalPages || 0);
      })
      .finally(() => setLoading(false));
  }, [category, search, sort, page]);

  const updateParam = (key, value) => {
    const next = new URLSearchParams(searchParams);
    if (value) next.set(key, value);
    else next.delete(key);
    if (key !== 'page') next.set('page', '0');
    setSearchParams(next);
  };

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="flex flex-col gap-8 lg:flex-row">
        <aside className="lg:w-56">
          <h2 className="font-semibold">Categories</h2>
          <ul className="mt-3 space-y-2">
            <li>
              <button onClick={() => updateParam('category', '')} className={`text-sm ${!category ? 'font-medium text-brand-600' : 'text-stone-600'}`}>All</button>
            </li>
            {categories.map((c) => (
              <li key={c.id}>
                <button onClick={() => updateParam('category', c.slug)} className={`text-sm ${category === c.slug ? 'font-medium text-brand-600' : 'text-stone-600'}`}>{c.name}</button>
              </li>
            ))}
          </ul>
        </aside>

        <div className="flex-1">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <input
              type="search"
              placeholder="Search products..."
              defaultValue={search}
              onKeyDown={(e) => e.key === 'Enter' && updateParam('search', e.target.value)}
              className="input-field max-w-md"
            />
            <select value={sort} onChange={(e) => updateParam('sort', e.target.value)} className="input-field w-auto">
              <option value="newest">Newest</option>
              <option value="price_asc">Price: Low to High</option>
              <option value="price_desc">Price: High to Low</option>
              <option value="name">Name</option>
            </select>
          </div>

          {loading ? (
            <div className="mt-12 text-center text-stone-500">Loading...</div>
          ) : products.length === 0 ? (
            <div className="mt-12 text-center text-stone-500">No products found.</div>
          ) : (
            <div className="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {products.map((p) => (
                <Link key={p.id} to={`/products/${p.id}`} className="card group overflow-hidden p-0 transition hover:shadow-md">
                  <div className="aspect-square bg-stone-100">
                    {p.imageUrl ? <img src={p.imageUrl} alt={p.name} className="h-full w-full object-cover" /> : (
                      <div className="flex h-full items-center justify-center text-stone-400">{p.categoryName}</div>
                    )}
                  </div>
                  <div className="p-4">
                    <p className="text-xs uppercase text-stone-500">{p.vendorName}</p>
                    <h3 className="mt-1 font-medium group-hover:text-brand-600">{p.name}</h3>
                    <p className="mt-2 font-semibold text-brand-700">${Number(p.price).toFixed(2)}</p>
                  </div>
                </Link>
              ))}
            </div>
          )}

          {totalPages > 1 && (
            <div className="mt-8 flex justify-center gap-2">
              {Array.from({ length: totalPages }, (_, i) => (
                <button key={i} onClick={() => updateParam('page', String(i))} className={`rounded px-3 py-1 text-sm ${Number(page) === i ? 'bg-brand-600 text-white' : 'bg-stone-200'}`}>{i + 1}</button>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

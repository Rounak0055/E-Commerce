import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';

export default function ProductDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [message, setMessage] = useState('');

  useEffect(() => {
    api.get(`/products/${id}`).then((res) => setProduct(res.data));
  }, [id]);

  const addToCart = async () => {
    if (!user || user.role !== 'CUSTOMER') {
      navigate('/login');
      return;
    }
    try {
      await api.post('/cart/items', { productId: Number(id), quantity });
      setMessage('Added to cart!');
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed to add');
    }
  };

  if (!product) return <div className="py-20 text-center">Loading...</div>;

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="grid gap-8 lg:grid-cols-2">
        <div className="aspect-square rounded-xl bg-stone-100">
          {product.imageUrl && <img src={product.imageUrl} alt={product.name} className="h-full w-full rounded-xl object-cover" />}
        </div>
        <div>
          <p className="text-sm uppercase text-stone-500">{product.categoryName} · {product.vendorName}</p>
          <h1 className="mt-2 font-display text-3xl font-bold">{product.name}</h1>
          <p className="mt-4 text-2xl font-semibold text-brand-700">${Number(product.price).toFixed(2)}</p>
          <p className="mt-4 text-stone-600">{product.description}</p>
          <p className="mt-2 text-sm text-stone-500">{product.stockQuantity} in stock</p>

          <div className="mt-6 flex items-center gap-4">
            <input type="number" min={1} max={product.stockQuantity} value={quantity} onChange={(e) => setQuantity(Number(e.target.value))} className="input-field w-20" />
            <button onClick={addToCart} className="btn-primary" disabled={product.stockQuantity === 0}>Add to Cart</button>
          </div>
          {message && <p className="mt-4 text-sm text-sage-600">{message}</p>}
        </div>
      </div>
    </div>
  );
}

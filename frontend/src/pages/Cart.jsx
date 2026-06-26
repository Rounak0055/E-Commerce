import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';

export default function Cart() {
  const [cart, setCart] = useState(null);

  const loadCart = () => api.get('/cart').then((res) => setCart(res.data));

  useEffect(() => { loadCart(); }, []);

  const updateQty = async (itemId, quantity) => {
    const item = cart.items.find((i) => i.id === itemId);
    await api.put(`/cart/items/${itemId}`, { productId: item.productId, quantity });
    loadCart();
  };

  const removeItem = async (itemId) => {
    await api.delete(`/cart/items/${itemId}`);
    loadCart();
  };

  if (!cart) return <div className="py-20 text-center">Loading...</div>;

  return (
    <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
      <h1 className="font-display text-3xl font-bold">Your Cart</h1>

      {cart.items.length === 0 ? (
        <p className="mt-8 text-stone-600">Your cart is empty. <Link to="/catalog" className="text-brand-600">Continue shopping</Link></p>
      ) : (
        <>
          <div className="mt-8 space-y-4">
            {cart.items.map((item) => (
              <div key={item.id} className="card flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <p className="font-medium">{item.productName}</p>
                  <p className="text-sm text-stone-500">Sold by {item.vendorName}</p>
                  <p className="mt-1 text-brand-700">${Number(item.unitPrice).toFixed(2)} each</p>
                </div>
                <div className="flex items-center gap-3">
                  <input type="number" min={1} value={item.quantity} onChange={(e) => updateQty(item.id, Number(e.target.value))} className="input-field w-16" />
                  <p className="font-semibold">${Number(item.lineTotal).toFixed(2)}</p>
                  <button onClick={() => removeItem(item.id)} className="text-sm text-red-600">Remove</button>
                </div>
              </div>
            ))}
          </div>
          <div className="mt-8 flex items-center justify-between border-t pt-6">
            <p className="text-xl font-bold">Total: ${Number(cart.total).toFixed(2)}</p>
            <Link to="/checkout" className="btn-primary">Proceed to Checkout</Link>
          </div>
        </>
      )}
    </div>
  );
}

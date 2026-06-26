import { useEffect, useMemo, useState } from 'react';
import { Formik, Form, Field } from 'formik';
import api from '../../api/axios';

const NEXT_STATUS = {
  PLACED: 'PROCESSING',
  PROCESSING: 'SHIPPED',
  SHIPPED: 'DELIVERED',
};

const tabs = ['orders', 'analytics', 'inventory', 'profile', 'earnings'];

export default function VendorDashboard() {
  const [tab, setTab] = useState('orders');
  const [orders, setOrders] = useState([]);
  const [analytics, setAnalytics] = useState(null);
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [showProductForm, setShowProductForm] = useState(false);
  const [showCategoryComposer, setShowCategoryComposer] = useState(false);
  const [categoryDraft, setCategoryDraft] = useState('');
  const [categoryBusy, setCategoryBusy] = useState(false);
  const [earnings, setEarnings] = useState(null);
  const [profile, setProfile] = useState({ businessName: '', description: '', bannerUrl: '', logoUrl: '', bio: '' });
  const [editingProduct, setEditingProduct] = useState(null);
  const [dragActive, setDragActive] = useState(false);

  const load = () => {
    api.get('/vendor/orders').then((r) => setOrders(r.data));
    api.get('/vendor/analytics').then((r) => setAnalytics(r.data));
    api.get('/vendor/products').then((r) => setProducts(r.data));
    api.get('/vendor/earnings').then((r) => setEarnings(r.data));
    api.get('/vendor/profile').then((r) => setProfile(r.data));
    api.get('/categories').then((r) => setCategories(r.data));
  };

  useEffect(() => { load(); }, []);

  const chartMax = useMemo(() => {
    const values = analytics?.salesTrend?.map((d) => Number(d.amount)) ?? [];
    return Math.max(...values, 1);
  }, [analytics]);
  const activeOrders = useMemo(() => orders.filter((order) => !['DELIVERED', 'CANCELLED'].includes(order.status)), [orders]);
  const pastOrders = useMemo(() => orders.filter((order) => ['DELIVERED', 'CANCELLED'].includes(order.status)), [orders]);
  const completedTotal = useMemo(() => pastOrders.filter((order) => order.status === 'DELIVERED').reduce((sum, order) => sum + Number(order.subtotal || 0), 0), [pastOrders]);

  const updateStatus = async (id, status) => {
    await api.patch(`/vendor/orders/${id}/status`, { status });
    load();
  };

  const createCategory = async () => {
    if (!categoryDraft.trim()) return;
    setCategoryBusy(true);
    try {
      const { data } = await api.post('/categories', { name: categoryDraft.trim() });
      setCategories((prev) => [...prev, data]);
      setCategoryDraft('');
      setShowCategoryComposer(false);
      return data;
    } finally {
      setCategoryBusy(false);
    }
  };

  const saveProduct = async (values, { resetForm }) => {
    const payload = {
      ...values,
      price: Number(values.price),
      compareAtPrice: values.compareAtPrice ? Number(values.compareAtPrice) : null,
      stockQuantity: Number(values.stockQuantity),
      categoryId: Number(values.categoryId),
      active: values.active !== undefined ? values.active : true,
    };

    if (!payload.categoryId) {
      const createdCategory = await createCategory();
      if (createdCategory?.id) payload.categoryId = createdCategory.id;
    }

    if (editingProduct?.id) {
      await api.put(`/vendor/products/${editingProduct.id}`, payload);
    } else {
      await api.post('/vendor/products', payload);
    }

    resetForm();
    setEditingProduct(null);
    setShowProductForm(false);
    load();
  };

  const updateInventoryField = async (id, field, value) => {
    const product = products.find((item) => item.id === id);
    if (!product) return;
    const payload = {
      ...product,
      [field]: field === 'price' ? Number(value) : field === 'stockQuantity' ? Number(value) : value,
      price: field === 'price' ? Number(value) : Number(product.price),
      stockQuantity: field === 'stockQuantity' ? Number(value) : Number(product.stockQuantity),
      categoryId: Number(product.categoryId),
      active: field === 'active' ? value : product.active,
    };
    await api.put(`/vendor/products/${id}`, payload);
    load();
  };

  const archiveProduct = async (id) => {
    await api.delete(`/vendor/products/${id}`);
    load();
  };

  const saveProfile = async (values) => {
    await api.put('/vendor/profile', values);
    setProfile(values);
  };

  const onDrop = async (event) => {
    event.preventDefault();
    setDragActive(false);
    const file = event.dataTransfer.files?.[0];
    if (!file) return;
    setProfile((prev) => ({ ...prev, logoUrl: URL.createObjectURL(file) }));
  };

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="rounded-3xl border border-stone-200 bg-white p-8 shadow-sm">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.3em] text-brand-600">Paint & Petals</p>
            <h1 className="mt-2 font-display text-3xl font-bold text-stone-900">Vendor Portal</h1>
            <p className="mt-2 max-w-2xl text-sm text-stone-600">Manage fulfillment, inventory, storefront identity, and performance from one calm workspace.</p>
          </div>
          <div className="rounded-full border border-brand-100 bg-brand-50 px-4 py-2 text-sm font-medium text-brand-700">Live merchant workspace</div>
        </div>

        <div className="mt-6 flex flex-wrap gap-2 border-b border-stone-200">
          {tabs.map((t) => (
            <button key={t} onClick={() => setTab(t)} className={`px-4 py-2 text-sm font-medium capitalize ${tab === t ? 'border-b-2 border-brand-600 text-brand-600' : 'text-stone-600'}`}>{t}</button>
          ))}
        </div>
      </div>

      {tab === 'orders' && (
        <div className="mt-8 space-y-6">
          <section className="space-y-4">
            <div className="flex flex-wrap items-end justify-between gap-3">
              <div>
                <h2 className="text-xl font-semibold">Incoming Active Orders</h2>
                <p className="mt-1 text-sm text-stone-500">Orders still moving through fulfillment.</p>
              </div>
              <span className="rounded-full border border-stone-200 px-3 py-1 text-sm text-stone-600">{activeOrders.length} active</span>
            </div>
            {activeOrders.length === 0 ? <p className="text-stone-500">No orders to fulfill.</p> : activeOrders.map((o) => (
              <div key={o.vendorOrderId} className="card">
                <div className="flex flex-wrap justify-between gap-2">
                  <div>
                    <p className="font-semibold">Order #{o.orderGroupId} - {o.status}</p>
                    <p className="text-sm text-stone-500">{new Date(o.orderDate).toLocaleString()}</p>
                  </div>
                  {NEXT_STATUS[o.status] && (
                    <button onClick={() => updateStatus(o.vendorOrderId, NEXT_STATUS[o.status])} className="btn-primary text-xs">
                      Mark {NEXT_STATUS[o.status]}
                    </button>
                  )}
                </div>
                <div className="mt-4 grid gap-4 md:grid-cols-2">
                  <div>
                    <p className="text-sm font-medium">Items</p>
                    <ul className="mt-2 text-sm text-stone-600">
                      {o.items?.map((item, i) => <li key={i}>{item.productName} x {item.quantity}</li>)}
                    </ul>
                  </div>
                  <div>
                    <p className="text-sm font-medium">Delivery Address</p>
                    <p className="mt-2 text-sm text-stone-600">
                      {o.customerName}<br />
                      {o.shippingStreet}<br />
                      {o.shippingCity}, {o.shippingState} {o.shippingZip}<br />
                      {o.shippingCountry}<br />
                      Phone: {o.shippingPhone}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </section>

          <section className="card overflow-x-auto">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <h2 className="font-semibold">Past Fulfilled Orders</h2>
                <p className="mt-1 text-sm text-stone-500">Historic completions and closed fulfillment records.</p>
              </div>
              <div className="text-right text-sm">
                <p className="font-semibold text-brand-700">${completedTotal.toFixed(2)}</p>
                <p className="text-stone-500">completed revenue</p>
              </div>
            </div>
            <table className="mt-4 w-full text-left text-sm">
              <thead>
                <tr className="border-b text-stone-500">
                  <th className="pb-2 pr-4">Order</th>
                  <th className="pb-2 pr-4">Customer</th>
                  <th className="pb-2 pr-4">Date</th>
                  <th className="pb-2 pr-4">Status</th>
                  <th className="pb-2">Subtotal</th>
                </tr>
              </thead>
              <tbody>
                {pastOrders.map((o) => (
                  <tr key={o.vendorOrderId} className="border-b border-stone-100">
                    <td className="py-3 pr-4">#{o.orderGroupId}</td>
                    <td className="py-3 pr-4">{o.customerName}</td>
                    <td className="py-3 pr-4">{new Date(o.orderDate).toLocaleDateString()}</td>
                    <td className="py-3 pr-4">{o.status}</td>
                    <td className="py-3">${Number(o.subtotal).toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            {pastOrders.length === 0 && <p className="mt-4 text-sm text-stone-500">No fulfilled orders yet.</p>}
          </section>
        </div>
      )}

      {tab === 'analytics' && analytics && (
        <div className="mt-8 space-y-6">
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {[
              { label: 'Daily Sales', value: analytics.dailySales },
              { label: 'Weekly Sales', value: analytics.weeklySales },
              { label: 'Monthly Sales', value: analytics.monthlySales },
            ].map((s) => (
              <div key={s.label} className="kpi-card">
                <p className="text-xs uppercase tracking-[0.25em] text-stone-500">{s.label}</p>
                <p className="mt-2 text-2xl font-bold text-brand-700">${Number(s.value).toFixed(2)}</p>
              </div>
            ))}
            <div className="kpi-card">
              <p className="text-xs uppercase tracking-[0.25em] text-stone-500">Order Status</p>
              <div className="mt-3 flex flex-wrap gap-2 text-sm">
                <span className="rounded-full bg-amber-100 px-3 py-1 text-amber-700">Pending {analytics.pendingCount}</span>
                <span className="rounded-full bg-sky-100 px-3 py-1 text-sky-700">Shipped {analytics.shippedCount}</span>
                <span className="rounded-full bg-rose-100 px-3 py-1 text-rose-700">Cancelled {analytics.cancelledCount}</span>
                <span className="rounded-full bg-emerald-100 px-3 py-1 text-emerald-700">Completed {analytics.completedCount}</span>
              </div>
            </div>
          </div>

          <div className="grid gap-6 lg:grid-cols-[1.5fr_0.9fr]">
            <div className="card">
              <div className="flex items-center justify-between">
                <h2 className="font-semibold">7-Day Sales Trend</h2>
                <span className="text-sm text-stone-500">Revenue</span>
              </div>
              <div className="mt-4 flex h-40 items-end gap-2">
                {analytics.salesTrend?.map((d) => {
                  const height = (Number(d.amount) / chartMax) * 100;
                  return (
                    <div key={d.date} className="flex flex-1 flex-col items-center gap-1">
                      <div className="w-full rounded-t bg-brand-500" style={{ height: `${Math.max(height, 6)}%` }} />
                      <span className="text-[10px] text-stone-500">{d.date.slice(5)}</span>
                    </div>
                  );
                })}
              </div>
            </div>
            <div className="card">
              <h2 className="font-semibold">Out-of-Stock Warnings</h2>
              <div className="mt-4 space-y-2">
                {(analytics.outOfStockProducts?.length ? analytics.outOfStockProducts : ['No alerts right now']).map((item) => (
                  <div key={item} className="flex items-center gap-2 rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-700">
                    <span className="h-2.5 w-2.5 rounded-full bg-amber-500" />
                    {item}
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {tab === 'inventory' && (
        <div className="mt-8">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h2 className="text-xl font-semibold">Inventory</h2>
            <button onClick={() => { setShowProductForm(!showProductForm); setEditingProduct(null); }} className="btn-primary">{showProductForm ? 'Cancel' : 'Create New Product'}</button>
          </div>

          {showProductForm && (
            <Formik
              initialValues={{
                name: editingProduct?.name || '',
                description: editingProduct?.description || '',
                price: editingProduct?.price || '',
                compareAtPrice: editingProduct?.compareAtPrice || '',
                stockQuantity: editingProduct?.stockQuantity || '',
                shippingTerms: editingProduct?.shippingTerms || '',
                imageUrl: editingProduct?.imageUrl || '',
                categoryId: editingProduct?.categoryId || categories[0]?.id || '',
                active: editingProduct?.active ?? true,
              }}
              enableReinitialize
              onSubmit={saveProduct}
            >
              {({ setFieldValue }) => (
                <Form className="card mt-4 grid gap-4 lg:grid-cols-2">
                  <Field name="name" placeholder="Title" className="input-field" />
                  <div className="space-y-2">
                    {!showCategoryComposer ? (
                      <div className="flex gap-2">
                        <Field name="categoryId" as="select" className="input-field flex-1">
                          {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                        </Field>
                        <button type="button" onClick={() => setShowCategoryComposer(true)} className="btn-secondary whitespace-nowrap text-xs">+ Add New</button>
                      </div>
                    ) : (
                      <div className="flex gap-2">
                        <input value={categoryDraft} onChange={(e) => setCategoryDraft(e.target.value)} placeholder="New Category Name" className="input-field flex-1" />
                        <button type="button" onClick={async () => {
                          const createdCategory = await createCategory();
                          if (createdCategory?.id) setFieldValue('categoryId', createdCategory.id);
                        }} disabled={categoryBusy} className="btn-secondary whitespace-nowrap text-xs">
                          {categoryBusy ? 'Saving...' : 'Create'}
                        </button>
                      </div>
                    )}
                  </div>
                  <Field as="textarea" name="description" placeholder="Description" className="input-field lg:col-span-2" rows="3" />
                  <Field name="price" placeholder="Price" className="input-field" />
                  <Field name="compareAtPrice" placeholder="Compare-at Price" className="input-field" />
                  <Field name="stockQuantity" placeholder="Stock Quantity" className="input-field" />
                  <Field name="shippingTerms" placeholder="Shipping Terms" className="input-field" />
                  <div className="lg:col-span-2">
                    <Field name="imageUrl" placeholder="Image URL" className="input-field" />
                    <div onDragOver={(e) => { e.preventDefault(); setDragActive(true); }} onDragLeave={() => setDragActive(false)} onDrop={(e) => { e.preventDefault(); setDragActive(false); const file = e.dataTransfer.files?.[0]; if (file) setFieldValue('imageUrl', URL.createObjectURL(file)); }} className={`mt-3 rounded-2xl border-2 border-dashed px-4 py-5 text-center text-sm ${dragActive ? 'border-brand-500 bg-brand-50' : 'border-stone-300'}`}>
                      Drag and drop an image here to stage it for this product.
                    </div>
                  </div>
                  <label className="flex items-center gap-2 text-sm text-stone-600 lg:col-span-2">
                    <Field type="checkbox" name="active" />
                    Live listing
                  </label>
                  <button type="submit" className="btn-primary lg:col-span-2">Save Product</button>
                </Form>
              )}
            </Formik>
          )}

          <div className="mt-6 overflow-x-auto rounded-2xl border border-stone-200 bg-white shadow-sm">
            <div className="border-b border-stone-100 px-4 py-4">
              <h3 className="font-semibold">Product Performance</h3>
              <p className="mt-1 text-sm text-stone-500">All products this shop has created, including archived listings.</p>
            </div>
            <table className="min-w-full text-left text-sm">
              <thead className="bg-stone-50 text-stone-500">
                <tr>
                  <th className="px-4 py-3">Product</th>
                  <th className="px-4 py-3">Price</th>
                  <th className="px-4 py-3">Stock</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Actions</th>
                </tr>
              </thead>
              <tbody>
                {products.map((p) => (
                  <tr key={p.id} className="border-t border-stone-100">
                    <td className="px-4 py-3">
                      <p className="font-medium text-stone-800">{p.name}</p>
                      <p className="text-xs text-stone-500">{p.categoryName}</p>
                    </td>
                    <td className="px-4 py-3">
                      <input value={p.price || ''} onChange={(e) => updateInventoryField(p.id, 'price', e.target.value)} className="input-field w-24" />
                    </td>
                    <td className="px-4 py-3">
                      <input value={p.stockQuantity || ''} onChange={(e) => updateInventoryField(p.id, 'stockQuantity', e.target.value)} className="input-field w-24" />
                    </td>
                    <td className="px-4 py-3">
                      <label className="inline-flex items-center gap-2 text-sm">
                        <input type="checkbox" checked={p.active} onChange={(e) => updateInventoryField(p.id, 'active', e.target.checked)} />
                        {p.active ? 'Live' : 'Archived'}
                      </label>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2">
                        <button onClick={() => { setEditingProduct(p); setShowProductForm(true); }} className="btn-secondary text-xs">Edit</button>
                        <button onClick={() => archiveProduct(p.id)} className="btn-secondary text-xs text-rose-600">Archive</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {tab === 'profile' && (
        <div className="mt-8 grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
          <div className="card">
            <h2 className="text-xl font-semibold">Storefront Settings</h2>
            <Formik initialValues={profile} enableReinitialize onSubmit={saveProfile}>
              {({ values, handleChange }) => (
                <Form className="mt-4 space-y-4">
                  <input name="businessName" value={values.businessName} onChange={handleChange} placeholder="Shop Name" className="input-field" />
                  <input name="logoUrl" value={values.logoUrl} onChange={handleChange} placeholder="Logo URL" className="input-field" />
                  <input name="bannerUrl" value={values.bannerUrl} onChange={handleChange} placeholder="Banner URL" className="input-field" />
                  <textarea name="bio" value={values.bio} onChange={handleChange} placeholder="About the Creator" rows="4" className="input-field" />
                  <textarea name="description" value={values.description} onChange={handleChange} placeholder="Store description" rows="3" className="input-field" />
                  <div onDragOver={(e) => { e.preventDefault(); setDragActive(true); }} onDragLeave={() => setDragActive(false)} onDrop={onDrop} className={`rounded-2xl border-2 border-dashed px-4 py-6 text-center text-sm ${dragActive ? 'border-brand-500 bg-brand-50' : 'border-stone-300'}`}>
                    Drag and drop a banner or logo image here.
                  </div>
                  <button type="submit" className="btn-primary">Save Store Preferences</button>
                </Form>
              )}
            </Formik>
          </div>
          <div className="card">
            <h3 className="font-semibold">Preview</h3>
            <div className="mt-4 overflow-hidden rounded-2xl border border-stone-200">
              <div className="h-28 bg-stone-100" style={{ backgroundImage: `url(${profile.bannerUrl || ''})`, backgroundSize: 'cover' }} />
              <div className="flex items-center gap-4 p-4">
                <div className="flex h-16 w-16 items-center justify-center rounded-full bg-brand-100 text-xl font-semibold text-brand-700">{profile.businessName?.[0] || 'P'}</div>
                <div>
                  <p className="font-semibold">{profile.businessName || 'Your Shop'}</p>
                  <p className="text-sm text-stone-500">{profile.bio || 'Tell your story.'}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {tab === 'earnings' && earnings && (
        <div className="mt-8 grid gap-6 lg:grid-cols-3">
          <div className="card">
            <p className="text-xs uppercase tracking-[0.25em] text-stone-500">Settled Revenue</p>
            <p className="mt-3 text-3xl font-bold text-brand-700">${Number(earnings.settledRevenue).toFixed(2)}</p>
          </div>
          <div className="card">
            <p className="text-xs uppercase tracking-[0.25em] text-stone-500">Pending Payout</p>
            <p className="mt-3 text-3xl font-bold text-amber-700">${Number(earnings.pendingPayout).toFixed(2)}</p>
          </div>
          <div className="card">
            <p className="text-xs uppercase tracking-[0.25em] text-stone-500">Available Balance</p>
            <p className="mt-3 text-3xl font-bold text-emerald-700">${Number(earnings.availableBalance).toFixed(2)}</p>
          </div>
        </div>
      )}
    </div>
  );
}

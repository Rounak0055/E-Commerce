import { useEffect, useMemo, useState } from 'react';
import api from '../../api/axios';

const tabs = ['overview', 'vendors', 'customers', 'categories', 'settings'];

export default function AdminDashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [categories, setCategories] = useState([]);
  const [settings, setSettings] = useState({ commissionRate: 12.5, alertBannerEnabled: false, alertBannerMessage: '' });
  const [tab, setTab] = useState('overview');
  const [showModal, setShowModal] = useState(false);
  const [targetId, setTargetId] = useState(null);
  const [confirmationText, setConfirmationText] = useState('');
  const [editingCategoryId, setEditingCategoryId] = useState(null);
  const [categoryDraft, setCategoryDraft] = useState('');
  const [newCategoryName, setNewCategoryName] = useState('');

  const load = () => {
    api.get('/admin/dashboard').then((r) => setDashboard(r.data));
    api.get('/admin/categories').then((r) => setCategories(r.data));
    api.get('/admin/settings').then((r) => setSettings(r.data));
  };

  useEffect(() => { load(); }, []);

  const vendorRows = useMemo(() => dashboard?.vendorBreakdown ?? [], [dashboard]);
  const customerRows = useMemo(() => dashboard?.customers ?? [], [dashboard]);

  const approve = async (id) => {
    await api.post(`/admin/vendors/${id}/approve`);
    load();
  };

  const reject = async (id) => {
    await api.post(`/admin/vendors/${id}/reject`);
    load();
  };

  const requestRemoval = (id) => {
    setTargetId(id);
    setConfirmationText('');
    setShowModal(true);
  };

  const removeAccount = async () => {
    if (confirmationText !== 'DELETE' || !targetId) return;
    await api.delete(`/admin/users/${targetId}`);
    setShowModal(false);
    setTargetId(null);
    setConfirmationText('');
    load();
  };

  const createCategory = async () => {
    if (!newCategoryName.trim()) return;
    await api.post('/categories', { name: newCategoryName.trim() });
    setNewCategoryName('');
    load();
  };

  const startCategoryEdit = (category) => {
    setEditingCategoryId(category.id);
    setCategoryDraft(category.name);
  };

  const saveCategory = async (id) => {
    if (!categoryDraft.trim()) return;
    await api.put(`/admin/categories/${id}`, { name: categoryDraft.trim() });
    setEditingCategoryId(null);
    setCategoryDraft('');
    load();
  };

  const deleteCategory = async (id) => {
    await api.delete(`/admin/categories/${id}`);
    load();
  };

  const saveSettings = async (event) => {
    event.preventDefault();
    const { data } = await api.put('/admin/settings', {
      commissionRate: Number(settings.commissionRate),
      alertBannerEnabled: Boolean(settings.alertBannerEnabled),
      alertBannerMessage: settings.alertBannerMessage || '',
    });
    setSettings(data);
  };

  if (!dashboard) return <div className="py-20 text-center">Loading...</div>;

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <h1 className="font-display text-3xl font-bold">Admin Dashboard</h1>

      <div className="mt-6 flex flex-wrap gap-2 border-b">
        {tabs.map((t) => (
          <button key={t} onClick={() => setTab(t)} className={`px-4 py-2 text-sm font-medium capitalize ${tab === t ? 'border-b-2 border-brand-600 text-brand-600' : 'text-stone-600'}`}>{t}</button>
        ))}
      </div>

      {tab === 'overview' && (
        <>
          <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
            {[
              { label: 'Total Revenue', value: `$${Number(dashboard.totalRevenue).toFixed(2)}` },
              { label: 'Average Order Value', value: `$${Number(dashboard.averageOrderValue).toFixed(2)}` },
              { label: 'Active Vendors', value: dashboard.activeVendorCount },
              { label: 'Daily Orders', value: dashboard.dailyOrderCount },
              { label: 'Daily Revenue', value: `$${Number(dashboard.dailyRevenue).toFixed(2)}` },
            ].map((kpi) => (
              <div key={kpi.label} className="kpi-card">
                <p className="text-xs uppercase text-stone-500">{kpi.label}</p>
                <p className="mt-2 text-2xl font-bold text-brand-700">{kpi.value}</p>
              </div>
            ))}
          </div>

          <div className="card mt-8 overflow-x-auto">
            <h2 className="font-semibold">Vendor Breakdown</h2>
            <table className="mt-4 w-full text-left text-sm">
              <thead>
                <tr className="border-b text-stone-500">
                  <th className="pb-2 pr-4">Vendor</th>
                  <th className="pb-2 pr-4">Email</th>
                  <th className="pb-2 pr-4">Status</th>
                  <th className="pb-2 pr-4">Active Orders</th>
                  <th className="pb-2 pr-4">Revenue</th>
                  <th className="pb-2">AOV</th>
                </tr>
              </thead>
              <tbody>
                {vendorRows.map((v) => (
                  <tr key={v.vendorId} className="border-b border-stone-100">
                    <td className="py-3 pr-4 font-medium">{v.vendorName}</td>
                    <td className="py-3 pr-4">{v.email}</td>
                    <td className="py-3 pr-4">{v.vendorStatus}</td>
                    <td className="py-3 pr-4">{v.activeOrders}</td>
                    <td className="py-3 pr-4">${Number(v.totalRevenue).toFixed(2)}</td>
                    <td className="py-3">${Number(v.averageOrderValue).toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      {tab === 'vendors' && (
        <div className="card mt-8 overflow-x-auto">
          <h2 className="font-semibold">Vendors</h2>
          {vendorRows.length === 0 ? (
            <p className="mt-4 text-stone-500">No vendor accounts found.</p>
          ) : (
            <table className="mt-4 w-full text-left text-sm">
              <thead>
                <tr className="border-b text-stone-500">
                  <th className="pb-2 pr-4">Vendor</th>
                  <th className="pb-2 pr-4">Email</th>
                  <th className="pb-2 pr-4">Status</th>
                  <th className="pb-2 pr-4">Revenue</th>
                  <th className="pb-2">Actions</th>
                </tr>
              </thead>
              <tbody>
                {vendorRows.map((v) => (
                  <tr key={v.vendorId} className="border-b border-stone-100">
                    <td className="py-3 pr-4 font-medium">{v.vendorName}</td>
                    <td className="py-3 pr-4">{v.email}</td>
                    <td className="py-3 pr-4">{v.vendorStatus}</td>
                    <td className="py-3 pr-4">${Number(v.totalRevenue).toFixed(2)}</td>
                    <td className="py-3">
                      <div className="flex flex-wrap gap-2">
                        {v.vendorStatus === 'PENDING' && (
                          <>
                            <button onClick={() => approve(v.vendorId)} className="btn-primary text-xs">Approve</button>
                            <button onClick={() => reject(v.vendorId)} className="btn-secondary text-xs text-red-600">Reject</button>
                          </>
                        )}
                        <button onClick={() => requestRemoval(v.vendorId)} className="bg-red-600 px-3 py-1 rounded text-white">Remove Account</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {tab === 'customers' && (
        <div className="card mt-8 overflow-x-auto">
          <h2 className="font-semibold">Customer Directory</h2>
          {customerRows.length === 0 ? (
            <p className="mt-4 text-stone-500">No customer accounts found.</p>
          ) : (
            <table className="mt-4 w-full text-left text-sm">
              <thead>
                <tr className="border-b text-stone-500">
                  <th className="pb-2 pr-4">Name</th>
                  <th className="pb-2 pr-4">Email</th>
                  <th className="pb-2 pr-4">Phone</th>
                  <th className="pb-2 pr-4">Orders</th>
                  <th className="pb-2">Total Spent</th>
                  <th className="pb-2">Actions</th>
                </tr>
              </thead>
              <tbody>
                {customerRows.map((c) => (
                  <tr key={c.id} className="border-b border-stone-100">
                    <td className="py-3 pr-4">{c.firstName} {c.lastName}</td>
                    <td className="py-3 pr-4">{c.email}</td>
                    <td className="py-3 pr-4">{c.phone || '-'}</td>
                    <td className="py-3 pr-4">{c.orderCount}</td>
                    <td className="py-3 pr-4">${Number(c.totalSpent).toFixed(2)}</td>
                    <td className="py-3">
                      <button onClick={() => requestRemoval(c.id)} className="bg-red-600 px-3 py-1 rounded text-white">Remove Account</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {tab === 'categories' && (
        <div className="card mt-8">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h2 className="font-semibold">Category Management</h2>
            <div className="flex min-w-0 flex-1 justify-end gap-2">
              <input value={newCategoryName} onChange={(e) => setNewCategoryName(e.target.value)} placeholder="New category" className="input-field max-w-xs" />
              <button onClick={createCategory} className="btn-secondary whitespace-nowrap">Add</button>
            </div>
          </div>
          <div className="mt-5 overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b text-stone-500">
                  <th className="pb-2 pr-4">Name</th>
                  <th className="pb-2 pr-4">Slug</th>
                  <th className="pb-2">Actions</th>
                </tr>
              </thead>
              <tbody>
                {categories.map((category) => (
                  <tr key={category.id} className="border-b border-stone-100">
                    <td className="py-3 pr-4">
                      {editingCategoryId === category.id ? (
                        <input value={categoryDraft} onChange={(e) => setCategoryDraft(e.target.value)} className="input-field" />
                      ) : category.name}
                    </td>
                    <td className="py-3 pr-4 text-stone-500">{category.slug}</td>
                    <td className="py-3">
                      <div className="flex flex-wrap gap-2">
                        {editingCategoryId === category.id ? (
                          <>
                            <button onClick={() => saveCategory(category.id)} className="btn-primary text-xs">Save</button>
                            <button onClick={() => setEditingCategoryId(null)} className="btn-secondary text-xs">Cancel</button>
                          </>
                        ) : (
                          <button onClick={() => startCategoryEdit(category)} className="btn-secondary text-xs">Edit</button>
                        )}
                        <button onClick={() => deleteCategory(category.id)} className="btn-secondary text-xs text-rose-600">Delete Empty</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {tab === 'settings' && (
        <form onSubmit={saveSettings} className="card mt-8 grid gap-4 lg:grid-cols-2">
          <div>
            <h2 className="font-semibold">Platform Settings</h2>
            <p className="mt-2 text-sm text-stone-500">Tune marketplace-level fees and storefront messaging.</p>
          </div>
          <label className="text-sm font-medium text-stone-700">
            Commission Rate (%)
            <input type="number" min="0" max="100" step="0.01" value={settings.commissionRate ?? ''} onChange={(e) => setSettings((prev) => ({ ...prev, commissionRate: e.target.value }))} className="input-field mt-1" />
          </label>
          <label className="flex items-center gap-3 text-sm font-medium text-stone-700">
            <input type="checkbox" checked={Boolean(settings.alertBannerEnabled)} onChange={(e) => setSettings((prev) => ({ ...prev, alertBannerEnabled: e.target.checked }))} />
            Global alert banner
          </label>
          <label className="text-sm font-medium text-stone-700 lg:col-span-2">
            Alert Message
            <input value={settings.alertBannerMessage || ''} onChange={(e) => setSettings((prev) => ({ ...prev, alertBannerMessage: e.target.value }))} className="input-field mt-1" />
          </label>
          <button type="submit" className="btn-primary lg:col-span-2">Save Settings</button>
        </form>
      )}

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-stone-950/50 px-4">
          <div className="w-full max-w-lg rounded-2xl border border-stone-200 bg-white p-6 shadow-xl">
            <p className="text-sm font-semibold uppercase tracking-[0.3em] text-rose-600">Warning</p>
            <h3 className="mt-2 text-xl font-semibold text-stone-900">This action is permanent.</h3>
            <p className="mt-3 text-sm text-stone-600">Deleting this account will remove the account and related marketplace records. Type <span className="font-semibold text-stone-900">DELETE</span> to confirm.</p>
            <input value={confirmationText} onChange={(e) => setConfirmationText(e.target.value)} placeholder="DELETE" className="input-field mt-4" />
            <div className="mt-6 flex justify-end gap-3">
              <button onClick={() => setShowModal(false)} className="btn-secondary">Cancel</button>
              <button onClick={removeAccount} disabled={confirmationText !== 'DELETE'} className="btn-primary bg-rose-600 hover:bg-rose-700 disabled:cursor-not-allowed">Remove Account</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

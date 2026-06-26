import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();

  return (
    <header className="sticky top-0 z-50 border-b border-stone-200 bg-white/95 backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8">
        <Link to="/" className="font-display text-2xl font-bold text-brand-700">
          Paint & Petals
        </Link>

        <nav className="hidden items-center gap-6 md:flex">
          <NavLink to="/catalog" className={({ isActive }) => isActive ? 'text-brand-600 font-medium' : 'text-stone-600 hover:text-brand-600'}>
            Shop
          </NavLink>
          {user?.role === 'CUSTOMER' && (
            <>
              <NavLink to="/cart" className={({ isActive }) => isActive ? 'text-brand-600 font-medium' : 'text-stone-600 hover:text-brand-600'}>Cart</NavLink>
              <NavLink to="/orders" className={({ isActive }) => isActive ? 'text-brand-600 font-medium' : 'text-stone-600 hover:text-brand-600'}>Orders</NavLink>
            </>
          )}
          {user?.role === 'VENDOR' && user.vendorStatus === 'APPROVED' && (
            <NavLink to="/vendor" className={({ isActive }) => isActive ? 'text-brand-600 font-medium' : 'text-stone-600 hover:text-brand-600'}>Vendor Portal</NavLink>
          )}
          {user?.role === 'ADMIN' && (
            <NavLink to="/admin" className={({ isActive }) => isActive ? 'text-brand-600 font-medium' : 'text-stone-600 hover:text-brand-600'}>Admin</NavLink>
          )}
        </nav>

        <div className="flex items-center gap-3">
          {user ? (
            <>
              <span className="hidden text-sm text-stone-600 sm:inline">{user.firstName}</span>
              <button onClick={logout} className="btn-secondary text-xs">Logout</button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn-secondary text-xs">Login</Link>
              <Link to="/register" className="btn-primary text-xs">Sign Up</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

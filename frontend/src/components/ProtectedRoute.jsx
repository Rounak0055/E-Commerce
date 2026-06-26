import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ roles, children }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-brand-200 border-t-brand-600" />
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (roles && !roles.includes(user.role)) {
    return <Navigate to="/" replace />;
  }

  if (user.role === 'VENDOR' && user.vendorStatus === 'PENDING') {
    if (!window.location.pathname.includes('/vendor/pending')) {
      return <Navigate to="/vendor/pending" replace />;
    }
  }

  if (user.role === 'VENDOR' && user.vendorStatus === 'APPROVED' && window.location.pathname.includes('/vendor/pending')) {
    return <Navigate to="/vendor" replace />;
  }

  return children || <Outlet />;
}

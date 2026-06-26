import { Formik, Form, Field, ErrorMessage } from 'formik';
import { Link, useNavigate } from 'react-router-dom';
import * as Yup from 'yup';
import { useAuth } from '../context/AuthContext';

const schema = Yup.object({
  email: Yup.string().email('Invalid email').required('Required'),
  password: Yup.string().required('Required'),
});

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (values, { setSubmitting, setStatus }) => {
    try {
      const user = await login(values.email, values.password);
      if (user.role === 'ADMIN') navigate('/admin');
      else if (user.role === 'VENDOR') navigate(user.vendorStatus === 'APPROVED' ? '/vendor' : '/vendor/pending');
      else navigate('/catalog');
    } catch (err) {
      setStatus(err.response?.data?.message || 'Login failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-md px-4 py-12">
      <div className="card">
        <h1 className="font-display text-2xl font-bold">Welcome back</h1>
        <p className="mt-2 text-sm text-stone-600">Sign in to your Paint & Petals account</p>

        <Formik initialValues={{ email: '', password: '' }} validationSchema={schema} onSubmit={handleSubmit}>
          {({ isSubmitting, status }) => (
            <Form className="mt-6 space-y-4">
              <div>
                <label className="text-sm font-medium">Email</label>
                <Field name="email" type="email" className="input-field mt-1" />
                <ErrorMessage name="email" component="p" className="mt-1 text-xs text-red-600" />
              </div>
              <div>
                <label className="text-sm font-medium">Password</label>
                <Field name="password" type="password" className="input-field mt-1" />
                <ErrorMessage name="password" component="p" className="mt-1 text-xs text-red-600" />
              </div>
              {status && <p className="text-sm text-red-600">{status}</p>}
              <button type="submit" disabled={isSubmitting} className="btn-primary w-full">Sign In</button>
            </Form>
          )}
        </Formik>

        <p className="mt-6 text-center text-sm text-stone-600">
          No account? <Link to="/register" className="font-medium text-brand-600">Register</Link>
        </p>
      </div>
    </div>
  );
}

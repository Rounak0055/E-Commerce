import { Formik, Form, Field, ErrorMessage } from 'formik';
import { Link, useNavigate } from 'react-router-dom';
import * as Yup from 'yup';
import { useAuth } from '../context/AuthContext';

const schema = Yup.object({
  email: Yup.string().email('Invalid email').required('Required'),
  password: Yup.string().min(6, 'Min 6 characters').required('Required'),
  firstName: Yup.string().required('Required'),
  lastName: Yup.string().required('Required'),
  phone: Yup.string(),
});

export default function Register() {
  const { registerCustomer } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="mx-auto max-w-md px-4 py-12">
      <div className="card">
        <h1 className="font-display text-2xl font-bold">Create account</h1>
        <Formik
          initialValues={{ email: '', password: '', firstName: '', lastName: '', phone: '' }}
          validationSchema={schema}
          onSubmit={async (values, { setSubmitting, setStatus }) => {
            try {
              await registerCustomer(values);
              navigate('/catalog');
            } catch (err) {
              setStatus(err.response?.data?.message || 'Registration failed');
            } finally {
              setSubmitting(false);
            }
          }}
        >
          {({ isSubmitting, status }) => (
            <Form className="mt-6 space-y-4">
              {['firstName', 'lastName', 'email', 'phone', 'password'].map((field) => (
                <div key={field}>
                  <label className="text-sm font-medium capitalize">{field.replace(/([A-Z])/g, ' $1')}</label>
                  <Field name={field} type={field === 'password' ? 'password' : field === 'email' ? 'email' : 'text'} className="input-field mt-1" />
                  <ErrorMessage name={field} component="p" className="mt-1 text-xs text-red-600" />
                </div>
              ))}
              {status && <p className="text-sm text-red-600">{status}</p>}
              <button type="submit" disabled={isSubmitting} className="btn-primary w-full">Register</button>
            </Form>
          )}
        </Formik>
        <p className="mt-6 text-center text-sm text-stone-600">
          Want to sell? <Link to="/vendor/register" className="font-medium text-brand-600">Apply as vendor</Link>
        </p>
      </div>
    </div>
  );
}

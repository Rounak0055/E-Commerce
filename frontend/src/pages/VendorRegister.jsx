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
  businessName: Yup.string().required('Required'),
  description: Yup.string().min(20, 'Describe your business (min 20 chars)').required('Required'),
});

export default function VendorRegister() {
  const { registerVendor } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="mx-auto max-w-lg px-4 py-12">
      <div className="card">
        <h1 className="font-display text-2xl font-bold">Vendor application</h1>
        <p className="mt-2 text-sm text-stone-600">Your account will remain pending until approved by our admin team.</p>

        <Formik
          initialValues={{ email: '', password: '', firstName: '', lastName: '', phone: '', businessName: '', description: '' }}
          validationSchema={schema}
          onSubmit={async (values, { setSubmitting, setStatus }) => {
            try {
              await registerVendor(values);
              navigate('/vendor/pending');
            } catch (err) {
              setStatus(err.response?.data?.message || 'Application failed');
            } finally {
              setSubmitting(false);
            }
          }}
        >
          {({ isSubmitting, status }) => (
            <Form className="mt-6 space-y-4">
              {['firstName', 'lastName', 'email', 'phone', 'password', 'businessName'].map((field) => (
                <div key={field}>
                  <label className="text-sm font-medium capitalize">{field.replace(/([A-Z])/g, ' $1')}</label>
                  <Field name={field} type={field === 'password' ? 'password' : field === 'email' ? 'email' : 'text'} className="input-field mt-1" />
                  <ErrorMessage name={field} component="p" className="mt-1 text-xs text-red-600" />
                </div>
              ))}
              <div>
                <label className="text-sm font-medium">Business description</label>
                <Field name="description" as="textarea" rows={4} className="input-field mt-1" />
                <ErrorMessage name="description" component="p" className="mt-1 text-xs text-red-600" />
              </div>
              {status && <p className="text-sm text-red-600">{status}</p>}
              <button type="submit" disabled={isSubmitting} className="btn-primary w-full">Submit Application</button>
            </Form>
          )}
        </Formik>
        <p className="mt-6 text-center text-sm text-stone-600">
          <Link to="/login" className="font-medium text-brand-600">Back to login</Link>
        </p>
      </div>
    </div>
  );
}

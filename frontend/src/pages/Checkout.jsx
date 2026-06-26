import { useState } from 'react';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import { useNavigate } from 'react-router-dom';
import * as Yup from 'yup';
import api from '../api/axios';

const addressSchema = Yup.object({
  street: Yup.string().required('Required'),
  city: Yup.string().required('Required'),
  state: Yup.string().required('Required'),
  zip: Yup.string().required('Required'),
  country: Yup.string().required('Required'),
  phone: Yup.string().required('Required'),
});

const RAZORPAY_KEY = import.meta.env.VITE_RAZORPAY_KEY_ID;

export default function Checkout() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [order, setOrder] = useState(null);
  const [error, setError] = useState('');

  const initiateCheckout = async (address) => {
    setError('');
    try {
      const { data } = await api.post('/checkout', address);
      setOrder(data);
      setStep(2);
    } catch (err) {
      setError(err.response?.data?.message || 'Checkout failed');
    }
  };

  const payWithRazorpay = () => {
    if (!order?.payment) return;

    const { razorpayOrderId, amount, razorpayKeyId } = {
      ...order.payment,
      razorpayKeyId: order.payment.razorpayKeyId || RAZORPAY_KEY,
    };

    if (!razorpayKeyId && razorpayOrderId?.startsWith('mock_order_')) {
      api.post('/checkout/verify-payment', {
        razorpayOrderId,
        razorpayPaymentId: 'mock_payment_' + Date.now(),
        razorpaySignature: 'mock_signature',
      }).then(() => navigate('/orders')).catch((e) => setError(e.response?.data?.message || 'Payment failed'));
      return;
    }

    const options = {
      key: razorpayKeyId,
      amount: Math.round(Number(amount) * 100),
      currency: order.payment.currency || 'INR',
      name: 'Paint & Petals',
      description: `Order #${order.id}`,
      order_id: razorpayOrderId,
      handler: async (response) => {
        try {
          await api.post('/checkout/verify-payment', {
            razorpayOrderId: response.razorpay_order_id,
            razorpayPaymentId: response.razorpay_payment_id,
            razorpaySignature: response.razorpay_signature,
          });
          navigate('/orders');
        } catch (e) {
          setError(e.response?.data?.message || 'Payment verification failed');
        }
      },
      theme: { color: '#c8473a' },
    };

    const rzp = new window.Razorpay(options);
    rzp.on('payment.failed', () => setError('Payment failed. Please try again.'));
    rzp.open();
  };

  return (
    <div className="mx-auto max-w-2xl px-4 py-8 sm:px-6 lg:px-8">
      <h1 className="font-display text-3xl font-bold">Checkout</h1>

      <div className="mt-6 flex gap-4">
        {[1, 2].map((s) => (
          <div key={s} className={`flex-1 rounded-lg py-2 text-center text-sm font-medium ${step >= s ? 'bg-brand-600 text-white' : 'bg-stone-200 text-stone-600'}`}>
            {s === 1 ? 'Shipping' : 'Payment'}
          </div>
        ))}
      </div>

      {error && <p className="mt-4 text-sm text-red-600">{error}</p>}

      {step === 1 && (
        <Formik
          initialValues={{ street: '', city: '', state: '', zip: '', country: 'India', phone: '' }}
          validationSchema={addressSchema}
          onSubmit={initiateCheckout}
        >
          {({ isSubmitting }) => (
            <Form className="card mt-6 space-y-4">
              <p className="text-sm text-stone-600">Multi-vendor orders will be split per seller automatically.</p>
              {Object.keys(addressSchema.fields).map((field) => (
                <div key={field}>
                  <label className="text-sm font-medium capitalize">{field}</label>
                  <Field name={field} className="input-field mt-1" />
                  <ErrorMessage name={field} component="p" className="mt-1 text-xs text-red-600" />
                </div>
              ))}
              <button type="submit" disabled={isSubmitting} className="btn-primary w-full">Continue to Payment</button>
            </Form>
          )}
        </Formik>
      )}

      {step === 2 && order && (
        <div className="card mt-6">
          <h2 className="font-semibold">Order Summary</h2>
          <p className="mt-2 text-2xl font-bold text-brand-700">${Number(order.totalAmount).toFixed(2)}</p>
          <p className="mt-2 text-sm text-stone-600">{order.vendorOrders?.length} vendor sub-order(s)</p>
          <ul className="mt-4 space-y-2 text-sm">
            {order.vendorOrders?.map((vo) => (
              <li key={vo.id} className="flex justify-between border-b pb-2">
                <span>{vo.vendorName}</span>
                <span>${Number(vo.subtotal).toFixed(2)}</span>
              </li>
            ))}
          </ul>
          <button onClick={payWithRazorpay} className="btn-primary mt-6 w-full">Pay Now</button>
        </div>
      )}
    </div>
  );
}

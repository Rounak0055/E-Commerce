export default function VendorPending() {
  return (
    <div className="mx-auto max-w-lg px-4 py-20 text-center">
      <div className="card">
        <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-amber-100 text-2xl">⏳</div>
        <h1 className="mt-6 font-display text-2xl font-bold">Application Under Review</h1>
        <p className="mt-4 text-stone-600">
          Your vendor account is pending admin approval. You'll gain access to the merchant portal once approved.
        </p>
      </div>
    </div>
  );
}

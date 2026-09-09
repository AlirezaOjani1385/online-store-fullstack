import { useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { useCart } from "../context/CartContext";
import { useToast } from "../context/ToastContext";
import { api, ApiError } from "../api/client";
import { formatPrice } from "../utils/format";
import { CheckIcon, MapPinIcon } from "../components/icons";

export default function Checkout() {
  const { order, total, itemCount, refresh } = useCart();
  const toast = useToast();

  const [address, setAddress] = useState("");
  const [postalCode, setPostalCode] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const [checkout, setCheckout] = useState(order?.checkout || null);

  if (!order || (order.items || []).length === 0) {
    return <Navigate to="/cart" replace />;
  }

  const submitCheckout = async (e) => {
    e.preventDefault();
    setError("");
    setSaving(true);
    try {
      const result = await api.createCheckout(order.id, { address, postalCode });
      setCheckout(result);
      refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "ثبت اطلاعات ارسال ناموفق بود");
      await refresh();
    } finally {
      setSaving(false);
    }
  };

  const handleFakePay = () => {
    toast.info("درگاه پرداخت هنوز راه‌اندازی نشده — این دکمه فقط نمایشیه.");
  };

  const shippingFee = checkout ? checkout.totalPrice - total : 0;

  return (
    <div className="container page">
      <h1 className="page-title">تکمیل خرید</h1>
      <p className="page-sub">{itemCount} کالا در سبد خریدت هست</p>

      <div className="cart-layout">
        <div className="panel">
          {!checkout ? (
            <>
              <h3 style={{ fontFamily: "var(--font-display)", marginTop: 0 }}>اطلاعات ارسال</h3>
              <form onSubmit={submitCheckout}>
                <div className="field">
                  <label htmlFor="address">آدرس کامل</label>
                  <textarea
                    id="address"
                    rows={3}
                    value={address}
                    onChange={(e) => setAddress(e.target.value)}
                    placeholder="استان، شهر، خیابان، پلاک، واحد..."
                    required
                  />
                </div>
                <div className="field">
                  <label htmlFor="postalCode">کد پستی</label>
                  <input
                    id="postalCode"
                    inputMode="numeric"
                    maxLength={10}
                    pattern="\d{10}"
                    value={postalCode}
                    onChange={(e) => setPostalCode(e.target.value)}
                    placeholder="۱۰ رقم، بدون خط تیره"
                    required
                  />
                  <div className="field-hint">کد پستی باید دقیقاً ۱۰ رقم باشه</div>
                </div>

                {error && <div className="field-error mt-8">{error}</div>}

                <button className="btn btn-primary btn-block mt-16" disabled={saving}>
                  {saving ? <span className="spinner" /> : "ثبت آدرس و ادامه"}
                </button>
              </form>
            </>
          ) : (
            <>
              <h3 style={{ fontFamily: "var(--font-display)", marginTop: 0 }}>اطلاعات ارسال ثبت شد</h3>
              <div className="flex gap-8" style={{ alignItems: "flex-start", background: "var(--success-soft)", color: "var(--success)", padding: 14, borderRadius: 12, marginBottom: 20 }}>
                <CheckIcon width={18} height={18} style={{ flexShrink: 0, marginTop: 2 }} />
                <span style={{ fontSize: "0.88rem", lineHeight: 1.9 }}>
                  سفارشت با موفقیت برای ارسال ثبت شد.
                </span>
              </div>
              <div className="flex gap-8" style={{ alignItems: "flex-start", marginBottom: 8 }}>
                <MapPinIcon width={16} height={16} style={{ flexShrink: 0, marginTop: 3, color: "var(--primary)" }} />
                <div>
                  <div style={{ fontWeight: 700, fontSize: "0.9rem" }}>{checkout.address}</div>
                  <div className="muted" style={{ fontSize: "0.82rem", marginTop: 4 }} dir="ltr">
                    کد پستی: {checkout.postalCode}
                  </div>
                </div>
              </div>
            </>
          )}
        </div>

        <div className="panel">
          <h3 style={{ fontFamily: "var(--font-display)", marginTop: 0 }}>خلاصه‌ی سفارش</h3>
          <div className="summary-row">
            <span>جمع کالاها ({itemCount})</span>
            <span className="amount">{formatPrice(total)} تومان</span>
          </div>
          <div className="summary-row">
            <span>هزینه ارسال</span>
            <span className="amount">{checkout ? `${formatPrice(shippingFee)} تومان` : "بعد از ثبت آدرس"}</span>
          </div>
          <div className="summary-row total">
            <span>مبلغ قابل پرداخت</span>
            <span className="amount">{formatPrice(checkout ? checkout.totalPrice : total)} تومان</span>
          </div>

          <button className="btn btn-primary btn-block mt-16" onClick={handleFakePay} disabled={!checkout}>
            پرداخت
          </button>
          {!checkout && (
            <div className="field-hint mt-8" style={{ textAlign: "center" }}>
              اول باید آدرس ارسال رو ثبت کنی
            </div>
          )}

          <Link to="/cart" className="btn btn-ghost btn-block mt-8">
            بازگشت به سبد خرید
          </Link>
        </div>
      </div>
    </div>
  );
}

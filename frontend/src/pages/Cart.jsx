import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";
import { useToast } from "../context/ToastContext";
import { ApiError } from "../api/client";
import { formatPrice } from "../utils/format";
import { TrashIcon, PlusIcon, MinusIcon, CartIcon, AlertIcon } from "../components/icons";

export default function Cart() {
  const { order, updateQuantity, removeItem, total, itemCount } = useCart();
  const toast = useToast();
  const navigate = useNavigate();
  const [busyId, setBusyId] = useState(null);

  const items = order?.items || [];

  const withBusy = async (id, fn) => {
    setBusyId(id);
    try {
      await fn();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "به‌روزرسانی سبد خرید ناموفق بود");
    } finally {
      setBusyId(null);
    }
  };

  if (items.length === 0) {
    return (
      <div className="container page">
        <div className="empty-state">
          <div className="glyph">
            <CartIcon />
          </div>
          <h3>سبد خریدت خالیه</h3>
          <p>هنوز چیزی به سبدت اضافه نکردی — بریم یه گشتی توی محصولات بزنیم.</p>
          <Link to="/" className="btn btn-primary mt-16" style={{ display: "inline-flex" }}>
            مشاهده محصولات
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container page">
      <h1 className="page-title">سبد خرید</h1>
      <p className="page-sub">{itemCount} کالا در سبد خریدت هست</p>

      <div className="cart-layout">
        <div className="panel">
          {items.map((item) => (
            <div className="cart-item-row" key={item.id}>
              <div className="cart-item-thumb">
                {item.product?.imageUrl ? (
                  <img src={item.product.imageUrl} alt={item.product.name} />
                ) : (
                  item.product?.name?.[0] || "?"
                )}
              </div>
              <div className="cart-item-info">
                <div className="name">{item.product?.name}</div>
                <span className="unit-price">{formatPrice(item.product?.price)} تومان / عدد</span>
              </div>

              <div className="qty-stepper">
                <button
                  disabled={busyId === item.id}
                  onClick={() => withBusy(item.id, () => updateQuantity(item.id, item.quantity - 1))}
                  aria-label="کم کردن تعداد"
                >
                  <MinusIcon width={14} height={14} />
                </button>
                <span>{item.quantity}</span>
                <button
                  disabled={busyId === item.id || (item.product?.stock != null && item.quantity >= item.product.stock)}
                  onClick={() => withBusy(item.id, () => updateQuantity(item.id, item.quantity + 1))}
                  aria-label="زیاد کردن تعداد"
                >
                  <PlusIcon width={14} height={14} />
                </button>
              </div>

              <div className="product-price" style={{ minWidth: 96, textAlign: "left" }}>
                {formatPrice((item.product?.price || 0) * item.quantity)}
              </div>

              <button
                className="icon-btn"
                disabled={busyId === item.id}
                onClick={() => withBusy(item.id, () => removeItem(item.id))}
                aria-label="حذف از سبد"
              >
                {busyId === item.id ? <span className="spinner spinner-dark" /> : <TrashIcon width={16} height={16} />}
              </button>
            </div>
          ))}
        </div>

        <div className="panel">
          <h3 style={{ fontFamily: "var(--font-display)", marginTop: 0 }}>خلاصه‌ی سفارش</h3>
          <div className="summary-row">
            <span>جمع کالاها ({itemCount})</span>
            <span className="amount">{formatPrice(total)} تومان</span>
          </div>
          <div className="summary-row">
            <span>هزینه ارسال</span>
            <span className="amount muted">تعیین در تسویه‌حساب</span>
          </div>
          <div className="summary-row total">
            <span>مبلغ قابل پرداخت</span>
            <span className="amount">{formatPrice(total)} تومان</span>
          </div>

          <button className="btn btn-primary btn-block mt-16" onClick={() => navigate("/checkout")}>
            ادامه‌ی فرآیند خرید
          </button>

          <div className="flex gap-8 mt-16" style={{ background: "var(--warning-soft)", color: "var(--warning)", padding: 12, borderRadius: 12, alignItems: "flex-start" }}>
            <AlertIcon width={16} height={16} style={{ flexShrink: 0, marginTop: 2 }} />
            <span style={{ fontSize: "0.8rem", lineHeight: 1.8 }}>
              می‌تونی آدرس ارسال رو ثبت کنی، ولی درگاه پرداخت واقعی وصل نیست — این پروژه یه تمرین بک‌اندیه و دکمه‌ی پرداخت صرفاً نمایشیه.
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}

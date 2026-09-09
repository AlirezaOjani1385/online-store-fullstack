import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { api, ApiError } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { useToast } from "../context/ToastContext";
import { formatPrice } from "../utils/format";
import { PlusIcon, MinusIcon, BoxIcon } from "../components/icons";

export default function ProductDetail() {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [qty, setQty] = useState(1);
  const [busy, setBusy] = useState(false);

  const { isAuthed } = useAuth();
  const { order, addToCart, updateQuantity } = useCart();
  const toast = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    setNotFound(false);
    api
      .getProductById(id)
      .then((data) => setProduct(data))
      .catch((err) => {
        if (err instanceof ApiError && err.status === 404) setNotFound(true);
        else toast.error(err instanceof ApiError ? err.message : "دریافت اطلاعات محصول ناموفق بود");
      })
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const cartItem = (order?.items || []).find((it) => it.product?.id === Number(id));

  const handleAdd = async () => {
    if (!isAuthed) {
      toast.info("اول باید وارد حساب کاربری‌ات بشی");
      navigate("/login");
      return;
    }
    setBusy(true);
    try {
      await addToCart(product, qty);
      toast.success(`«${product.name}» به سبد خرید اضافه شد`);
      setQty(1);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "افزودن به سبد خرید ناموفق بود");
    } finally {
      setBusy(false);
    }
  };

  const handleChangeCartQty = async (nextQty) => {
    setBusy(true);
    try {
      await updateQuantity(cartItem.id, nextQty);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "به‌روزرسانی تعداد ناموفق بود");
    } finally {
      setBusy(false);
    }
  };

  if (loading) {
    return (
      <div className="container page">
        <div className="panel" style={{ maxWidth: 900, margin: "0 auto" }}>
          <div className="grid-2">
            <div className="skeleton" style={{ aspectRatio: "1/1", borderRadius: 16 }} />
            <div>
              <div className="skeleton" style={{ height: 28, width: "70%", marginBottom: 16 }} />
              <div className="skeleton" style={{ height: 20, width: "40%", marginBottom: 24 }} />
              <div className="skeleton" style={{ height: 80, width: "100%" }} />
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (notFound || !product) {
    return (
      <div className="container page">
        <div className="empty-state">
          <div className="glyph">
            <BoxIcon />
          </div>
          <h3>این محصول پیدا نشد</h3>
          <p>ممکنه حذف شده باشه یا آدرس اشتباه باشه.</p>
          <Link to="/" className="btn btn-primary mt-16" style={{ display: "inline-flex" }}>
            بازگشت به محصولات
          </Link>
        </div>
      </div>
    );
  }

  const stock = product.stock ?? 0;
  const outOfStock = !product.available || stock <= 0;
  const maxQty = Math.max(1, stock);

  return (
    <div className="container page">
      <div className="panel" style={{ maxWidth: 900, margin: "0 auto" }}>
        <div className="grid-2">
          <div className="product-thumb" style={{ borderRadius: 16 }}>
            {product.imageUrl ? (
              <img src={product.imageUrl} alt={product.name} onError={(e) => (e.currentTarget.style.display = "none")} />
            ) : (
              <span className="placeholder">{product.name?.[0] || "?"}</span>
            )}
            {outOfStock && <span className="badge badge-out">ناموجود</span>}
          </div>

          <div>
            <h1 className="page-title" style={{ marginBottom: 10 }}>{product.name}</h1>
            <div className="product-price" style={{ fontSize: "1.4rem", marginBottom: 8 }}>
              {formatPrice(product.price)} <small>تومان</small>
            </div>
            {!outOfStock && (
              <div className="muted" style={{ fontSize: "0.9rem", marginBottom: 20, fontWeight: 600 }}>
                موجودی انبار: {stock} عدد
              </div>
            )}
            {outOfStock && <div style={{ marginBottom: 20 }} />}

            <p style={{ color: "var(--ink-soft)", lineHeight: 2, marginBottom: 28, whiteSpace: "pre-wrap" }}>
              {product.description || "توضیحاتی برای این محصول ثبت نشده."}
            </p>

            {outOfStock ? (
              <div className="field-hint" style={{ fontSize: "0.9rem" }}>این محصول در حال حاضر ناموجوده.</div>
            ) : cartItem ? (
              <div className="flex gap-12" style={{ alignItems: "center" }}>
                <span className="qty-stepper" style={{ transform: "scale(1.1)" }}>
                  <button disabled={busy} onClick={() => handleChangeCartQty(cartItem.quantity - 1)} aria-label="کم کردن تعداد">
                    <MinusIcon width={14} height={14} />
                  </button>
                  <span>{busy ? "…" : cartItem.quantity}</span>
                  <button disabled={busy || cartItem.quantity >= stock} onClick={() => handleChangeCartQty(cartItem.quantity + 1)} aria-label="زیاد کردن تعداد">
                    <PlusIcon width={14} height={14} />
                  </button>
                </span>
                <span className="muted" style={{ fontSize: "0.85rem", fontWeight: 700, color: "var(--primary)" }}>
                  در سبد خرید
                </span>
              </div>
            ) : (
              <div className="flex gap-12" style={{ alignItems: "center" }}>
                <span className="qty-stepper" style={{ transform: "scale(1.1)" }}>
                  <button disabled={busy} onClick={() => setQty((q) => Math.max(1, q - 1))} aria-label="کم کردن تعداد">
                    <MinusIcon width={14} height={14} />
                  </button>
                  <span>{qty}</span>
                  <button disabled={busy} disabled={qty >= maxQty} onClick={() => setQty((q) => Math.min(maxQty, q + 1))} aria-label="زیاد کردن تعداد">
                    <PlusIcon width={14} height={14} />
                  </button>
                </span>
                <button className="btn btn-primary" onClick={handleAdd} disabled={busy}>
                  {busy ? <span className="spinner" /> : "افزودن به سبد خرید"}
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

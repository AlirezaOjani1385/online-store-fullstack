import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api, ApiError } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { useToast } from "../context/ToastContext";
import { formatPrice } from "../utils/format";
import { SearchIcon, BoxIcon, CartIcon, PlusIcon, MinusIcon } from "../components/icons";
import Pagination from "../components/Pagination";

const PAGE_SIZE = 12;

export default function Products() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState("");
  const [filter, setFilter] = useState("available"); // available | all
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const { isAuthed } = useAuth();
  const { addToCart, updateQuantity, order, itemCount, total } = useCart();
  const toast = useToast();
  const navigate = useNavigate();
  const [pending, setPending] = useState({}); // productId -> bool

  const load = async (search, targetPage = 0) => {
    setLoading(true);
    try {
      const params = { page: targetPage, size: PAGE_SIZE };
      let result;
      if (search) {
        result = await api.searchProducts(search, params);
      } else if (filter === "available") {
        result = await api.getAvailableProducts(params);
      } else {
        result = await api.getProducts(params);
      }
      setProducts(result.content || []);
      setTotalPages(result.totalPages || 0);
      setTotalElements(result.totalElements || 0);
      setPage(result.page || 0);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "دریافت محصولات با خطا مواجه شد");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load(query.trim(), 0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filter]);

  useEffect(() => {
    const t = setTimeout(() => load(query.trim(), 0), 380);
    return () => clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query]);

  const goToPage = (nextPage) => {
    load(query.trim(), nextPage);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const cartItemByProduct = useMemo(() => {
    const map = {};
    (order?.items || []).forEach((it) => {
      if (it.product?.id) map[it.product.id] = it;
    });
    return map;
  }, [order]);

  const handleAdd = async (product, quantity) => {
    if (!isAuthed) {
      toast.info("اول باید وارد حساب کاربری‌ات بشی");
      navigate("/login");
      return;
    }
    setPending((p) => ({ ...p, [product.id]: true }));
    try {
      await addToCart(product, quantity);
      toast.success(`«${product.name}» به سبد خرید اضافه شد`);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "افزودن به سبد خرید ناموفق بود");
    } finally {
      setPending((p) => ({ ...p, [product.id]: false }));
    }
  };

  const handleChangeQty = async (product, cartItem, nextQty) => {
    setPending((p) => ({ ...p, [product.id]: true }));
    try {
      await updateQuantity(cartItem.id, nextQty);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "به‌روزرسانی تعداد ناموفق بود");
    } finally {
      setPending((p) => ({ ...p, [product.id]: false }));
    }
  };

  return (
    <div className="container page">
      <div className="hero">
        <span className="hero-eyebrow">🛍️ فروشگاه آنلاین</span>
        <h1>هرچی نیاز داری، همین‌جاست.</h1>
        <p>محصولات رو مرور کن، به سبدت اضافه کن و در چند کلیک خریدت رو تکمیل کن.</p>
        <div className="search-bar">
          <SearchIcon width={18} height={18} color="#6b7a99" />
          <input
            placeholder="جستجوی محصول..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
          <button aria-label="جستجو" onClick={() => load(query.trim())}>
            <SearchIcon width={16} height={16} />
          </button>
        </div>
      </div>

      <div className="flex-between">
        <div className="chip-row" style={{ marginBottom: 0 }}>
          <button className={`chip ${filter === "available" ? "active" : ""}`} onClick={() => setFilter("available")}>
            موجود در انبار
          </button>
          <button className={`chip ${filter === "all" ? "active" : ""}`} onClick={() => setFilter("all")}>
            همه‌ی محصولات
          </button>
        </div>
        <span className="muted" style={{ fontSize: "0.85rem" }}>
          {loading ? "در حال بارگذاری..." : `${totalElements} محصول`}
        </span>
      </div>

      <div className="mt-24">
        {loading ? (
          <div className="product-grid">
            {Array.from({ length: 8 }).map((_, i) => (
              <div className="product-card" key={i}>
                <div className="skeleton" style={{ aspectRatio: "1/1" }} />
                <div className="product-body">
                  <div className="skeleton" style={{ height: 14, width: "80%" }} />
                  <div className="skeleton" style={{ height: 20, width: "40%" }} />
                </div>
              </div>
            ))}
          </div>
        ) : products.length === 0 ? (
          <div className="empty-state">
            <div className="glyph">
              <BoxIcon />
            </div>
            <h3>محصولی پیدا نشد</h3>
            <p>عبارت جستجو رو تغییر بده یا فیلتر «همه‌ی محصولات» رو امتحان کن.</p>
          </div>
        ) : (
          <div className="product-grid">
            {products.map((p) => (
              <ProductCard
                key={p.id}
                product={p}
                cartItem={cartItemByProduct[p.id] || null}
                onAdd={(qty) => handleAdd(p, qty)}
                onChangeQty={(nextQty) => handleChangeQty(p, cartItemByProduct[p.id], nextQty)}
                busy={!!pending[p.id]}
              />
            ))}
          </div>
        )}
      </div>

      <Pagination page={page} totalPages={totalPages} onChange={goToPage} />

      {isAuthed && itemCount > 0 && (
        <button className="cart-fab" onClick={() => navigate("/cart")}>
          <span className="fab-icon">
            <CartIcon width={16} height={16} />
          </span>
          <span className="fab-text">
            <span className="fab-count">{itemCount} کالا</span>
            <span className="fab-total">{formatPrice(total)} تومان</span>
          </span>
        </button>
      )}
    </div>
  );
}

function ProductCard({ product, cartItem, onAdd, onChangeQty, busy }) {
  const stock = product.stock ?? 0;
  const outOfStock = !product.available || stock <= 0;
  const inCart = !!cartItem;
  const maxQty = Math.max(1, stock);
  const [localQty, setLocalQty] = useState(1);

  const stopNav = (e) => {
    e.preventDefault();
    e.stopPropagation();
  };

  return (
    <div className="product-card">
      <Link to={`/products/${product.id}`} style={{ display: "contents" }}>
        <div className="product-thumb">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} loading="lazy" onError={(e) => (e.currentTarget.style.display = "none")} />
          ) : (
            <span className="placeholder">{product.name?.[0] || "?"}</span>
          )}
          {outOfStock && <span className="badge badge-out">ناموجود</span>}
        </div>
        <div className="product-body">
          <div className="product-name">{product.name}</div>
          <div className="product-price">
            {formatPrice(product.price)} <small>تومان</small>
          </div>
          {!outOfStock && (
            <div className="muted" style={{ fontSize: "0.75rem", marginTop: 4 }}>
              موجودی: {stock}
            </div>
          )}
        </div>
      </Link>

      <div style={{ padding: "0 16px 16px" }}>
        {outOfStock ? (
          <span className="muted" style={{ fontSize: "0.78rem" }}>در حال حاضر ناموجود</span>
        ) : inCart ? (
          <div className="flex-between" onClick={stopNav}>
            <span className="muted" style={{ fontSize: "0.78rem", fontWeight: 700, color: "var(--primary)" }}>
              در سبد خرید
            </span>
            <span className="qty-stepper">
              <button
                disabled={busy}
                onClick={(e) => {
                  stopNav(e);
                  onChangeQty(cartItem.quantity - 1);
                }}
                aria-label="کم کردن تعداد"
              >
                <MinusIcon width={13} height={13} />
              </button>
              <span>{busy ? "…" : cartItem.quantity}</span>
              <button
                disabled={busy || cartItem.quantity >= stock}
                onClick={(e) => {
                  stopNav(e);
                  if (cartItem.quantity < stock) onChangeQty(cartItem.quantity + 1);
                }}
                aria-label="زیاد کردن تعداد"
              >
                <PlusIcon width={13} height={13} />
              </button>
            </span>
          </div>
        ) : (
          <div className="flex-between" onClick={stopNav}>
            <span className="qty-stepper">
              <button
                disabled={busy}
                onClick={(e) => {
                  stopNav(e);
                  setLocalQty((q) => Math.max(1, q - 1));
                }}
                aria-label="کم کردن تعداد"
              >
                <MinusIcon width={13} height={13} />
              </button>
              <span>{localQty}</span>
              <button
                disabled={busy || localQty >= maxQty}
                onClick={(e) => {
                  stopNav(e);
                  setLocalQty((q) => Math.min(maxQty, q + 1));
                }}
                aria-label="زیاد کردن تعداد"
              >
                <PlusIcon width={13} height={13} />
              </button>
            </span>
            <button
              className="btn btn-secondary btn-sm"
              onClick={(e) => {
                stopNav(e);
                onAdd(localQty);
                setLocalQty(1);
              }}
              disabled={busy}
            >
              {busy ? <span className="spinner spinner-dark" /> : <><PlusIcon width={14} height={14} /> افزودن</>}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

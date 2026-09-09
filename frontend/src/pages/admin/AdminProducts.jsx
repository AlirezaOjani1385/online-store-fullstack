import { useEffect, useState } from "react";
import { api, ApiError } from "../../api/client";
import { useToast } from "../../context/ToastContext";
import { formatPrice } from "../../utils/format";
import { PlusIcon, EditIcon, TrashIcon, BoxIcon } from "../../components/icons";
import Pagination from "../../components/Pagination";

const emptyForm = { name: "", price: "", stock: "", imageUrl: "", description: "", available: true };
const PAGE_SIZE = 10;

export default function AdminProducts() {
  const [products, setProducts] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [modal, setModal] = useState(null); // null | "create" | product object being edited
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const toast = useToast();

  const load = (targetPage = 0) => {
    api
      .getProducts({ page: targetPage, size: PAGE_SIZE })
      .then((result) => {
        setProducts(result.content || []);
        setTotalPages(result.totalPages || 0);
        setTotalElements(result.totalElements || 0);
        setPage(result.page || 0);
      })
      .catch((err) => toast.error(err instanceof ApiError ? err.message : "دریافت محصولات ناموفق بود"));
  };

  useEffect(() => load(0), []); // eslint-disable-line react-hooks/exhaustive-deps

  const openCreate = () => {
    setForm(emptyForm);
    setModal("create");
  };

  const openEdit = (p) => {
    setForm({
      name: p.name,
      price: p.price,
      stock: p.stock ?? 0,
      imageUrl: p.imageUrl || "",
      description: p.description || "",
      available: p.available,
    });
    setModal(p);
  };

  const closeModal = () => setModal(null);

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const stockNum = Number(form.stock);
      const payload = {
        ...form,
        price: Number(form.price),
        stock: Number.isFinite(stockNum) ? stockNum : 0,
      };
      if (modal === "create") {
        await api.addProduct(payload);
        toast.success("محصول اضافه شد");
      } else {
        await api.editProduct(modal.id, payload);
        toast.success("محصول ویرایش شد");
      }
      closeModal();
      load(page);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "ذخیره‌ی محصول ناموفق بود");
    } finally {
      setSaving(false);
    }
  };

  const toggleAvailable = async (p) => {
    try {
      await api.editProduct(p.id, {
        name: p.name,
        price: p.price,
        stock: p.stock ?? 0,
        imageUrl: p.imageUrl,
        description: p.description,
        available: !p.available,
      });
      toast.success(!p.available ? "محصول موجود شد" : "محصول ناموجود شد");
      load(page);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "به‌روزرسانی ناموفق بود");
    }
  };

  const remove = async (p) => {
    if (!confirm(`«${p.name}» حذف بشه؟`)) return;
    try {
      await api.deleteProduct(p.id);
      toast.info("محصول حذف شد");
      load(page);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "حذف محصول ناموفق بود");
    }
  };

  return (
    <div>
      <div className="section-head">
        <div>
          <h2>محصولات</h2>
          <div className="sub">{products ? `${totalElements} محصول` : "در حال بارگذاری..."}</div>
        </div>
        <button className="btn btn-primary" onClick={openCreate}>
          <PlusIcon width={16} height={16} /> محصول جدید
        </button>
      </div>

      {products === null ? (
        <div className="skeleton" style={{ height: 240, borderRadius: 16 }} />
      ) : products.length === 0 ? (
        <div className="empty-state">
          <div className="glyph"><BoxIcon /></div>
          <h3>هنوز محصولی ثبت نشده</h3>
          <p>اولین محصول فروشگاه رو اضافه کن.</p>
        </div>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>محصول</th>
                <th>قیمت</th>
                <th>موجودی انبار</th>
                <th>وضعیت موجودی</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {products.map((p) => (
                <tr key={p.id}>
                  <td>
                    <div className="flex gap-8" style={{ alignItems: "center" }}>
                      <div className="cart-item-thumb" style={{ width: 40, height: 40 }}>
                        {p.imageUrl ? <img src={p.imageUrl} alt="" /> : p.name?.[0]}
                      </div>
                      {p.name}
                    </div>
                  </td>
                  <td className="product-price" style={{ fontSize: "0.85rem" }}>{formatPrice(p.price)} تومان</td>
                  <td style={{ fontWeight: 700, fontVariantNumeric: "tabular-nums" }}>
                    {p.stock ?? 0}
                  </td>
                  <td>
                    <span
                      className={`status-pill ${p.available ? "status-completed" : "status-cancelled"}`}
                      style={{ cursor: "pointer" }}
                      onClick={() => toggleAvailable(p)}
                      title="برای تغییر وضعیت کلیک کن"
                    >
                      {p.available ? "موجود" : "ناموجود"}
                    </span>
                  </td>
                  <td>
                    <div className="flex gap-8">
                      <button className="icon-btn" onClick={() => openEdit(p)} aria-label="ویرایش">
                        <EditIcon width={15} height={15} />
                      </button>
                      <button className="icon-btn" onClick={() => remove(p)} aria-label="حذف">
                        <TrashIcon width={15} height={15} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Pagination page={page} totalPages={totalPages} onChange={load} />

      {modal && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-head">
              <h3>{modal === "create" ? "محصول جدید" : "ویرایش محصول"}</h3>
            </div>
            <form onSubmit={submit}>
              <div className="field">
                <label>نام محصول</label>
                <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} minLength={2} required />
              </div>
              <div className="field">
                <label>قیمت (تومان)</label>
                <input type="number" min={0} value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} required />
              </div>
              <div className="field">
                <label>موجودی انبار (تعداد)</label>
                <input
                  type="number"
                  min={0}
                  value={form.stock}
                  onChange={(e) => {
                    const v = e.target.value;
                    setForm((prev) => ({
                      ...prev,
                      stock: v,
                      // اگر موجودی صفر شد، خودکار ناموجود شود
                      available: Number(v) > 0 ? prev.available : false,
                    }));
                  }}
                  required
                />
              </div>
              <div className="field">
                <label>آدرس تصویر (اختیاری)</label>
                <input value={form.imageUrl} onChange={(e) => setForm({ ...form, imageUrl: e.target.value })} placeholder="https://..." />
              </div>
              <div className="field">
                <label>توضیحات محصول</label>
                <textarea
                  rows={4}
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                  placeholder="توضیح کامل محصول که در صفحه‌ی جزئیات نمایش داده می‌شود..."
                  required
                />
              </div>
              <div className="checkbox-row field">
                <input
                  type="checkbox"
                  id="available"
                  checked={form.available}
                  disabled={Number(form.stock) <= 0}
                  onChange={(e) => setForm({ ...form, available: e.target.checked })}
                  style={{ width: "auto" }}
                />
                <label htmlFor="available" style={{ margin: 0 }}>
                  موجود در انبار{Number(form.stock) <= 0 ? " (با موجودی صفر امکان‌پذیر نیست)" : ""}
                </label>
              </div>
              <div className="flex gap-8 mt-16">
                <button className="btn btn-primary" disabled={saving} style={{ flex: 1 }}>
                  {saving ? <span className="spinner" /> : "ذخیره"}
                </button>
                <button type="button" className="btn btn-ghost" onClick={closeModal}>
                  انصراف
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

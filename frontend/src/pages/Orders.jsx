import { useEffect, useState } from "react";
import { api, ApiError } from "../api/client";
import { useToast } from "../context/ToastContext";
import { formatPrice, formatDate, STATUS_FA, STATUS_CLASS } from "../utils/format";
import { ReceiptIcon } from "../components/icons";
import Pagination from "../components/Pagination";

const PAGE_SIZE = 10;

export default function Orders() {
  const [orders, setOrders] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [cancellingId, setCancellingId] = useState(null);
  const toast = useToast();

  const load = (targetPage = 0) => {
    api
      .getMyOrders({ page: targetPage, size: PAGE_SIZE })
      .then((result) => {
        setOrders(result.content || []);
        setTotalPages(result.totalPages || 0);
        setPage(result.page || 0);
      })
      .catch((err) => toast.error(err instanceof ApiError ? err.message : "دریافت سفارش‌ها ناموفق بود"));
  };

  useEffect(() => {
    load(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const canCancel = (status) => status === "PROCESSING";

  const handleCancel = async (order) => {
    if (!canCancel(order.status)) return;
    const msg =
      order.status === "PROCESSING"
        ? `سفارش #${order.id} لغو شود؟ موجودی محصولات به انبار برمی‌گردد.`
        : `سفارش #${order.id} لغو شود؟`;
    if (!confirm(msg)) return;
    setCancellingId(order.id);
    try {
      await api.cancelOrder(order.id);
      toast.success("سفارش لغو شد");
      load(page);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "لغو سفارش ناموفق بود");
    } finally {
      setCancellingId(null);
    }
  };

  return (
    <div className="container page">
      <h1 className="page-title">سفارش‌های من</h1>
      <p className="page-sub">تاریخچه‌ی همه‌ی سفارش‌هایی که تا الان ثبت کردی</p>

      {orders === null ? (
        <div className="table-wrap">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="skeleton" style={{ height: 60, margin: 12, borderRadius: 10 }} />
          ))}
        </div>
      ) : orders.length === 0 ? (
        <div className="empty-state">
          <div className="glyph">
            <ReceiptIcon />
          </div>
          <h3>هنوز سفارشی ثبت نکردی</h3>
          <p>وقتی خریدی انجام بدی، اینجا نمایش داده میشه.</p>
        </div>
      ) : (
        <>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>شماره سفارش</th>
                  <th>تاریخ</th>
                  <th>اقلام</th>
                  <th>مبلغ</th>
                  <th>وضعیت</th>
                  <th>اطلاعات ارسال</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {orders.map((o) => (
                  <tr key={o.id}>
                    <td>#{o.id}</td>
                    <td>{formatDate(o.createdAt)}</td>
                    <td>{(o.items || []).map((it) => it.product?.name).filter(Boolean).join("، ") || "—"}</td>
                    <td className="product-price" style={{ fontSize: "0.85rem" }}>{formatPrice(o.totalPrice)} تومان</td>
                    <td>
                      <span className={`status-pill ${STATUS_CLASS[o.status] || ""}`}>{STATUS_FA[o.status] || o.status}</span>
                    </td>
                    <td style={{ fontSize: "0.82rem" }}>
                      {o.checkout && (
                        <>
                          <div>{o.checkout.address}</div>
                          <div className="muted" dir="ltr">{o.checkout.postalCode}</div>
                        </>
                      )}
                    </td>
                    <td>
                      {canCancel(o.status) && (
                        <button
                          className="btn btn-ghost btn-sm"
                          style={{ color: "var(--danger, #e11d48)", fontWeight: 700 }}
                          disabled={cancellingId === o.id}
                          onClick={() => handleCancel(o)}
                        >
                          {cancellingId === o.id ? <span className="spinner" /> : "لغو سفارش"}
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination page={page} totalPages={totalPages} onChange={load} />
        </>
      )}
    </div>
  );
}

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { api, ApiError } from "../api/client";
import { UserIcon, TrashIcon } from "../components/icons";

export default function Profile() {
  const { user, refresh, logout } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    firstName: user?.firstName || "",
    lastName: user?.lastName || "",
    number: user?.number || "",
    email: user?.email || "",
    dateOfBirth: user?.dateOfBirth || "",
  });
  const [saving, setSaving] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const save = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await api.updateMe(form);
      await refresh();
      toast.success("اطلاعات حساب به‌روزرسانی شد");
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "به‌روزرسانی ناموفق بود");
    } finally {
      setSaving(false);
    }
  };

  const deleteAccount = async () => {
    try {
      await api.deleteMe();
      toast.info("حساب کاربری‌ات حذف شد");
      logout();
      navigate("/login");
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "حذف حساب ناموفق بود");
    }
  };

  return (
    <div className="container page" style={{ maxWidth: 640 }}>
      <div className="flex gap-12 mt-8" style={{ alignItems: "center", marginBottom: 24 }}>
        <span className="avatar-dot" style={{ width: 52, height: 52, fontSize: "1.1rem" }}>
          <UserIcon width={22} height={22} />
        </span>
        <div>
          <h1 className="page-title" style={{ margin: 0 }}>
            {user?.firstName} {user?.lastName}
          </h1>
          <p className="page-sub" style={{ margin: 0 }}>{user?.number}</p>
        </div>
      </div>

      <div className="panel">
        <h3 style={{ fontFamily: "var(--font-display)", marginTop: 0 }}>ویرایش اطلاعات</h3>
        <form onSubmit={save}>
          <div className="grid-2">
            <div className="field">
              <label>نام</label>
              <input value={form.firstName} onChange={set("firstName")} minLength={2} required />
            </div>
            <div className="field">
              <label>نام خانوادگی</label>
              <input value={form.lastName} onChange={set("lastName")} minLength={2} required />
            </div>
          </div>
          <div className="field">
            <label>شماره موبایل</label>
            <input value={form.number} disabled style={{ opacity: 0.6, cursor: "not-allowed" }} />
            <div className="field-hint">شماره موبایل از همین صفحه قابل تغییر نیست</div>
          </div>
          <div className="field">
            <label>ایمیل</label>
            <input type="email" value={form.email} onChange={set("email")} />
          </div>
          <div className="field">
            <label>تاریخ تولد</label>
            <input value={form.dateOfBirth} onChange={set("dateOfBirth")} placeholder="1380/05/12" />
          </div>
          <button className="btn btn-primary" disabled={saving}>
            {saving ? <span className="spinner" /> : "ذخیره تغییرات"}
          </button>
        </form>
      </div>

      <div className="panel mt-24" style={{ borderColor: "var(--danger-soft)" }}>
        <h3 style={{ fontFamily: "var(--font-display)", marginTop: 0, color: "var(--danger)" }}>منطقه‌ی خطر</h3>
        <p className="muted" style={{ fontSize: "0.88rem" }}>
          حذف حساب کاربری غیرقابل بازگشته و همه‌ی اطلاعات مربوط به تو رو پاک می‌کنه.
        </p>
        {!confirmDelete ? (
          <button className="btn btn-danger" onClick={() => setConfirmDelete(true)}>
            <TrashIcon width={16} height={16} /> حذف حساب کاربری
          </button>
        ) : (
          <div className="flex gap-8">
            <button className="btn btn-danger" onClick={deleteAccount}>
              بله، مطمئنم — حذف کن
            </button>
            <button className="btn btn-ghost" onClick={() => setConfirmDelete(false)}>
              انصراف
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { ApiError } from "../api/client";

export default function Login() {
  const { login } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ number: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login(form.number, form.password);
      toast.success("خوش اومدی 👋");
      navigate(location.state?.from?.pathname || "/", { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "ورود ناموفق بود");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrap">
      <div className="auth-showcase">
        <div className="auth-blob" style={{ top: "-60px", insetInlineEnd: "-80px" }} />
        <div className="auth-blob" style={{ bottom: "-100px", insetInlineStart: "-60px", width: 220, height: 220 }} />
        <h2>خرید ساده، سریع و بی‌دردسر — از هر جا که باشی.</h2>
        <p>
          به حساب کاربری‌ات وارد شو تا سبد خریدت رو ادامه بدی، سفارش‌های قبلی رو ببینی و
          از پیشنهادهای ویژه‌ی فروشگاه باخبر بشی.
        </p>
      </div>

      <div className="auth-form-col">
        <div className="auth-card">
          <h1>ورود به حساب</h1>
          <p className="sub">با شماره موبایل و رمز عبورت وارد شو</p>

          <form onSubmit={submit}>
            <div className="field">
              <label htmlFor="number">شماره موبایل</label>
              <input
                id="number"
                inputMode="numeric"
                placeholder="09xxxxxxxxx"
                maxLength={11}
                value={form.number}
                onChange={(e) => setForm({ ...form, number: e.target.value })}
                required
              />
            </div>
            <div className="field">
              <label htmlFor="password">رمز عبور</label>
              <input
                id="password"
                type="password"
                placeholder="••••••••"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                required
              />
              <div className="field-hint">
                <Link to="/forgot-password">رمز عبورت رو فراموش کردی؟</Link>
              </div>
            </div>

            {error && <div className="field-error mt-8">{error}</div>}

            <button className="btn btn-primary btn-block mt-16" disabled={loading}>
              {loading ? <span className="spinner" /> : "ورود"}
            </button>
          </form>

          <div className="auth-switch">
            حساب نداری؟ <Link to="/register">ثبت‌نام کن</Link>
          </div>
        </div>
      </div>
    </div>
  );
}

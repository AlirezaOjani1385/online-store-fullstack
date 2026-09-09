import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { ApiError } from "../api/client";

const empty = { firstName: "", lastName: "", number: "", email: "", password: "", dateOfBirth: "" };

export default function Register() {
  const { register, verifyRegistration } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [form, setForm] = useState(empty);
  const [code, setCode] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const submitDetails = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const payload = { ...form };
      if (!payload.email) delete payload.email;
      if (!payload.dateOfBirth) delete payload.dateOfBirth;
      await register(payload);
      toast.info("کد تأیید ارسال شد — چون سرویس پیامکی به‌صورت ترمینال کار می‌کنه، کد رو از کنسول بک‌اند بردار.");
      setStep(2);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "ثبت‌نام ناموفق بود");
    } finally {
      setLoading(false);
    }
  };

  const submitCode = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await verifyRegistration(form.number, code);
      toast.success("حساب کاربری‌ات با موفقیت تأیید شد");
      navigate("/", { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "تأیید کد ناموفق بود");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrap">
      <div className="auth-showcase">
        <div className="auth-blob" style={{ top: "-60px", insetInlineEnd: "-80px" }} />
        <div className="auth-blob" style={{ bottom: "-100px", insetInlineStart: "-60px", width: 220, height: 220 }} />
        <h2>یک حساب کاربری، یک دنیا خرید راحت.</h2>
        <p>
          چند لحظه‌ای طول می‌کشه؛ بعدش می‌تونی سبد خریدت رو بین دستگاه‌های مختلف دنبال کنی
          و تاریخچه‌ی سفارش‌هات همیشه در دسترسه.
        </p>
      </div>

      <div className="auth-form-col">
        <div className="auth-card">
          {step === 1 ? (
            <>
              <h1>ساخت حساب کاربری</h1>
              <p className="sub">اطلاعاتت رو وارد کن تا شروع کنیم</p>

              <form onSubmit={submitDetails}>
                <div className="grid-2">
                  <div className="field">
                    <label htmlFor="firstName">نام</label>
                    <input id="firstName" value={form.firstName} onChange={set("firstName")} minLength={2} required />
                  </div>
                  <div className="field">
                    <label htmlFor="lastName">نام خانوادگی</label>
                    <input id="lastName" value={form.lastName} onChange={set("lastName")} minLength={2} required />
                  </div>
                </div>

                <div className="field">
                  <label htmlFor="number">شماره موبایل</label>
                  <input
                    id="number"
                    inputMode="numeric"
                    placeholder="09xxxxxxxxx"
                    maxLength={11}
                    value={form.number}
                    onChange={set("number")}
                    required
                  />
                  <div className="field-hint">کد تأیید به همین شماره ارسال می‌شه</div>
                </div>

                <div className="field">
                  <label htmlFor="email">ایمیل (اختیاری)</label>
                  <input id="email" type="email" value={form.email} onChange={set("email")} />
                </div>

                <div className="grid-2">
                  <div className="field">
                    <label htmlFor="password">رمز عبور</label>
                    <input
                      id="password"
                      type="password"
                      value={form.password}
                      onChange={set("password")}
                      minLength={4}
                      required
                    />
                  </div>
                  <div className="field">
                    <label htmlFor="dob">تاریخ تولد (اختیاری)</label>
                    <input id="dob" placeholder="1380/05/12" value={form.dateOfBirth} onChange={set("dateOfBirth")} />
                  </div>
                </div>

                {error && <div className="field-error mt-8">{error}</div>}

                <button className="btn btn-primary btn-block mt-16" disabled={loading}>
                  {loading ? <span className="spinner" /> : "ادامه"}
                </button>
              </form>

              <div className="auth-switch">
                قبلاً ثبت‌نام کردی؟ <Link to="/login">وارد شو</Link>
              </div>
            </>
          ) : (
            <>
              <h1>کد رو وارد کن</h1>
              <p className="sub">کد ۶ رقمی ارسال‌شده به {form.number} رو وارد کن</p>

              <form onSubmit={submitCode}>
                <div className="field">
                  <label htmlFor="code">کد تأیید</label>
                  <input
                    id="code"
                    inputMode="numeric"
                    maxLength={6}
                    value={code}
                    onChange={(e) => setCode(e.target.value)}
                    required
                    autoFocus
                  />
                  <div className="field-hint">این کد ۲ دقیقه اعتبار داره</div>
                </div>

                {error && <div className="field-error mt-8">{error}</div>}

                <button className="btn btn-primary btn-block mt-16" disabled={loading}>
                  {loading ? <span className="spinner" /> : "تأیید و ورود"}
                </button>
                <button type="button" className="btn btn-ghost btn-block mt-8" onClick={() => setStep(1)}>
                  شماره اشتباهه؟ برگرد
                </button>
              </form>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

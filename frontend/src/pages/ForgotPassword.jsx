import { useState } from "react";
import { Link } from "react-router-dom";
import { api, ApiError } from "../api/client";
import { useToast } from "../context/ToastContext";

export default function ForgotPassword() {
  const toast = useToast();
  const [step, setStep] = useState(1);
  const [number, setNumber] = useState("");
  const [code, setCode] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const requestCode = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await api.requestResetCode(number);
      toast.info("کد بازیابی ارسال شد — چون سرویس پیامکی به‌صورت ترمینال کار می‌کنه، کد رو از کنسول بک‌اند بردار.");
      setStep(2);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "ارسال کد ناموفق بود");
    } finally {
      setLoading(false);
    }
  };

  const resetPassword = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await api.resetPassword({ number, code, newPassword });
      toast.success("رمز عبور با موفقیت تغییر کرد. حالا وارد شو.");
      setStep(3);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "بازنشانی رمز ناموفق بود");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrap">
      <div className="auth-showcase">
        <div className="auth-blob" style={{ top: "-60px", insetInlineEnd: "-80px" }} />
        <h2>رمزت یادت رفته؟ مشکلی نیست.</h2>
        <p>یه کد بازیابی برات می‌فرستیم؛ فقط کافیه واردش کنی و رمز جدید بذاری.</p>
      </div>

      <div className="auth-form-col">
        <div className="auth-card">
          {step === 1 && (
            <>
              <h1>بازیابی رمز عبور</h1>
              <p className="sub">شماره موبایلت رو وارد کن تا کد بازیابی برات ارسال بشه</p>
              <form onSubmit={requestCode}>
                <div className="field">
                  <label htmlFor="number">شماره موبایل</label>
                  <input
                    id="number"
                    inputMode="numeric"
                    maxLength={11}
                    value={number}
                    onChange={(e) => setNumber(e.target.value)}
                    required
                  />
                </div>
                {error && <div className="field-error mt-8">{error}</div>}
                <button className="btn btn-primary btn-block mt-16" disabled={loading}>
                  {loading ? <span className="spinner" /> : "ارسال کد"}
                </button>
              </form>
            </>
          )}

          {step === 2 && (
            <>
              <h1>کد رو وارد کن</h1>
              <p className="sub">کد ارسال‌شده به {number} و رمز عبور جدیدت رو وارد کن</p>
              <form onSubmit={resetPassword}>
                <div className="field">
                  <label htmlFor="code">کد بازیابی</label>
                  <input id="code" value={code} onChange={(e) => setCode(e.target.value)} required />
                </div>
                <div className="field">
                  <label htmlFor="newPassword">رمز عبور جدید</label>
                  <input
                    id="newPassword"
                    type="password"
                    minLength={4}
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    required
                  />
                </div>
                {error && <div className="field-error mt-8">{error}</div>}
                <button className="btn btn-primary btn-block mt-16" disabled={loading}>
                  {loading ? <span className="spinner" /> : "تغییر رمز عبور"}
                </button>
                <button type="button" className="btn btn-ghost btn-block mt-8" onClick={() => setStep(1)}>
                  شماره اشتباهه؟ برگرد
                </button>
              </form>
            </>
          )}

          {step === 3 && (
            <div className="text-center">
              <h1>تمام شد 🎉</h1>
              <p className="sub">رمز عبورت با موفقیت تغییر کرد</p>
              <Link to="/login" className="btn btn-primary btn-block mt-16">
                برو به صفحه‌ی ورود
              </Link>
            </div>
          )}

          {step !== 3 && (
            <div className="auth-switch">
              یادت اومد؟ <Link to="/login">برگرد به ورود</Link>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

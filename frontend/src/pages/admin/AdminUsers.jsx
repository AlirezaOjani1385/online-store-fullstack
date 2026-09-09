import { useEffect, useState } from "react";
import { api, ApiError } from "../../api/client";
import { useToast } from "../../context/ToastContext";
import { useAuth } from "../../context/AuthContext";
import { TrashIcon, UsersIcon } from "../../components/icons";
import Pagination from "../../components/Pagination";

const PAGE_SIZE = 10;

export default function AdminUsers() {
  const [users, setUsers] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const toast = useToast();
  const { user: me } = useAuth();

  const load = (targetPage = 0) => {
    api
      .getAllUsers({ page: targetPage, size: PAGE_SIZE })
      .then((result) => {
        setUsers(result.content || []);
        setTotalPages(result.totalPages || 0);
        setTotalElements(result.totalElements || 0);
        setPage(result.page || 0);
      })
      .catch((err) => toast.error(err instanceof ApiError ? err.message : "دریافت کاربران ناموفق بود"));
  };

  useEffect(() => load(0), []); // eslint-disable-line react-hooks/exhaustive-deps

  const remove = async (u) => {
    if (u.number === me?.number) {
      toast.error("نمی‌تونی حساب خودت رو از اینجا حذف کنی");
      return;
    }
    if (!confirm(`کاربر «${u.firstName} ${u.lastName}» حذف بشه؟`)) return;
    try {
      await api.deleteUserById(u.id);
      toast.info("کاربر حذف شد");
      load(page);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "حذف کاربر ناموفق بود");
    }
  };

  return (
    <div>
      <div className="section-head">
        <div>
          <h2>کاربران</h2>
          <div className="sub">{users ? `${totalElements} کاربر` : "در حال بارگذاری..."}</div>
        </div>
      </div>

      {users === null ? (
        <div className="skeleton" style={{ height: 240, borderRadius: 16 }} />
      ) : users.length === 0 ? (
        <div className="empty-state">
          <div className="glyph"><UsersIcon /></div>
          <h3>کاربری یافت نشد</h3>
        </div>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>نام</th>
                <th>شماره موبایل</th>
                <th>ایمیل</th>
                <th>نقش</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.firstName} {u.lastName}</td>
                  <td style={{ direction: "ltr", textAlign: "right" }}>{u.number}</td>
                  <td>{u.email || "—"}</td>
                  <td>
                    <span className={`role-pill ${u.role === "ADMIN" ? "admin" : ""}`}>
                      {u.role === "ADMIN" ? "ادمین" : "کاربر"}
                    </span>
                  </td>
                  <td>
                    <button className="icon-btn" onClick={() => remove(u)} aria-label="حذف کاربر">
                      <TrashIcon width={15} height={15} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Pagination page={page} totalPages={totalPages} onChange={load} />
    </div>
  );
}

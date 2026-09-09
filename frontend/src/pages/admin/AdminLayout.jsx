import { NavLink, Outlet } from "react-router-dom";
import { BoxIcon, ReceiptIcon, UsersIcon } from "../../components/icons";

export default function AdminLayout() {
  return (
    <div className="container page">
      <h1 className="page-title">پنل مدیریت</h1>
      <p className="page-sub">محصولات، سفارش‌ها و کاربران فروشگاه رو از اینجا مدیریت کن</p>

      <div className="admin-shell">
        <aside className="admin-side">
          <NavLink to="/admin/products" className={({ isActive }) => (isActive ? "active" : "")}>
            <BoxIcon width={16} height={16} /> محصولات
          </NavLink>
          <NavLink to="/admin/orders" className={({ isActive }) => (isActive ? "active" : "")}>
            <ReceiptIcon width={16} height={16} /> سفارش‌ها
          </NavLink>
          <NavLink to="/admin/users" className={({ isActive }) => (isActive ? "active" : "")}>
            <UsersIcon width={16} height={16} /> کاربران
          </NavLink>
        </aside>

        <div>
          <Outlet />
        </div>
      </div>
    </div>
  );
}

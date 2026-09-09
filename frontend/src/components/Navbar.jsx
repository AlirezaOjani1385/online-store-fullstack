import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { useTheme } from "../hooks/useTheme";
import { CartIcon, SunIcon, MoonIcon, MenuIcon, CloseIcon, LogoutIcon } from "./icons";

export default function Navbar() {
  const { user, isAuthed, isAdmin, logout } = useAuth();
  const { itemCount } = useCart();
  const { theme, toggle } = useTheme();
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();

  const initials = user ? `${user.firstName?.[0] || ""}${user.lastName?.[0] || ""}` : "";

  const handleLogout = () => {
    logout();
    setOpen(false);
    navigate("/login");
  };

  return (
    <nav className={`navbar ${open ? "menu-open" : ""}`}>
      <div className="container navbar-inner">
        <NavLink to="/" className="brand" onClick={() => setOpen(false)}>
          <span className="brand-mark" />
          مارکت
        </NavLink>

        <div className="nav-links">
          <NavLink to="/" end className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`} onClick={() => setOpen(false)}>
            محصولات
          </NavLink>
          {isAuthed && (
            <NavLink to="/orders" className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`} onClick={() => setOpen(false)}>
              سفارش‌های من
            </NavLink>
          )}
          {isAdmin && (
            <NavLink to="/admin/products" className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`} onClick={() => setOpen(false)}>
              پنل مدیریت
            </NavLink>
          )}
        </div>

        <div className="nav-actions">
          <button className="icon-btn" onClick={toggle} title="تغییر پوسته" aria-label="تغییر پوسته">
            {theme === "light" ? <MoonIcon width={18} height={18} /> : <SunIcon width={18} height={18} />}
          </button>

          {isAuthed && (
            <NavLink to="/cart" className="icon-btn cart-badge" aria-label="سبد خرید">
              <CartIcon width={18} height={18} />
              {itemCount > 0 && <span className="cart-count">{itemCount}</span>}
            </NavLink>
          )}

          {isAuthed ? (
            <>
              <NavLink to="/profile" className="avatar-chip">
                <span className="avatar-dot">{initials || "؟"}</span>
                {user?.firstName}
              </NavLink>
              <button className="icon-btn" onClick={handleLogout} title="خروج" aria-label="خروج">
                <LogoutIcon width={18} height={18} />
              </button>
            </>
          ) : (
            <NavLink to="/login" className="btn btn-primary btn-sm">
              ورود
            </NavLink>
          )}

          <button className="mobile-toggle" onClick={() => setOpen((o) => !o)} aria-label="منو">
            {open ? <CloseIcon width={18} height={18} /> : <MenuIcon width={18} height={18} />}
          </button>
        </div>
      </div>
    </nav>
  );
}

import { createContext, useContext, useEffect, useState } from "react";
import { api, setToken } from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadMe = async () => {
    const token = localStorage.getItem("store_token");
    if (!token) {
      setUser(null);
      setLoading(false);
      return;
    }
    try {
      const me = await api.getMe();
      setUser(me);
    } catch {
      setToken(null);
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMe();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const login = async (number, password) => {
    const { token } = await api.login(number, password);
    setToken(token);
    const me = await api.getMe();
    setUser(me);
    return me;
  };

  const register = async (payload) => {
    // Account is created but disabled until the SMS code is verified.
    // No token yet — the backend only returns a confirmation message here.
    return api.register(payload);
  };

  const verifyRegistration = async (number, code) => {
    const { token } = await api.verifyRegistration(number, code);
    setToken(token);
    const me = await api.getMe();
    setUser(me);
    return me;
  };

  const logout = () => {
    setToken(null);
    setUser(null);
  };

  const refresh = async () => {
    const me = await api.getMe();
    setUser(me);
    return me;
  };

  const isAdmin = user?.role === "ADMIN";

  return (
    <AuthContext.Provider
      value={{ user, loading, login, register, verifyRegistration, logout, refresh, isAdmin, isAuthed: !!user }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}

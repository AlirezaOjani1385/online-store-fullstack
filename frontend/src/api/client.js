const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/store";

function buildQuery(params = {}) {
  const usp = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") usp.set(key, value);
  });
  const qs = usp.toString();
  return qs ? `?${qs}` : "";
}

// Normalizes a Spring Data Page<T> response into a consistent shape, regardless
// of whether the backend serializes pagination metadata flat (older Spring Data)
// or nested under "page" (newer Spring Data / Spring Boot).
function normalizePage(data) {
  if (!data) return { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };
  const content = data.content ?? (Array.isArray(data) ? data : []);
  const meta = data.page ?? data;
  return {
    content,
    page: meta.number ?? 0,
    size: meta.size ?? content.length,
    totalElements: meta.totalElements ?? content.length,
    totalPages: meta.totalPages ?? 1,
  };
}

function getToken() {
  return localStorage.getItem("store_token");
}

export function setToken(token) {
  if (token) localStorage.setItem("store_token", token);
  else localStorage.removeItem("store_token");
}

class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.status = status;
  }
}

async function request(path, { method = "GET", body, auth = true } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (auth) {
    const token = getToken();
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  let res;
  try {
    res = await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(
      "به سرور بک‌اند وصل نشد. مطمئن شو پروژه‌ی Spring Boot روی پورت 8080 در حال اجراست.",
      0
    );
  }

  if (res.status === 204) return null;

  const isJson = res.headers.get("content-type")?.includes("application/json");
  const data = isJson ? await res.json().catch(() => null) : await res.text().catch(() => null);

  if (!res.ok) {
    const message =
      (data && typeof data === "object" && (data.message || data.error)) ||
      (typeof data === "string" && data) ||
      `خطا در ارتباط با سرور (${res.status})`;
    throw new ApiError(message, res.status);
  }

  return data;
}

export const api = {
  // auth
  login: (number, password) =>
    request("/login", { method: "POST", body: { number, password }, auth: false }),
  register: (payload) =>
    request("/register", { method: "POST", body: payload, auth: false }),
  verifyRegistration: (number, code) =>
    request("/register/verify", { method: "POST", body: { number, code }, auth: false }),
  requestResetCode: (number) =>
    request("/forgot-password/request", { method: "POST", body: { number }, auth: false }),
  resetPassword: (payload) =>
    request("/forgot-password/reset", { method: "POST", body: payload, auth: false }),

  // users
  getMe: () => request("/users/me"),
  updateMe: (user) => request("/users/me", { method: "PUT", body: user }),
  deleteMe: () => request("/users/me", { method: "DELETE" }),
  getAllUsers: async (params) => normalizePage(await request(`/users${buildQuery(params)}`)),
  getUserByNumber: (number) => request(`/users/by-number${buildQuery({ number })}`),
  deleteUserById: (id) => request(`/users/${id}`, { method: "DELETE" }),

  // products
  getProducts: async (params) => normalizePage(await request(`/products${buildQuery(params)}`, { auth: false })),
  getAvailableProducts: async (params) =>
    normalizePage(await request(`/products/available${buildQuery(params)}`, { auth: false })),
  getProductById: (id) => request(`/products/${id}`, { auth: false }),
  searchProducts: async (name, params) =>
    normalizePage(await request(`/products/search${buildQuery({ name, ...params })}`, { auth: false })),
  addProduct: (product) => request("/products", { method: "POST", body: product }),
  editProduct: (id, product) => request(`/products/${id}`, { method: "PUT", body: product }),
  deleteProduct: (id) => request(`/products/${id}`, { method: "DELETE" }),

  // orders
  getMyOrders: async (params) => normalizePage(await request(`/orders/user/me${buildQuery(params)}`)),
  getAllOrders: async (params) => normalizePage(await request(`/orders${buildQuery(params)}`)),
  createOrder: (order) => request("/orders/user/me", { method: "POST", body: order }),
  addItem: (orderId, item) => request(`/orders/${orderId}`, { method: "POST", body: item }),
  editItem: (orderId, itemId, item) =>
    request(`/orders/${orderId}/${itemId}`, { method: "PUT", body: item }),
  deleteItem: (orderId, itemId) =>
    request(`/orders/${orderId}/${itemId}`, { method: "DELETE" }),
  deleteOrder: (id) => request(`/orders/${id}`, { method: "DELETE" }),
  cancelOrder: (id) => request(`/orders/cancel/${id}`, { method: "PUT" }),
  createCheckout: (orderId, payload) =>
    request(`/orders/user/me/checkout/${orderId}`, { method: "POST", body: payload }),
};

export { ApiError };

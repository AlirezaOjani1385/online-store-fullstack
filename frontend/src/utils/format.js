export function toFa(input) {
  const map = { 0: "۰", 1: "۱", 2: "۲", 3: "۳", 4: "۴", 5: "۵", 6: "۶", 7: "۷", 8: "۸", 9: "۹" };
  return String(input).replace(/[0-9]/g, (d) => map[d]);
}

export function formatPrice(value) {
  const n = Math.round(Number(value) || 0);
  return n.toLocaleString("en-US");
}

export function formatDate(iso) {
  if (!iso) return "";
  try {
    return new Intl.DateTimeFormat("fa-IR", { dateStyle: "medium", timeStyle: "short" }).format(
      new Date(iso)
    );
  } catch {
    return iso;
  }
}

export const STATUS_FA = {
  PENDING: "در انتظار",
  PROCESSING: "در حال پردازش",
  COMPLETED: "تکمیل‌شده",
  CANCELLED: "لغوشده",
};

export const STATUS_CLASS = {
  PENDING: "status-pending",
  PROCESSING: "status-processing",
  COMPLETED: "status-completed",
  CANCELLED: "status-cancelled",
};

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { api } from "../api/client";
import { useAuth } from "./AuthContext";

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { isAuthed } = useAuth();
  const [order, setOrder] = useState(null); // current PENDING order, or null
  const [loading, setLoading] = useState(false);

  const refresh = useCallback(async () => {
    if (!isAuthed) {
      setOrder(null);
      return;
    }
    setLoading(true);
    try {
      const { content } = await api.getMyOrders({ size: 50 });
      const pending = (content || []).find((o) => o.status === "PENDING") || null;
      setOrder(pending);
    } finally {
      setLoading(false);
    }
  }, [isAuthed]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const addToCart = async (product, quantity = 1) => {
    if (!order) {
      const created = await api.createOrder({
        items: [{ productId: product.id, quantity }],
      });
      setOrder(created);
      return created;
    }

    const existing = order.items.find((it) => it.product?.id === product.id);
    let updated;
    if (existing) {
      updated = await api.editItem(order.id, existing.id, {
        productId: product.id,
        quantity: existing.quantity + quantity,
      });
    } else {
      updated = await api.addItem(order.id, { productId: product.id, quantity });
    }
    setOrder(updated);
    return updated;
  };

  const updateQuantity = async (itemId, quantity) => {
    if (!order) return;
    if (quantity < 1) return removeItem(itemId);
    const item = order.items.find((it) => it.id === itemId);
    const updated = await api.editItem(order.id, itemId, {
      productId: item.product.id,
      quantity,
    });
    setOrder(updated);
  };

  const removeItem = async (itemId) => {
    if (!order) return;
    if (order.items.length === 1) {
      await api.deleteOrder(order.id);
      setOrder(null);
      return;
    }
    const updated = await api.deleteItem(order.id, itemId);
    setOrder(updated);
  };

  const clearCart = async () => {
    if (!order) return;
    await api.deleteOrder(order.id);
    setOrder(null);
  };

  const itemCount = useMemo(
    () => (order?.items || []).reduce((sum, it) => sum + it.quantity, 0),
    [order]
  );

  const total = useMemo(
    () => (order?.items || []).reduce((sum, it) => sum + (it.product?.price || 0) * it.quantity, 0),
    [order]
  );

  return (
    <CartContext.Provider
      value={{ order, loading, itemCount, total, addToCart, updateQuantity, removeItem, clearCart, refresh }}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error("useCart must be used inside CartProvider");
  return ctx;
}

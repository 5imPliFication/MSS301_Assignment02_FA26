const API_ROOT = (import.meta.env.VITE_API_URL || "").replace(/\/$/, "");

export async function api(path, { token, body, ...options } = {}) {
  let response;
  try {
    response = await fetch(`${API_ROOT}/api${path}`, {
      ...options,
      headers: {
        ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
      ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
    });
  } catch (error) {
    if (error.name === "AbortError") throw error;
    throw new Error(
      "We could not reach the store. Check your connection and try again.",
    );
  }
  const data =
    response.status === 204 ? null : await response.json().catch(() => null);
  if (!response.ok) {
    if (response.status === 401 && token)
      window.dispatchEvent(new Event("session-expired"));
    const error = new Error(
      data?.message ||
        `The request could not be completed (${response.status}).`,
    );
    error.status = response.status;
    throw error;
  }
  return data;
}

export const money = (value) =>
  new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 2,
  }).format(value);
export const number = (value) => new Intl.NumberFormat("en-US").format(value);
export const today = () => {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
};
export const dateLabel = (value) =>
  new Date(`${value}T12:00:00`).toLocaleDateString("en-US", {
    day: "numeric",
    month: "short",
    year: "numeric",
  });

export function exportWatches(watches, brands) {
  const rows = [
    [
      "ID",
      "Name",
      "Brand",
      "Category",
      "Case material",
      "Strap material",
      "Water resistance",
      "Price (USD)",
      "Release date",
      "Stock",
    ],
    ...watches.map((w) => [
      w.watchId,
      w.name,
      brands.find((b) => b.brandId === w.brandId)?.brandName || w.brandId,
      w.categoryName,
      w.caseMaterial,
      w.strapMaterial,
      w.waterpMaterial,
      w.price,
      w.releaseDate,
      w.stock,
    ]),
  ];
  const escape = (value) => {
    let text = String(value ?? "");
    if (/^[=+@\-\t\r]/.test(text)) text = `'${text}`;
    return `"${text.replaceAll('"', '""')}"`;
  };
  const url = URL.createObjectURL(
    new Blob(
      ["\uFEFF" + rows.map((r) => r.map(escape).join(",")).join("\r\n")],
      { type: "text/csv;charset=utf-8;" },
    ),
  );
  const link = document.createElement("a");
  link.href = url;
  link.download = `meridian-inventory-${today()}.csv`;
  link.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

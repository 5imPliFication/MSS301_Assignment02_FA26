import { useState } from "react";
import { Trash2, Pencil, AlertTriangle } from "lucide-react";
import { api, dateLabel, money, number } from "../api.js";
import { useAuth } from "../hooks/useAuth.jsx";
import Dialog from "./Dialog.jsx";
import WatchDial from "./WatchDial.jsx";

export function WatchDetails({ watch, brand, onClose, onEdit }) {
  const { isAdmin } = useAuth();
  return (
    <Dialog
      title={watch.name}
      eyebrow={`${brand?.brandName || "TIMEPIECE"} · #${String(watch.watchId).padStart(3, "0")}`}
      onClose={onClose}
    >
      <div className="dialog-body">
        <div className="detail-art">
          <WatchDial variant={watch.brandId} large />
          <span>THE MERIDIAN COLLECTION</span>
        </div>
        <div className="detail-price">
          <strong>{money(watch.price)}</strong>
          <span
            className={`stock-badge ${watch.stock === 0 ? "stock-out" : watch.stock < 50 ? "stock-low" : ""}`}
          >
            {number(watch.stock)} units available
          </span>
        </div>
        <dl className="detail-grid">
          {[
            ["Category", watch.categoryName],
            ["Origin", brand?.countryOfOrigin || "—"],
            ["Case", watch.caseMaterial],
            ["Strap", watch.strapMaterial],
            ["Water resistance", watch.waterpMaterial],
            ["Release date", dateLabel(watch.releaseDate)],
          ].map(([label, value]) => (
            <div key={label}>
              <dt>{label}</dt>
              <dd>{value}</dd>
            </div>
          ))}
        </dl>
        {isAdmin && (
          <button
            className="btn btn-primary w-100 justify-content-center"
            onClick={onEdit}
          >
            <Pencil size={16} /> Edit timepiece
          </button>
        )}
      </div>
    </Dialog>
  );
}
export function DeleteDialog({ watch, onClose, onDeleted }) {
  const { session } = useAuth();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  async function remove() {
    setBusy(true);
    setError("");
    try {
      await api(`/watches/${watch.watchId}`, {
        method: "DELETE",
        token: session.token,
      });
      onDeleted();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <Dialog
      title="Remove this timepiece?"
      eyebrow="COLLECTION MANAGEMENT"
      onClose={onClose}
      busy={busy}
    >
      <div className="dialog-body">
        <div className="delete-warning">
          <AlertTriangle size={24} />
        </div>
        <p>
          <strong>{watch.name}</strong> will be permanently removed from the
          collection. Its brand total will be updated.
        </p>
        <p className="muted">This action cannot be undone.</p>
        {error && (
          <div className="alert alert-danger" role="alert">
            {error}
          </div>
        )}
      </div>
      <div className="dialog-footer">
        <button
          className="btn btn-outline-secondary"
          disabled={busy}
          onClick={onClose}
        >
          Keep timepiece
        </button>
        <button className="btn btn-danger" disabled={busy} onClick={remove}>
          {busy ? (
            <span className="spinner-border spinner-border-sm" />
          ) : (
            <Trash2 size={16} />
          )}{" "}
          {busy ? "Removing…" : "Remove timepiece"}
        </button>
      </div>
    </Dialog>
  );
}

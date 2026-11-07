import { useState } from "react";
import { Check, Info } from "lucide-react";
import { api, today } from "../api.js";
import { useAuth } from "../hooks/useAuth.jsx";
import Dialog from "./Dialog.jsx";

export default function WatchEditor({
  watch,
  brands,
  categories,
  onClose,
  onSaved,
}) {
  const { session } = useAuth();
  const [form, setForm] = useState({
    name: watch?.name || "",
    brandId: watch?.brandId || "",
    categoryId: watch?.categoryId || "",
    caseMaterial: watch?.caseMaterial || "",
    strapMaterial: watch?.strapMaterial || "",
    waterpMaterial: watch?.waterpMaterial || "",
    price: watch?.price ?? "",
    stock: watch?.stock ?? "",
    releaseDate: today(),
  });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [errors, setErrors] = useState({});
  const change = (event) => {
    const { name, value } = event.target;
    setForm((f) => ({ ...f, [name]: value }));
    setErrors((e) => ({ ...e, [name]: "" }));
  };
  async function submit(event) {
    event.preventDefault();
    const next = {};
    if (form.name.trim().length < 11)
      next.name = "Use at least 11 characters, excluding surrounding spaces.";
    for (const key of ["caseMaterial", "strapMaterial", "waterpMaterial"])
      if (!form[key].trim()) next[key] = "This field is required.";
    if (form.releaseDate !== today()) next.releaseDate = "Choose today’s date.";
    if (Object.keys(next).length) {
      setErrors(next);
      return;
    }
    setBusy(true);
    setError("");
    try {
      await api(watch ? `/watches/${watch.watchId}` : "/watches", {
        method: watch ? "PUT" : "POST",
        token: session.token,
        body: {
          ...form,
          name: form.name.trim(),
          categoryId: Number(form.categoryId),
          brandId: Number(form.brandId),
          price: Number(form.price),
          stock: Number(form.stock),
        },
      });
      onSaved(
        watch
          ? "Timepiece updated."
          : "New timepiece added to your collection.",
      );
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }
  function field(key, label, props = {}, hint) {
    return (
      <div className={key === "name" ? "col-12" : "col-sm-6"}>
        <label className="form-label" htmlFor={key}>
          {label}{" "}
          <span className="required-mark" aria-hidden="true">
            *
          </span>
        </label>
        <input
          id={key}
          name={key}
          className={`form-control ${errors[key] ? "is-invalid" : ""}`}
          value={form[key]}
          onChange={change}
          required
          disabled={busy}
          aria-invalid={Boolean(errors[key])}
          aria-describedby={
            errors[key] ? `${key}-error` : hint ? `${key}-hint` : undefined
          }
          {...props}
        />
        {errors[key] && (
          <div className="invalid-feedback" id={`${key}-error`}>
            {errors[key]}
          </div>
        )}
        {hint && (
          <div className="form-text" id={`${key}-hint`}>
            {hint}
          </div>
        )}
      </div>
    );
  }
  return (
    <Dialog
      title={watch ? "Refine the details." : "Add a timepiece."}
      eyebrow={
        watch
          ? `EDIT TIMEPIECE · #${String(watch.watchId).padStart(3, "0")}`
          : "GROW YOUR COLLECTION"
      }
      onClose={onClose}
      busy={busy}
      wide
    >
      <form onSubmit={submit}>
        <div className="dialog-body">
          {error && (
            <div className="alert alert-danger" role="alert">
              {error}
            </div>
          )}
          <div className="form-section-label">01 / Identity</div>
          <div className="row g-3">
            {field(
              "name",
              "Watch name",
              {
                maxLength: 255,
                minLength: 11,
                placeholder: "e.g. Seamaster Diver 300M",
                autoFocus: true,
              },
              "Between 11 and 255 characters.",
            )}
            {[
              ["brandId", "Brand", brands, "brandId", "brandName"],
              [
                "categoryId",
                "Category",
                categories,
                "categoryId",
                "categoryName",
              ],
            ].map(([key, label, items, id, name]) => (
              <div className="col-sm-6" key={key}>
                <label htmlFor={key} className="form-label">
                  {label}{" "}
                  <span className="required-mark" aria-hidden="true">
                    *
                  </span>
                </label>
                <select
                  id={key}
                  name={key}
                  className="form-select"
                  value={form[key]}
                  onChange={change}
                  required
                  disabled={busy}
                >
                  <option value="">Select {label.toLowerCase()}</option>
                  {items.map((item) => (
                    <option value={item[id]} key={item[id]}>
                      {item[name]}
                    </option>
                  ))}
                </select>
              </div>
            ))}
          </div>
          <div className="form-section-label mt-4">
            02 / Construction & inventory
          </div>
          <div className="row g-3">
            {field("caseMaterial", "Case material", {
              maxLength: 100,
              placeholder: "Stainless steel",
            })}
            {field("strapMaterial", "Strap material", {
              maxLength: 100,
              placeholder: "Leather",
            })}
            {field("waterpMaterial", "Water resistance", {
              maxLength: 50,
              placeholder: "100 meters",
            })}
            {field("stock", "Units in stock", {
              type: "number",
              min: 0,
              step: 1,
              max: 2147483647,
              placeholder: "0",
            })}
            {field(
              "price",
              "Price (USD)",
              {
                type: "number",
                min: 100,
                max: 99999999.99,
                step: "0.01",
                placeholder: "100.00",
              },
              "Minimum price: $100.00.",
            )}
            {field(
              "releaseDate",
              "Release date",
              { type: "date", min: today(), max: today() },
              "New and updated records must use today’s date.",
            )}
          </div>
          <div className="editor-note">
            <Info size={16} />
            <span>
              All fields are required. Brand totals update when you save.
            </span>
          </div>
        </div>
        <div className="dialog-footer">
          <button
            type="button"
            className="btn btn-outline-secondary"
            onClick={onClose}
            disabled={busy}
          >
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={busy}>
            {busy ? (
              <>
                <span className="spinner-border spinner-border-sm" /> Saving…
              </>
            ) : (
              <>
                <Check size={17} /> {watch ? "Save changes" : "Add timepiece"}
              </>
            )}
          </button>
        </div>
      </form>
    </Dialog>
  );
}

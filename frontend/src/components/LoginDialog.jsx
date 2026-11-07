import { useState } from "react";
import { ArrowRight, Eye, EyeOff, LockKeyhole } from "lucide-react";
import { useAuth } from "../hooks/useAuth.jsx";
import Dialog from "./Dialog.jsx";

export default function LoginDialog({ onClose, onSuccess }) {
  const { login } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      await login(username.trim(), password);
      onSuccess();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <Dialog
      title="Welcome back."
      eyebrow="YOUR MERIDIAN WORKSPACE"
      onClose={onClose}
      busy={busy}
    >
      <form onSubmit={submit} className="dialog-body">
        <p className="muted mb-4">
          Sign in to your account. Administrators can add, edit, and manage
          timepieces.
        </p>
        {error && (
          <div className="alert alert-danger" role="alert">
            {error}
          </div>
        )}
        <div className="mb-3">
          <label className="form-label" htmlFor="username">
            Username or email
          </label>
          <input
            id="username"
            className="form-control"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            autoFocus
            required
            disabled={busy}
            placeholder="Enter your username"
          />
        </div>
        <div className="mb-4">
          <label className="form-label" htmlFor="password">
            Password
          </label>
          <div className="password-input">
            <input
              id="password"
              className="form-control"
              type={showPassword ? "text" : "password"}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              required
              disabled={busy}
              placeholder="Enter your password"
            />
            <button
              type="button"
              className="icon-button"
              aria-label={showPassword ? "Hide password" : "Show password"}
              onClick={() => setShowPassword(!showPassword)}
            >
              {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>
        </div>
        <button
          type="submit"
          className="btn btn-primary w-100 justify-content-center"
          disabled={busy}
        >
          {busy ? (
            <>
              <span className="spinner-border spinner-border-sm" /> Signing in…
            </>
          ) : (
            <>
              Sign in <ArrowRight size={17} />
            </>
          )}
        </button>
        <div className="login-note">
          <LockKeyhole size={14} /> Your session stays in this browser tab.
        </div>
      </form>
    </Dialog>
  );
}

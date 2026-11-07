import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import { api } from "../api.js";

const AuthContext = createContext(null);
const storageKey = "meridian-session";
function readSession() {
  try {
    const stored = JSON.parse(sessionStorage.getItem(storageKey));
    if (!stored?.token || !stored?.account) return null;
    const payload = JSON.parse(
      atob(
        stored.token.split(".")[1].replaceAll("-", "+").replaceAll("_", "/"),
      ),
    );
    return payload.exp * 1000 > Date.now() ? stored : null;
  } catch {
    return null;
  }
}
export function AuthProvider({ children }) {
  const [session, setSession] = useState(readSession);
  const [expired, setExpired] = useState(false);
  const logout = useCallback(() => {
    setSession(null);
    sessionStorage.removeItem(storageKey);
  }, []);
  useEffect(() => {
    const expire = () => {
      logout();
      setExpired(true);
    };
    window.addEventListener("session-expired", expire);
    let timer;
    if (session) {
      const payload = JSON.parse(
        atob(
          session.token.split(".")[1].replaceAll("-", "+").replaceAll("_", "/"),
        ),
      );
      timer = setTimeout(expire, Math.max(0, payload.exp * 1000 - Date.now()));
    }
    return () => {
      clearTimeout(timer);
      window.removeEventListener("session-expired", expire);
    };
  }, [session, logout]);
  async function login(username, password) {
    const result = await api("/accounts/login", {
      method: "POST",
      body: { username, password },
    });
    sessionStorage.setItem(storageKey, JSON.stringify(result));
    setSession(result);
    setExpired(false);
  }
  return (
    <AuthContext.Provider
      value={{
        session,
        login,
        logout,
        expired,
        dismissExpired: () => setExpired(false),
        isAdmin: session?.account.role === 1,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
export const useAuth = () => useContext(AuthContext);

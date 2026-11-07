import { useCallback, useEffect, useRef, useState } from "react";
import { api } from "../api.js";

export function useCatalog() {
  const [data, setData] = useState({ watches: [], brands: [], categories: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const controller = useRef(null);
  const reload = useCallback(async () => {
    controller.current?.abort();
    const current = new AbortController();
    controller.current = current;
    setLoading(true);
    setError("");
    try {
      const [watches, brands, categories] = await Promise.all([
        api("/watches", { signal: current.signal }),
        api("/brands", { signal: current.signal }),
        api("/watches/categories", { signal: current.signal }),
      ]);
      if (!current.signal.aborted) setData({ watches, brands, categories });
    } catch (err) {
      if (!current.signal.aborted) setError(err.message);
    } finally {
      if (!current.signal.aborted) setLoading(false);
    }
  }, []);
  useEffect(() => {
    reload();
    return () => controller.current?.abort();
  }, [reload]);
  return { ...data, loading, error, reload };
}

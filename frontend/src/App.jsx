import { useEffect, useMemo, useState } from "react";
import {
  ArrowDownUp,
  ArrowRight,
  ArrowUpRight,
  Boxes,
  Check,
  ChevronLeft,
  ChevronRight,
  CircleHelp,
  Compass,
  Download,
  Eye,
  Globe2,
  Layers3,
  LogIn,
  LogOut,
  Menu,
  Package,
  Pencil,
  Plus,
  RefreshCw,
  Search,
  ShieldCheck,
  Tag,
  Trash2,
  Watch,
  X,
} from "lucide-react";
import { exportWatches, money, number } from "./api.js";
import { useAuth } from "./hooks/useAuth.jsx";
import { useCatalog } from "./hooks/useCatalog.js";
import Dialog from "./components/Dialog.jsx";
import LoginDialog from "./components/LoginDialog.jsx";
import WatchEditor from "./components/WatchEditor.jsx";
import { DeleteDialog, WatchDetails } from "./components/WatchDetails.jsx";
import WatchDial from "./components/WatchDial.jsx";

const navItems = [
  { key: "inventory", label: "Inventory", icon: Watch },
  { key: "brands", label: "Brands", icon: Globe2 },
  { key: "categories", label: "Categories", icon: Layers3 },
];
const pageSize = 6;

export default function App() {
  const catalog = useCatalog();
  const { watches, brands, categories, loading, error, reload } = catalog;
  const { session, isAdmin, logout, expired, dismissExpired } = useAuth();
  const [view, setView] = useState("inventory");
  const [narrow, setNarrow] = useState(
    () => window.matchMedia("(max-width: 760px)").matches,
  );
  useEffect(() => {
    const media = window.matchMedia("(max-width: 760px)");
    const update = () => setNarrow(media.matches);
    media.addEventListener("change", update);
    return () => media.removeEventListener("change", update);
  }, []);
  const [mobileNav, setMobileNav] = useState(false);
  const [query, setQuery] = useState("");
  const [brandFilter, setBrandFilter] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("");
  const [stockFilter, setStockFilter] = useState("");
  const [sort, setSort] = useState("newest");
  const [page, setPage] = useState(1);
  const [modal, setModal] = useState(null);
  const [toast, setToast] = useState("");
  const [help, setHelp] = useState(false);
  useEffect(() => {
    if (!toast) return;
    const timeout = setTimeout(() => setToast(""), 5000);
    return () => clearTimeout(timeout);
  }, [toast]);
  useEffect(() => {
    if (!session && (modal?.type === "edit" || modal?.type === "delete"))
      setModal(null);
  }, [session, modal]);
  const brandMap = useMemo(
    () => Object.fromEntries(brands.map((b) => [b.brandId, b])),
    [brands],
  );
  const filtered = useMemo(() => {
    const q = query.toLowerCase().trim();
    return watches
      .filter(
        (w) =>
          (!q ||
            `${w.name} ${brandMap[w.brandId]?.brandName || ""} ${w.watchId}`
              .toLowerCase()
              .includes(q)) &&
          (!brandFilter || w.brandId === Number(brandFilter)) &&
          (!categoryFilter || w.categoryId === Number(categoryFilter)) &&
          (!stockFilter ||
            (stockFilter === "low"
              ? w.stock > 0 && w.stock < 50
              : stockFilter === "out"
                ? w.stock === 0
                : w.stock > 0)),
      )
      .sort((a, b) =>
        sort === "price-low"
          ? a.price - b.price
          : sort === "price-high"
            ? b.price - a.price
            : sort === "stock"
              ? a.stock - b.stock
              : b.watchId - a.watchId,
      );
  }, [
    watches,
    brandMap,
    query,
    brandFilter,
    categoryFilter,
    stockFilter,
    sort,
  ]);
  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const currentPage = Math.min(page, totalPages);
  const visible = filtered.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize,
  );
  const units = watches.reduce((sum, w) => sum + w.stock, 0);
  const value = watches.reduce((sum, w) => sum + w.stock * w.price, 0);
  const lowCount = watches.filter((w) => w.stock < 50).length;
  const activeFilters = Boolean(
    query || brandFilter || categoryFilter || stockFilter,
  );
  function updateFilter(setter, value) {
    setter(value);
    setPage(1);
  }
  function resetFilters() {
    setQuery("");
    setBrandFilter("");
    setCategoryFilter("");
    setStockFilter("");
    setPage(1);
  }
  function navigate(key) {
    setView(key);
    setMobileNav(false);
  }
  function saved(message) {
    setModal(null);
    resetFilters();
    setSort("newest");
    setToast(message);
    reload();
  }
  function browseBrand(id) {
    resetFilters();
    setBrandFilter(String(id));
    navigate("inventory");
  }
  function browseCategory(id) {
    resetFilters();
    setCategoryFilter(String(id));
    navigate("inventory");
  }
  return (
    <div className="workspace">
      <a className="skip-link" href="#main-content">
        Skip to content
      </a>
      {mobileNav && (
        <button
          className="nav-scrim"
          aria-label="Close navigation"
          onClick={() => setMobileNav(false)}
        />
      )}
      <aside
        id="workspace-navigation"
        inert={narrow && !mobileNav}
        aria-hidden={narrow && !mobileNav}
        className={`sidebar ${mobileNav ? "sidebar-open" : ""}`}
      >
        <button
          className="wordmark"
          onClick={() => navigate("inventory")}
          aria-label="Meridian inventory"
        >
          <span className="brand-symbol">
            <Compass size={28} strokeWidth={1.2} />
          </span>
          <span>
            MERIDIAN<small>WATCH WORKSPACE</small>
          </span>
        </button>
        <div className="sidebar-section-label">WORKSPACE</div>
        <nav aria-label="Main navigation">
          {navItems.map(({ key, label, icon: Icon }) => (
            <button
              key={key}
              className={`nav-item ${view === key ? "active" : ""}`}
              aria-current={view === key ? "page" : undefined}
              onClick={() => navigate(key)}
            >
              <Icon size={18} strokeWidth={1.6} />
              <span>{label}</span>
              {key === "inventory" && (
                <span className="nav-count">
                  {loading ? "—" : watches.length}
                </span>
              )}
            </button>
          ))}
        </nav>
        <div className="sidebar-note">
          <div className="small-line" />
          <span>
            GOOD TIMEPIECES.
            <br />
            GREAT ORGANIZATION.
          </span>
          <p>Every detail, in its place.</p>
        </div>
        <div className="sidebar-bottom">
          <button
            className="nav-item"
            onClick={() => {
              setHelp(true);
              setMobileNav(false);
            }}
          >
            <CircleHelp size={18} strokeWidth={1.6} />
            <span>Workspace guide</span>
            <ArrowUpRight size={14} />
          </button>
          <div className="user-panel">
            <div className="avatar">
              {session
                ? session.account.username.slice(0, 2).toUpperCase()
                : "G"}
            </div>
            <div className="user-copy">
              <strong>{session?.account.username || "Guest workspace"}</strong>
              <span>
                {isAdmin
                  ? "Administrator"
                  : session
                    ? session.account.role === 2
                      ? "Staff · view access"
                      : "Member · view access"
                    : "Explore the collection"}
              </span>
            </div>
            <button
              className="sidebar-icon"
              aria-label={session ? "Sign out" : "Sign in"}
              title={session ? "Sign out" : "Sign in"}
              onClick={() => {
                if (session) {
                  logout();
                  setToast("You have signed out.");
                } else setModal({ type: "login" });
              }}
            >
              {session ? <LogOut size={17} /> : <LogIn size={17} />}
            </button>
          </div>
        </div>
      </aside>
      <div className="main-shell">
        <header className="topbar">
          <div className="d-flex align-items-center gap-3">
            <button
              className="icon-button mobile-menu"
              aria-label="Open navigation"
              aria-controls="workspace-navigation"
              aria-expanded={mobileNav}
              onClick={() => setMobileNav(true)}
            >
              <Menu size={21} />
            </button>
            <span className="breadcrumb-label">Workspace</span>
            <ChevronRight size={13} className="muted" />
            <span className="breadcrumb-current">
              {navItems.find((n) => n.key === view).label}
            </span>
          </div>
          <div className="topbar-right">
            <span
              className={`connection-label ${error ? "connection-error" : ""}`}
            >
              <i />
              {error
                ? "Connection unavailable"
                : loading
                  ? "Syncing catalog"
                  : "Catalog connected"}
            </span>
            <span className="topbar-divider" />
            {session ? (
              <span className="role-label">
                <ShieldCheck size={15} />
                {isAdmin ? "Admin access" : "View access"}
              </span>
            ) : (
              <button
                className="text-button"
                onClick={() => setModal({ type: "login" })}
              >
                Sign in <ArrowRight size={15} />
              </button>
            )}
          </div>
        </header>
        <main id="main-content">
          <section className="page-heading">
            <div>
              <span className="eyebrow">THE MERIDIAN WORKSPACE</span>
              <h1>
                {view === "inventory"
                  ? "The collection."
                  : view === "brands"
                    ? "The watchmakers."
                    : "A place for every style."}
              </h1>
              <p>
                {view === "inventory"
                  ? "Exceptional timepieces. Every detail accounted for."
                  : view === "brands"
                    ? "The names, origins, and craftsmanship behind your collection."
                    : "Explore your collection by purpose, movement, and character."}
              </p>
            </div>
            <div className="page-actions">
              {view === "inventory" && (
                <>
                  <button
                    className="btn btn-outline-secondary"
                    disabled={loading || Boolean(error) || !filtered.length}
                    onClick={() => {
                      exportWatches(filtered, brands);
                      setToast("Your inventory export is ready.");
                    }}
                  >
                    <Download size={16} />
                    <span>Export CSV</span>
                  </button>
                  {isAdmin ? (
                    <button
                      className="btn btn-primary"
                      onClick={() => setModal({ type: "edit" })}
                      disabled={loading || Boolean(error)}
                    >
                      <Plus size={18} /> Add timepiece
                    </button>
                  ) : (
                    !session && (
                      <button
                        className="btn btn-primary"
                        onClick={() => setModal({ type: "login" })}
                      >
                        <LogIn size={16} /> Manage collection
                      </button>
                    )
                  )}
                </>
              )}
            </div>
          </section>
          {expired && (
            <div
              className="alert alert-warning d-flex justify-content-between align-items-center"
              role="alert"
            >
              <span>
                Your session has expired. Sign in again to make changes.
              </span>
              <button
                className="icon-button"
                aria-label="Dismiss session message"
                onClick={dismissExpired}
              >
                <X size={18} />
              </button>
            </div>
          )}
          {error ? (
            <section className="error-panel" role="alert">
              <div className="empty-icon">
                <RefreshCw size={25} />
              </div>
              <h2>The catalog is taking a moment.</h2>
              <p>{error}</p>
              <button className="btn btn-primary" onClick={reload}>
                <RefreshCw size={16} /> Try again
              </button>
            </section>
          ) : (
            <>
              <section className="metrics" aria-label="Inventory overview">
                {[
                  {
                    label: "TIMEPIECES",
                    value: number(watches.length),
                    note: `Across ${brands.length} watchmakers`,
                    icon: Watch,
                  },
                  {
                    label: "UNITS IN STOCK",
                    value: number(units),
                    note: "Ready for their next chapter",
                    icon: Boxes,
                  },
                  {
                    label: "INVENTORY VALUE",
                    value: money(value),
                    note: "Total retail value · USD",
                    icon: Tag,
                  },
                  {
                    label: "LOW STOCK",
                    value: String(lowCount).padStart(2, "0"),
                    note: "Fewer than 50 units available",
                    icon: Package,
                  },
                ].map(({ label, value: metric, note, icon: Icon }) => (
                  <div className="metric" key={label}>
                    <div className="metric-label">
                      <span>{label}</span>
                      <Icon size={17} strokeWidth={1.5} />
                    </div>
                    <strong className={loading ? "loading-text" : ""}>
                      {loading ? "—" : metric}
                    </strong>
                    <span className="metric-note">{note}</span>
                  </div>
                ))}
              </section>
              {view === "inventory" && (
                <section
                  className="inventory-panel"
                  aria-label="Watch inventory"
                >
                  <div className="panel-title">
                    <div className="d-flex align-items-center gap-2">
                      <h2>All timepieces</h2>
                      <span className="count-pill">
                        {loading ? "…" : watches.length}
                      </span>
                    </div>
                    <span className="panel-subtitle">
                      A collection worth keeping track of.
                    </span>
                  </div>
                  <div className="inventory-toolbar">
                    <div className="search-control">
                      <Search size={17} />
                      <input
                        className="form-control"
                        aria-label="Search watches"
                        placeholder="Search by name, brand, or ID…"
                        value={query}
                        onChange={(e) => updateFilter(setQuery, e.target.value)}
                      />
                      {query && (
                        <button
                          className="icon-button"
                          aria-label="Clear search"
                          onClick={() => updateFilter(setQuery, "")}
                        >
                          <X size={15} />
                        </button>
                      )}
                    </div>
                    <div className="toolbar-selects">
                      <select
                        className="form-select"
                        aria-label="Filter by brand"
                        value={brandFilter}
                        onChange={(e) =>
                          updateFilter(setBrandFilter, e.target.value)
                        }
                      >
                        <option value="">All brands</option>
                        {brands.map((b) => (
                          <option value={b.brandId} key={b.brandId}>
                            {b.brandName}
                          </option>
                        ))}
                      </select>
                      <select
                        className="form-select"
                        aria-label="Filter by stock"
                        value={stockFilter}
                        onChange={(e) =>
                          updateFilter(setStockFilter, e.target.value)
                        }
                      >
                        <option value="">All stock levels</option>
                        <option value="available">In stock</option>
                        <option value="low">Low stock</option>
                        <option value="out">Out of stock</option>
                      </select>
                      <button
                        className="icon-button refresh-button"
                        title="Refresh catalog"
                        aria-label="Refresh catalog"
                        onClick={reload}
                        disabled={loading}
                      >
                        <RefreshCw
                          size={17}
                          className={loading ? "spin" : ""}
                        />
                      </button>
                    </div>
                  </div>
                  <div className="category-toolbar">
                    <div
                      className="category-tabs"
                      aria-label="Category filters"
                    >
                      <button
                        className={!categoryFilter ? "selected" : ""}
                        aria-pressed={!categoryFilter}
                        onClick={() => updateFilter(setCategoryFilter, "")}
                      >
                        All watches
                      </button>
                      {categories.map((c) => (
                        <button
                          key={c.categoryId}
                          className={
                            categoryFilter === String(c.categoryId)
                              ? "selected"
                              : ""
                          }
                          aria-pressed={categoryFilter === String(c.categoryId)}
                          onClick={() =>
                            updateFilter(
                              setCategoryFilter,
                              String(c.categoryId),
                            )
                          }
                        >
                          {c.categoryName.replace(" Watches", "")}
                        </button>
                      ))}
                    </div>
                    <label className="sort-control">
                      <ArrowDownUp size={14} />
                      <select
                        aria-label="Sort watches"
                        value={sort}
                        onChange={(e) => updateFilter(setSort, e.target.value)}
                      >
                        <option value="newest">Newest first</option>
                        <option value="price-low">Price: low to high</option>
                        <option value="price-high">Price: high to low</option>
                        <option value="stock">Lowest stock first</option>
                      </select>
                    </label>
                  </div>
                  {loading ? (
                    <div
                      className="skeleton-list"
                      aria-label="Loading watches"
                      role="status"
                    >
                      {[1, 2, 3, 4].map((i) => (
                        <div className="skeleton-row" key={i}>
                          <span />
                          <div />
                          <div />
                          <div />
                        </div>
                      ))}
                      <span className="visually-hidden">Loading watches…</span>
                    </div>
                  ) : !filtered.length ? (
                    <div className="empty-state">
                      <div className="empty-icon">
                        <Search size={28} />
                      </div>
                      <h3>
                        {watches.length
                          ? "No timepieces found."
                          : "Your collection starts here."}
                      </h3>
                      <p>
                        {watches.length
                          ? "Try another search or give your filters a fresh start."
                          : "Add your first timepiece to bring this workspace to life."}
                      </p>
                      {activeFilters ? (
                        <button
                          className="btn btn-outline-secondary"
                          onClick={resetFilters}
                        >
                          Clear all filters
                        </button>
                      ) : (
                        isAdmin && (
                          <button
                            className="btn btn-primary"
                            onClick={() => setModal({ type: "edit" })}
                          >
                            <Plus size={17} /> Add timepiece
                          </button>
                        )
                      )}
                    </div>
                  ) : (
                    <div className="table-responsive">
                      <table className="table inventory-table">
                        <thead>
                          <tr>
                            <th scope="col">TIMEPIECE</th>
                            <th scope="col">CATEGORY</th>
                            <th scope="col" className="text-end">
                              UNIT PRICE
                            </th>
                            <th scope="col">AVAILABILITY</th>
                            <th scope="col" className="text-end">
                              {isAdmin ? "ACTIONS" : "DETAILS"}
                            </th>
                          </tr>
                        </thead>
                        <tbody>
                          {visible.map((w) => (
                            <tr key={w.watchId}>
                              <td>
                                <button
                                  className="watch-cell"
                                  onClick={() =>
                                    setModal({ type: "detail", watch: w })
                                  }
                                >
                                  <WatchDial variant={w.brandId} />
                                  <span>
                                    <span className="watch-brand">
                                      {brandMap[w.brandId]?.brandName ||
                                        "Unknown brand"}{" "}
                                      <span>
                                        · REF{" "}
                                        {String(w.watchId).padStart(3, "0")}
                                      </span>
                                    </span>
                                    <strong>{w.name}</strong>
                                    <span className="watch-material">
                                      {w.caseMaterial} <span> / </span>{" "}
                                      {w.strapMaterial}
                                    </span>
                                  </span>
                                </button>
                              </td>
                              <td>
                                <span className="category-badge">
                                  {w.categoryName?.replace(" Watches", "")}
                                </span>
                              </td>
                              <td className="price-cell text-end">
                                {money(w.price)}
                              </td>
                              <td>
                                <span
                                  className={`stock-badge ${w.stock === 0 ? "stock-out" : w.stock < 50 ? "stock-low" : ""}`}
                                >
                                  <i />
                                  {w.stock === 0
                                    ? "Out of stock"
                                    : w.stock < 50
                                      ? "Low stock"
                                      : "In stock"}
                                </span>
                                <span className="stock-units">
                                  {number(w.stock)} units
                                </span>
                              </td>
                              <td>
                                <div className="row-actions">
                                  <button
                                    className="icon-button"
                                    title="View details"
                                    aria-label={`View ${w.name}`}
                                    onClick={() =>
                                      setModal({ type: "detail", watch: w })
                                    }
                                  >
                                    <Eye size={17} />
                                  </button>
                                  {isAdmin && (
                                    <>
                                      <button
                                        className="icon-button"
                                        title="Edit timepiece"
                                        aria-label={`Edit ${w.name}`}
                                        onClick={() =>
                                          setModal({ type: "edit", watch: w })
                                        }
                                      >
                                        <Pencil size={16} />
                                      </button>
                                      <button
                                        className="icon-button danger-action"
                                        title="Remove timepiece"
                                        aria-label={`Delete ${w.name}`}
                                        onClick={() =>
                                          setModal({ type: "delete", watch: w })
                                        }
                                      >
                                        <Trash2 size={16} />
                                      </button>
                                    </>
                                  )}
                                </div>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                  <div className="table-footer">
                    <span>
                      {loading ? (
                        "Updating your collection…"
                      ) : (
                        <>
                          Showing{" "}
                          <strong>
                            {filtered.length
                              ? (currentPage - 1) * pageSize + 1
                              : 0}
                            –{Math.min(currentPage * pageSize, filtered.length)}
                          </strong>{" "}
                          of <strong>{filtered.length}</strong> timepieces
                        </>
                      )}
                      {activeFilters && (
                        <button className="reset-link" onClick={resetFilters}>
                          Reset filters
                        </button>
                      )}
                    </span>
                    <div className="pagination-controls">
                      <button
                        aria-label="Previous page"
                        disabled={currentPage <= 1 || loading}
                        onClick={() => setPage(currentPage - 1)}
                      >
                        <ChevronLeft size={17} />
                      </button>
                      {Array.from({ length: totalPages }, (_, i) => i + 1)
                        .filter(
                          (p) =>
                            p === 1 ||
                            p === totalPages ||
                            Math.abs(p - currentPage) <= 1,
                        )
                        .map((p) => (
                          <button
                            key={p}
                            aria-label={`Page ${p}`}
                            aria-current={
                              currentPage === p ? "page" : undefined
                            }
                            className={currentPage === p ? "current" : ""}
                            onClick={() => setPage(p)}
                            disabled={loading}
                          >
                            {p}
                          </button>
                        ))}
                      <button
                        aria-label="Next page"
                        disabled={currentPage >= totalPages || loading}
                        onClick={() => setPage(currentPage + 1)}
                      >
                        <ChevronRight size={17} />
                      </button>
                    </div>
                  </div>
                </section>
              )}
              {view === "brands" && (
                <section className="brand-grid" aria-label="Brand directory">
                  {brands.map((b, i) => (
                    <article className="brand-card" key={b.brandId}>
                      <div className="brand-card-top">
                        <span className="eyebrow">
                          MAKER {String(i + 1).padStart(2, "0")}
                        </span>
                        <Globe2 size={19} strokeWidth={1.3} />
                      </div>
                      <h2>{b.brandName}</h2>
                      <p>{b.countryOfOrigin}</p>
                      <div className="brand-card-bottom">
                        <span>
                          <strong>{number(b.totalWatches)}</strong> timepieces
                        </span>
                        <button
                          className="icon-button"
                          aria-label={`Browse ${b.brandName}`}
                          onClick={() => browseBrand(b.brandId)}
                        >
                          <ArrowUpRight size={20} />
                        </button>
                      </div>
                    </article>
                  ))}
                </section>
              )}
              {view === "categories" && (
                <section
                  className="category-directory"
                  aria-label="Category directory"
                >
                  {categories.map((c, i) => (
                    <article key={c.categoryId}>
                      <span className="category-index">0{i + 1}</span>
                      <div className="category-description">
                        <h2>{c.categoryName}</h2>
                        <p>{c.description}</p>
                        <span>
                          {c.movementType} <span className="mx-2">·</span>{" "}
                          {c.priceRange}
                        </span>
                      </div>
                      <span className="category-total">
                        {
                          watches.filter((w) => w.categoryId === c.categoryId)
                            .length
                        }{" "}
                        timepieces
                      </span>
                      <button
                        className="icon-button"
                        aria-label={`Browse ${c.categoryName}`}
                        onClick={() => browseCategory(c.categoryId)}
                      >
                        <ArrowUpRight size={20} />
                      </button>
                    </article>
                  ))}
                </section>
              )}
            </>
          )}
          <footer className="workspace-footer">
            <span>
              <Compass size={13} strokeWidth={1.5} /> Considered down to the
              second.
            </span>
            <span>
              MERIDIAN <span className="footer-dot">·</span> WATCH MANAGEMENT
            </span>
          </footer>
        </main>
      </div>
      {toast && (
        <div className="notification-toast" role="status">
          <span className="toast-check">
            <Check size={16} />
          </span>
          {toast}
          <button
            className="icon-button"
            aria-label="Dismiss notification"
            onClick={() => setToast("")}
          >
            <X size={16} />
          </button>
        </div>
      )}
      {modal?.type === "login" && (
        <LoginDialog
          onClose={() => setModal(null)}
          onSuccess={() => {
            setModal(null);
            setToast("Welcome back. Your workspace is ready.");
          }}
        />
      )}
      {modal?.type === "edit" && isAdmin && (
        <WatchEditor
          watch={modal.watch}
          brands={brands}
          categories={categories}
          onClose={() => setModal(null)}
          onSaved={saved}
        />
      )}
      {modal?.type === "detail" && (
        <WatchDetails
          watch={modal.watch}
          brand={brandMap[modal.watch.brandId]}
          onClose={() => setModal(null)}
          onEdit={() => setModal({ type: "edit", watch: modal.watch })}
        />
      )}
      {modal?.type === "delete" && isAdmin && (
        <DeleteDialog
          watch={modal.watch}
          onClose={() => setModal(null)}
          onDeleted={() =>
            saved("Timepiece removed. Your collection is up to date.")
          }
        />
      )}
      {help && (
        <Dialog
          title="Make yourself at home."
          eyebrow="WORKSPACE GUIDE"
          onClose={() => setHelp(false)}
        >
          <div className="dialog-body guide-content">
            <h3>Explore freely</h3>
            <p>
              Browse watches, filter by brand or category, and open a timepiece
              for its complete specifications. Export the filtered collection as
              a CSV whenever you need it.
            </p>
            <h3>Manage with care</h3>
            <p>
              Sign in with an administrator account to add, edit, or remove
              timepieces. Staff and members can explore the collection with view
              access.
            </p>
            <h3>A few house rules</h3>
            <p>
              New and updated watches need a name longer than 10 characters, a
              price of at least $100, and today’s release date. All fields are
              required.
            </p>
            <h3>Keep an eye on stock</h3>
            <p>
              A watch with fewer than 50 units is marked low stock. Your
              inventory value is the total retail price of all available units.
            </p>
          </div>
        </Dialog>
      )}
    </div>
  );
}

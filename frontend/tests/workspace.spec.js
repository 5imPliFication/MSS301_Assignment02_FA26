import { test, expect } from "@playwright/test";

async function login(page, username = "adminsys") {
  await page
    .getByRole("button", { name: "Sign in", exact: true })
    .first()
    .click();
  await page.getByLabel("Username or email").fill(username);
  await page.getByLabel("Password", { exact: true }).fill("@1");
  await page
    .getByRole("dialog")
    .getByRole("button", { name: "Sign in", exact: true })
    .click();
  await expect(page.getByRole("dialog")).toHaveCount(0);
}

test("public collection, filters, details, export and directories", async ({
  page,
}) => {
  const errors = [];
  page.on("pageerror", (error) => errors.push(error.message));
  await page.goto("/");
  await expect(
    page.getByRole("button", { name: "View Submariner Date", exact: true }),
  ).toHaveCount(0);
  await expect(
    page.getByText("Catalog connected", { exact: true }),
  ).toBeVisible();
  await expect(
    page.getByRole("button", { name: "Add timepiece", exact: true }),
  ).toHaveCount(0);
  await page.screenshot({
    path: "../logs/frontend-desktop.png",
    fullPage: true,
  });
  await page.getByLabel("Search watches").fill("Submariner");
  await page
    .getByRole("button", { name: "View Submariner Date", exact: true })
    .click();
  await expect(page.getByRole("dialog")).toContainText("300 meters");
  await expect(page.getByRole("dialog")).toContainText("Dive Watches");
  await page.keyboard.press("Escape");
  const downloadPromise = page.waitForEvent("download");
  await page.getByRole("button", { name: "Export CSV" }).click();
  expect((await downloadPromise).suggestedFilename()).toMatch(
    /meridian-inventory.*\.csv/,
  );
  await page.getByLabel("Search watches").fill("nothing-matches-this-query");
  await expect(page.getByText("No timepieces found.")).toBeVisible();
  await page.getByRole("button", { name: "Clear all filters" }).click();
  await page.getByRole("button", { name: "Brands", exact: true }).click();
  await expect(
    page.getByRole("heading", { name: "Rolex", exact: true }),
  ).toBeVisible();
  await page.getByRole("button", { name: "Browse Rolex" }).click();
  await expect(page.getByLabel("Filter by brand")).toHaveValue("1");
  await page.getByRole("button", { name: "Categories", exact: true }).click();
  await expect(
    page.getByRole("heading", { name: "Dive Watches" }),
  ).toBeVisible();
  expect(errors).toEqual([]);
});

test("administrator creates, edits and deletes a watch with brand synchronization", async ({
  page,
  request,
}) => {
  const initialBrands = await (await request.get("/api/brands")).json();
  const count = (id) =>
    initialBrands.find((b) => b.brandId === id).totalWatches;
  const name = `Meridian Browser Test ${Date.now()}`;
  await page.goto("/");
  await login(page);
  await page
    .getByRole("button", { name: "Add timepiece", exact: true })
    .click();
  await page.getByLabel("Watch name").fill(name);
  await page.getByRole("combobox", { name: "Brand", exact: true }).selectOption("3");
  await page.getByRole("combobox", { name: "Category", exact: true }).selectOption("1");
  await page.getByLabel("Case material").fill("Stainless Steel");
  await page.getByLabel("Strap material").fill("Leather");
  await page.getByLabel("Water resistance").fill("100 meters");
  await page.getByLabel("Units in stock").fill("25");
  await page.getByLabel("Price (USD)").fill("650");
  await page.screenshot({
    path: "../logs/frontend-editor.png",
    fullPage: true,
  });
  await page
    .getByRole("dialog")
    .getByRole("button", { name: "Add timepiece", exact: true })
    .click();
  await expect(page.getByRole("dialog")).toHaveCount(0);
  await expect(page.locator("tbody tr").first()).toContainText(name);
  expect((await (await request.get("/api/brands/3")).json()).totalWatches).toBe(
    count(3) + 1,
  );
  await page.getByRole("button", { name: `Edit ${name}`, exact: true }).click();
  await page.getByRole("combobox", { name: "Brand", exact: true }).selectOption("1");
  await page.getByLabel("Price (USD)").fill("750.50");
  await page.getByRole("button", { name: "Save changes", exact: true }).click();
  await expect(page.getByRole("dialog")).toHaveCount(0);
  await expect(page.locator("tbody tr").first()).toContainText("$750.50");
  expect((await (await request.get("/api/brands/3")).json()).totalWatches).toBe(
    count(3),
  );
  expect((await (await request.get("/api/brands/1")).json()).totalWatches).toBe(
    count(1) + 1,
  );
  await page
    .getByRole("button", { name: `Delete ${name}`, exact: true })
    .click();
  await page.getByRole("button", { name: "Keep timepiece" }).click();
  await expect(page.getByText(name, { exact: true })).toBeVisible();
  await page
    .getByRole("button", { name: `Delete ${name}`, exact: true })
    .click();
  await page
    .getByRole("button", { name: "Remove timepiece", exact: true })
    .click();
  await expect(page.getByRole("dialog")).toHaveCount(0);
  await expect(page.getByText(name, { exact: true })).toHaveCount(0);
  expect((await (await request.get("/api/brands/1")).json()).totalWatches).toBe(
    count(1),
  );
});

test("inactive account rejected, staff read-only, session survives reload", async ({
  page,
}) => {
  await page.goto("/");
  await page
    .getByRole("button", { name: "Sign in", exact: true })
    .first()
    .click();
  await page.getByLabel("Username or email").fill("staff01");
  await page.getByLabel("Password", { exact: true }).fill("@1");
  await page
    .getByRole("dialog")
    .getByRole("button", { name: "Sign in", exact: true })
    .click();
  await expect(page.getByRole("alert")).toContainText("inactive");
  await page.getByLabel("Username or email").fill("staff02");
  await page
    .getByRole("dialog")
    .getByRole("button", { name: "Sign in", exact: true })
    .click();
  await expect(page.getByRole("dialog")).toHaveCount(0);
  await expect(page.getByText("Staff · view access")).toBeVisible();
  await page.reload();
  await expect(page.getByText("Staff · view access")).toBeVisible();
  await expect(
    page.getByRole("button", { name: "Add timepiece", exact: true }),
  ).toHaveCount(0);
  await expect(page.getByRole("button", { name: /^Edit / })).toHaveCount(0);
  await page.getByRole("button", { name: "Sign out", exact: true }).click();
  await expect(page.getByText("Guest workspace")).toBeVisible();
});

test("mobile layout, navigation and keyboard-accessible login", async ({
  page,
}) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto("/");
  await expect(
    page.getByText("Catalog connected", { exact: true }),
  ).toBeAttached();
  await page.screenshot({
    path: "../logs/frontend-mobile.png",
    fullPage: true,
  });
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth,
    ),
  ).toBeTruthy();
  await page.getByRole("button", { name: "Open navigation" }).click();
  await page.getByRole("button", { name: "Brands", exact: true }).click();
  await expect(
    page.getByRole("heading", { name: "The watchmakers." }),
  ).toBeVisible();
  await page
    .getByRole("button", { name: "Sign in", exact: true })
    .first()
    .click();
  await expect(page.getByLabel("Username or email")).toBeFocused();
  await page.keyboard.press("Escape");
  await expect(page.getByRole("dialog")).toHaveCount(0);
});

test("unavailable catalog displays an actionable error", async ({ page }) => {
  await page.route("**/api/watches", (route) => route.abort());
  await page.goto("/");
  await expect(
    page.getByRole("heading", { name: "The catalog is taking a moment." }),
  ).toBeVisible();
  await page.unroute("**/api/watches");
  await page.getByRole("button", { name: "Try again" }).click();
  await expect(
    page.getByText("Catalog connected", { exact: true }),
  ).toBeVisible();
});

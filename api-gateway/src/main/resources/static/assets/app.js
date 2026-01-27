const grid = document.getElementById("productGrid");
const statusEl = document.getElementById("catalogStatus");
const countEl = document.getElementById("catalogCount");
const categorySelect = document.getElementById("categoryFilter");
const brandSelect = document.getElementById("brandFilter");
const cartCountEl = document.getElementById("cartCount");
const catalogReloadButton = document.getElementById("catalogReloadButton");
const searchForm = document.getElementById("catalogSearchForm");
const searchInput = document.getElementById("catalogSearch");
const heroChips = document.getElementById("heroChips");

const loginForm = document.getElementById("loginForm");
const registerForm = document.getElementById("registerForm");
const registerPassword = document.getElementById("registerPassword");
const registerPasswordHint = document.getElementById("registerPasswordHint");
const authMessage = document.getElementById("authMessage");
const sessionStatus = document.getElementById("sessionStatus");
const sessionUserId = document.getElementById("sessionUserId");
const sessionProfileId = document.getElementById("sessionProfileId");
const sessionEmail = document.getElementById("sessionEmail");
const sessionPhone = document.getElementById("sessionPhone");
const signOutButton = document.getElementById("signOutButton");
const refreshProfileButton = document.getElementById("refreshProfileButton");
const profileInfo = document.getElementById("profileInfo");
const profileForm = document.getElementById("profileForm");
const profileFirstName = document.getElementById("profileFirstName");
const profileLastName = document.getElementById("profileLastName");
const profilePhone = document.getElementById("profilePhone");
const profileFillButton = document.getElementById("profileFillButton");
const profileUpdateMessage = document.getElementById("profileUpdateMessage");

const cartItems = document.getElementById("cartItems");
const cartMessage = document.getElementById("cartMessage");
const cartTotal = document.getElementById("cartTotal");
const cartRefreshButton = document.getElementById("cartRefreshButton");
const cartClearButton = document.getElementById("cartClearButton");

const orderForm = document.getElementById("orderForm");
const orderEmail = document.getElementById("orderEmail");
const cardNumber = document.getElementById("cardNumber");
const cardName = document.getElementById("cardName");
const cardExpiry = document.getElementById("cardExpiry");
const cardCvc = document.getElementById("cardCvc");
const paymentMessage = document.getElementById("paymentMessage");
const orderMessage = document.getElementById("orderMessage");
const orderTrackId = document.getElementById("orderTrackId");
const orderTrackButton = document.getElementById("orderTrackButton");
const orderResult = document.getElementById("orderResult");
const useTestCardButton = document.getElementById("useTestCardButton");
const useDeclineCardButton = document.getElementById("useDeclineCardButton");
const orderListButton = document.getElementById("orderListButton");
const orderList = document.getElementById("orderList");
const orderListMessage = document.getElementById("orderListMessage");
const orderManageId = document.getElementById("orderManageId");
const orderStatusSelect = document.getElementById("orderStatusSelect");
const orderStatusButton = document.getElementById("orderStatusButton");
const orderCancelButton = document.getElementById("orderCancelButton");
const orderManageMessage = document.getElementById("orderManageMessage");

const productForm = document.getElementById("productForm");
const productMessage = document.getElementById("productMessage");
const inventoryProductId = document.getElementById("inventoryProductId");
const inventoryCheckButton = document.getElementById("inventoryCheckButton");
const inventoryMessage = document.getElementById("inventoryMessage");
const productEditForm = document.getElementById("productEditForm");
const productEditId = document.getElementById("productEditId");
const productEditName = document.getElementById("productEditName");
const productEditBrand = document.getElementById("productEditBrand");
const productEditCategory = document.getElementById("productEditCategory");
const productEditDescription = document.getElementById("productEditDescription");
const productEditPrice = document.getElementById("productEditPrice");
const productEditDelta = document.getElementById("productEditDelta");
const productLoadButton = document.getElementById("productLoadButton");
const productDeleteButton = document.getElementById("productDeleteButton");
const productEditMessage = document.getElementById("productEditMessage");

const authTabs = document.querySelectorAll(".ms-auth-switch .ms-tab");
const flowSeedButton = document.getElementById("flowSeedButton");
const flowAddButton = document.getElementById("flowAddButton");
const flowCheckoutButton = document.getElementById("flowCheckoutButton");
const flowClearButton = document.getElementById("flowClearButton");
const flowReloadButton = document.getElementById("flowReloadButton");
const activityLog = document.getElementById("activityLog");

const currency = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

const sessionKey = "megasegashop.session";
const state = {
  session: loadSession(),
  products: [],
  cart: null,
  profile: null,
  lastOrderId: null,
  lastProductId: null,
  searchQuery: "",
};
const prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
const passwordPolicy = {
  pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/,
  hint: "Password must be at least 8 characters and include at least one letter and one number. Allowed: A-Z, 0-9, @$!%*#?&.",
};

function loadSession() {
  try {
    const raw = localStorage.getItem(sessionKey);
    return raw ? JSON.parse(raw) : null;
  } catch (error) {
    console.error(error);
    return null;
  }
}

function saveSession(session) {
  state.session = session;
  if (session) {
    localStorage.setItem(sessionKey, JSON.stringify(session));
  } else {
    localStorage.removeItem(sessionKey);
  }
  updateSessionUI();
}

function setMessage(element, text, stateType) {
  if (!element) {
    return;
  }
  element.textContent = text || "";
  if (stateType) {
    element.dataset.state = stateType;
  } else {
    delete element.dataset.state;
  }
}

function isPasswordValid(value) {
  return passwordPolicy.pattern.test(value || "");
}

function setFieldValidity(input, isValid) {
  if (!input) {
    return;
  }
  if (isValid) {
    input.removeAttribute("aria-invalid");
    input.setCustomValidity("");
  } else {
    input.setAttribute("aria-invalid", "true");
    input.setCustomValidity(passwordPolicy.hint);
  }
}

function setStatus(text, stateType) {
  statusEl.textContent = text;
  if (stateType) {
    statusEl.dataset.state = stateType;
  } else {
    delete statusEl.dataset.state;
  }
}

function logActivity(message, stateType) {
  if (!activityLog) {
    return;
  }
  const entry = document.createElement("div");
  entry.className = "ms-activity-entry";
  const time = document.createElement("span");
  time.className = "ms-activity-time";
  time.textContent = new Date().toLocaleTimeString("en-US", { hour12: false });
  const text = document.createElement("span");
  text.className = "ms-activity-text";
  text.textContent = message;
  entry.appendChild(time);
  entry.appendChild(text);
  if (stateType) {
    entry.dataset.state = stateType;
  }
  activityLog.prepend(entry);
  while (activityLog.children.length > 14) {
    activityLog.removeChild(activityLog.lastChild);
  }
}

function normalizeDigits(value, maxLength) {
  if (!value) {
    return "";
  }
  const digits = value.replace(/\D/g, "");
  if (!maxLength) {
    return digits;
  }
  return digits.slice(0, maxLength);
}

function formatCardNumberInput(value) {
  const digits = normalizeDigits(value, 19);
  return digits.replace(/(.{4})/g, "$1 ").trim();
}

function formatExpiryInput(value) {
  const digits = normalizeDigits(value, 4);
  if (digits.length <= 2) {
    return digits;
  }
  return `${digits.slice(0, 2)}/${digits.slice(2)}`;
}

function formatCvcInput(value) {
  return normalizeDigits(value, 4);
}

function bindCardInputFormatting() {
  if (cardNumber) {
    cardNumber.addEventListener("input", () => {
      cardNumber.value = formatCardNumberInput(cardNumber.value);
    });
  }
  if (cardExpiry) {
    cardExpiry.addEventListener("input", () => {
      cardExpiry.value = formatExpiryInput(cardExpiry.value);
    });
  }
  if (cardCvc) {
    cardCvc.addEventListener("input", () => {
      cardCvc.value = formatCvcInput(cardCvc.value);
    });
  }
}

function requireSession(message, element) {
  if (!state.session || !state.session.token) {
    setMessage(element || authMessage, message || "Please sign in first.", "warn");
    document.getElementById("account").scrollIntoView({ behavior: prefersReducedMotion ? "auto" : "smooth" });
    return false;
  }
  return true;
}

function updateSessionUI() {
  const loggedIn = !!(state.session && state.session.token);
  sessionStatus.textContent = loggedIn ? "Signed in" : "Signed out";
  sessionUserId.textContent = loggedIn ? state.session.userId ?? "-" : "-";
  sessionProfileId.textContent = loggedIn ? state.session.profileId ?? "-" : "-";
  sessionEmail.textContent = loggedIn ? state.session.email ?? "-" : "-";
  sessionPhone.textContent = loggedIn ? state.session.phone ?? "-" : "-";
  signOutButton.disabled = !loggedIn;
  refreshProfileButton.disabled = !loggedIn;
  if (profileFillButton) {
    profileFillButton.disabled = !loggedIn;
  }
  cartRefreshButton.disabled = !loggedIn;
  cartClearButton.disabled = !loggedIn;
  if (orderTrackButton) {
    orderTrackButton.disabled = !loggedIn;
  }
  if (orderListButton) {
    orderListButton.disabled = !loggedIn;
  }
  if (orderStatusButton) {
    orderStatusButton.disabled = !loggedIn;
  }
  if (orderCancelButton) {
    orderCancelButton.disabled = !loggedIn;
  }
  if (useTestCardButton) {
    useTestCardButton.disabled = !loggedIn;
  }
  if (useDeclineCardButton) {
    useDeclineCardButton.disabled = !loggedIn;
  }
  if (inventoryCheckButton) {
    inventoryCheckButton.disabled = !loggedIn;
  }
  if (productLoadButton) {
    productLoadButton.disabled = !loggedIn;
  }
  if (productDeleteButton) {
    productDeleteButton.disabled = !loggedIn;
  }
  if (flowSeedButton) {
    flowSeedButton.disabled = !loggedIn;
  }
  if (flowAddButton) {
    flowAddButton.disabled = !loggedIn;
  }
  if (flowCheckoutButton) {
    flowCheckoutButton.disabled = !loggedIn;
  }
  if (flowClearButton) {
    flowClearButton.disabled = !loggedIn;
  }
  if (loggedIn && state.session.email) {
    orderEmail.value = state.session.email;
  }
}

async function parseJson(response) {
  const contentType = response.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    return response.json();
  }
  return null;
}

function buildHeaders(extra) {
  const headers = Object.assign({}, extra || {});
  if (!headers["Content-Type"]) {
    headers["Content-Type"] = "application/json";
  }
  if (state.session && state.session.token) {
    headers.Authorization = `Bearer ${state.session.token}`;
  }
  return headers;
}

async function apiRequest(path, options) {
  const config = Object.assign({
    method: "GET",
    headers: buildHeaders(options && options.headers),
  }, options || {});

  const timeoutMs = config.timeoutMs || 8000;
  delete config.timeoutMs;
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  config.signal = controller.signal;
  try {
    const response = await fetch(path, config);
    const data = await parseJson(response);

    if (!response.ok) {
      const message = data && (data.message || data.error) ? data.message || data.error : `Request failed (${response.status})`;
      throw new Error(message);
    }
    return data;
  } catch (error) {
    if (error.name === "AbortError") {
      throw new Error("Request timed out");
    }
    throw error;
  } finally {
    clearTimeout(timer);
  }
}

function buildSelectOptions(select, values, label) {
  select.innerHTML = "";
  const allOption = document.createElement("option");
  allOption.value = "";
  allOption.textContent = `All ${label}`;
  select.appendChild(allOption);

  values.forEach((value) => {
    const option = document.createElement("option");
    option.value = value;
    option.textContent = value;
    select.appendChild(option);
  });
}

function inventoryPercent(inventory) {
  if (inventory === null || inventory === undefined) {
    return 0;
  }
  const numeric = Number(inventory);
  if (!Number.isFinite(numeric)) {
    return 0;
  }
  const clamped = Math.max(0, Math.min(numeric, 120));
  return Math.round((clamped / 120) * 100);
}

function getStockInfo(stockValue) {
  const numeric = Number(stockValue);
  if (!Number.isFinite(numeric)) {
    return {
      label: "Stock n/a",
      countLabel: "Stock n/a",
      state: "unknown",
    };
  }
  if (numeric <= 0) {
    return {
      label: "Out of stock",
      countLabel: "Stock 0",
      state: "out",
    };
  }
  if (numeric <= 5) {
    return {
      label: "Low stock",
      countLabel: `Stock ${numeric}`,
      state: "low",
    };
  }
  return {
    label: "In stock",
    countLabel: `Stock ${numeric}`,
    state: "in",
  };
}

function buildMonogram(name, brand) {
  const source = `${name || ""} ${brand || ""}`.trim();
  const letters = source.match(/[A-Za-z0-9]/g) || [];
  const monogram = letters.slice(0, 2).join("").toUpperCase();
  return monogram || "MS";
}

function normalizeSearchQuery(value) {
  return (value || "").trim().toLowerCase();
}

function matchesSearch(product, query) {
  if (!query) {
    return true;
  }
  const haystack = [
    product.name,
    product.brand,
    product.category,
    product.description,
  ].filter(Boolean).join(" ").toLowerCase();
  return haystack.includes(query);
}

function updateChipActive() {
  if (!heroChips) {
    return;
  }
  const selected = categorySelect.value;
  heroChips.querySelectorAll(".ms-chip").forEach((chip) => {
    const value = chip.dataset.value || "";
    chip.classList.toggle("active", value === selected);
  });
}

function renderHeroChips(categories) {
  if (!heroChips) {
    return;
  }
  heroChips.innerHTML = "";
  const allChip = document.createElement("button");
  allChip.type = "button";
  allChip.className = "ms-chip";
  allChip.dataset.value = "";
  allChip.textContent = "All";
  allChip.addEventListener("click", () => {
    categorySelect.value = "";
    updateChipActive();
    applyFilters();
    scrollToCatalog();
  });
  heroChips.appendChild(allChip);

  categories.forEach((category) => {
    const chip = document.createElement("button");
    chip.type = "button";
    chip.className = "ms-chip";
    chip.dataset.value = category;
    chip.textContent = category;
    chip.addEventListener("click", () => {
      categorySelect.value = category;
      updateChipActive();
      applyFilters();
      scrollToCatalog();
    });
    heroChips.appendChild(chip);
  });

  updateChipActive();
}

function scrollToCatalog() {
  const catalog = document.getElementById("catalog");
  if (catalog) {
    catalog.scrollIntoView({ behavior: prefersReducedMotion ? "auto" : "smooth" });
  }
}

function buildCard(product, index) {
  const card = document.createElement("article");
  card.className = "ms-product-card";
  card.style.setProperty("--delay", `${index * 60}ms`);

  const stockInfo = getStockInfo(product.stock);

  const media = document.createElement("div");
  media.className = "ms-product-media";

  const badges = document.createElement("div");
  badges.className = "ms-product-badges";

  const stockBadge = document.createElement("span");
  stockBadge.className = "ms-badge";
  stockBadge.textContent = stockInfo.label;
  if (stockInfo.state) {
    stockBadge.dataset.state = stockInfo.state;
  }

  const categoryTag = document.createElement("span");
  categoryTag.className = "ms-tag";
  categoryTag.textContent = product.category || "Uncategorized";

  badges.appendChild(stockBadge);
  badges.appendChild(categoryTag);

  const image = document.createElement("div");
  image.className = "ms-product-image";
  image.textContent = buildMonogram(product.name, product.brand);

  const brand = document.createElement("div");
  brand.className = "ms-product-brand";
  brand.textContent = product.brand || "Independent";

  media.appendChild(badges);
  media.appendChild(image);
  media.appendChild(brand);

  const title = document.createElement("h3");
  title.textContent = product.name || "Unnamed product";

  const description = document.createElement("div");
  description.className = "ms-product-description";
  description.textContent = product.description || "No description available.";

  const metaRow = document.createElement("div");
  metaRow.className = "ms-product-meta-row";

  const sku = document.createElement("span");
  sku.textContent = product.id ? `SKU #${product.id}` : "SKU n/a";

  const stockMeta = document.createElement("span");
  stockMeta.textContent = stockInfo.countLabel;

  metaRow.appendChild(sku);
  metaRow.appendChild(stockMeta);

  const inventory = document.createElement("div");
  inventory.className = "ms-inventory";

  const inventoryLabel = document.createElement("span");
  inventoryLabel.textContent = stockInfo.state === "unknown"
    ? "Inventory unavailable"
    : "Inventory level";

  const bar = document.createElement("div");
  bar.className = "ms-inventory-bar";
  const barFill = document.createElement("div");
  barFill.style.width = `${inventoryPercent(product.stock)}%`;
  bar.appendChild(barFill);

  inventory.appendChild(inventoryLabel);
  inventory.appendChild(bar);

  const footer = document.createElement("div");
  footer.className = "ms-product-footer";

  const price = document.createElement("div");
  price.className = "ms-price";
  price.textContent = currency.format(Number(product.price || 0));

  const actions = document.createElement("div");
  actions.className = "ms-card-actions";
  const addButton = document.createElement("button");
  addButton.className = "ms-btn ms-btn-secondary";
  addButton.textContent = stockInfo.state === "out" ? "Out of stock" : "Add to cart";
  addButton.type = "button";
  addButton.disabled = stockInfo.state === "out";
  addButton.addEventListener("click", () => handleAddToCart(product.id));
  actions.appendChild(addButton);

  footer.appendChild(price);
  footer.appendChild(actions);

  card.appendChild(media);
  card.appendChild(title);
  card.appendChild(description);
  card.appendChild(metaRow);
  card.appendChild(inventory);
  card.appendChild(footer);

  requestAnimationFrame(() => {
    card.classList.add("is-visible");
  });

  return card;
}

function renderProducts(products) {
  grid.classList.remove("loading");
  grid.innerHTML = "";

  if (!products.length) {
    const empty = document.createElement("div");
    empty.className = "ms-empty-state";
    empty.textContent = "No products match the selected filters.";
    grid.appendChild(empty);
    countEl.textContent = "0 products";
    return;
  }

  products.forEach((product, index) => {
    grid.appendChild(buildCard(product, index));
  });

  countEl.textContent = `${products.length} products`;
}

function applyFilters() {
  const category = categorySelect.value;
  const brand = brandSelect.value;
  const query = normalizeSearchQuery(state.searchQuery);

  let filtered = state.products;
  if (category) {
    filtered = filtered.filter((product) => product.category === category);
  }
  if (brand) {
    filtered = filtered.filter((product) => product.brand === brand);
  }
  if (query) {
    filtered = filtered.filter((product) => matchesSearch(product, query));
  }
  renderProducts(filtered);
}

async function loadProducts() {
  try {
    setStatus("Syncing with product-service", "warn");
    const data = await apiRequest("/api/products", { timeoutMs: 8000, cache: "no-store" });
    const baseProducts = Array.isArray(data) ? data : [];
    state.products = await Promise.all(baseProducts.map(async (product) => {
      try {
        const stock = await apiRequest(`/api/products/${product.id}/stock`, { timeoutMs: 4000 });
        return Object.assign({}, product, { stock: stock.availableQuantity });
      } catch (error) {
        return Object.assign({}, product, { stock: null });
      }
    }));

    const categories = [...new Set(state.products.map((p) => p.category).filter(Boolean))].sort();
    const brands = [...new Set(state.products.map((p) => p.brand).filter(Boolean))].sort();

    buildSelectOptions(categorySelect, categories, "categories");
    buildSelectOptions(brandSelect, brands, "brands");
    renderHeroChips(categories);

    setStatus("Catalog live", "success");
    logActivity(`Catalog synced (${state.products.length} products)`, "success");
    applyFilters();
  } catch (error) {
    setStatus("Unable to reach product-service", "error");
    grid.classList.remove("loading");
    grid.innerHTML = "";
    const message = document.createElement("div");
    message.className = "ms-empty-state";
    message.textContent = "Catalog is unavailable. Check product-service logs.";
    grid.appendChild(message);
    countEl.textContent = "0 products";
    console.error(error);
    logActivity(`Catalog sync failed: ${error.message}`, "error");
  }
}

function setAuthMode(mode) {
  authTabs.forEach((tab) => {
    tab.classList.toggle("active", tab.dataset.mode === mode);
  });
  if (mode === "register") {
    registerForm.classList.remove("ms-hidden");
    loginForm.classList.add("ms-hidden");
  } else {
    loginForm.classList.remove("ms-hidden");
    registerForm.classList.add("ms-hidden");
  }
}

async function login(event) {
  event.preventDefault();
  setMessage(authMessage, "", "");
  const email = document.getElementById("loginEmail").value.trim();
  const password = document.getElementById("loginPassword").value.trim();

  try {
    const data = await apiRequest("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
    saveSession({
      token: data.accessToken,
      userId: data.userId,
      profileId: data.profileId,
      email,
      expiresAt: data.expiresAt,
    });
    setMessage(authMessage, "Signed in successfully.", "success");
    logActivity(`Signed in as ${email}`, "success");
    await fetchProfile();
    await fetchCart();
  } catch (error) {
    setMessage(authMessage, error.message, "error");
    logActivity(`Login failed: ${error.message}`, "error");
  }
}

async function register(event) {
  event.preventDefault();
  setMessage(authMessage, "", "");
  const firstName = document.getElementById("registerFirstName").value.trim();
  const lastName = document.getElementById("registerLastName").value.trim();
  const email = document.getElementById("registerEmail").value.trim();
  const phone = document.getElementById("registerPhone").value.trim();
  const password = document.getElementById("registerPassword").value.trim();

  if (!isPasswordValid(password)) {
    setFieldValidity(registerPassword, false);
    setMessage(authMessage, passwordPolicy.hint, "warn");
    if (registerPasswordHint) {
      registerPasswordHint.dataset.state = "warn";
    }
    if (registerPassword) {
      registerPassword.focus();
    }
    return;
  }
  setFieldValidity(registerPassword, true);
  if (registerPasswordHint) {
    registerPasswordHint.dataset.state = "success";
  }

  try {
    const payload = { email, password, firstName, lastName };
    if (phone) {
      payload.phone = phone;
    }
    const data = await apiRequest("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(payload),
    });
    saveSession({
      token: data.accessToken,
      userId: data.userId,
      profileId: data.profileId,
      email,
      phone: phone || null,
      expiresAt: data.expiresAt,
    });
    setMessage(authMessage, "Account created and signed in.", "success");
    logActivity(`Registered ${email}`, "success");
    await fetchProfile();
    await fetchCart();
  } catch (error) {
    setMessage(authMessage, error.message, "error");
    logActivity(`Register failed: ${error.message}`, "error");
  }
}

function signOut() {
  saveSession(null);
  setMessage(authMessage, "Signed out.", "success");
  setMessage(profileInfo, "", "");
  setMessage(profileUpdateMessage, "", "");
  setMessage(paymentMessage, "", "");
  setMessage(orderMessage, "", "");
  setMessage(orderResult, "", "");
  setMessage(orderListMessage, "", "");
  setMessage(orderManageMessage, "", "");
  setMessage(productMessage, "", "");
  setMessage(productEditMessage, "", "");
  cartItems.textContent = "Sign in to see your cart.";
  cartTotal.textContent = currency.format(0);
  cartCountEl.textContent = "0";
  if (orderList) {
    orderList.textContent = "Sign in to view orders.";
  }
  logActivity("Signed out", "warn");
}

function fillProfileForm(profile) {
  if (!profileForm || !profile) {
    return;
  }
  profileFirstName.value = profile.firstName || "";
  profileLastName.value = profile.lastName || "";
  profilePhone.value = profile.phone || "";
}

async function fetchProfile() {
  if (!requireSession("Sign in to load your profile.", profileInfo)) {
    return;
  }
  if (!state.session.profileId) {
    setMessage(profileInfo, "Profile ID is not available.", "warn");
    return;
  }
  try {
    const profile = await apiRequest(`/api/users/${state.session.profileId}`);
    state.profile = profile;
    fillProfileForm(profile);
    setMessage(profileInfo, `Profile loaded: ${profile.firstName} ${profile.lastName}`, "success");
    if (profile && Object.prototype.hasOwnProperty.call(profile, "phone")) {
      saveSession(Object.assign({}, state.session, { phone: profile.phone }));
    }
    logActivity("Profile loaded", "success");
  } catch (error) {
    setMessage(profileInfo, error.message, "error");
    logActivity(`Profile load failed: ${error.message}`, "error");
  }
}

async function updateProfile(event) {
  event.preventDefault();
  if (!requireSession("Sign in to update your profile.", profileUpdateMessage)) {
    return;
  }
  if (!state.session.profileId) {
    setMessage(profileUpdateMessage, "Profile ID is not available.", "warn");
    return;
  }
  const payload = {
    firstName: profileFirstName.value.trim(),
    lastName: profileLastName.value.trim(),
    phone: profilePhone.value.trim(),
  };
  try {
    const profile = await apiRequest(`/api/users/${state.session.profileId}`, {
      method: "PATCH",
      body: JSON.stringify(payload),
    });
    state.profile = profile;
    fillProfileForm(profile);
    setMessage(profileUpdateMessage, "Profile updated.", "success");
    saveSession(Object.assign({}, state.session, { phone: profile.phone, email: profile.email }));
    logActivity("Profile updated", "success");
  } catch (error) {
    setMessage(profileUpdateMessage, error.message, "error");
    logActivity(`Profile update failed: ${error.message}`, "error");
  }
}

async function handleAddToCart(productId) {
  if (!requireSession("Sign in to add items to your cart.", authMessage)) {
    setStatus("Sign in to add items", "warn");
    return;
  }
  try {
    await apiRequest("/api/cart/items", {
      method: "POST",
      body: JSON.stringify({
        userId: state.session.userId,
        productId,
        quantity: 1,
      }),
    });
    setMessage(cartMessage, "Item added to cart.", "success");
    logActivity(`Added product ${productId} to cart`, "success");
    await fetchCart();
  } catch (error) {
    setMessage(cartMessage, error.message, "error");
    logActivity(`Add to cart failed: ${error.message}`, "error");
  }
}

async function fetchCart() {
  if (!state.session || !state.session.userId) {
    cartItems.textContent = "Sign in to see your cart.";
    cartTotal.textContent = currency.format(0);
    cartCountEl.textContent = "0";
    return;
  }
  try {
    const data = await apiRequest(`/api/cart/${state.session.userId}`);
    state.cart = data;
    renderCart(data);
    logActivity("Cart refreshed", "success");
  } catch (error) {
    setMessage(cartMessage, error.message, "error");
    logActivity(`Cart refresh failed: ${error.message}`, "error");
  }
}

function renderCart(cart) {
  cartItems.innerHTML = "";
  setMessage(cartMessage, "", "");

  if (!cart || !cart.items || cart.items.length === 0) {
    cartItems.textContent = "Your cart is empty.";
    cartTotal.textContent = currency.format(0);
    cartCountEl.textContent = "0";
    return;
  }

  let count = 0;
  cart.items.forEach((item) => {
    count += item.quantity;
    const row = document.createElement("div");
    row.className = "ms-cart-item";

    const info = document.createElement("div");
    const title = document.createElement("div");
    title.className = "ms-cart-item-title";
    title.textContent = item.productName;
    const meta = document.createElement("div");
    meta.className = "ms-cart-item-meta";
    meta.textContent = `Unit: ${currency.format(Number(item.unitPrice || 0))}`;
    info.appendChild(title);
    info.appendChild(meta);

    const qty = document.createElement("input");
    qty.type = "number";
    qty.min = "1";
    qty.value = item.quantity;
    qty.dataset.productId = item.productId;
    qty.setAttribute("aria-label", `Quantity for ${item.productName}`);

    const lineTotal = document.createElement("div");
    lineTotal.textContent = currency.format(Number(item.lineTotal || 0));

    const actions = document.createElement("div");
    actions.className = "ms-inline-actions";
    const updateBtn = document.createElement("button");
    updateBtn.className = "ms-btn ms-btn-secondary";
    updateBtn.type = "button";
    updateBtn.textContent = "Update";
    updateBtn.addEventListener("click", () => updateCartQuantity(item.productId, qty.value));

    const removeBtn = document.createElement("button");
    removeBtn.className = "ms-btn ms-btn-ghost";
    removeBtn.type = "button";
    removeBtn.textContent = "Remove";
    removeBtn.addEventListener("click", () => removeFromCart(item.productId));

    actions.appendChild(updateBtn);
    actions.appendChild(removeBtn);

    row.appendChild(info);
    row.appendChild(qty);
    row.appendChild(lineTotal);
    row.appendChild(actions);

    cartItems.appendChild(row);
  });

  cartTotal.textContent = currency.format(Number(cart.totalAmount || 0));
  cartCountEl.textContent = String(count);
}

async function updateCartQuantity(productId, quantityValue) {
  const quantity = Number(quantityValue);
  if (Number.isNaN(quantity) || quantity < 1) {
    setMessage(cartMessage, "Quantity must be 1 or more.", "warn");
    return;
  }
  try {
    await apiRequest("/api/cart/items", {
      method: "DELETE",
      body: JSON.stringify({ userId: state.session.userId, productId }),
    });
    await apiRequest("/api/cart/items", {
      method: "POST",
      body: JSON.stringify({ userId: state.session.userId, productId, quantity }),
    });
    setMessage(cartMessage, "Cart updated.", "success");
    logActivity(`Updated cart item ${productId} to ${quantity}`, "success");
    await fetchCart();
  } catch (error) {
    setMessage(cartMessage, error.message, "error");
    logActivity(`Cart update failed: ${error.message}`, "error");
  }
}

async function removeFromCart(productId) {
  try {
    await apiRequest("/api/cart/items", {
      method: "DELETE",
      body: JSON.stringify({ userId: state.session.userId, productId }),
    });
    setMessage(cartMessage, "Item removed.", "success");
    logActivity(`Removed product ${productId} from cart`, "success");
    await fetchCart();
  } catch (error) {
    setMessage(cartMessage, error.message, "error");
    logActivity(`Remove from cart failed: ${error.message}`, "error");
  }
}

async function clearCart() {
  if (!requireSession("Sign in to clear your cart.", cartMessage)) {
    return;
  }
  try {
    await apiRequest(`/api/cart/${state.session.userId}`, {
      method: "DELETE",
    });
    setMessage(cartMessage, "Cart cleared.", "success");
    logActivity("Cart cleared", "success");
    await fetchCart();
  } catch (error) {
    setMessage(cartMessage, error.message, "error");
    logActivity(`Cart clear failed: ${error.message}`, "error");
  }
}

function buildPaymentPayload() {
  const amount = state.cart && state.cart.totalAmount ? Number(state.cart.totalAmount) : 0;
  return {
    amount,
    currency: "USD",
    cardNumber: normalizeDigits(cardNumber.value, 19),
    cardHolder: cardName.value.trim(),
    expiry: formatExpiryInput(cardExpiry.value),
    cvc: formatCvcInput(cardCvc.value),
    email: orderEmail.value.trim(),
    reference: `ui-${state.session.userId}-${Date.now()}`,
  };
}

async function chargePayment() {
  if (!requireSession("Sign in to checkout.", paymentMessage)) {
    return null;
  }
  setMessage(paymentMessage, "", "");
  if (!state.cart || !state.cart.items) {
    await fetchCart();
  }
  if (!state.cart || !state.cart.items || state.cart.items.length === 0) {
    setMessage(paymentMessage, "Cart is empty.", "warn");
    logActivity("Payment blocked: cart empty", "warn");
    return null;
  }
  const payload = buildPaymentPayload();
  if (!Number.isFinite(payload.amount) || payload.amount <= 0) {
    setMessage(paymentMessage, "Cart total is invalid.", "warn");
    return null;
  }
  if (!payload.cardNumber || !payload.cardHolder || !payload.expiry || !payload.cvc) {
    setMessage(paymentMessage, "Card details are required.", "warn");
    return null;
  }
  try {
    const payment = await apiRequest("/api/payments/charge", {
      method: "POST",
      body: JSON.stringify(payload),
    });
    if (payment.approved) {
      setMessage(
        paymentMessage,
        `Approved · ${payment.paymentId} · Risk ${payment.riskLevel}`,
        "success"
      );
      logActivity(`Payment approved (${payment.paymentId})`, "success");
      return payment;
    }
    setMessage(paymentMessage, `Declined: ${payment.message}`, "error");
    logActivity(`Payment declined (${payment.message})`, "error");
    return null;
  } catch (error) {
    setMessage(paymentMessage, error.message, "error");
    logActivity(`Payment failed: ${error.message}`, "error");
    return null;
  }
}

function fillTestCard(variant) {
  if (!cardNumber || !cardName || !cardExpiry || !cardCvc) {
    return;
  }
  if (variant === "decline") {
    cardNumber.value = formatCardNumberInput("4000000000000002");
  } else {
    cardNumber.value = formatCardNumberInput("4111111111111111");
  }
  cardName.value = "Test Operator";
  cardExpiry.value = formatExpiryInput("1230");
  cardCvc.value = formatCvcInput("123");
}

async function submitOrder() {
  if (!requireSession("Sign in to place an order.", orderMessage)) {
    return null;
  }
  const email = orderEmail.value.trim();
  if (!email) {
    setMessage(orderMessage, "Email is required for checkout.", "warn");
    return null;
  }
  const payment = await chargePayment();
  if (!payment) {
    return null;
  }
  try {
    const order = await apiRequest("/api/orders", {
      method: "POST",
      body: JSON.stringify({ userId: state.session.userId, email }),
    });
    setMessage(orderMessage, `Order placed. ID: ${order.orderId}`, "success");
    orderTrackId.value = order.orderId;
    renderOrder(order);
    await clearCart();
    logActivity(`Order placed (#${order.orderId})`, "success");
    if (orderList) {
      await fetchOrders();
    }
    return order;
  } catch (error) {
    setMessage(orderMessage, error.message, "error");
    logActivity(`Order failed: ${error.message}`, "error");
    return null;
  }
}

async function placeOrder(event) {
  event.preventDefault();
  await submitOrder();
}

function renderOrder(order) {
  if (!order) {
    return;
  }
  const lines = [];
  if (order.orderId) {
    lines.push(`Order: #${order.orderId}`);
  }
  lines.push(`Status: ${order.status}`);
  lines.push(`Total: ${currency.format(Number(order.totalAmount || 0))}`);
  if (order.items && order.items.length) {
    lines.push("Items:");
    order.items.forEach((item) => {
      lines.push(`- ${item.productName} x${item.quantity}`);
    });
  }
  orderResult.textContent = lines.join("\n");
  orderResult.dataset.state = "success";
  if (order.orderId) {
    state.lastOrderId = order.orderId;
    if (orderManageId) {
      orderManageId.value = order.orderId;
    }
  }
}

async function trackOrder() {
  if (!requireSession("Sign in to track orders.", orderResult)) {
    return;
  }
  const id = Number(orderTrackId.value);
  if (!id) {
    setMessage(orderResult, "Provide a valid order ID.", "warn");
    return;
  }
  try {
    const order = await apiRequest(`/api/orders/${id}`);
    renderOrder(order);
    logActivity(`Order tracked (#${id})`, "success");
  } catch (error) {
    setMessage(orderResult, error.message, "error");
    logActivity(`Order track failed: ${error.message}`, "error");
  }
}

function renderOrderList(orders) {
  if (!orderList) {
    return;
  }
  orderList.innerHTML = "";
  if (!orders || orders.length === 0) {
    orderList.textContent = "No orders yet.";
    return;
  }
  orders.forEach((order) => {
    const item = document.createElement("button");
    item.type = "button";
    item.className = "ms-order-pill";
    const id = document.createElement("span");
    id.textContent = `#${order.orderId}`;
    const status = document.createElement("strong");
    status.textContent = order.status;
    const total = document.createElement("small");
    total.textContent = currency.format(Number(order.totalAmount || 0));
    item.appendChild(id);
    item.appendChild(status);
    item.appendChild(total);
    item.addEventListener("click", () => {
      orderTrackId.value = order.orderId;
      if (orderManageId) {
        orderManageId.value = order.orderId;
      }
      renderOrder(order);
    });
    orderList.appendChild(item);
  });
}

async function fetchOrders() {
  if (!requireSession("Sign in to load orders.", orderListMessage)) {
    return;
  }
  try {
    const orders = await apiRequest(`/api/orders/user/${state.session.userId}`);
    const sorted = Array.isArray(orders)
      ? orders.slice().sort((a, b) => (b.orderId || 0) - (a.orderId || 0))
      : [];
    renderOrderList(sorted);
    setMessage(orderListMessage, `${sorted.length} orders loaded.`, "success");
    logActivity("Order list loaded", "success");
  } catch (error) {
    setMessage(orderListMessage, error.message, "error");
    logActivity(`Order list failed: ${error.message}`, "error");
  }
}

async function updateOrderStatus() {
  if (!requireSession("Sign in to update order status.", orderManageMessage)) {
    return;
  }
  const id = Number(orderManageId.value);
  if (!id) {
    setMessage(orderManageMessage, "Provide a valid order ID.", "warn");
    return;
  }
  const status = orderStatusSelect.value;
  try {
    const order = await apiRequest(`/api/orders/${id}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status }),
    });
    renderOrder(order);
    setMessage(orderManageMessage, `Order ${id} updated to ${order.status}.`, "success");
    await fetchOrders();
    logActivity(`Order ${id} status -> ${order.status}`, "success");
  } catch (error) {
    setMessage(orderManageMessage, error.message, "error");
    logActivity(`Order status failed: ${error.message}`, "error");
  }
}

async function cancelOrder() {
  if (!requireSession("Sign in to cancel orders.", orderManageMessage)) {
    return;
  }
  const id = Number(orderManageId.value);
  if (!id) {
    setMessage(orderManageMessage, "Provide a valid order ID.", "warn");
    return;
  }
  try {
    const order = await apiRequest(`/api/orders/${id}/cancel`, {
      method: "POST",
    });
    renderOrder(order);
    setMessage(orderManageMessage, `Order ${id} cancelled.`, "success");
    await fetchOrders();
    logActivity(`Order ${id} cancelled`, "warn");
  } catch (error) {
    setMessage(orderManageMessage, error.message, "error");
    logActivity(`Order cancel failed: ${error.message}`, "error");
  }
}

async function createProduct(event) {
  event.preventDefault();
  if (!requireSession("Sign in to create products.", productMessage)) {
    return;
  }
  const payload = {
    name: document.getElementById("productName").value.trim(),
    brand: document.getElementById("productBrand").value.trim(),
    category: document.getElementById("productCategory").value.trim(),
    description: document.getElementById("productDescription").value.trim(),
    price: Number(document.getElementById("productPrice").value),
    initialQuantity: Number(document.getElementById("productInitialQuantity").value),
  };

  try {
    const product = await apiRequest("/api/products", {
      method: "POST",
      body: JSON.stringify(payload),
    });
    setMessage(productMessage, `Product created: ${product.name}`, "success");
    state.lastProductId = product.id;
    if (productEditId) {
      productEditId.value = product.id;
    }
    if (productEditName) {
      productEditName.value = product.name || "";
      productEditBrand.value = product.brand || "";
      productEditCategory.value = product.category || "";
      productEditDescription.value = product.description || "";
      productEditPrice.value = product.price || "";
      productEditDelta.value = "";
    }
    logActivity(`Product created (#${product.id})`, "success");
    productForm.reset();
    await loadProducts();
  } catch (error) {
    setMessage(productMessage, error.message, "error");
    logActivity(`Product create failed: ${error.message}`, "error");
  }
}

async function loadProductForEdit() {
  if (!requireSession("Sign in to edit products.", productEditMessage)) {
    return;
  }
  const id = Number(productEditId.value);
  if (!id) {
    setMessage(productEditMessage, "Enter a valid product ID.", "warn");
    return;
  }
  try {
    const product = await apiRequest(`/api/products/${id}`);
    productEditName.value = product.name || "";
    productEditBrand.value = product.brand || "";
    productEditCategory.value = product.category || "";
    productEditDescription.value = product.description || "";
    productEditPrice.value = product.price || "";
    productEditDelta.value = "";
    setMessage(productEditMessage, `Loaded ${product.name}.`, "success");
    state.lastProductId = product.id;
    logActivity(`Product loaded (#${product.id})`, "success");
  } catch (error) {
    setMessage(productEditMessage, error.message, "error");
    logActivity(`Product load failed: ${error.message}`, "error");
  }
}

async function updateProduct(event) {
  event.preventDefault();
  if (!requireSession("Sign in to edit products.", productEditMessage)) {
    return;
  }
  const id = Number(productEditId.value);
  if (!id) {
    setMessage(productEditMessage, "Enter a valid product ID.", "warn");
    return;
  }
  const payload = {
    name: productEditName.value.trim(),
    brand: productEditBrand.value.trim(),
    category: productEditCategory.value.trim(),
    description: productEditDescription.value.trim(),
    price: Number(productEditPrice.value),
  };
  const deltaValue = productEditDelta.value.trim();
  if (deltaValue !== "") {
    payload.inventoryDelta = Number(deltaValue);
  }
  try {
    const product = await apiRequest(`/api/products/${id}`, {
      method: "PUT",
      body: JSON.stringify(payload),
    });
    setMessage(productEditMessage, `Updated ${product.name}.`, "success");
    productEditDelta.value = "";
    await loadProducts();
    logActivity(`Product updated (#${id})`, "success");
  } catch (error) {
    setMessage(productEditMessage, error.message, "error");
    logActivity(`Product update failed: ${error.message}`, "error");
  }
}

async function deleteProduct() {
  if (!requireSession("Sign in to delete products.", productEditMessage)) {
    return;
  }
  const id = Number(productEditId.value);
  if (!id) {
    setMessage(productEditMessage, "Enter a valid product ID.", "warn");
    return;
  }
  try {
    await apiRequest(`/api/products/${id}`, {
      method: "DELETE",
    });
    setMessage(productEditMessage, `Product ${id} deleted.`, "success");
    await loadProducts();
    logActivity(`Product deleted (#${id})`, "warn");
  } catch (error) {
    setMessage(productEditMessage, error.message, "error");
    logActivity(`Product delete failed: ${error.message}`, "error");
  }
}

async function checkInventory() {
  if (!requireSession("Sign in to check inventory.", inventoryMessage)) {
    return;
  }
  const productId = Number(inventoryProductId.value);
  if (!productId) {
    setMessage(inventoryMessage, "Enter a valid product ID.", "warn");
    return;
  }
  try {
    const item = await apiRequest(`/api/inventory/${productId}`);
    setMessage(inventoryMessage, `Available: ${item.availableQuantity}`, "success");
    logActivity(`Inventory checked (${productId})`, "success");
  } catch (error) {
    setMessage(inventoryMessage, error.message, "error");
    logActivity(`Inventory check failed: ${error.message}`, "error");
  }
}

async function seedDemoProduct() {
  if (!requireSession("Sign in to seed products.", productMessage)) {
    return;
  }
  const suffix = Math.floor(100 + Math.random() * 900);
  const payload = {
    name: `Demo Console ${suffix}`,
    brand: "MegaSega",
    description: "Demo build for flow testing.",
    price: 99.99,
    initialQuantity: 4,
    category: "Demo",
  };
  try {
    const product = await apiRequest("/api/products", {
      method: "POST",
      body: JSON.stringify(payload),
    });
    setMessage(productMessage, `Demo product created: ${product.name}`, "success");
    state.lastProductId = product.id;
    productEditId.value = product.id;
    productEditName.value = product.name || "";
    productEditBrand.value = product.brand || "";
    productEditCategory.value = product.category || "";
    productEditDescription.value = product.description || "";
    productEditPrice.value = product.price || "";
    productEditDelta.value = "";
    await loadProducts();
    logActivity(`Demo product seeded (#${product.id})`, "success");
  } catch (error) {
    setMessage(productMessage, error.message, "error");
    logActivity(`Demo seed failed: ${error.message}`, "error");
  }
}

async function addFeaturedToCart() {
  if (!requireSession("Sign in to add items.", cartMessage)) {
    return;
  }
  if (!state.products.length) {
    await loadProducts();
  }
  const product = state.products[0];
  if (!product) {
    setMessage(cartMessage, "No products available to add.", "warn");
    return;
  }
  await handleAddToCart(product.id);
}

async function quickCheckout() {
  if (cardNumber && !cardNumber.value.trim()) {
    fillTestCard("approve");
  }
  const order = await submitOrder();
  if (order) {
    await fetchOrders();
  }
}

bindCardInputFormatting();

categorySelect.addEventListener("change", () => {
  updateChipActive();
  applyFilters();
});
brandSelect.addEventListener("change", applyFilters);
if (searchInput) {
  searchInput.addEventListener("input", (event) => {
    state.searchQuery = event.target.value;
    applyFilters();
  });
}
if (searchForm) {
  searchForm.addEventListener("submit", (event) => {
    event.preventDefault();
    applyFilters();
    scrollToCatalog();
  });
}

catalogReloadButton.addEventListener("click", () => {
  setStatus("Retrying catalog", "warn");
  loadProducts();
});

loginForm.addEventListener("submit", login);
registerForm.addEventListener("submit", register);
if (registerPassword) {
  registerPassword.addEventListener("input", () => {
    const value = registerPassword.value.trim();
    if (!value) {
      setFieldValidity(registerPassword, true);
      if (registerPasswordHint) {
        delete registerPasswordHint.dataset.state;
      }
      return;
    }
    const isValid = isPasswordValid(value);
    setFieldValidity(registerPassword, isValid);
    if (registerPasswordHint) {
      registerPasswordHint.dataset.state = isValid ? "success" : "warn";
    }
  });
}
if (profileForm) {
  profileForm.addEventListener("submit", updateProfile);
}
if (profileFillButton) {
  profileFillButton.addEventListener("click", fetchProfile);
}

signOutButton.addEventListener("click", signOut);
refreshProfileButton.addEventListener("click", fetchProfile);

cartRefreshButton.addEventListener("click", fetchCart);
cartClearButton.addEventListener("click", clearCart);

orderForm.addEventListener("submit", placeOrder);
orderTrackButton.addEventListener("click", trackOrder);
if (useTestCardButton) {
  useTestCardButton.addEventListener("click", () => fillTestCard("approve"));
}
if (useDeclineCardButton) {
  useDeclineCardButton.addEventListener("click", () => fillTestCard("decline"));
}
if (orderListButton) {
  orderListButton.addEventListener("click", fetchOrders);
}
if (orderStatusButton) {
  orderStatusButton.addEventListener("click", updateOrderStatus);
}
if (orderCancelButton) {
  orderCancelButton.addEventListener("click", cancelOrder);
}

productForm.addEventListener("submit", createProduct);
inventoryCheckButton.addEventListener("click", checkInventory);
if (productEditForm) {
  productEditForm.addEventListener("submit", updateProduct);
}
if (productLoadButton) {
  productLoadButton.addEventListener("click", loadProductForEdit);
}
if (productDeleteButton) {
  productDeleteButton.addEventListener("click", deleteProduct);
}

if (flowSeedButton) {
  flowSeedButton.addEventListener("click", seedDemoProduct);
}
if (flowAddButton) {
  flowAddButton.addEventListener("click", addFeaturedToCart);
}
if (flowCheckoutButton) {
  flowCheckoutButton.addEventListener("click", quickCheckout);
}
if (flowClearButton) {
  flowClearButton.addEventListener("click", clearCart);
}
if (flowReloadButton) {
  flowReloadButton.addEventListener("click", loadProducts);
}

authTabs.forEach((tab) => {
  tab.addEventListener("click", () => setAuthMode(tab.dataset.mode));
});

setAuthMode("login");
updateSessionUI();
loadProducts();
fetchCart();
if (state.session && state.session.profileId) {
  fetchProfile();
}
if (state.session && state.session.userId && orderList) {
  fetchOrders();
}

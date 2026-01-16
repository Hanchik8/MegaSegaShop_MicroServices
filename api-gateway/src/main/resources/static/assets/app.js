const grid = document.getElementById("productGrid");
const statusEl = document.getElementById("catalogStatus");
const countEl = document.getElementById("catalogCount");
const categorySelect = document.getElementById("categoryFilter");
const brandSelect = document.getElementById("brandFilter");
const cartCountEl = document.getElementById("cartCount");
const catalogReloadButton = document.getElementById("catalogReloadButton");

const loginForm = document.getElementById("loginForm");
const registerForm = document.getElementById("registerForm");
const authMessage = document.getElementById("authMessage");
const sessionStatus = document.getElementById("sessionStatus");
const sessionUserId = document.getElementById("sessionUserId");
const sessionProfileId = document.getElementById("sessionProfileId");
const sessionEmail = document.getElementById("sessionEmail");
const sessionPhone = document.getElementById("sessionPhone");
const signOutButton = document.getElementById("signOutButton");
const refreshProfileButton = document.getElementById("refreshProfileButton");
const profileInfo = document.getElementById("profileInfo");

const cartItems = document.getElementById("cartItems");
const cartMessage = document.getElementById("cartMessage");
const cartTotal = document.getElementById("cartTotal");
const cartRefreshButton = document.getElementById("cartRefreshButton");
const cartClearButton = document.getElementById("cartClearButton");

const orderForm = document.getElementById("orderForm");
const orderEmail = document.getElementById("orderEmail");
const orderMessage = document.getElementById("orderMessage");
const orderTrackId = document.getElementById("orderTrackId");
const orderTrackButton = document.getElementById("orderTrackButton");
const orderResult = document.getElementById("orderResult");

const productForm = document.getElementById("productForm");
const productMessage = document.getElementById("productMessage");
const inventoryProductId = document.getElementById("inventoryProductId");
const inventoryCheckButton = document.getElementById("inventoryCheckButton");
const inventoryMessage = document.getElementById("inventoryMessage");

const authTabs = document.querySelectorAll(".auth-switch .tab");

const currency = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

const sessionKey = "megasegashop.session";
const state = {
  session: loadSession(),
  products: [],
  cart: null,
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

function setStatus(text, stateType) {
  statusEl.textContent = text;
  if (stateType) {
    statusEl.dataset.state = stateType;
  } else {
    delete statusEl.dataset.state;
  }
}

function requireSession(message, element) {
  if (!state.session || !state.session.token) {
    setMessage(element || authMessage, message || "Please sign in first.", "warn");
    document.getElementById("account").scrollIntoView({ behavior: "smooth" });
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
  cartRefreshButton.disabled = !loggedIn;
  cartClearButton.disabled = !loggedIn;
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

function buildCard(product, index) {
  const card = document.createElement("article");
  card.className = "product-card";
  card.style.setProperty("--delay", `${index * 60}ms`);

  const title = document.createElement("h3");
  title.textContent = product.name || "Unnamed product";

  const brand = document.createElement("div");
  brand.className = "brand";
  brand.textContent = product.brand || "Independent";

  const description = document.createElement("div");
  description.className = "description";
  description.textContent = product.description || "No description available.";

  const meta = document.createElement("div");
  meta.className = "product-meta";

  const price = document.createElement("div");
  price.className = "price";
  price.textContent = currency.format(Number(product.price || 0));

  const tag = document.createElement("div");
  tag.className = "tag";
  tag.textContent = product.category || "Uncategorized";

  meta.appendChild(price);
  meta.appendChild(tag);

  const inventory = document.createElement("div");
  inventory.className = "inventory";

  const inventoryLabel = document.createElement("span");
  inventoryLabel.textContent = `Stock: ${product.stock ?? "n/a"}`;

  const bar = document.createElement("div");
  bar.className = "inventory-bar";
  const barFill = document.createElement("div");
  barFill.style.width = `${inventoryPercent(product.stock)}%`;
  bar.appendChild(barFill);

  inventory.appendChild(inventoryLabel);
  inventory.appendChild(bar);

  const actions = document.createElement("div");
  actions.className = "card-actions";
  const addButton = document.createElement("button");
  addButton.className = "button secondary";
  addButton.textContent = "Add to cart";
  addButton.type = "button";
  addButton.addEventListener("click", () => handleAddToCart(product.id));
  actions.appendChild(addButton);

  card.appendChild(title);
  card.appendChild(brand);
  card.appendChild(description);
  card.appendChild(meta);
  card.appendChild(inventory);
  card.appendChild(actions);

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

  let filtered = state.products;
  if (category) {
    filtered = filtered.filter((product) => product.category === category);
  }
  if (brand) {
    filtered = filtered.filter((product) => product.brand === brand);
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

    setStatus("Catalog live", "success");
    applyFilters();
  } catch (error) {
    setStatus("Unable to reach product-service", "error");
    grid.classList.remove("loading");
    grid.innerHTML = "";
    const message = document.createElement("div");
    message.textContent = "Catalog is unavailable. Check product-service logs.";
    grid.appendChild(message);
    countEl.textContent = "0 products";
    console.error(error);
  }
}

function setAuthMode(mode) {
  authTabs.forEach((tab) => {
    tab.classList.toggle("active", tab.dataset.mode === mode);
  });
  if (mode === "register") {
    registerForm.classList.remove("hidden");
    loginForm.classList.add("hidden");
  } else {
    loginForm.classList.remove("hidden");
    registerForm.classList.add("hidden");
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
    await fetchProfile();
    await fetchCart();
  } catch (error) {
    setMessage(authMessage, error.message, "error");
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
    await fetchProfile();
    await fetchCart();
  } catch (error) {
    setMessage(authMessage, error.message, "error");
  }
}

function signOut() {
  saveSession(null);
  setMessage(authMessage, "Signed out.", "success");
  setMessage(profileInfo, "", "");
  cartItems.textContent = "Sign in to see your cart.";
  cartTotal.textContent = currency.format(0);
  cartCountEl.textContent = "0";
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
    setMessage(profileInfo, `Profile loaded: ${profile.firstName} ${profile.lastName}`, "success");
    if (profile && Object.prototype.hasOwnProperty.call(profile, "phone")) {
      saveSession(Object.assign({}, state.session, { phone: profile.phone }));
    }
  } catch (error) {
    setMessage(profileInfo, error.message, "error");
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
    await fetchCart();
  } catch (error) {
    setMessage(cartMessage, error.message, "error");
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
  } catch (error) {
    setMessage(cartMessage, error.message, "error");
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
    row.className = "cart-item";

    const info = document.createElement("div");
    const title = document.createElement("div");
    title.className = "cart-item-title";
    title.textContent = item.productName;
    const meta = document.createElement("div");
    meta.className = "cart-item-meta";
    meta.textContent = `Unit: ${currency.format(Number(item.unitPrice || 0))}`;
    info.appendChild(title);
    info.appendChild(meta);

    const qty = document.createElement("input");
    qty.type = "number";
    qty.min = "1";
    qty.value = item.quantity;
    qty.dataset.productId = item.productId;

    const lineTotal = document.createElement("div");
    lineTotal.textContent = currency.format(Number(item.lineTotal || 0));

    const actions = document.createElement("div");
    actions.className = "inline-actions";
    const updateBtn = document.createElement("button");
    updateBtn.className = "button secondary";
    updateBtn.type = "button";
    updateBtn.textContent = "Update";
    updateBtn.addEventListener("click", () => updateCartQuantity(item.productId, qty.value));

    const removeBtn = document.createElement("button");
    removeBtn.className = "button ghost";
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
    await fetchCart();
  } catch (error) {
    setMessage(cartMessage, error.message, "error");
  }
}

async function removeFromCart(productId) {
  try {
    await apiRequest("/api/cart/items", {
      method: "DELETE",
      body: JSON.stringify({ userId: state.session.userId, productId }),
    });
    setMessage(cartMessage, "Item removed.", "success");
    await fetchCart();
  } catch (error) {
    setMessage(cartMessage, error.message, "error");
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
    await fetchCart();
  } catch (error) {
    setMessage(cartMessage, error.message, "error");
  }
}

async function placeOrder(event) {
  event.preventDefault();
  if (!requireSession("Sign in to place an order.", orderMessage)) {
    return;
  }
  const email = orderEmail.value.trim();
  if (!email) {
    setMessage(orderMessage, "Email is required for checkout.", "warn");
    return;
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
  } catch (error) {
    setMessage(orderMessage, error.message, "error");
  }
}

function renderOrder(order) {
  if (!order) {
    return;
  }
  const lines = [];
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
  } catch (error) {
    setMessage(orderResult, error.message, "error");
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
    productForm.reset();
    await loadProducts();
  } catch (error) {
    setMessage(productMessage, error.message, "error");
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
  } catch (error) {
    setMessage(inventoryMessage, error.message, "error");
  }
}

categorySelect.addEventListener("change", applyFilters);
brandSelect.addEventListener("change", applyFilters);

catalogReloadButton.addEventListener("click", () => {
  setStatus("Retrying catalog", "warn");
  loadProducts();
});

loginForm.addEventListener("submit", login);
registerForm.addEventListener("submit", register);

signOutButton.addEventListener("click", signOut);
refreshProfileButton.addEventListener("click", fetchProfile);

cartRefreshButton.addEventListener("click", fetchCart);
cartClearButton.addEventListener("click", clearCart);

orderForm.addEventListener("submit", placeOrder);
orderTrackButton.addEventListener("click", trackOrder);

productForm.addEventListener("submit", createProduct);
inventoryCheckButton.addEventListener("click", checkInventory);

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

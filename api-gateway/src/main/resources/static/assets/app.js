const grid = document.getElementById("productGrid");
const statusEl = document.getElementById("catalogStatus");
const countEl = document.getElementById("catalogCount");
const categorySelect = document.getElementById("categoryFilter");
const brandSelect = document.getElementById("brandFilter");

const currency = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

let allProducts = [];

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
  const numeric = Number(inventory);
  if (Number.isNaN(numeric)) {
    return 40;
  }
  const clamped = Math.max(6, Math.min(numeric, 120));
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
  inventoryLabel.textContent = `Stock: ${product.inventory ?? "n/a"}`;

  const bar = document.createElement("div");
  bar.className = "inventory-bar";
  const barFill = document.createElement("div");
  barFill.style.width = `${inventoryPercent(product.inventory)}%`;
  bar.appendChild(barFill);

  inventory.appendChild(inventoryLabel);
  inventory.appendChild(bar);

  card.appendChild(title);
  card.appendChild(brand);
  card.appendChild(description);
  card.appendChild(meta);
  card.appendChild(inventory);

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

  let filtered = allProducts;
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
    statusEl.textContent = "Syncing with product-service";
    const response = await fetch("/api/products");
    if (!response.ok) {
      throw new Error(`Request failed (${response.status})`);
    }
    const data = await response.json();
    allProducts = Array.isArray(data) ? data : [];

    const categories = [...new Set(allProducts.map((p) => p.category).filter(Boolean))].sort();
    const brands = [...new Set(allProducts.map((p) => p.brand).filter(Boolean))].sort();

    buildSelectOptions(categorySelect, categories, "categories");
    buildSelectOptions(brandSelect, brands, "brands");

    statusEl.textContent = "Catalog live";
    applyFilters();
  } catch (error) {
    statusEl.textContent = "Unable to reach product-service";
    grid.classList.remove("loading");
    grid.innerHTML = "";
    const message = document.createElement("div");
    message.textContent = "Catalog is unavailable. Check product-service logs.";
    grid.appendChild(message);
    countEl.textContent = "0 products";
    console.error(error);
  }
}

categorySelect.addEventListener("change", applyFilters);
brandSelect.addEventListener("change", applyFilters);

loadProducts();

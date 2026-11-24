/* =====================================================
   HarvestHub - Frontend JavaScript
   Handles event handling (form submits, button clicks),
   calls the Java backend API, and updates the page.
   ===================================================== */

// The Java backend runs on localhost:8080
const API = 'http://localhost:8080/api';

// ---- Session helper: store logged-in user in localStorage ----
function saveSession(user) {
  localStorage.setItem('harvesthub_user', JSON.stringify(user));
}

function getSession() {
  const data = localStorage.getItem('harvesthub_user')
  return data ? JSON.parse(data) : null;
}

function clearSession() {
  localStorage.removeItem('harvesthub_user');
}

function requireRole(role) {
  const user = getSession();

  if (!user || user.role !== role) {
    alert('Please login as ' + role + ' to access this page.');
    window.location.href = 'login.html';
    return null;
  }

  return user;
}

// ---- Cart helper: store cart in localStorage ----
function getCart() {
  const data = localStorage.getItem('harvesthub_cart');
  return data ? JSON.parse(data) : [];
}

function saveCart(cart) {
  localStorage.setItem('harvesthub_cart', JSON.stringify(cart));
}

function clearCart() {
  localStorage.removeItem('harvesthub_cart');
}

// ---- Show a message inside an element with id="message" ----
function showMessage(text, type) {
  const box = document.getElementById('message');

  if (!box) return;

  box.textContent = text;
  box.className = 'message show ' + (type || 'info');
}

// ---- Generic fetch wrapper that sends JSON and parses JSON response ----
async function apiPost(url, body) {
  const res = await fetch(API + url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(body)
  });

  return res.json();
}

async function apiPut(url, body) {
  const res = await fetch(API + url, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(body)
  });

  return res.json();
}

async function apiDelete(url) {
  const res = await fetch(API + url, {
    method: 'DELETE'
  });

  return res.json();
}

async function apiGet(url) {
  const res = await fetch(API + url);
  return res.json();
}


// =====================================================
// EVENT HANDLING
// =====================================================

// ---- HOME PAGE ----
function initHome() {
  // Nothing dynamic needed
}


// ---- REGISTER PAGE ----
function initRegister() {

  const form = document.getElementById('registerForm');

  if (!form) return;

  form.addEventListener('submit', async function (e) {

    e.preventDefault();

    const name = document.getElementById('name').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const role = document.getElementById('role').value;

    if (!name || !email || !password || !role) {

      showMessage(
        'All fields are required.',
        'error'
      );

      return;
    }

    if (!email.includes('@') || !email.includes('.')) {

      showMessage(
        'Please enter a valid email address.',
        'error'
      );

      return;
    }

    if (password.length < 5) {

      showMessage(
        'Password must be at least 5 characters.',
        'error'
      );

      return;
    }

    const data = await apiPost('/register', {
      name,
      email,
      password,
      role
    });

    if (data.success) {

      showMessage(
        'Registration successful! Redirecting to login...',
        'success'
      );

      setTimeout(
        () => window.location.href = 'login.html',
        1500
      );

    } else {

      showMessage(
        data.message || 'Registration failed.',
        'error'
      );
    }
  });
}


// ---- LOGIN PAGE ----
function initLogin() {

  const form = document.getElementById('loginForm');

  if (!form) return;

  form.addEventListener('submit', async function (e) {

    e.preventDefault();

    const email =
      document.getElementById('email').value.trim();

    const password =
      document.getElementById('password').value;

    if (!email || !password) {

      showMessage(
        'Please enter email and password.',
        'error'
      );

      return;
    }

    const data = await apiPost('/login', {
      email,
      password
    });

    if (data.success) {

      saveSession({
        userId: data.userId,
        name: data.name,
        role: data.role
      });

      if (data.role === 'farmer') {

        window.location.href =
          'farmer-dashboard.html';

      } else {

        window.location.href =
          'products.html';
      }

    } else {

      showMessage(
        data.message || 'Invalid login.',
        'error'
      );
    }
  });
}


// =====================================================
// AI PRODUCT IMAGE
// =====================================================



 function getProductImage(name) {
    const product = name.toLowerCase().trim();

    const images = {
        apple: "images/products/apple.jpg",
        mango: "images/products/mango.jpg",
        banana: "images/products/banana.jpg",
        orange: "images/products/orange.jpg",
        grapes: "images/products/grapes.jpg",

        tomato: "images/products/tomato.jpg",
        potato: "images/products/potato.jpg",
        carrot: "images/products/carrot.jpg",
        onion: "images/products/onion.jpg",
        spinach: "images/products/spinach.jpg",

        rice: "images/products/rice.jpg",
        wheat: "images/products/wheat.jpg",
        ragi: "images/products/ragi.jpg",
        maize: "images/products/maize.jpg",
        millet: "images/products/millet.jpg"
    };

    for (const key in images) {
        if (product.includes(key)) {
            return images[key];
        }
    }

    return "";
}
// =====================================================
// PRODUCTS PAGE
// =====================================================

async function initProducts() {

  const grid =
    document.getElementById('productGrid');

  const searchInput =
    document.getElementById('searchInput');

  const searchBtn =
    document.getElementById('searchBtn');

  if (!grid) return;


  async function loadProducts(keyword) {

    let url = '/products';

    if (keyword && keyword.trim()) {

      url +=
        '?search=' +
        encodeURIComponent(
          keyword.trim()
        );
    }

    const products =
      await apiGet(url);

    renderProducts(products);
  }


  function renderProducts(products) {

    if (!products || products.length === 0) {

      grid.innerHTML = `
        <div class="empty-state">
          <h3>No products found</h3>
          <p>
            Try a different search or check back later.
          </p>
        </div>
      `;

      return;
    }


    grid.innerHTML = products.map(p => `

      <div class="card">

        <img
          src="${p.imageUrl || getProductImage(p.productName)}"
          alt="${p.productName}"
          style="
            width:100%;
            height:200px;
            object-fit:cover;
            border-radius:12px 12px 0 0;
          "
        >

        <div class="card-body">

          <span class="category">
            ${p.category}
          </span>

          <h3>
            ${p.productName}
          </h3>

          <p class="price">
            &#8377;${p.price} / kg
          </p>

          

          <p class="farmer">
            By: ${p.farmerName || 'Farmer'}
          </p>

          <div
            style="
              margin-top:12px;
              display:flex;
              gap:8px;
            "
          >

            <button
              class="btn btn-primary btn-sm"
              onclick="addToCart(${p.productId})"
            >
              Add to Cart
            </button>

            <a
              class="btn btn-outline btn-sm"
              style="
                border-color:var(--primary);
                color:var(--primary-dark);
              "
              href="product-details.html?id=${p.productId}"
            >
              Details
            </a>

          </div>

        </div>

      </div>

    `).join('');
  }


  // Search button click

  if (searchBtn) {

    searchBtn.addEventListener(
      'click',
      () => loadProducts(searchInput.value)
    );
  }


  // Enter key search

  if (searchInput) {

    searchInput.addEventListener(
      'keypress',
      (e) => {

        if (e.key === 'Enter') {

          loadProducts(
            searchInput.value
          );
        }
      }
    );
  }


  loadProducts();
}


// =====================================================
// ADD TO CART
// =====================================================

function addToCart(productId) {

  fetch(
    API + '/product?id=' + productId
  )

    .then(r => r.json())

    .then(p => {

      if (!p || p.error) {

        alert('Product not found.');

        return;
      }

      const cart = getCart();

      const existing =
        cart.find(
          i => i.productId === productId
        );


      if (existing) {

        existing.quantity += 1;

      } else {

        cart.push({

          productId: p.productId,

          productName: p.productName,

          price: p.price,

          quantity: 1

        });
      }


      saveCart(cart);

      alert(
        p.productName +
        ' added to cart!'
      );
    });
}


// =====================================================
// PRODUCT DETAILS PAGE
// =====================================================

async function initProductDetails() {

  const box =
    document.getElementById('detailsBox');

  if (!box) return;


  const params =
    new URLSearchParams(
      window.location.search
    );

  const id =
    params.get('id');


  if (!id) {

    box.innerHTML =
      '<p>Product not found.</p>';

    return;
  }


  const p =
    await apiGet(
      '/product?id=' + id
    );


  if (!p || p.error) {

    box.innerHTML = `
      <div class="empty-state">
        <h3>Product not found</h3>
      </div>
    `;

    return;
  }


  box.innerHTML = `

    <div
      class="card"
      style="
        max-width:600px;
        margin:0 auto;
      "
    >

      <img
        src="${p.imageUrl || getProductImage(p.productName)}"
        alt="${p.productName}"
        style="
          width:100%;
          height:300px;
          object-fit:cover;
          border-radius:12px 12px 0 0;
        "
      >

      <div class="card-body">

        <span class="category">
          ${p.category}
        </span>

        <h2
          style="
            color:var(--primary-dark);
          "
        >
          ${p.productName}
        </h2>

        <p
          style="
            font-size:24px;
            color:var(--accent);
            font-weight:700;
            margin:10px 0;
          "
        >
          &#8377;${p.price} / kg
        </p>

        

        <p>
          <strong>
            Sold by:
          </strong>
          ${p.farmerName || 'Farmer'}
        </p>

        <p style="margin-top:12px;">

          <strong>
            Description:
          </strong>

          <br>

          ${p.description || 'No description available.'}

        </p>

        <div style="margin-top:20px;">

          <button
            class="btn btn-primary"
            onclick="addToCart(${p.productId})"
          >
            Add to Cart
          </button>

          <a
            href="products.html"
            class="btn btn-outline"
            style="
              border-color:var(--primary);
              color:var(--primary-dark);
            "
          >
            Back to Products
          </a>

        </div>

      </div>

    </div>

  `;
}


// =====================================================
// CART PAGE
// =====================================================

function initCart() {

  const box =
    document.getElementById('cartBox');

  const totalBox =
    document.getElementById('cartTotal');

  if (!box) return;


  const user = getSession();


  if (!user) {

    box.innerHTML = `
      <div class="empty-state">

        <h3>Please login</h3>

        <p>
          <a href="login.html">
            Login here
          </a>
          to view your cart.
        </p>

      </div>
    `;

    return;
  }


  function render() {

    const cart = getCart();


    if (cart.length === 0) {

      box.innerHTML = `
        <div class="empty-state">

          <h3>Your cart is empty</h3>

          <p>
            <a href="products.html">
              Browse products
            </a>
            to add items.
          </p>

        </div>
      `;

      if (totalBox) {

        totalBox.textContent = '';
      }

      return;
    }


    let total = 0;


    box.innerHTML =
      cart.map(
        (item, idx) => {

          const lineTotal =
            item.price *
            item.quantity;

          total += lineTotal;


          return `

            <div class="cart-item">

              <div class="info">

                <h4>
                  ${item.productName}
                </h4>

                <p>
                  &#8377;${item.price}
                  x
                  ${item.quantity}
                  =
                  &#8377;${lineTotal.toFixed(2)}
                </p>

              </div>


              <div class="qty-controls">

                <button
                  onclick="changeQty(${idx}, -1)"
                >
                  -
                </button>

                <span>
                  ${item.quantity}
                </span>

                <button
                  onclick="changeQty(${idx}, 1)"
                >
                  +
                </button>

                <button
                  class="btn btn-danger btn-sm"
                  style="margin-left:10px;"
                  onclick="removeFromCart(${idx})"
                >
                  Remove
                </button>

              </div>

            </div>

          `;
        }
      ).join('');


    if (totalBox) {

      totalBox.innerHTML =
        `Total: &#8377;${total.toFixed(2)}`;
    }
  }


  render();
}


// ---- Change quantity in cart ----

function changeQty(index, delta) {

  const cart = getCart();

  if (!cart[index]) return;


  cart[index].quantity += delta;


  if (cart[index].quantity <= 0) {

    cart.splice(index, 1);
  }


  saveCart(cart);

  location.reload();
}


// ---- Remove item from cart ----

function removeFromCart(index) {

  const cart = getCart();

  cart.splice(index, 1);

  saveCart(cart);

  location.reload();
}


// =====================================================
// PLACE ORDER
// =====================================================

async function placeOrder() {

  const user = getSession();


  if (!user) {

    alert('Please login first.');

    window.location.href =
      'login.html';

    return;
  }


  const cart = getCart();


  if (cart.length === 0) {

    alert(
      'Your cart is empty. Add products before placing an order.'
    );

    return;
  }


  const customerName =
    document.getElementById(
      'customerName'
    ).value.trim();


  const paymentMethod =
    document.querySelector(
      'input[name="paymentMethod"]:checked'
    ).value;


  const customerPhone =
    document.getElementById(
      'customerPhone'
    ).value.trim();


  const deliveryAddress =
    document.getElementById(
      'deliveryAddress'
    ).value.trim();


  const deliveryCity =
    document.getElementById(
      'deliveryCity'
    ).value.trim();


  const deliveryPincode =
    document.getElementById(
      'deliveryPincode'
    ).value.trim();


  if (
    !customerName ||
    !customerPhone ||
    !deliveryAddress ||
    !deliveryCity ||
    !deliveryPincode
  ) {

    alert(
      'Please fill all delivery details.'
    );

    return;
  }


  const items =
    cart.map(i => ({

      productId: i.productId,

      quantity: i.quantity,

      price: i.price,

      productName: i.productName

    }));


  const data =
    await apiPost('/order', {

      customerId:
        user.userId,

      paymentMethod:
        paymentMethod,

      customerName:
        customerName,

      customerPhone:
        customerPhone,

      deliveryAddress:
        deliveryAddress,

      deliveryCity:
        deliveryCity,

      deliveryPincode:
        deliveryPincode,

      items:
        items

    });


  if (data.success) {

    alert(
      'Order placed successfully! Order ID: ' +
      data.orderId
    );

    clearCart();

    window.location.href =
      'orders.html';

  } else {

    alert(
      data.message ||
      'Could not place order.'
    );
  }
}


// =====================================================
// CUSTOMER ORDER HISTORY
// =====================================================

async function initOrders() {

  const box =
    document.getElementById(
      'ordersBox'
    );

  if (!box) return;


  const user =
    requireRole('customer');


  if (!user) return;


  const orders =
    await apiGet(
      '/orders/customer?customerId=' +
      user.userId
    );


  if (!orders || orders.length === 0) {

    box.innerHTML = `
      <div class="empty-state">

        <h3>No orders yet</h3>

        <p>
          <a href="products.html">
            Start shopping
          </a>
          to place your first order.
        </p>

      </div>
    `;

    return;
  }


  box.innerHTML = `

    <div class="table-wrap">

      <table>

        <thead>

          <tr>

            <th>Order ID</th>

            <th>Date</th>

            <th>Total Amount</th>

            <th>Status</th>

            <th>Review</th>

          </tr>

        </thead>


        <tbody>

          ${orders.map(o => `

            <tr>

              <td>
                #${o.orderId}
              </td>

              <td>
                ${o.orderDate}
              </td>

              <td>
                &#8377;${o.totalAmount.toFixed(2)}
              </td>

              <td>

                <span
                  class="status-badge status-${o.status.toLowerCase()}"
                >
                  ${o.status}
                </span>

              </td>

              <td>

                <button
                  class="btn btn-primary"
                  onclick="openReview(${o.orderId})"
                >
                  ⭐ Give Review
                </button>

              </td>

            </tr>

          `).join('')}

        </tbody>

      </table>

    </div>

  `;
}


// =====================================================
// FARMER DASHBOARD
// =====================================================

async function initFarmerDashboard() {

  const nameEl =
    document.getElementById(
      'farmerName'
    );

  if (!nameEl) return;


  const user =
    requireRole('farmer');


  if (!user) return;


  nameEl.textContent =
    user.name;


  const products =
    await apiGet(
      '/farmer/products?farmerId=' +
      user.userId
    );


  const productCountEl =
    document.getElementById(
      'productCount'
    );


  if (productCountEl) {

    productCountEl.textContent =
      products.length;
  }
  if (productCountEl) {

  productCountEl.textContent =
    products.length;
}


// =====================================================
// LOW STOCK / OUT OF STOCK ALERT
// =====================================================

const lowStockLimit = 10;

const stockAlerts = products.filter(
  p => Number(p.quantity) <= lowStockLimit
);

if (stockAlerts.length > 0) {

  const alertBox =
    document.createElement('div');

  alertBox.id = 'stockAlertBox';

  alertBox.style.margin = '20px 0';
  alertBox.style.padding = '18px';
  alertBox.style.borderRadius = '10px';
  alertBox.style.background = '#fff3cd';
  alertBox.style.border = '1px solid #ffc107';

  let alertHTML = `
    <h3 style="margin-top:0;">
      ⚠️ Stock Alert
    </h3>
  `;

  stockAlerts.forEach(p => {

    const quantity =
      Number(p.quantity);

    if (quantity <= 0) {

      alertHTML += `
        <p style="color:#dc3545;font-weight:bold;">
          🔴 ${p.productName} — Out of Stock
        </p>
      `;

    } else {

      alertHTML += `
        <p style="color:#d97706;font-weight:bold;">
          🟠 ${p.productName} — Low Stock: ${quantity} kg left
        </p>
      `;
    }
  });

  alertBox.innerHTML = alertHTML;

  const main =
    document.querySelector('main');

  if (main) {

    main.insertBefore(
      alertBox,
      main.firstChild
    );

  } else {

    document.body.insertBefore(
      alertBox,
      document.body.firstChild
    );
  }
}


  const orders =
    await apiGet(
      '/orders/farmer?farmerId=' +
      user.userId
    );


  const orderCountEl =
    document.getElementById(
      'orderCount'
    );


  if (orderCountEl) {

    orderCountEl.textContent =
      orders.length;
  }


  const reviews =
    await apiGet(
      '/reviews/farmer?farmerId=' +
      user.userId
    );


  const reviewsBox =
    document.getElementById(
      'farmerReviewsBox'
    );


  if (reviewsBox) {

    if (!reviews || reviews.length === 0) {

      reviewsBox.innerHTML = `

        <div class="empty-state">

          <h3>
            No reviews yet
          </h3>

          <p>
            Customer reviews will appear here.
          </p>

        </div>

      `;

    } else {

      reviewsBox.innerHTML = `

        <div class="table-wrap">

          <table>

            <thead>

              <tr>

                <th>Product</th>

                <th>Rating</th>

                <th>Review</th>

                <th>Date</th>

              </tr>

            </thead>


            <tbody>

              ${reviews.map(r => `

                <tr>

                  <td>
                    ${r.productName}
                  </td>

                  <td>
                    ${'⭐'.repeat(r.rating)}
                  </td>

                  <td>
                    ${r.reviewText}
                  </td>

                  <td>
                    ${r.reviewDate}
                  </td>

                </tr>

              `).join('')}

            </tbody>

          </table>

        </div>

      `;
    }
  }
}


// =====================================================
// ADD PRODUCT PAGE
// =====================================================

function initAddProduct() {

  const form =
    document.getElementById(
      'productForm'
    );

  if (!form) return;


  const user =
    requireRole('farmer');


  if (!user) return;


  form.addEventListener(
    'submit',
    async function (e) {

      e.preventDefault();


      const productName =
        document.getElementById(
          'productName'
        ).value.trim();


      const category =
        document.getElementById(
          'category'
        ).value;


      const price =
        document.getElementById(
          'price'
        ).value;


      const quantity =
        document.getElementById(
          'quantity'
        ).value;


      const description =
        document.getElementById(
          'description'
        ).value.trim();


      if (
        !productName ||
        !category ||
        !price ||
        !quantity
      ) {

        showMessage(
          'All fields except description are required.',
          'error'
        );

        return;
      }


      if (parseFloat(price) <= 0) {

        showMessage(
          'Price must be greater than zero.',
          'error'
        );

        return;
      }


      if (parseInt(quantity) <= 0) {

        showMessage(
          'Quantity must be greater than zero.',
          'error'
        );

        return;
      }


      const data =
        await apiPost(
          '/farmer/products',
          {

            farmerId:
              user.userId,

            productName:
              productName,

            category:
              category,

            price:
              price,

            quantity:
              quantity,

            description:
              description,

            imageUrl:
              getProductImage(
                productName
              )
          }
        );


      if (data.success) {

        showMessage(
          'Product added successfully! Redirecting...',
          'success'
        );

        setTimeout(
          () =>
            window.location.href =
              'manage-products.html',
          1500
        );

      } else {

        showMessage(
          data.message ||
          'Could not add product.',
          'error'
        );
      }
    }
  );
}


// =====================================================
// MANAGE PRODUCTS PAGE
// =====================================================

async function initManageProducts() {

  const box =
    document.getElementById(
      'manageBox'
    );

  if (!box) return;


  const user =
    requireRole('farmer');


  if (!user) return;


  const products =
    await apiGet(
      '/farmer/products?farmerId=' +
      user.userId
    );


  if (!products || products.length === 0) {

    box.innerHTML = `

      <div class="empty-state">

        <h3>
          No products yet
        </h3>

        <p>
          <a href="add-product.html">
            Add your first product
          </a>
        </p>

      </div>

    `;

    return;
  }


  box.innerHTML = `

    <div class="table-wrap">

      <table>

        <thead>

          <tr>
<th>ID</th>
<th>Image</th>
<th>Name</th>
<th>Category</th>
<th>Price</th>
<th>Available Stock</th>
<th>Description</th>
<th>Actions</th>

          </tr>

        </thead>


        <tbody>

          ${products.map(p => `

            <tr id="row-${p.productId}">

    <td>
        ${p.productId}
    </td>

    <td>
        <img
            src="${p.imageUrl || getProductImage(p.productName)}"
            alt="${p.productName}"
            style="width:60px;height:60px;object-fit:cover;border-radius:8px;"
        >
    </td>

    <td>
        ${p.productName}
    </td>

    <td>
        ${p.category}
    </td>

    <td>
        &#8377;${p.price}
    </td>

    <td>
        <strong>${p.quantity} kg</strong>
    </td>

    <td>
        ${p.description || 'No description'}
    </td>
              <td>

                <button
                  class="btn btn-primary btn-sm"
                  onclick="editProduct(${p.productId})"
                >
                  Edit
                </button>

                <button
                  class="btn btn-danger btn-sm"
                  onclick="deleteProduct(${p.productId})"
                >
                  Delete
                </button>

              </td>

            </tr>

          `).join('')}

        </tbody>

      </table>

    </div>

    <div id="editBox"></div>

  `;
}


// =====================================================
// EDIT PRODUCT
// =====================================================

async function editProduct(productId) {

  const user =
    getSession();


  const p =
    await apiGet(
      '/product?id=' +
      productId
    );


  const box =
    document.getElementById(
      'editBox'
    );


  box.innerHTML = `

    <div
      class="form-card wide"
      style="margin-top:20px;"
    >

      <h2>
        Edit Product
      </h2>


      <form id="editForm">

        <div class="form-group">

          <label>
            Product Name
          </label>

          <input
            type="text"
            id="editName"
            value="${p.productName}"
          >

        </div>


        <div class="form-group">

          <label>
            Category
          </label>

          <select id="editCategory">

            <option
              ${p.category === 'Vegetable'
                ? 'selected'
                : ''}
            >
              Vegetable
            </option>

            <option
              ${p.category === 'Fruit'
                ? 'selected'
                : ''}
            >
              Fruit
            </option>

            <option
              ${p.category === 'Grain'
                ? 'selected'
                : ''}
            >
              Grain
            </option>

          </select>

        </div>


        <div class="form-group">

          <label>
            Price (per kg)
          </label>

          <input
            type="number"
            step="0.01"
            id="editPrice"
            value="${p.price}"
          >

        </div>


        <div class="form-group">

          <label>
            Quantity (kg)
          </label>

          <input
            type="number"
            id="editQty"
            value="${p.quantity}"
          >

        </div>


        <div class="form-group">

          <label>
            Description
          </label>

          <textarea
            id="editDesc"
          >${p.description || ''}</textarea>

        </div>


        <div class="form-actions">

          <button
            type="submit"
            class="btn btn-success"
          >
            Update Product
          </button>

          <button
            type="button"
            class="btn btn-danger"
            onclick="document.getElementById('editBox').innerHTML=''"
          >
            Cancel
          </button>

        </div>

      </form>

    </div>

  `;


  document
    .getElementById('editForm')
    .addEventListener(
      'submit',
      async function (e) {

        e.preventDefault();


        const data =
          await apiPut(
            '/farmer/products',
            {

              productId:
                productId,

              farmerId:
                user.userId,

              productName:
                document
                  .getElementById('editName')
                  .value
                  .trim(),

              category:
                document
                  .getElementById('editCategory')
                  .value,

              price:
                document
                  .getElementById('editPrice')
                  .value,

              quantity:
                document
                  .getElementById('editQty')
                  .value,

              description:
                document
                  .getElementById('editDesc')
                  .value
                  .trim(),

              imageUrl:
                p.imageUrl ||
                getProductImage(
                  document
                    .getElementById('editName')
                    .value
                    .trim()
                )
            }
          );


        if (data.success) {

          alert(
            'Product updated!'
          );

          location.reload();

        } else {

          alert(
            data.message ||
            'Update failed.'
          );
        }
      }
    );
}


// =====================================================
// DELETE PRODUCT
// =====================================================

async function deleteProduct(productId) {

  const user =
    getSession();


  if (
    !confirm(
      'Are you sure you want to delete this product?'
    )
  ) {

    return;
  }


  const data =
    await apiDelete(
      '/farmer/products?id=' +
      productId +
      '&farmerId=' +
      user.userId
    );


  if (data.success) {

    alert(
      'Product deleted.'
    );

    location.reload();

  } else {

    alert(
      data.message ||
      'Delete failed.'
    );
  }
}


// =====================================================
// FARMER ORDERS
// =====================================================

async function initFarmerOrders() {

  const box =
    document.getElementById(
      'farmerOrdersBox'
    );

  if (!box) return;


  const user =
    requireRole('farmer');


  if (!user) return;


  const orders =
    await apiGet(
      '/orders/farmer?farmerId=' +
      user.userId
    );


  if (!orders || orders.length === 0) {

    box.innerHTML = `

      <div class="empty-state">

        <h3>
          No orders received yet
        </h3>

        <p>
          When customers buy your products,
          orders will appear here.
        </p>

      </div>

    `;

    return;
  }


  box.innerHTML = `

    <div class="table-wrap">

      <table>

        <thead>

          <tr>

            <th>
              Order ID
            </th>

            <th>
              Customer
            </th>

            <th>
              Delivery Details
            </th>

            <th>
              Date
            </th>

            <th>
              Products Ordered
            </th>

            <th>
              Amount
            </th>

            <th>
              Status
            </th>

          </tr>

        </thead>


        <tbody>

          ${orders.map(o => `

            <tr>

              <td>
                #${o.orderId}
              </td>


              <td>
                ${o.customerName || 'Customer'}
              </td>


              <td>

                <strong>
                  Phone:
                </strong>

                ${o.customerPhone || '-'}

                <br>

                <strong>
                  Address:
                </strong>

                ${o.deliveryAddress || '-'}

                <br>

                <strong>
                  City:
                </strong>

                ${o.deliveryCity || '-'}

                <br>

                <strong>
                  Pincode:
                </strong>

                ${o.deliveryPincode || '-'}

              </td>


              <td>
                ${o.orderDate}
              </td>


              <td>

                ${
                  o.items &&
                  o.items.length > 0

                    ?

                    o.items.map(item => `

                      <div
                        style="margin-bottom:8px;"
                      >

                        <strong>
                          ${item.productName}
                        </strong>

                        <br>

                        Quantity:
                        ${item.quantity}

                        <br>

                        Price:
                        &#8377;${item.price.toFixed(2)}

                      </div>

                    `).join('')

                    :

                    'No product details'
                }

              </td>


              <td>

                &#8377;${o.totalAmount.toFixed(2)}

              </td>


              <td>

                <span
                  class="status-badge status-${o.status.toLowerCase()}"
                >
                  ${o.status}
                </span>

              </td>

            </tr>

          `).join('')}

        </tbody>

      </table>

    </div>

  `;
}


// =====================================================
// LOGOUT
// =====================================================

function logout() {

  clearSession();

  window.location.href =
    'index.html';
}


// =====================================================
// NAVBAR
// =====================================================

function updateNavbar() {

  const navUser =
    document.getElementById(
      'navUser'
    );

  const navLogout =
    document.getElementById(
      'navLogout'
    );

  const navProducts =
    document.getElementById(
      'navProducts'
    );

  const navCart =
    document.getElementById(
      'navCart'
    );

  const navOrders =
    document.getElementById(
      'navOrders'
    );

  const navDashboard =
    document.getElementById(
      'navDashboard'
    );

  const navAddProduct =
    document.getElementById(
      'navAddProduct'
    );

  const navManage =
    document.getElementById(
      'navManage'
    );


  const user =
    getSession();


  if (user) {

    if (navUser) {

      navUser.textContent =
        'Hi, ' + user.name;
    }


    if (navLogout) {

      navLogout.style.display =
        'inline-block';
    }


    if (user.role === 'customer') {

      if (navProducts) {

        navProducts.style.display =
          'inline-block';
      }

      if (navCart) {

        navCart.style.display =
          'inline-block';
      }

      if (navOrders) {

        navOrders.style.display =
          'inline-block';
      }
    }


    if (user.role === 'farmer') {

      if (navDashboard) {

        navDashboard.style.display =
          'inline-block';
      }

      if (navAddProduct) {

        navAddProduct.style.display =
          'inline-block';
      }

      if (navManage) {

        navManage.style.display =
          'inline-block';
      }
    }

  } else {

    if (navLogout) {

      navLogout.style.display =
        'none';
    }
  }
}


// =====================================================
// REVIEW
// =====================================================

function openReview(orderId) {

  const rating =
    prompt(
      'Give a rating from 1 to 5 stars:\n\n' +
      '1 = ⭐\n' +
      '2 = ⭐⭐\n' +
      '3 = ⭐⭐⭐\n' +
      '4 = ⭐⭐⭐⭐\n' +
      '5 = ⭐⭐⭐⭐⭐'
    );


  if (rating === null) return;


  const ratingNumber =
    parseInt(rating);


  if (
    isNaN(ratingNumber) ||
    ratingNumber < 1 ||
    ratingNumber > 5
  ) {

    alert(
      'Please enter a rating between 1 and 5.'
    );

    return;
  }


  const reviewText =
    prompt(
      'Write your review:'
    );


  if (
    reviewText === null ||
    reviewText.trim() === ''
  ) {

    alert(
      'Please write a review.'
    );

    return;
  }


  submitReview(
    orderId,
    ratingNumber,
    reviewText.trim()
  );
}


async function submitReview(
  orderId,
  rating,
  reviewText
) {

  const user =
    getSession();


  if (!user) {

    alert(
      'Please login first.'
    );

    return;
  }


  const data =
    await apiPost(
      '/review',
      {

        orderId:
          orderId,

        customerId:
          user.userId,

        rating:
          rating,

        reviewText:
          reviewText
      }
    );


  if (data.success) {

    alert(
      '⭐ Review submitted successfully!'
    );

  } else {

    alert(
      data.message ||
      'Could not submit review.'
    );
  }
}


// =====================================================
// RUN ON EVERY PAGE
// =====================================================

document.addEventListener(
  'DOMContentLoaded',
  function () {

    updateNavbar();

    initHome();

    initRegister();

    initLogin();

    initProducts();

    initProductDetails();

    initCart();

    initOrders();

    initFarmerDashboard();

    initAddProduct();

    initManageProducts();

    initFarmerOrders();

  }
);
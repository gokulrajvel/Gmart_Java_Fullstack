import { initNotificationSocket } from './notifications.js';

function getSavedTheme() {
    try {
        return localStorage.getItem('theme') || 'dark';
    } catch (e) {
        return 'dark';
    }
}

function setSavedTheme(theme) {
    try {
        localStorage.setItem('theme', theme);
    } catch (e) {
        console.warn('localStorage is not accessible:', e);
    }
}

function getSessionUser() {
    try {
        const userStr = window.cookies.get('user');
        return userStr ? JSON.parse(userStr) : null;
    } catch (e) {
        console.warn('Cookies are not accessible:', e);
        return null;
    }
}

function removeSessionUser() {
    try {
        window.cookies.remove('user');
        sessionStorage.removeItem('clientToken');
    } catch (e) {
        console.warn('Cookies/Session storage is not accessible:', e);
    }
}

function updateThemeIcon(theme) {
    const themeIcon = document.getElementById('themeIcon');
    if (themeIcon) {
        if (theme === 'light') {
            themeIcon.className = 'fas fa-moon';
        } else {
            themeIcon.className = 'fas fa-sun';
        }
    }
}

function initTheme() {
    const theme = getSavedTheme();
    document.documentElement.setAttribute('data-theme', theme);
    updateThemeIcon(theme);
}

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme') || 'dark';
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', newTheme);
    setSavedTheme(newTheme);
    updateThemeIcon(newTheme);
}

document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    
    // Bind theme button handler
    const themeToggleBtn = document.getElementById('themeToggleBtn');
    if (themeToggleBtn) {
        themeToggleBtn.addEventListener('click', toggleTheme);
    }

    const user = getSessionUser();
    if (!user) {
        window.location.href = 'index.html';
        return;
    }

    document.getElementById('userName').textContent = user.username;
    document.getElementById('userRoleBadge').textContent = user.role;

    // Apply strict Role-Based Access Control (RBAC)
    applyRBAC(user.role);

    // Initialize real-time WebSocket notifications
    initNotificationSocket();

    // Initial load
    updateStats();
    showSection('overview');
});

function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    const icon = type === 'success' 
        ? '<i class="fas fa-circle-check" style="color: var(--success); font-size: 1.15rem;"></i>' 
        : '<i class="fas fa-circle-xmark" style="color: var(--danger); font-size: 1.15rem;"></i>';
    
    toast.innerHTML = `${icon} <span style="font-size: 0.9rem; font-weight: 500;">${message}</span>`;
    toast.style.borderColor = type === 'success' ? 'var(--success-glow)' : 'var(--danger-glow)';
    toast.style.boxShadow = type === 'success' 
        ? 'var(--shadow), 0 0 15px var(--success-glow)' 
        : 'var(--shadow), 0 0 15px var(--danger-glow)';
        
    toast.classList.remove('hidden');
    setTimeout(() => toast.classList.add('hidden'), 3500);
}

function applyRBAC(role) {
    const permissions = {
        'nav-overview': ['ADMIN', 'BILLING_STAFF', 'WAREHOUSE', 'PURCHASING_MANAGER'],
        'nav-inventory': ['ADMIN', 'BILLING_STAFF', 'WAREHOUSE', 'PURCHASING_MANAGER'],
        'nav-suppliers': ['ADMIN', 'PURCHASING_MANAGER'],
        'nav-reports': ['ADMIN', 'PURCHASING_MANAGER'],
        'nav-users': ['ADMIN'],
        'nav-billing': ['ADMIN', 'BILLING_STAFF'],
        'nav-transactions': ['ADMIN', 'WAREHOUSE']
    };

    Object.keys(permissions).forEach(id => {
        const item = document.getElementById(id);
        if (item) {
            if (!permissions[id].includes(role)) {
                item.remove(); // Completely remove from DOM for safety
            }
        }
    });
}

function showSection(sectionId) {
    // Hide all sections
    document.querySelectorAll('section').forEach(s => s.classList.add('hidden'));
    
    const targetSection = document.getElementById(`${sectionId}Section`);
    if (targetSection) {
        targetSection.classList.remove('hidden');
    }
    
    // Update nav active state
    document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));
    const navItem = document.getElementById(`nav-${sectionId}`);
    if (navItem) navItem.classList.add('active');

    // Update title
    const titles = {
        overview: 'Dashboard Overview',
        inventory: 'Inventory & Stock',
        suppliers: 'Vendor Directory',
        reports: 'Sales Analytics & Reports',
        users: 'Employee Management',
        billing: 'Point of Sale',
        transactions: 'Movement History'
    };
    document.getElementById('sectionTitle').textContent = titles[sectionId] || 'Dashboard';

    loadSectionData(sectionId);
}

let currentCart = [];
let pendingBillData = null;
let loadedBills = [];

async function addToCartBySku() {
    const skuInput = document.getElementById('billingSkuInput');
    const sku = skuInput.value.trim();
    if (!sku) return;

    try {
        const product = await api.getProduct(sku);
        const existing = currentCart.find(item => item.product.id === product.id);
        
        if (existing) {
            if (existing.quantity + 1 > product.stockQuantity) {
                showToast('Insufficient stock!', 'error');
                return;
            }
            existing.quantity++;
        } else {
            if (product.stockQuantity < 1) {
                showToast('Product out of stock!', 'error');
                return;
            }
            currentCart.push({ product, quantity: 1 });
        }
        
        skuInput.value = '';
        updateCartUI();
    } catch (e) {
        showToast('Product not found or error occurred', 'error');
    }
}

function updateCartUI() {
    const tbody = document.querySelector('#cartTable tbody');
    tbody.innerHTML = currentCart.map((item, index) => {
        const baseVal = item.product.price * item.quantity;
        const discountPct = item.product.discount || 0;
        const gstPct = item.product.gst || 0;
        const discVal = baseVal * (discountPct / 100);
        const finalItemVal = baseVal - discVal;
        const taxVal = finalItemVal * (gstPct / 100);
        const itemTotal = finalItemVal + taxVal;

        return `
        <tr>
            <td>
                <strong style="color: var(--text-main);">${item.product.name}</strong>
                <div style="font-size: 0.72rem; color: var(--text-muted); margin-top: 0.25rem; line-height: 1.4;">
                    <div>Base: ₹${item.product.price.toFixed(2)} × ${item.quantity} = ₹${baseVal.toFixed(2)}</div>
                    ${discountPct > 0 ? `<div style="color: var(--success);"><i class="fas fa-tags" style="font-size: 0.65rem;"></i> Discount (-${discountPct}%): -₹${discVal.toFixed(2)} (Taxable: ₹${finalItemVal.toFixed(2)})</div>` : ''}
                    ${gstPct > 0 ? `<div><i class="fas fa-percent" style="font-size: 0.65rem;"></i> GST (+${gstPct}%): +₹${taxVal.toFixed(2)}</div>` : ''}
                </div>
            </td>
            <td>₹${item.product.price.toFixed(2)}</td>
            <td>
                <input type="number" value="${item.quantity}" min="1" max="${item.product.stockQuantity}" 
                    onchange="updateCartQuantity(${index}, this.value)" class="quantity-input">
            </td>
            <td>
                <span style="font-weight: 600; color: var(--text-main);">₹${itemTotal.toFixed(2)}</span>
            </td>
            <td style="text-align: right;">
                <button class="btn btn-sm btn-danger" onclick="removeFromCart(${index})" style="padding: 0.35rem 0.6rem; border-radius: 0.5rem; background: transparent; border: 1px solid var(--danger); color: var(--danger); box-shadow: none;">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `; }).join('');

    let subtotal = 0;
    let totalDiscount = 0;
    let totalTax = 0;

    currentCart.forEach(item => {
        const baseVal = item.product.price * item.quantity;
        const discVal = baseVal * ((item.product.discount || 0) / 100);
        const finalVal = baseVal - discVal;
        const taxVal = finalVal * ((item.product.gst || 0) / 100);

        subtotal += baseVal;
        totalDiscount += discVal;
        totalTax += taxVal;
    });

    const total = subtotal - totalDiscount + totalTax;

    document.getElementById('cartSubtotal').textContent = `₹${subtotal.toFixed(2)}`;
    document.getElementById('cartDiscount').textContent = `-₹${totalDiscount.toFixed(2)}`;
    document.getElementById('cartTax').textContent = `₹${totalTax.toFixed(2)}`;
    document.getElementById('cartTotal').textContent = `₹${total.toFixed(2)}`;
}

function updateCartQuantity(index, qty) {
    const quantity = parseInt(qty);
    if (isNaN(quantity) || quantity <= 0) {
        showToast('Quantity must be greater than zero', 'error');
        currentCart[index].quantity = 1;
    } else if (quantity > currentCart[index].product.stockQuantity) {
        showToast('Only ' + currentCart[index].product.stockQuantity + ' available', 'error');
        currentCart[index].quantity = currentCart[index].product.stockQuantity;
    } else {
        currentCart[index].quantity = quantity;
    }
    updateCartUI();
}

function removeFromCart(index) {
    currentCart.splice(index, 1);
    updateCartUI();
}

function clearCart() {
    currentCart = [];
    updateCartUI();
}

async function finalizeSale() {
    if (currentCart.length === 0) {
        showToast('Cart is empty!', 'error');
        return;
    }

    const user = getSessionUser();
    if (!user) {
        showToast('Session expired, please login again', 'error');
        return;
    }

    let subtotal = 0;
    let totalDiscount = 0;
    let totalTax = 0;
    let confirmItemsHTML = '';

    currentCart.forEach(item => {
        const baseVal = item.product.price * item.quantity;
        const discountPct = item.product.discount || 0;
        const discVal = baseVal * (discountPct / 100);
        const taxableVal = baseVal - discVal;
        const gstPct = item.product.gst || 0;
        const taxVal = taxableVal * (gstPct / 100);
        const finalItemVal = taxableVal + taxVal;

        subtotal += baseVal;
        totalDiscount += discVal;
        totalTax += taxVal;

        // Render confirm item row with base, gst, offer/discount explicitly detailed
        confirmItemsHTML += `
            <div class="confirm-item-row">
                <div class="confirm-item-title">
                    <span>${item.product.name}</span>
                    <span>₹${finalItemVal.toFixed(2)}</span>
                </div>
                <div class="confirm-item-details">
                    <span>Base: ₹${item.product.price.toFixed(2)} × ${item.quantity} = ₹${baseVal.toFixed(2)}</span>
                    <span style="color: var(--success); font-weight: 500;">
                        ${discountPct > 0 ? `Offer (-${discountPct}%): -₹${discVal.toFixed(2)}` : 'No Offer'}
                    </span>
                </div>
                <div class="confirm-item-details" style="margin-top: 0.1rem;">
                    <span>GST: ${gstPct}% (+₹${taxVal.toFixed(2)})</span>
                    <span>Taxable: ₹${taxableVal.toFixed(2)}</span>
                </div>
            </div>
        `;
    });

    const total = subtotal - totalDiscount + totalTax;
    const paymentMethod = document.getElementById('paymentMethod').value;

    // Create the final bill payload structure
    pendingBillData = {
        userId: user.id,
        totalAmount: total,
        taxAmount: totalTax,
        paymentMethod: paymentMethod,
        billItems: currentCart.map(item => {
            const baseVal = item.product.price * item.quantity;
            const discVal = baseVal * ((item.product.discount || 0) / 100);
            const finalVal = baseVal - discVal;
            return {
                productId: item.product.id,
                productName: item.product.name,
                quantity: item.quantity,
                priceAtSale: finalVal / item.quantity
            };
        })
    };

    // Render confirm totals box
    const confirmTotalsHTML = `
        <div class="confirm-total-row">
            <span>Items Base Total</span>
            <span>₹${subtotal.toFixed(2)}</span>
        </div>
        <div class="confirm-total-row" style="color: var(--success); font-weight: 500;">
            <span>Offers & Discounts</span>
            <span>-₹${totalDiscount.toFixed(2)}</span>
        </div>
        <div class="confirm-total-row">
            <span>GST Tax Amount</span>
            <span>+₹${totalTax.toFixed(2)}</span>
        </div>
        <div class="confirm-total-row grand-total">
            <span>Total Payable Amount</span>
            <span>₹${total.toFixed(2)}</span>
        </div>
    `;

    // Populate modal and show
    document.getElementById('confirmBillItems').innerHTML = confirmItemsHTML;
    document.getElementById('confirmBillTotals').innerHTML = confirmTotalsHTML;

    const confirmModal = document.getElementById('checkoutConfirmModal');
    if (confirmModal) {
        confirmModal.classList.add('active');
    }
}

function closeConfirmModal() {
    const confirmModal = document.getElementById('checkoutConfirmModal');
    if (confirmModal) {
        confirmModal.classList.remove('active');
    }
    pendingBillData = null;
}

async function executeCheckout() {
    if (!pendingBillData) {
        showToast('No pending checkout found', 'error');
        return;
    }

    const user = getSessionUser();
    if (!user) {
        showToast('Session expired, please login again', 'error');
        return;
    }

    // Build the final bill payload including employee/user metadata
    const finalBill = {
        userId: user.id,
        totalAmount: pendingBillData.totalAmount,
        taxAmount: pendingBillData.taxAmount,
        paymentMethod: pendingBillData.paymentMethod,
        billItems: pendingBillData.billItems
    };

    // Store a copy of currentCart locally in case we clear it before loop finishes
    const cartToRecord = [...currentCart];

    // Close confirm modal immediately to give snappy response
    const confirmModal = document.getElementById('checkoutConfirmModal');
    if (confirmModal) {
        confirmModal.classList.remove('active');
    }

    try {
        await api.createBill(finalBill);
        
        // Record transactions for each item
        for (const item of cartToRecord) {
            await api.recordTransaction({
                productId: item.product.id,
                userId: user.id,
                transactionType: 'OUTWARD',
                quantity: item.quantity
            });
        }

        const successModal = document.getElementById('checkoutSuccessModal');
        if (successModal) {
            successModal.classList.add('active');
            setTimeout(() => {
                successModal.classList.remove('active');
            }, 3000);
        }
        
        clearCart();
        updateStats();
    } catch (e) {
        showToast('Failed to finalize sale: ' + e.message, 'error');
    } finally {
        pendingBillData = null;
    }
}

function showTableSkeleton(selector, colCount, rowCount = 5) {
    const tbody = document.querySelector(selector);
    if (!tbody) return;
    
    let rowsHtml = '';
    for (let i = 0; i < rowCount; i++) {
        rowsHtml += `
            <tr class="skeleton-row">
                ${Array(colCount).fill(0).map(() => `
                    <td>
                        <div class="skeleton skeleton-line"></div>
                    </td>
                `).join('')}
            </tr>
        `;
    }
    tbody.innerHTML = rowsHtml;
}

function showStatsSkeleton() {
    const statIds = ['totalProducts', 'totalCategories', 'lowStockCount', 'outOfStockCount', 'totalSuppliers', 'totalTransactions'];
    statIds.forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.innerHTML = `<span class="skeleton skeleton-text" style="width: 3rem; height: 1.5rem; border-radius: 4px;"></span>`;
        }
    });
}

async function loadSectionData(sectionId) {
    try {
        switch(sectionId) {
            case 'overview': await updateStats(); break;
            case 'inventory': await loadInventory(); break;
            case 'users': await loadUsers(); break;
            case 'suppliers': await loadSuppliers(); break;
            case 'reports': await loadReports(); break;
            case 'transactions': await loadTransactions(); break;
        }
    } catch (e) {
        showToast('Failed to load ' + sectionId + ' data', 'error');
    }
}

async function updateStats() {
    showStatsSkeleton();
    const [products, suppliers, transactions] = await Promise.all([
        api.getProducts(),
        api.getSuppliers(),
        api.getTransactions()
    ]);
    
    const uniqueCategories = new Set(products.map(p => p.categoryId).filter(id => id !== null && id !== undefined));
    
    document.getElementById('totalProducts').textContent = products.length;
    document.getElementById('totalCategories').textContent = uniqueCategories.size;
    document.getElementById('lowStockCount').textContent = products.filter(p => p.stockQuantity > 0 && p.stockQuantity < 10).length;
    document.getElementById('outOfStockCount').textContent = products.filter(p => p.stockQuantity === 0).length;
    document.getElementById('totalSuppliers').textContent = suppliers.length;
    document.getElementById('totalTransactions').textContent = transactions.length;
}

function renderProductTable(productsToRender, supplierMap) {
    const tbody = document.querySelector('#productTable tbody');
    if (!tbody) return;
    
    const user = getSessionUser();
    const isAdmin = user && user.role === 'ADMIN';
    const isBillingStaff = user && user.role === 'BILLING_STAFF';

    const categoryNames = {
        0: 'GROCERIES',
        1: 'HOUSEHOLD_ITEMS',
        2: 'APPAREL',
        3: 'ELECTRONICS'
    };

    tbody.innerHTML = productsToRender.map(p => {
        if (isBillingStaff) {
            return `
                <tr>
                    <td><strong style="color: var(--text-main);">${p.skuCode}</strong></td>
                    <td>${p.name}</td>
                    <td>₹${p.price.toFixed(2)}</td>
                </tr>
            `;
        }

        const supplierName = supplierMap[p.supplierId] || `Supplier #${p.supplierId || 'N/A'}`;
        const categoryName = categoryNames[p.categoryId] || 'N/A';
        
        const actionHtml = isAdmin 
            ? `<div style="display: inline-flex; gap: 0.5rem; align-items: center;">
                <button class="btn btn-sm btn-primary" onclick="stockAdjust('${p.id}', 10)" title="Add 10"><i class="fas fa-plus"></i></button>
                <button class="btn btn-sm btn-danger" onclick="stockAdjust('${p.id}', -10)" title="Remove 10" style="background: transparent; border: 1px solid var(--danger); color: var(--danger); box-shadow: none;"><i class="fas fa-minus"></i></button>
                <button class="btn btn-sm btn-primary" onclick="openEditProductModal('${p.id}')" title="Edit Product" style="background: transparent; border: 1px solid var(--primary); color: var(--primary); box-shadow: none;"><i class="fas fa-edit"></i></button>
               </div>`
            : `<div style="display: inline-flex; gap: 0.5rem; align-items: center;">
                <button class="btn btn-sm btn-primary" onclick="stockAdjust('${p.id}', 10)" title="Add 10"><i class="fas fa-plus"></i></button>
                <button class="btn btn-sm btn-danger" onclick="stockAdjust('${p.id}', -10)" title="Remove 10" style="background: transparent; border: 1px solid var(--danger); color: var(--danger); box-shadow: none;"><i class="fas fa-minus"></i></button>
               </div>`;

        let statusClass = 'badge-success';
        let statusText = 'Optimal';
        if (p.stockQuantity === 0) {
            statusClass = 'badge-danger';
            statusText = 'Out of Stock';
        } else if (p.stockQuantity < 10) {
            statusClass = 'badge-warning';
            statusText = 'Low Stock';
        }

        return `
            <tr>
                <td><strong style="color: var(--text-main);">${p.skuCode}</strong></td>
                <td>${p.name}</td>
                <td><span class="badge" style="background: rgba(255,255,255,0.05); color: var(--text-main); border: 1px solid var(--border-color);">${categoryName}</span></td>
                <td>${supplierName}</td>
                <td>₹${p.price.toFixed(2)}</td>
                <td>${p.discount ? p.discount.toFixed(1) : '0.0'}%</td>
                <td>${p.gst ? p.gst.toFixed(1) : '0.0'}%</td>
                <td>${p.stockQuantity}</td>
                <td>
                    <span class="badge ${statusClass}">
                        ${statusText}
                    </span>
                </td>
                <td style="text-align: right; padding-right: 2rem;">
                    ${actionHtml}
                </td>
            </tr>
        `;
    }).join('');
}

function filterInventory() {
    const searchQuery = (document.getElementById('inventorySearchInput')?.value || '').toLowerCase();
    const categoryFilter = document.getElementById('inventoryCategoryFilter')?.value || '';
    
    let filtered = window.allProductsList || [];
    
    if (searchQuery) {
        filtered = filtered.filter(p => 
            p.name.toLowerCase().includes(searchQuery) || 
            p.skuCode.toLowerCase().includes(searchQuery)
        );
    }
    
    if (categoryFilter !== '') {
        const catId = parseInt(categoryFilter);
        filtered = filtered.filter(p => p.categoryId === catId);
    }
    
    renderProductTable(filtered, window.currentSupplierMap || {});
}

async function loadInventory() {
    showTableSkeleton('#productTable tbody', 10, 5);
    const [products, suppliers] = await Promise.all([
        api.getProducts(),
        api.getSuppliers()
    ]);
    
    window.allProductsList = products;

    const user = getSessionUser();
    const isAdmin = user && user.role === 'ADMIN';

    // Hide or show New Product button based on ADMIN role
    const newProductBtn = document.getElementById('newProductBtn');
    if (newProductBtn) {
        newProductBtn.style.display = isAdmin ? 'block' : 'none';
    }

    const supplierMap = {};
    suppliers.forEach(s => {
        supplierMap[s.id] = s.name;
    });
    window.currentSupplierMap = supplierMap;

    // Populate Stock Manager products dropdown
    const txProductSelect = document.getElementById('txProduct');
    if (txProductSelect) {
        txProductSelect.innerHTML = '<option value="" disabled selected>Choose a product...</option>' +
            products.map(p => `<option value="${p.id}">${p.name} (SKU: ${p.skuCode})</option>`).join('');
    }

    // Set up search and filter listeners once
    const searchInput = document.getElementById('inventorySearchInput');
    const catFilter = document.getElementById('inventoryCategoryFilter');
    
    if (searchInput && !searchInput.dataset.listenerAttached) {
        searchInput.addEventListener('input', filterInventory);
        searchInput.dataset.listenerAttached = 'true';
    }
    
    if (catFilter && !catFilter.dataset.listenerAttached) {
        catFilter.addEventListener('change', filterInventory);
        catFilter.dataset.listenerAttached = 'true';
    }

    // Reset search/filter inputs on reload
    if (searchInput) searchInput.value = '';
    if (catFilter) catFilter.value = '';

    renderProductTable(products, supplierMap);
}

async function stockAdjust(productId, change) {
    const user = getSessionUser();
    const transaction = {
        productId: parseInt(productId),
        userId: user ? user.id : null,
        transactionType: change > 0 ? 'INWARD' : 'OUTWARD',
        quantity: Math.abs(change)
    };
    
    try {
        await api.recordTransaction(transaction);
        showToast('Stock adjusted successfully');
        loadInventory();
        updateStats();
    } catch (e) {
        showToast('Adjustment failed: ' + e.message, 'error');
    }
}

let usersList = [];

async function loadUsers() {
    showTableSkeleton('#userTable tbody', 7, 4);
    const tbody = document.querySelector('#userTable tbody');
    usersList = await api.getUsers();
    
    tbody.innerHTML = usersList.map(u => {
        let badgeClass = 'badge-success';
        if (u.role === 'BILLING_STAFF') badgeClass = 'badge-warning';
        if (u.role === 'WAREHOUSE') badgeClass = 'badge-danger';
        
        return `
            <tr>
                <td>#${u.id}</td>
                <td><strong style="color: var(--text-main);">${u.username}</strong></td>
                <td><span class="badge ${badgeClass}">${u.role}</span></td>
                <td>${u.address || '<span style="color: var(--text-muted); font-size: 0.85rem; font-style: italic;">Not Set</span>'}</td>
                <td>${u.phone || '<span style="color: var(--text-muted); font-size: 0.85rem; font-style: italic;">Not Set</span>'}</td>
                <td>${u.aadharNo || '<span style="color: var(--text-muted); font-size: 0.85rem; font-style: italic;">Not Set</span>'}</td>
                <td style="text-align: right; padding-right: 2rem;">
                    <div style="display: inline-flex; align-items: center; gap: 0.5rem;">
                        <button class="btn btn-sm btn-primary" onclick="openEditUserModal(${u.id})" title="Edit Employee" style="padding: 0.35rem 0.6rem; border-radius: 0.5rem; background: rgba(59, 130, 246, 0.15); color: var(--primary); box-shadow: none; border: 1px solid rgba(59, 130, 246, 0.2);">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-sm btn-danger" onclick="deleteUser(${u.id})" title="Remove Employee" style="padding: 0.35rem 0.6rem; border-radius: 0.5rem; background: rgba(244, 63, 94, 0.15); color: var(--danger); box-shadow: none; border: 1px solid rgba(244, 63, 94, 0.2);">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

async function deleteUser(id) {
    if (!confirm('Are you sure you want to remove this employee?')) return;
    try {
        await api.deleteUser(id);
        showToast('Employee removed successfully');
        loadUsers();
    } catch (e) {
        showToast('Failed to remove employee: ' + e.message, 'error');
    }
}

function openNewUserModal() {
    document.getElementById('u_id').value = '';
    document.getElementById('u_username').value = '';
    document.getElementById('u_username').disabled = false;
    document.getElementById('u_password').value = '';
    document.getElementById('u_role').value = 'ADMIN';
    document.getElementById('u_address').value = '';
    document.getElementById('u_phone').value = '';
    document.getElementById('u_aadhar').value = '';

    document.getElementById('userModalTitle').innerHTML = '<i class="fas fa-user-plus" style="color: var(--primary); margin-right: 0.5rem;"></i> Register New Employee';
    document.getElementById('userSubmitBtn').textContent = 'Create Account';
    showModal('userModal');
}

function openEditUserModal(userId) {
    const user = usersList.find(u => u.id === userId);
    if (!user) return;

    document.getElementById('u_id').value = user.id;
    document.getElementById('u_username').value = user.username;
    document.getElementById('u_username').disabled = true; // prevent username editing to avoid unique constraint issues
    document.getElementById('u_password').value = user.password;
    document.getElementById('u_role').value = user.role;
    document.getElementById('u_address').value = user.address || '';
    document.getElementById('u_phone').value = user.phone || '';
    document.getElementById('u_aadhar').value = user.aadharNo || '';

    document.getElementById('userModalTitle').innerHTML = '<i class="fas fa-edit" style="color: var(--primary); margin-right: 0.5rem;"></i> Edit Employee Details';
    document.getElementById('userSubmitBtn').textContent = 'Save Changes';
    showModal('userModal');
}

let suppliersList = [];

async function loadSuppliers() {
    showTableSkeleton('#supplierTable tbody', 4, 4);
    const tbody = document.querySelector('#supplierTable tbody');
    suppliersList = await api.getSuppliers();
    
    const user = getSessionUser();
    const isAdmin = user && user.role === 'ADMIN';

    tbody.innerHTML = suppliersList.map(s => {
        const actionHtml = isAdmin 
            ? `<div style="display: inline-flex; gap: 0.5rem;">
                <button class="btn btn-sm btn-primary" onclick="editSupplier(${s.id})" style="padding: 0.4rem 0.8rem; border-radius: 0.5rem;"><i class="fas fa-edit"></i> Edit</button>
                <button class="btn btn-sm btn-primary" style="padding: 0.4rem 0.8rem; border-radius: 0.5rem; background: transparent; border: 1px solid var(--primary); color: var(--primary); box-shadow: none;"><i class="fas fa-envelope"></i> Contact</button>
               </div>`
            : `<button class="btn btn-sm btn-primary" style="padding: 0.4rem 0.8rem; border-radius: 0.5rem; background: transparent; border: 1px solid var(--primary); color: var(--primary); box-shadow: none;"><i class="fas fa-envelope"></i> Contact</button>`;

        return `
            <tr>
                <td>#${s.id}</td>
                <td><strong style="color: var(--text-main);">${s.name}</strong></td>
                <td style="color: var(--text-muted); max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${s.contactInfo}</td>
                <td style="text-align: right; padding-right: 2rem;">
                    ${actionHtml}
                </td>
            </tr>
        `;
    }).join('');
}

function openNewSupplierModal() {
    document.getElementById('s_id').value = '';
    document.getElementById('s_name').value = '';
    document.getElementById('s_contact').value = '';
    document.getElementById('supplierModalTitle').innerHTML = '<i class="fas fa-truck-moving" style="color: var(--primary); margin-right: 0.5rem;"></i> Register New Supplier';
    document.getElementById('supplierSubmitBtn').textContent = 'Save Vendor';
    showModal('supplierModal');
}

function editSupplier(id) {
    const supplier = suppliersList.find(s => s.id === id);
    if (!supplier) return;
    document.getElementById('s_id').value = supplier.id;
    document.getElementById('s_name').value = supplier.name;
    document.getElementById('s_contact').value = supplier.contactInfo;
    document.getElementById('supplierModalTitle').innerHTML = '<i class="fas fa-edit" style="color: var(--primary); margin-right: 0.5rem;"></i> Edit Supplier Details';
    document.getElementById('supplierSubmitBtn').textContent = 'Update Vendor';
    showModal('supplierModal');
}

async function loadTransactions() {
    showTableSkeleton('#transactionTable tbody', 6, 6);
    const tbody = document.querySelector('#transactionTable tbody');
    const [logs, products] = await Promise.all([
        api.getTransactions(),
        api.getProducts()
    ]);
    
    const productMap = {};
    products.forEach(p => {
        productMap[p.id] = p.name;
    });

    tbody.innerHTML = logs.map(l => {
        const productName = productMap[l.productId] || `Prod #${l.productId}`;
        return `
            <tr>
                <td style="color: var(--text-muted); font-size: 0.85rem;">${new Date(l.transactionDate).toLocaleString()}</td>
                <td><strong style="color: var(--text-main);">${productName}</strong></td>
                <td>User #${l.userId}</td>
                <td>
                    <span class="badge ${l.transactionType === 'INWARD' ? 'badge-success' : 'badge-danger'}">
                        ${l.transactionType}
                    </span>
                </td>
                <td><strong style="color: var(--text-main);">${l.quantity}</strong></td>
                <td><span style="font-size: 0.9rem; color: var(--text-muted);">${l.notes || '—'}</span></td>
            </tr>
        `;
    }).join('');
}

// Modal logic
async function showModal(id) {
    if (id === 'productModal') {
        await populateSuppliersDropdown();
    }
    document.getElementById(id).style.display = 'flex';
}

function openNewProductModal() {
    const user = getSessionUser();
    if (!user || user.role !== 'ADMIN') {
        showToast('Access denied: Admins only', 'error');
        return;
    }
    document.getElementById('p_id').value = '';
    document.getElementById('sku').value = '';
    document.getElementById('pname').value = '';
    document.getElementById('pcategory').value = '';
    document.getElementById('price').value = '';
    document.getElementById('pdiscount').value = 0;
    document.getElementById('pgst').value = 0;
    document.getElementById('stock').value = 0;
    document.getElementById('psupplier').value = '';
    
    document.getElementById('productModalTitle').innerHTML = '<i class="fas fa-box-open" style="color: var(--primary); margin-right: 0.5rem;"></i> Register New Product';
    document.getElementById('productSubmitBtn').textContent = 'Save Product';
    
    showModal('productModal');
}

async function openEditProductModal(productId) {
    const user = getSessionUser();
    if (!user || user.role !== 'ADMIN') {
        showToast('Access denied: Admins only', 'error');
        return;
    }
    const pIdInt = parseInt(productId);
    const product = (window.allProductsList || []).find(p => p.id === pIdInt);
    if (!product) {
        showToast('Product data not found', 'error');
        return;
    }
    
    await populateSuppliersDropdown();
    
    document.getElementById('p_id').value = product.id;
    document.getElementById('sku').value = product.skuCode;
    document.getElementById('pname').value = product.name;
    document.getElementById('pcategory').value = product.categoryId !== null && product.categoryId !== undefined ? product.categoryId : '';
    document.getElementById('price').value = product.price;
    document.getElementById('pdiscount').value = product.discount || 0;
    document.getElementById('pgst').value = product.gst || 0;
    document.getElementById('stock').value = product.stockQuantity;
    document.getElementById('psupplier').value = product.supplierId || '';
    
    document.getElementById('productModalTitle').innerHTML = '<i class="fas fa-edit" style="color: var(--primary); margin-right: 0.5rem;"></i> Edit Product Details';
    document.getElementById('productSubmitBtn').textContent = 'Update Product';
    
    showModal('productModal');
}

function hideModal(id) {
    document.getElementById(id).style.display = 'none';
}

async function populateSuppliersDropdown() {
    const select = document.getElementById('psupplier');
    if (!select) return;
    try {
        const suppliers = await api.getSuppliers();
        select.innerHTML = '<option value="" disabled selected>Select Supplier</option>' + 
            suppliers.map(s => `<option value="${s.id}">${s.name} (ID: ${s.id})</option>`).join('');
    } catch (e) {
        showToast('Failed to load suppliers for selection', 'error');
    }
}

// Form Submissions
document.getElementById('productForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const user = getSessionUser();
    if (!user || user.role !== 'ADMIN') {
        showToast('Access denied: Admins only', 'error');
        return;
    }
    const idVal = document.getElementById('p_id').value;
    const supplierSelect = document.getElementById('psupplier');
    const categoryVal = document.getElementById('pcategory').value;
    const product = {
        skuCode: document.getElementById('sku').value,
        name: document.getElementById('pname').value,
        categoryId: categoryVal ? parseInt(categoryVal) : null,
        price: parseFloat(document.getElementById('price').value),
        discount: parseFloat(document.getElementById('pdiscount').value || 0),
        gst: parseFloat(document.getElementById('pgst').value || 0),
        stockQuantity: parseInt(document.getElementById('stock').value),
        supplierId: supplierSelect.value ? parseInt(supplierSelect.value) : null
    };

    if (idVal) {
        product.id = parseInt(idVal);
    }

    try {
        await api.saveProduct(product);
        showToast(idVal ? 'Product updated successfully' : 'Product added to catalog');
        hideModal('productModal');
        loadInventory();
        updateStats();
        e.target.reset();
    } catch (e) { showToast(e.message, 'error'); }
});

// Quick Stock Transaction Form (Stock Manager)
const quickTxForm = document.getElementById('quickTransactionForm');
if (quickTxForm) {
    quickTxForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const user = getSessionUser();
        const productId = parseInt(document.getElementById('txProduct').value);
        const type = document.getElementById('txType').value;
        const qty = parseInt(document.getElementById('txQuantity').value);
        const notes = document.getElementById('txNotes').value;
        
        if (!productId || qty <= 0) {
            showToast('Please select a valid product and enter quantity', 'error');
            return;
        }
        
        const transaction = {
            productId: productId,
            userId: user ? user.id : null,
            transactionType: type,
            quantity: qty,
            notes: notes || null
        };
        
        try {
            await api.recordTransaction(transaction);
            showToast('Stock transaction recorded successfully');
            document.getElementById('txQuantity').value = '';
            document.getElementById('txNotes').value = '';
            document.getElementById('txProduct').value = '';
            loadInventory();
            updateStats();
        } catch (err) {
            showToast('Failed to record transaction: ' + err.message, 'error');
        }
    });
}

document.getElementById('userForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const idVal = document.getElementById('u_id').value;
    const user = {
        username: document.getElementById('u_username').value,
        password: document.getElementById('u_password').value,
        role: document.getElementById('u_role').value,
        address: document.getElementById('u_address').value,
        phone: document.getElementById('u_phone').value,
        aadharNo: document.getElementById('u_aadhar').value
    };
    if (idVal) {
        user.id = parseInt(idVal);
    }

    try {
        await api.saveUser(user);
        showToast(idVal ? 'Employee details updated' : 'Employee account created');
        hideModal('userModal');
        loadUsers();
        e.target.reset();
    } catch (e) { showToast(e.message, 'error'); }
});

document.getElementById('supplierForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const idVal = document.getElementById('s_id').value;
    const supplier = {
        name: document.getElementById('s_name').value,
        contactInfo: document.getElementById('s_contact').value
    };
    if (idVal) {
        supplier.id = parseInt(idVal);
    }

    try {
        await api.saveSupplier(supplier);
        showToast(idVal ? 'Supplier details updated' : 'Supplier registered');
        hideModal('supplierModal');
        loadSuppliers();
        e.target.reset();
    } catch (e) { showToast(e.message, 'error'); }
});

async function logout() {
    try {
        await api.logout();
    } catch (e) {
        console.warn('Backend logout failed:', e);
    }
    removeSessionUser();
    window.location.href = 'index.html';
}

// Expose functions to global scope for HTML event handlers
window.getSavedTheme = getSavedTheme;
window.setSavedTheme = setSavedTheme;
window.updateThemeIcon = updateThemeIcon;
window.initTheme = initTheme;
window.toggleTheme = toggleTheme;
window.showToast = showToast;
window.showSection = showSection;
window.addToCartBySku = addToCartBySku;
window.updateCartUI = updateCartUI;
window.updateCartQuantity = updateCartQuantity;
window.removeFromCart = removeFromCart;
window.clearCart = clearCart;
window.finalizeSale = finalizeSale;
window.closeConfirmModal = closeConfirmModal;
window.executeCheckout = executeCheckout;
window.loadSectionData = loadSectionData;
window.updateStats = updateStats;
window.loadInventory = loadInventory;
window.stockAdjust = stockAdjust;
window.loadUsers = loadUsers;
window.deleteUser = deleteUser;
window.openNewUserModal = openNewUserModal;
window.openEditUserModal = openEditUserModal;
window.loadSuppliers = loadSuppliers;
window.openNewSupplierModal = openNewSupplierModal;
window.editSupplier = editSupplier;
window.loadTransactions = loadTransactions;
window.showModal = showModal;
window.openNewProductModal = openNewProductModal;
window.openEditProductModal = openEditProductModal;
window.hideModal = hideModal;
window.logout = logout;

// Sales Report & Export functions
async function loadReports() {
    // Select default dates if not already selected
    const dailyInput = document.getElementById('reportDailyDate');
    const monthlyInput = document.getElementById('reportMonthlyMonth');
    
    if (dailyInput && !dailyInput.value) {
        dailyInput.value = new Date().toISOString().split('T')[0];
    }
    if (monthlyInput && !monthlyInput.value) {
        const d = new Date();
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        monthlyInput.value = `${year}-${month}`;
    }

    // Load recent bills preview
    try {
        showTableSkeleton('#reportsPreviewTable tbody', 7, 5);
        const bills = await api.getBills();
        loadedBills = bills;
        renderReportsPreview(bills);
    } catch (e) {
        showToast('Failed to load sales data: ' + e.message, 'error');
    }
}

function renderReportsPreview(bills) {
    const tbody = document.querySelector('#reportsPreviewTable tbody');
    if (!tbody) return;

    if (!bills || bills.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 2rem;">No sales records found in the system.</td></tr>`;
        return;
    }

    // Sort bills by date descending
    const sorted = [...bills].sort((a, b) => new Date(b.billDate) - new Date(a.billDate));

    tbody.innerHTML = sorted.slice(0, 10).map(b => {
        const dateStr = new Date(b.billDate).toLocaleString();
        const itemsCount = b.items ? b.items.reduce((sum, item) => sum + item.quantity, 0) : 0;
        return `
            <tr onclick="showBillDetails(${b.id})" style="cursor: pointer;">
                <td><strong>#${b.id}</strong></td>
                <td style="color: var(--text-muted); font-size: 0.85rem;">${dateStr}</td>
                <td><span class="badge" style="background: rgba(255,255,255,0.05); color: var(--text-main); border: 1px solid var(--border-color);">${b.paymentMethod || 'CASH'}</span></td>
                <td>${itemsCount}</td>
                <td>₹${b.taxAmount.toFixed(2)}</td>
                <td><strong style="color: var(--success);">₹${b.totalAmount.toFixed(2)}</strong></td>
                <td>
                    <button class="btn btn-primary" style="padding: 0.35rem 0.75rem; font-size: 0.75rem; min-width: auto; height: auto;" onclick="event.stopPropagation(); showBillDetails(${b.id});">
                        <i class="fas fa-eye"></i> View
                    </button>
                </td>
            </tr>
        `;
    }).join('') + (sorted.length > 10 ? `<tr><td colspan="7" style="text-align: center; color: var(--text-muted); font-size: 0.85rem; padding: 0.75rem;">Showing recent 10 transactions. Use the download buttons to get full details.</td></tr>` : '');
}

function showBillDetails(billId) {
    const bill = loadedBills.find(b => b.id === billId);
    if (!bill) {
        showToast('Bill details not found', 'error');
        return;
    }

    const titleEl = document.getElementById('billDetailsTitle');
    if (titleEl) {
        titleEl.innerHTML = `<i class="fas fa-receipt" style="color: var(--success); margin-right: 0.5rem;"></i> Bill Details - #${bill.id}`;
    }

    const metadataEl = document.getElementById('billDetailsMetadata');
    if (metadataEl) {
        const dateStr = new Date(bill.billDate).toLocaleString();
        metadataEl.innerHTML = `
            <div><strong>Date & Time:</strong> ${dateStr}</div>
            <div><strong>Payment Method:</strong> <span class="badge" style="background: rgba(255,255,255,0.05); color: var(--text-main); border: 1px solid var(--border-color);">${bill.paymentMethod || 'CASH'}</span></div>
            <div><strong>Staff ID / User ID:</strong> #${bill.userId}</div>
        `;
    }

    const tbody = document.querySelector('#billDetailsItemsTable tbody');
    if (tbody) {
        if (!bill.items || bill.items.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No items recorded in this bill.</td></tr>`;
        } else {
            tbody.innerHTML = bill.items.map(item => {
                const price = item.priceAtSale || 0;
                const qty = item.quantity || 0;
                const itemTotal = price * qty;
                return `
                    <tr>
                        <td style="font-weight: 500;">${item.productName || 'Unknown Product (#' + item.productId + ')'}</td>
                        <td>${qty}</td>
                        <td>₹${price.toFixed(2)}</td>
                        <td><strong style="color: var(--text-main);">₹${itemTotal.toFixed(2)}</strong></td>
                    </tr>
                `;
            }).join('');
        }
    }

    const summaryEl = document.getElementById('billDetailsSummary');
    if (summaryEl) {
        const taxVal = bill.taxAmount || 0;
        const totalVal = bill.totalAmount || 0;
        const subtotalVal = totalVal - taxVal;
        summaryEl.innerHTML = `
            <div class="flex-between" style="margin-bottom: 0.35rem; color: var(--text-muted);">
                <span>Subtotal:</span>
                <span>₹${subtotalVal.toFixed(2)}</span>
            </div>
            <div class="flex-between" style="margin-bottom: 0.35rem; color: var(--text-muted);">
                <span>Tax Collected:</span>
                <span>₹${taxVal.toFixed(2)}</span>
            </div>
            <div class="flex-between" style="font-size: 1.05rem; font-weight: 700; color: var(--text-main); margin-top: 0.5rem; border-top: 1px solid var(--border-color); padding-top: 0.5rem;">
                <span>Total Amount:</span>
                <span style="color: var(--success);">₹${totalVal.toFixed(2)}</span>
            </div>
        `;
    }

    showModal('billDetailsModal');
}

async function downloadDailySalesReport() {
    const dateVal = document.getElementById('reportDailyDate')?.value;
    if (!dateVal) {
        showToast('Please select a date first', 'error');
        return;
    }

    try {
        const bills = await api.getBills();
        // Filter bills by the selected date (YYYY-MM-DD)
        const dailyBills = bills.filter(b => {
            const bDateStr = new Date(b.billDate).toISOString().split('T')[0];
            return bDateStr === dateVal;
        });

        if (dailyBills.length === 0) {
            showToast(`No sales recorded on ${dateVal}`, 'warning');
            return;
        }

        generateCsvReport(dailyBills, `sales_report_daily_${dateVal}.csv`);
        showToast(`Downloaded daily sales report for ${dateVal}`);
    } catch (e) {
        showToast('Failed to generate report: ' + e.message, 'error');
    }
}

async function downloadMonthlySalesReport() {
    const monthVal = document.getElementById('reportMonthlyMonth')?.value; // Format: YYYY-MM
    if (!monthVal) {
        showToast('Please select a month first', 'error');
        return;
    }

    try {
        const bills = await api.getBills();
        // Filter bills by the selected month (YYYY-MM)
        const monthlyBills = bills.filter(b => {
            const bDateStr = new Date(b.billDate).toISOString().split('T')[0].substring(0, 7);
            return bDateStr === monthVal;
        });

        if (monthlyBills.length === 0) {
            showToast(`No sales recorded in ${monthVal}`, 'warning');
            return;
        }

        generateCsvReport(monthlyBills, `sales_report_monthly_${monthVal}.csv`);
        showToast(`Downloaded monthly sales report for ${monthVal}`);
    } catch (e) {
        showToast('Failed to generate report: ' + e.message, 'error');
    }
}

function generateCsvReport(bills, filename) {
    const headers = [
        'Bill ID',
        'Timestamp',
        'User ID (Employee)',
        'Payment Method',
        'Tax Amount (INR)',
        'Total Amount (INR)',
        'Items Detail'
    ];

    const csvRows = [headers.join(',')];

    bills.forEach(b => {
        const dateStr = new Date(b.billDate).toISOString().replace('T', ' ').substring(0, 19);
        const itemsDetail = b.items ? b.items.map(i => `${i.productName || 'Prod#' + i.productId} (${i.quantity} x Rs.${i.priceAtSale})`).join(' | ') : '';
        
        // Escape quotes in CSV
        const escapedItemsDetail = itemsDetail.replace(/"/g, '""');
        
        const row = [
            b.id,
            `"${dateStr}"`,
            b.userId,
            b.paymentMethod || 'CASH',
            b.taxAmount.toFixed(2),
            b.totalAmount.toFixed(2),
            `"${escapedItemsDetail}"`
        ];
        csvRows.push(row.join(','));
    });

    const csvContent = csvRows.join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", filename);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
}

window.loadReports = loadReports;
window.downloadDailySalesReport = downloadDailySalesReport;
window.downloadMonthlySalesReport = downloadMonthlySalesReport;
window.showBillDetails = showBillDetails;

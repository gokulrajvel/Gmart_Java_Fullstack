const BASE_URL = '/api';

// Global Loader Management
const loader = {
    showTimeout: null,
    activeRequests: 0,
    
    show(message = 'Processing request...') {
        this.activeRequests++;
        if (this.activeRequests === 1) {
            const textEl = document.getElementById('globalLoaderText');
            if (textEl) textEl.textContent = message;
            
            clearTimeout(this.showTimeout);
            this.showTimeout = setTimeout(() => {
                const el = document.getElementById('globalLoader');
                if (el) el.classList.add('active');
            }, 250); // Delay showing loader by 250ms to prevent flickers on fast connections
        }
    },
    
    hide() {
        this.activeRequests = Math.max(0, this.activeRequests - 1);
        if (this.activeRequests === 0) {
            clearTimeout(this.showTimeout);
            const el = document.getElementById('globalLoader');
            if (el) el.classList.remove('active');
        }
    }
};

async function apiRequest(endpoint, method = 'GET', body = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json'
        }
    };

    if (body) {
        options.body = JSON.stringify(body);
    }

    // Determine custom loader message based on API route/action
    let msg = 'Processing request...';
    if (endpoint.startsWith('/auth')) msg = 'Signing in...';
    else if (endpoint.startsWith('/products')) msg = method === 'GET' ? 'Fetching products...' : 'Saving product...';
    else if (endpoint.startsWith('/suppliers')) msg = method === 'GET' ? 'Fetching vendors...' : 'Saving vendor...';
    else if (endpoint.startsWith('/users')) msg = method === 'GET' ? 'Fetching staff accounts...' : 'Saving staff account...';
    else if (endpoint.startsWith('/transactions')) msg = method === 'GET' ? 'Loading history...' : 'Recording transaction...';
    else if (endpoint.startsWith('/bills')) msg = method === 'GET' ? 'Loading sales logs...' : 'Generating bill invoice...';

    loader.show(msg);

    try {
        const response = await fetch(`${BASE_URL}${endpoint}`, options);
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Something went wrong');
        }
        const text = await response.text();
        return text ? JSON.parse(text) : {};
    } catch (error) {
        console.error(`API Error (${endpoint}):`, error);
        throw error;
    } finally {
        loader.hide();
    }
}

const api = {
    login: (credentials) => apiRequest('/auth/login', 'POST', credentials),
    
    // Products
    getProducts: () => apiRequest('/products'),
    getProduct: (sku) => apiRequest(`/products/${sku}`),
    saveProduct: (product) => apiRequest('/products', 'POST', product),
    
    // Suppliers
    getSuppliers: () => apiRequest('/suppliers'),
    saveSupplier: (supplier) => apiRequest('/suppliers', 'POST', supplier),
    
    // Users
    getUsers: () => apiRequest('/users'),
    saveUser: (user) => apiRequest('/users', 'POST', user),
    deleteUser: (id) => apiRequest(`/users/${id}`, 'DELETE'),

    // Transactions
    getTransactions: () => apiRequest('/transactions'),
    recordTransaction: (transaction) => apiRequest('/transactions', 'POST', transaction),

    // Bills
    getBills: () => apiRequest('/bills'),
    createBill: (bill) => apiRequest('/bills', 'POST', bill)
};

window.api = api;

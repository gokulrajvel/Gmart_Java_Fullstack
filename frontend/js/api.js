/**
 * GMart Client-Side API Utility Layer
 * Handles global AJAX requests, loading spinner state management,
 * cookie interactions, and 401 session-expiration handler.
 */

const BASE_URL = '/api';

/**
 * Global Loader UI management.
 * Dispatches and coordinates loading spinner animation.
 * Minimizes flickering using delay thresholds.
 */
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

/**
 * Core fetch wrapper that makes HTTP request to GMart endpoints.
 * Handles automatic loader animations, HTTP error handling, and session invalidation.
 * 
 * @param {string} endpoint - The target API route (e.g. /products)
 * @param {string} method - HTTP method (GET, POST, PUT, DELETE)
 * @param {object} body - Request payload body
 * @returns {Promise<object>} Parsed JSON response object
 */
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
            if (response.status === 401 && !endpoint.includes('/auth/login')) {
                try {
                    window.cookies.remove('user');
                } catch (e) {
                    console.warn('Cookies are not accessible:', e);
                }
                window.location.href = 'index.html';
                return;
            }
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

/**
 * Unified API Client Services.
 * Maps components (Products, Suppliers, Users/Staff, Transactions, Bills) to backend endpoints.
 */
const api = {
    login: (credentials) => apiRequest('/auth/login', 'POST', credentials),
    
    // Products
    getProducts: () => apiRequest('/products'),
    getProduct: (sku) => apiRequest(`/products/${sku}`),
    saveProduct: (product) => product.id ? apiRequest(`/products/${product.id}`, 'PUT', product) : apiRequest('/products', 'POST', product),
    
    // Suppliers
    getSuppliers: () => apiRequest('/suppliers'),
    saveSupplier: (supplier) => supplier.id ? apiRequest(`/suppliers/${supplier.id}`, 'PUT', supplier) : apiRequest('/suppliers', 'POST', supplier),
    
    // Users
    getUsers: () => apiRequest('/users'),
    saveUser: (user) => user.id ? apiRequest(`/users/${user.id}`, 'PUT', user) : apiRequest('/users', 'POST', user),
    deleteUser: (id) => apiRequest(`/users/${id}`, 'DELETE'),

    // Transactions
    getTransactions: () => apiRequest('/transactions'),
    recordTransaction: (transaction) => apiRequest('/transactions', 'POST', transaction),

    // Bills
    getBills: () => apiRequest('/bills'),
    createBill: (bill) => apiRequest('/bills', 'POST', bill)
};

/**
 * Client-side Cookie Helper utility.
 * Manages storing, retrieving, and clearing cookies with standard path and security flags.
 */
const cookies = {
    /**
     * Set a cookie value with a specific lifetime.
     * 
     * @param {string} name - Cookie name
     * @param {string} value - Cookie value
     * @param {number} days - Cookie lifetime in days
     */
    set: (name, value, days) => {
        let expires = "";
        if (days) {
            let date = new Date();
            date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
            expires = "; expires=" + date.toUTCString();
        }
        document.cookie = name + "=" + encodeURIComponent(value || "") + expires + "; path=/; SameSite=Strict";
    },
    
    /**
     * Retrieve a cookie value by name.
     * 
     * @param {string} name - Target cookie name
     * @returns {string|null} Cookie value, or null if not found
     */
    get: (name) => {
        let nameEQ = name + "=";
        let ca = document.cookie.split(';');
        for(let i=0; i < ca.length; i++) {
            let c = ca[i];
            while (c.charAt(0) == ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) == 0) return decodeURIComponent(c.substring(nameEQ.length, c.length));
        }
        return null;
    },
    
    /**
     * Deletes a cookie by setting its expiration date in the past.
     * 
     * @param {string} name - Target cookie name
     */
    remove: (name) => {   
        document.cookie = name + '=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT; SameSite=Strict';
    }
};

window.api = api;
window.cookies = cookies;

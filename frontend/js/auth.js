/**
 * GMart Frontend Authentication & Theme Manager.
 * Orchestrates login form submissions, credentials transport via api.js,
 * session caching via cookies, and theme persistence via localStorage.
 */

document.addEventListener('DOMContentLoaded', () => {
    // Redirect to dashboard immediately if user session cookie is already present
    try {
        if (window.cookies && window.cookies.get('user')) {
            window.location.href = 'dashboard.html';
            return;
        }
    } catch (e) {
        console.warn('Cookies are not accessible:', e);
    }

    // Bind and handle login form submissions
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            const errorDiv = document.getElementById('error');

            try {
                // Call authentication api service
                const user = await api.login({ username, password });
                
                // Store user details in cookie (valid for 30 days) for client-side view management
                try {
                    window.cookies.set('user', JSON.stringify(user), 30);
                } catch (se) {
                    console.warn('Cookies are not accessible:', se);
                }
                
                // Redirect user to GMart dashboard console
                window.location.href = 'dashboard.html';
            } catch (error) {
                // Render error payload in UI
                errorDiv.textContent = error.message;
                errorDiv.style.display = 'block';
            }
        });
    }

    // Initialize display theme
    initTheme();

    // Bind theme toggling controls
    const themeToggleBtn = document.getElementById('themeToggleBtn');
    if (themeToggleBtn) {
        themeToggleBtn.addEventListener('click', toggleTheme);
    }
});

/**
 * Retrieve saved theme choice from localStorage.
 * Defaults to 'dark' if theme configuration is empty or inaccessible.
 * 
 * @returns {string} The stored theme value ('light' or 'dark')
 */
function getSavedTheme() {
    try {
        return localStorage.getItem('theme') || 'dark';
    } catch (e) {
        return 'dark';
    }
}

/**
 * Cache current theme choice to localStorage.
 * 
 * @param {string} theme - The theme configuration ('light' or 'dark')
 */
function setSavedTheme(theme) {
    try {
        localStorage.setItem('theme', theme);
    } catch (e) {
        console.warn('localStorage is not accessible:', e);
    }
}

/**
 * Update UI icons depending on the theme state.
 * 
 * @param {string} theme - The theme configuration ('light' or 'dark')
 */
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

/**
 * Setup layout theme configuration at application start.
 */
function initTheme() {
    const theme = getSavedTheme();
    document.documentElement.setAttribute('data-theme', theme);
    updateThemeIcon(theme);
}

/**
 * Switch page visual style from light to dark or vice-versa.
 */
function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme') || 'dark';
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', newTheme);
    setSavedTheme(newTheme);
    updateThemeIcon(newTheme);
}

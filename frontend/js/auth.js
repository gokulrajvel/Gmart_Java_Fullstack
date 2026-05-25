document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            const errorDiv = document.getElementById('error');

            try {
                const user = await api.login({ username, password });
                // Save user to session storage safely
                try {
                    sessionStorage.setItem('user', JSON.stringify(user));
                } catch (se) {
                    console.warn('sessionStorage is not accessible:', se);
                }
                // Redirect to dashboard
                window.location.href = 'dashboard.html';
            } catch (error) {
                errorDiv.textContent = error.message;
                errorDiv.style.display = 'block';
            }
        });
    }

    // Initialize theme
    initTheme();

    // Bind theme button handler
    const themeToggleBtn = document.getElementById('themeToggleBtn');
    if (themeToggleBtn) {
        themeToggleBtn.addEventListener('click', toggleTheme);
    }
});

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

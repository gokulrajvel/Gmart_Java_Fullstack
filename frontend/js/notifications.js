// notifications.js
let stompClient = null;

export function initNotificationSocket() {
    // 1. Retrieve current logged-in user
    const userStr = window.cookies.get('user');
    const user = userStr ? JSON.parse(userStr) : null;

    if (!user) {
        console.warn('Notification System: User is not logged in.');
        return;
    }

    // 2. Establish WebSocket connection (allowed for all roles to support real-time logout checks)
    const socket = new SockJS('/ws'); // Init SockJS over /ws endpoint
    stompClient = Stomp.over(socket);

    // Disable debug logging in production to keep console clean
    // stompClient.debug = null;

    stompClient.connect({}, function (frame) {
        console.log('Notification System: Connected via STOMP.');

        // Subscribe to user-specific queue
        stompClient.subscribe('/user/queue/notifications', function (message) {
            try {
                const payload = JSON.parse(message.body);

                // Handle real-time concurrent session logout command
                if (payload && payload.action === 'logout') {
                    const currentToken = sessionStorage.getItem('clientToken');
                    if (currentToken !== payload.exceptClientToken) {
                        console.log('Notification System: Another system logged in. Invalidating local session.');
                        try {
                            window.cookies.remove('user');
                            sessionStorage.removeItem('clientToken');
                        } catch (e) {}
                        window.location.href = 'index.html';
                        return;
                    }
                }

                // Handle low stock warnings (restricted to authorized roles)
                const authorizedAlertRoles = ['ADMIN', 'PURCHASING_MANAGER'];
                if (authorizedAlertRoles.includes(user.role)) {
                    handleStockAlert(payload);
                }
            } catch (e) {
                console.error('Failed to parse incoming notification:', e);
            }
        });

    }, function (error) {
        console.error('Notification System WebSocket error. Retrying in 5 seconds...', error);
        setTimeout(initNotificationSocket, 5000); // Retry reconnecting
    });
}

function handleStockAlert(alertData) {
    const msg = alertData.alertMessage || `Stock Alert for SKU ${alertData.skuCode}: Only ${alertData.currentStock} left!`;

    // 1. Trigger browser OS native notification (if supported & allowed)
    if ('Notification' in window) {
        if (Notification.permission === 'granted') {
            new Notification('⚠️ Low Stock Alert', {
                body: msg,
                icon: '/favicon.png'
            });
        } else if (Notification.permission !== 'denied') {
            Notification.requestPermission().then(permission => {
                if (permission === 'granted') {
                    new Notification('⚠️ Low Stock Alert', { body: msg });
                }
            });
        }
    }

    // 2. Display an inline HTML toast alert (using GMart's global toast UI)
    if (typeof window.showToast === 'function') {
        window.showToast(msg, 'error');
    } else {
        alert(msg);
    }
}

// Request permission for push notifications on page load
if (typeof window !== 'undefined' && 'Notification' in window) {
    if (Notification.permission !== 'granted' && Notification.permission !== 'denied') {
        Notification.requestPermission();
    }
}

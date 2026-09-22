// This file is dynamically replaced by Kubernetes at runtime in Azure.
// It populates the global window object before the React app initializes.

window._env_ = {
    // The browser automatically attaches the current domain/IP it is visiting
    VITE_CUSTOMER_API_URL: window.location.origin + "/api/customer",
    VITE_ACCOUNT_API_URL: window.location.origin + "/api/account"
};


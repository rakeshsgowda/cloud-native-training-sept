import axios from "axios";

export const customerApi = axios.create({
  baseURL: window._env_?.VITE_CUSTOMER_API_URL || import.meta.env.VITE_CUSTOMER_API_URL || "http://localhost:8081",
  headers: { "Content-Type": "application/json" },
});

export const accountApi = axios.create({
  baseURL: window._env_?.VITE_ACCOUNT_API_URL || import.meta.env.VITE_ACCOUNT_API_URL || "http://localhost:8082",
  headers: { "Content-Type": "application/json" },
});

const AUTH_STORAGE_KEY = "banking-auth";

function attachToken(config) {
  const raw = localStorage.getItem(AUTH_STORAGE_KEY);
  if (raw) {
    const { token } = JSON.parse(raw);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
}

function handleAuthError(error) {
  if (error?.response?.status === 401) {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    if (window.location.pathname !== "/login") {
      window.location.href = "/login";
    }
  }
  return Promise.reject(error);
}

[customerApi, accountApi].forEach((instance) => {
  instance.interceptors.request.use(attachToken);
  instance.interceptors.response.use((res) => res, handleAuthError);
});

export function extractErrorMessage(error) {
  const data = error?.response?.data;

  if (!data) return error?.message || "Something went wrong. Please try again.";
  if (typeof data === "string") return data;
  if (data.message) return data.message;
  if (Array.isArray(data.errors)) return data.errors.join(", ");
  if (data.error) return data.error;

  return "Something went wrong. Please try again.";
}

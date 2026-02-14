"use client";

import { useEffect } from "react";
import { useAuthStore } from "@/lib/store/auth-store";
import { authApi } from "@/lib/api";

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const { hydrate, setAuth, setLoading, logout, accessToken, refreshToken } =
    useAuthStore();

  useEffect(() => {
    hydrate();
  }, [hydrate]);

  // After hydration, fetch the user profile if tokens exist
  useEffect(() => {
    if (!accessToken || !refreshToken) return;

    authApi
      .me()
      .then((user) => {
        setAuth(user, accessToken, refreshToken);
      })
      .catch(() => {
        logout();
      })
      .finally(() => {
        setLoading(false);
      });
  }, [accessToken, refreshToken, setAuth, setLoading, logout]);

  return <>{children}</>;
}

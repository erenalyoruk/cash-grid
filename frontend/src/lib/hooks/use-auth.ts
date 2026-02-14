import { useMutation } from "@tanstack/react-query";
import { authApi } from "@/lib/api";
import { useAuthStore } from "@/lib/store/auth-store";
import type { LoginRequest, RegisterRequest } from "@/lib/types";

export function useLogin() {
  const setAuth = useAuthStore((s) => s.setAuth);

  return useMutation({
    mutationFn: async (data: LoginRequest) => {
      const tokens = await authApi.login(data);
      const user = await authApi.me();
      return { tokens, user };
    },
    onSuccess: ({ tokens, user }) => {
      setAuth(user, tokens.accessToken, tokens.refreshToken);
    },
  });
}

export function useRegister() {
  const setAuth = useAuthStore((s) => s.setAuth);

  return useMutation({
    mutationFn: async (data: RegisterRequest) => {
      const tokens = await authApi.register(data);
      const user = await authApi.me();
      return { tokens, user };
    },
    onSuccess: ({ tokens, user }) => {
      setAuth(user, tokens.accessToken, tokens.refreshToken);
    },
  });
}

export function useLogout() {
  const logout = useAuthStore((s) => s.logout);

  return useMutation({
    mutationFn: async () => {
      logout();
    },
  });
}

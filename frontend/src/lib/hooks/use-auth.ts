import { useMutation } from "@tanstack/react-query";
import { authApi } from "@/lib/api";
import { useAuthStore } from "@/lib/store/auth-store";
import type {
  LoginRequest,
  RegisterRequest,
  UpdatePasswordRequest,
} from "@/lib/types";
import { fetchMe } from "@/lib/api/me";

export function useLogin() {
  const setAuth = useAuthStore((s) => s.setAuth);

  return useMutation({
    mutationFn: async (data: LoginRequest) => {
      const tokens = await authApi.login(data);
      // Use the returned accessToken directly for /me
      const user = await fetchMe(tokens.accessToken);
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
      // Use the returned accessToken directly for /me
      const user = await fetchMe(tokens.accessToken);
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

export function useUpdateUsername() {
  const setAuth = useAuthStore((s) => s.setAuth);

  return useMutation({
    mutationFn: async (newUsername: string) => {
      const tokens = await authApi.updateUsername(newUsername);
      const user = await fetchMe(tokens.accessToken);
      return { tokens, user };
    },
    onSuccess: ({ tokens, user }) => {
      setAuth(user, tokens.accessToken, tokens.refreshToken);
    },
  });
}

export function useUpdateEmail() {
  const { setAuth, accessToken, refreshToken } = useAuthStore();

  return useMutation({
    mutationFn: async (newEmail: string) => {
      await authApi.updateEmail(newEmail);
      // Refresh profile
      return await authApi.me();
    },
    onSuccess: (user) => {
      if (accessToken && refreshToken) {
        setAuth(user, accessToken, refreshToken);
      }
    },
  });
}

export function useUpdatePassword() {
  return useMutation({
    mutationFn: (data: UpdatePasswordRequest) => authApi.updatePassword(data),
  });
}

import client from "./client";
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  RefreshRequest,
  UserResponse,
  UpdatePasswordRequest,
} from "@/lib/types";

const AUTH_BASE = "/api/v1/auth";

export const authApi = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await client.post<AuthResponse>(
      `${AUTH_BASE}/login`,
      data,
    );
    return response.data;
  },

  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await client.post<AuthResponse>(
      `${AUTH_BASE}/register`,
      data,
    );
    return response.data;
  },

  refresh: async (data: RefreshRequest): Promise<AuthResponse> => {
    const response = await client.post<AuthResponse>(
      `${AUTH_BASE}/refresh`,
      data,
    );
    return response.data;
  },

  me: async (): Promise<UserResponse> => {
    const response = await client.get<UserResponse>(`${AUTH_BASE}/me`);
    return response.data;
  },

  updateUsername: async (newUsername: string): Promise<AuthResponse> => {
    const response = await client.patch<AuthResponse>(
      `${AUTH_BASE}/me/username`,
      {
        newUsername,
      },
    );
    return response.data;
  },

  updateEmail: async (newEmail: string): Promise<UserResponse> => {
    const response = await client.patch<UserResponse>(`${AUTH_BASE}/me/email`, {
      newEmail,
    });
    return response.data;
  },

  updatePassword: async (data: UpdatePasswordRequest): Promise<void> => {
    await client.put(`${AUTH_BASE}/me/password`, data);
  },
};

import client from "./client";
import type {
  AccountResponse,
  CreateAccountRequest,
  UpdateAccountRequest,
  PageResponse,
} from "@/lib/types";

const ACCOUNTS_BASE = "/api/v1/accounts";

export const accountsApi = {
  list: async (page = 0, size = 10): Promise<PageResponse<AccountResponse>> => {
    const response = await client.get<PageResponse<AccountResponse>>(
      ACCOUNTS_BASE,
      { params: { page, size } },
    );
    return response.data;
  },

  getById: async (id: string): Promise<AccountResponse> => {
    const response = await client.get<AccountResponse>(
      `${ACCOUNTS_BASE}/${id}`,
    );
    return response.data;
  },

  create: async (data: CreateAccountRequest): Promise<AccountResponse> => {
    const response = await client.post<AccountResponse>(ACCOUNTS_BASE, data);
    return response.data;
  },

  update: async (
    id: string,
    data: UpdateAccountRequest,
  ): Promise<AccountResponse> => {
    const response = await client.put<AccountResponse>(
      `${ACCOUNTS_BASE}/${id}`,
      data,
    );
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await client.delete(`${ACCOUNTS_BASE}/${id}`);
  },
};

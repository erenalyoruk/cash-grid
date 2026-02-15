import client from "./client";
import type {
  LimitResponse,
  CreateLimitRequest,
  UpdateLimitRequest,
} from "@/lib/types";

const LIMITS_BASE = "/api/v1/limits";

export const limitsApi = {
  list: async (): Promise<LimitResponse[]> => {
    const response = await client.get<LimitResponse[]>(LIMITS_BASE);
    return response.data;
  },

  create: async (data: CreateLimitRequest): Promise<LimitResponse> => {
    const response = await client.post<LimitResponse>(LIMITS_BASE, data);
    return response.data;
  },

  update: async (
    id: string,
    data: UpdateLimitRequest,
  ): Promise<LimitResponse> => {
    const response = await client.put<LimitResponse>(
      `${LIMITS_BASE}/${id}`,
      data,
    );
    return response.data;
  },
};

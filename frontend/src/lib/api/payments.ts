import client from "./client";
import type {
  PaymentResponse,
  CreatePaymentRequest,
  PageResponse,
} from "@/lib/types";

const PAYMENTS_BASE = "/api/v1/payments";

export const paymentsApi = {
  list: async (page = 0, size = 10): Promise<PageResponse<PaymentResponse>> => {
    const response = await client.get<PageResponse<PaymentResponse>>(
      PAYMENTS_BASE,
      { params: { page, size } },
    );
    return response.data;
  },

  getById: async (id: string): Promise<PaymentResponse> => {
    const response = await client.get<PaymentResponse>(
      `${PAYMENTS_BASE}/${id}`,
    );
    return response.data;
  },

  create: async (data: CreatePaymentRequest): Promise<PaymentResponse> => {
    const response = await client.post<PaymentResponse>(PAYMENTS_BASE, data);
    return response.data;
  },

  approve: async (id: string): Promise<PaymentResponse> => {
    const response = await client.post<PaymentResponse>(
      `${PAYMENTS_BASE}/${id}/approve`,
    );
    return response.data;
  },

  reject: async (id: string, reason: string): Promise<PaymentResponse> => {
    const response = await client.post<PaymentResponse>(
      `${PAYMENTS_BASE}/${id}/reject`,
      { reason },
    );
    return response.data;
  },
};

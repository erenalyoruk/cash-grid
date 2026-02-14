import client from "./client";
import type { AuditLogResponse, PageResponse } from "@/lib/types";

const AUDIT_BASE = "/api/v1/audit-logs";

export const auditApi = {
  list: async (
    page = 0,
    size = 10,
  ): Promise<PageResponse<AuditLogResponse>> => {
    const response = await client.get<PageResponse<AuditLogResponse>>(
      AUDIT_BASE,
      { params: { page, size } },
    );
    return response.data;
  },

  getById: async (id: number): Promise<AuditLogResponse> => {
    const response = await client.get<AuditLogResponse>(`${AUDIT_BASE}/${id}`);
    return response.data;
  },
};

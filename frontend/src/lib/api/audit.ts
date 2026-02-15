import client from "./client";
import type { AuditLogResponse, PageResponse } from "@/lib/types";

const AUDIT_BASE = "/api/v1/audit-logs";

export const auditApi = {
  list: async (
    page = 0,
    size = 10,
    action?: string,
    performedBy?: string,
    from?: string,
    to?: string,
    correlationId?: string,
    entityType?: string,
  ): Promise<PageResponse<AuditLogResponse>> => {
    const params: Record<string, string | number | undefined> = { page, size };
    if (action) params.action = action;
    if (performedBy) params.performedBy = performedBy;
    if (from) params.from = from;
    if (to) params.to = to;
    if (correlationId) params.correlationId = correlationId;
    if (entityType) params.entityType = entityType;

    const response = await client.get<PageResponse<AuditLogResponse>>(
      AUDIT_BASE,
      {
        params,
      },
    );
    return response.data;
  },

  getById: async (id: string): Promise<AuditLogResponse> => {
    const response = await client.get<AuditLogResponse>(`${AUDIT_BASE}/${id}`);
    return response.data;
  },
};

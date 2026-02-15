import { useQuery } from "@tanstack/react-query";
import { auditApi } from "@/lib/api";

const AUDIT_KEY = ["audit-logs"];

export function useAuditLogs(opts?: {
  page?: number;
  size?: number;
  action?: string;
  performedBy?: string;
  from?: string;
  to?: string;
  correlationId?: string;
  entityType?: string;
}) {
  const {
    page = 0,
    size = 10,
    action,
    performedBy,
    from,
    to,
    correlationId,
    entityType,
  } = opts ?? {};

  return useQuery({
    queryKey: [
      ...AUDIT_KEY,
      page,
      size,
      action ?? null,
      performedBy ?? null,
      from ?? null,
      to ?? null,
      correlationId ?? null,
      entityType ?? null,
    ],
    queryFn: () =>
      auditApi.list(
        page,
        size,
        action,
        performedBy,
        from,
        to,
        correlationId,
        entityType,
      ),
  });
}

export function useAuditLog(id: string) {
  return useQuery({
    queryKey: [...AUDIT_KEY, id],
    queryFn: () => auditApi.getById(id),
    enabled: !!id,
  });
}

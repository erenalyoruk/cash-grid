import { useQuery } from "@tanstack/react-query";
import { auditApi } from "@/lib/api";

const AUDIT_KEY = ["audit-logs"];

export function useAuditLogs(page = 0, size = 10) {
  return useQuery({
    queryKey: [...AUDIT_KEY, page, size],
    queryFn: () => auditApi.list(page, size),
  });
}

export function useAuditLog(id: number) {
  return useQuery({
    queryKey: [...AUDIT_KEY, id],
    queryFn: () => auditApi.getById(id),
    enabled: !!id,
  });
}

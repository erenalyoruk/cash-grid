import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { limitsApi } from "@/lib/api";
import type { CreateLimitRequest, UpdateLimitRequest } from "@/lib/types";

const LIMITS_KEY = ["limits"];

export function useLimits() {
  return useQuery({
    queryKey: LIMITS_KEY,
    queryFn: () => limitsApi.list(),
  });
}

export function useCreateLimit() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateLimitRequest) => limitsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: LIMITS_KEY });
    },
  });
}

export function useUpdateLimit() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateLimitRequest }) =>
      limitsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: LIMITS_KEY });
    },
  });
}

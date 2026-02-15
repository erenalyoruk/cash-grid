"use client";

import { useMemo, useState } from "react";
import { usePayments, useApprovePayment, useRejectPayment } from "@/lib/hooks";
import { Button } from "@/components/ui/button";
import SearchInput from "@/components/ui/search-input";
import ConfirmDialog from "@/components/ui/confirm-dialog";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { toast } from "sonner";
import type { ErrorResponse } from "@/lib/types";
import {
  CheckCircle2,
  XCircle,
  MoreHorizontal,
  Eye,
  FileText,
} from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Separator } from "@/components/ui/separator";
import type { PaymentResponse } from "@/lib/types";

const statusConfig: Record<string, { label: string; color: string }> = {
  PENDING: {
    label: "Pending",
    color: "bg-yellow-500/10 text-yellow-500 border-yellow-500/20",
  },
  APPROVED: {
    label: "Approved",
    color: "bg-blue-500/10 text-blue-500 border-blue-500/20",
  },
  REJECTED: {
    label: "Rejected",
    color: "bg-red-500/10 text-red-500 border-red-500/20",
  },
  COMPLETED: {
    label: "Completed",
    color: "bg-green-500/10 text-green-500 border-green-500/20",
  },
  FAILED: {
    label: "Failed",
    color: "bg-red-500/10 text-red-500 border-red-500/20",
  },
};

export default function PaymentTable() {
  const { data, isLoading, isError, refetch } = usePayments();
  const approve = useApprovePayment();
  const reject = useRejectPayment();
  const [filter, setFilter] = useState("");
  const [loadingId, setLoadingId] = useState<string | null>(null);
  const [rejectId, setRejectId] = useState<string | null>(null);
  const [rejectReason, setRejectReason] = useState("");
  const [selectedPayment, setSelectedPayment] =
    useState<PaymentResponse | null>(null);

  const rows = useMemo(() => {
    const content = data?.content ?? [];
    if (!filter) return content;
    const q = filter.toLowerCase();
    return content.filter((p) => {
      return (
        p.idempotencyKey.toLowerCase().includes(q) ||
        p.sourceIban.toLowerCase().includes(q) ||
        p.targetIban.toLowerCase().includes(q) ||
        p.id.toLowerCase().includes(q)
      );
    });
  }, [data, filter]);

  if (isLoading)
    return <div className="p-8 text-center">Loading payments...</div>;
  if (isError)
    return (
      <div className="text-destructive p-8 text-center">
        Failed to load payments.
      </div>
    );

  const handleApprove = async (id: string) => {
    setLoadingId(id);
    try {
      await approve.mutateAsync(id);
      toast.success("Payment approved successfully");
      refetch();
    } catch (err) {
      const apiError = err as unknown as ErrorResponse;
      toast.error(apiError.message || "Failed to approve payment");
    } finally {
      setLoadingId(null);
    }
  };

  const handleReject = async () => {
    if (!rejectId || !rejectReason) return;
    setLoadingId(rejectId);
    try {
      await reject.mutateAsync({ id: rejectId, reason: rejectReason });
      toast.success("Payment rejected successfully");
      setRejectId(null);
      setRejectReason("");
      refetch();
    } catch (err) {
      const apiError = err as unknown as ErrorResponse;
      toast.error(apiError.message || "Failed to reject payment");
    } finally {
      setLoadingId(null);
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <SearchInput
          value={filter}
          onChange={setFilter}
          placeholder="Filter by reference, IBAN or ID..."
          className="max-w-sm"
        />
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="px-4">Reference</TableHead>
              <TableHead className="px-4">Source IBAN</TableHead>
              <TableHead className="px-4">Target IBAN</TableHead>
              <TableHead className="px-4 text-right">Amount</TableHead>
              <TableHead className="px-4">Status</TableHead>
              <TableHead className="w-[80px] px-4"></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {rows.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="h-24 text-center">
                  No payments found.
                </TableCell>
              </TableRow>
            ) : (
              rows.map((p) => (
                <TableRow key={p.id}>
                  <TableCell className="px-4 font-mono text-xs">
                    {p.idempotencyKey}
                  </TableCell>
                  <TableCell className="px-4 font-mono text-xs">
                    {p.sourceIban}
                  </TableCell>
                  <TableCell className="px-4 font-mono text-xs">
                    {p.targetIban}
                  </TableCell>
                  <TableCell className="px-4 text-right font-medium">
                    {new Intl.NumberFormat("tr-TR", {
                      style: "currency",
                      currency: p.currency,
                    }).format(p.amount)}
                  </TableCell>
                  <TableCell className="px-4">
                    <Badge className={statusConfig[p.status]?.color || ""}>
                      {statusConfig[p.status]?.label || p.status}
                    </Badge>
                  </TableCell>
                  <TableCell className="px-4">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button
                          variant="ghost"
                          className="h-8 w-8 p-0"
                          disabled={loadingId === p.id}
                        >
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuLabel>Actions</DropdownMenuLabel>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem onClick={() => setSelectedPayment(p)}>
                          <Eye className="mr-2 h-4 w-4" />
                          View Details
                        </DropdownMenuItem>
                        {p.status === "PENDING" && (
                          <>
                            <DropdownMenuItem
                              onClick={() => handleApprove(p.id)}
                            >
                              <CheckCircle2 className="mr-2 h-4 w-4 text-green-500" />
                              Approve
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() => setRejectId(p.id)}
                              className="text-destructive focus:text-destructive"
                            >
                              <XCircle className="mr-2 h-4 w-4" />
                              Reject
                            </DropdownMenuItem>
                          </>
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <ConfirmDialog
        open={!!rejectId}
        onOpenChange={(v) => {
          if (!v) {
            setRejectId(null);
            setRejectReason("");
          }
        }}
        title="Reject Payment"
        description={
          <div className="space-y-4 pt-2">
            <p>
              Are you sure you want to reject this payment request? Please
              provide a reason.
            </p>
            <textarea
              className="border-input placeholder:text-muted-foreground focus-visible:ring-ring min-h-[100px] w-full rounded-md border bg-transparent px-3 py-2 text-sm shadow-sm focus-visible:ring-1 focus-visible:outline-none"
              placeholder="Enter rejection reason..."
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
            />
          </div>
        }
        confirmLabel="Reject"
        confirmVariant="destructive"
        onConfirm={handleReject}
        confirmDisabled={!rejectReason}
      />

      <Dialog
        open={!!selectedPayment}
        onOpenChange={(open) => !open && setSelectedPayment(null)}
      >
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <div className="flex items-center gap-2">
              <FileText className="text-primary h-5 w-5" />
              <DialogTitle>Payment Details</DialogTitle>
            </div>
          </DialogHeader>

          {selectedPayment && (
            <div className="space-y-6 py-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1">
                  <p className="text-muted-foreground text-[10px] font-bold tracking-widest uppercase">
                    Status
                  </p>
                  <Badge
                    className={
                      statusConfig[selectedPayment.status]?.color || ""
                    }
                  >
                    {statusConfig[selectedPayment.status]?.label ||
                      selectedPayment.status}
                  </Badge>
                </div>
                <div className="space-y-1">
                  <p className="text-muted-foreground text-[10px] font-bold tracking-widest uppercase">
                    Currency
                  </p>
                  <p className="text-sm font-semibold">
                    {selectedPayment.currency}
                  </p>
                </div>
              </div>

              <Separator />

              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <p className="text-muted-foreground text-xs font-medium">
                    Amount
                  </p>
                  <p className="text-lg font-bold">
                    {new Intl.NumberFormat("tr-TR", {
                      style: "currency",
                      currency: selectedPayment.currency,
                    }).format(selectedPayment.amount)}
                  </p>
                </div>
                <div className="space-y-1">
                  <p className="text-muted-foreground text-[10px] font-bold tracking-widest uppercase">
                    Reference (Idempotency Key)
                  </p>
                  <p className="bg-muted rounded p-2 font-mono text-xs break-all">
                    {selectedPayment.idempotencyKey}
                  </p>
                </div>
              </div>

              <div className="bg-muted/30 border-muted grid grid-cols-1 gap-4 rounded-xl border p-4">
                <div className="space-y-1">
                  <p className="text-muted-foreground flex items-center gap-1 text-[10px] font-bold tracking-widest uppercase">
                    <span className="h-1.5 w-1.5 rounded-full bg-red-400" />{" "}
                    Source IBAN
                  </p>
                  <p className="font-mono text-xs">
                    {selectedPayment.sourceIban}
                  </p>
                </div>
                <div className="space-y-1">
                  <p className="text-muted-foreground flex items-center gap-1 text-[10px] font-bold tracking-widest uppercase">
                    <span className="h-1.5 w-1.5 rounded-full bg-green-400" />{" "}
                    Target IBAN
                  </p>
                  <p className="font-mono text-xs">
                    {selectedPayment.targetIban}
                  </p>
                </div>
              </div>

              {selectedPayment.description && (
                <div className="space-y-1">
                  <p className="text-muted-foreground text-[10px] font-bold tracking-widest uppercase">
                    Description
                  </p>
                  <p className="text-muted-foreground border-l-2 py-1 pl-3 text-sm italic">
                    &quot;{selectedPayment.description}&quot;
                  </p>
                </div>
              )}

              <Separator />

              <div className="grid grid-cols-2 gap-4 text-xs">
                <div className="space-y-1">
                  <p className="text-muted-foreground font-bold tracking-widest uppercase opacity-60">
                    Created By
                  </p>
                  <p className="font-semibold">
                    {selectedPayment.createdByUsername}
                  </p>
                  <p className="text-muted-foreground text-[10px]">
                    {new Date(selectedPayment.createdAt).toLocaleString()}
                  </p>
                </div>
                {selectedPayment.approvedByUsername && (
                  <div className="space-y-1">
                    <p className="text-muted-foreground font-bold tracking-widest uppercase opacity-60">
                      Approved By
                    </p>
                    <p className="font-semibold text-green-600">
                      {selectedPayment.approvedByUsername}
                    </p>
                    <p className="text-muted-foreground text-[10px]">
                      {new Date(selectedPayment.updatedAt).toLocaleString()}
                    </p>
                  </div>
                )}
                {selectedPayment.rejectionReason && (
                  <div className="col-span-2 space-y-1">
                    <p className="font-bold tracking-widest text-red-500 uppercase opacity-60">
                      Rejection Reason
                    </p>
                    <p className="rounded border border-red-100 bg-red-50 p-2 font-semibold text-red-600">
                      {selectedPayment.rejectionReason}
                    </p>
                  </div>
                )}
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}

"use client";

import { useMemo, useState } from "react";
import { useAccounts, useDeleteAccount } from "@/lib/hooks";
import AccountForm from "./account-form";
import { Button } from "@/components/ui/button";
import ConfirmDialog from "@/components/ui/confirm-dialog";
import SearchInput from "@/components/ui/search-input";
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
import { MoreHorizontal, Trash2, Edit2 } from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

export default function AccountTable() {
  const { data, isLoading, isError, refetch } = useAccounts();
  const deleteAccount = useDeleteAccount();
  const [filter, setFilter] = useState("");
  const [deleteId, setDeleteId] = useState<string | null>(null);

  const rows = useMemo(() => {
    const content = data?.content ?? [];
    if (!filter) return content;
    const q = filter.toLowerCase();
    return content.filter(
      (a) =>
        a.iban.toLowerCase().includes(q) ||
        a.customerName.toLowerCase().includes(q) ||
        a.id.toLowerCase().includes(q),
    );
  }, [data, filter]);

  if (isLoading)
    return <div className="p-8 text-center">Loading accounts...</div>;
  if (isError)
    return (
      <div className="text-destructive p-8 text-center">
        Failed to load accounts.
      </div>
    );

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <SearchInput
          value={filter}
          onChange={setFilter}
          placeholder="Filter by IBAN, owner or ID..."
          className="max-w-sm"
        />
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="px-4">IBAN</TableHead>
              <TableHead className="px-4">Owner</TableHead>
              <TableHead className="px-4">Currency</TableHead>
              <TableHead className="px-4 text-right">Balance</TableHead>
              <TableHead className="px-4">Status</TableHead>
              <TableHead className="w-[80px] px-4"></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {rows.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="h-24 text-center">
                  No accounts found.
                </TableCell>
              </TableRow>
            ) : (
              rows.map((a) => (
                <TableRow key={a.id}>
                  <TableCell className="px-4 font-mono">{a.iban}</TableCell>
                  <TableCell className="px-4">{a.customerName}</TableCell>
                  <TableCell className="px-4">
                    <Badge variant="outline">{a.currency}</Badge>
                  </TableCell>
                  <TableCell className="px-4 text-right font-medium">
                    {new Intl.NumberFormat("tr-TR", {
                      style: "currency",
                      currency: a.currency,
                    }).format(a.balance)}
                  </TableCell>
                  <TableCell className="px-4">
                    {a.isActive ? (
                      <Badge className="border-green-500/20 bg-green-500/10 text-green-500 hover:bg-green-500/20">
                        Active
                      </Badge>
                    ) : (
                      <Badge variant="destructive">Inactive</Badge>
                    )}
                  </TableCell>
                  <TableCell className="px-4">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" className="h-8 w-8 p-0">
                          <span className="sr-only">Open menu</span>
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuLabel>Actions</DropdownMenuLabel>
                        <DropdownMenuSeparator />
                        <AccountForm
                          account={a}
                          onSaved={refetch}
                          triggerLabel={
                            <div className="flex w-full items-center">
                              <Edit2 className="mr-2 h-4 w-4" />
                              <span>Edit</span>
                            </div>
                          }
                          isDropdownItem
                        />
                        <DropdownMenuItem
                          className="text-destructive focus:text-destructive"
                          onClick={() => setDeleteId(a.id)}
                        >
                          <Trash2 className="mr-2 h-4 w-4" />
                          Delete
                        </DropdownMenuItem>
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
        open={!!deleteId}
        onOpenChange={(v) => !v && setDeleteId(null)}
        title="Delete Account"
        description="Are you sure you want to delete this account? This action cannot be undone."
        confirmLabel="Delete"
        confirmVariant="destructive"
        onConfirm={async () => {
          if (!deleteId) return;
          deleteAccount.mutate(deleteId, {
            onSuccess: () => {
              toast.success("Account deleted successfully");
              setDeleteId(null);
              refetch();
            },
            onError: (err) => {
              const apiError = err as unknown as ErrorResponse;
              toast.error(apiError.message || "Failed to delete account");
            },
          });
        }}
      />
    </div>
  );
}

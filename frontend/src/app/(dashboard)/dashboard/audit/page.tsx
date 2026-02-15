"use client";

import { useState } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { useAuditLogs } from "@/lib/hooks";
import { toast } from "sonner";
import { Eye, ChevronLeft, ChevronRight, Save } from "lucide-react";

export default function AuditPage() {
  const [page, setPage] = useState(0);
  const size = 10;

  const [actionFilter, setActionFilter] = useState<string>("ALL");
  const [performedByFilter, setPerformedByFilter] = useState("");
  const [fromFilter, setFromFilter] = useState("");
  const [toFilter, setToFilter] = useState("");
  const [correlationFilter, setCorrelationFilter] = useState("");
  const [entityTypeFilter, setEntityTypeFilter] = useState("");

  const [presets, setPresets] = useState<Record<string, string>[]>(() => {
    if (typeof window !== "undefined") {
      try {
        const raw = localStorage.getItem("auditPresets");
        return raw ? JSON.parse(raw) : [];
      } catch {
        return [];
      }
    }
    return [];
  });
  const [presetName, setPresetName] = useState("");

  const toISO = (s: string) => (s ? new Date(s).toISOString() : undefined);

  const { data, isLoading, isError } = useAuditLogs({
    page,
    size,
    action: actionFilter === "ALL" ? undefined : actionFilter,
    performedBy: performedByFilter || undefined,
    from: toISO(fromFilter) || undefined,
    to: toISO(toFilter) || undefined,
    correlationId: correlationFilter || undefined,
    entityType: entityTypeFilter || undefined,
  });

  const savePreset = () => {
    if (!presetName) {
      toast.error("Please enter a name for the preset");
      return;
    }
    const p = {
      name: presetName,
      action: actionFilter,
      performedBy: performedByFilter,
      from: fromFilter,
      to: toFilter,
      correlationId: correlationFilter,
      entityType: entityTypeFilter,
    };
    const next = [p, ...presets].slice(0, 10);
    setPresets(next);
    localStorage.setItem("auditPresets", JSON.stringify(next));
    setPresetName("");
    toast.success("Preset saved successfully");
  };

  const applyPreset = (p: Record<string, string>) => {
    setActionFilter(p.action ?? "ALL");
    setPerformedByFilter(p.performedBy ?? "");
    setFromFilter(p.from ?? "");
    setToFilter(p.to ?? "");
    setCorrelationFilter(p.correlationId ?? "");
    setEntityTypeFilter(p.entityType ?? "");
    setPage(0);
    toast.success(`Applied preset: ${p.name}`);
  };

  if (isError) {
    return (
      <div className="text-destructive p-8 text-center">
        Failed to load audit logs. Please try again later.
      </div>
    );
  }

  return (
    <div className="flex-1 space-y-4 p-4 pt-6 md:p-8">
      <div className="flex items-center justify-between space-y-2">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Audit Logs</h2>
          <p className="text-muted-foreground">
            Monitor all system activities and payment events.
          </p>
        </div>
      </div>

      <Card>
        <CardContent className="p-6">
          <div className="mb-6 grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-4">
            <div className="space-y-2">
              <label className="text-muted-foreground text-xs font-medium uppercase">
                Action
              </label>
              <Select
                value={actionFilter}
                onValueChange={(v) => {
                  setActionFilter(v);
                  setPage(0);
                }}
              >
                <SelectTrigger>
                  <SelectValue placeholder="All Actions" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All Actions</SelectItem>
                  <SelectItem value="PAYMENT_CREATED">
                    Payment Created
                  </SelectItem>
                  <SelectItem value="PAYMENT_APPROVED">
                    Payment Approved
                  </SelectItem>
                  <SelectItem value="PAYMENT_REJECTED">
                    Payment Rejected
                  </SelectItem>
                  <SelectItem value="PAYMENT_COMPLETED">
                    Payment Completed
                  </SelectItem>
                  <SelectItem value="PAYMENT_FAILED">Payment Failed</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <label className="text-muted-foreground text-xs font-medium uppercase">
                Performed By
              </label>
              <Input
                placeholder="User ID..."
                value={performedByFilter}
                onChange={(e) => {
                  setPerformedByFilter(e.target.value);
                  setPage(0);
                }}
              />
            </div>

            <div className="space-y-2">
              <label className="text-muted-foreground text-xs font-medium uppercase">
                Entity Type
              </label>
              <Input
                placeholder="e.g. PAYMENT"
                value={entityTypeFilter}
                onChange={(e) => {
                  setEntityTypeFilter(e.target.value);
                  setPage(0);
                }}
              />
            </div>

            <div className="space-y-2">
              <label className="text-muted-foreground text-xs font-medium uppercase">
                Correlation ID
              </label>
              <Input
                placeholder="Search correlation..."
                value={correlationFilter}
                onChange={(e) => {
                  setCorrelationFilter(e.target.value);
                  setPage(0);
                }}
              />
            </div>

            <div className="space-y-2">
              <label className="text-muted-foreground text-xs font-medium uppercase">
                From Date
              </label>
              <Input
                type="datetime-local"
                value={fromFilter}
                onChange={(e) => {
                  setFromFilter(e.target.value);
                  setPage(0);
                }}
              />
            </div>

            <div className="space-y-2">
              <label className="text-muted-foreground text-xs font-medium uppercase">
                To Date
              </label>
              <Input
                type="datetime-local"
                value={toFilter}
                onChange={(e) => {
                  setToFilter(e.target.value);
                  setPage(0);
                }}
              />
            </div>

            <div className="flex items-end gap-2 space-y-2">
              <div className="flex-1">
                <label className="text-muted-foreground mb-2 block text-xs font-medium uppercase">
                  Save Filter
                </label>
                <Input
                  placeholder="Preset name..."
                  value={presetName}
                  onChange={(e) => setPresetName(e.target.value)}
                />
              </div>
              <Button size="icon" variant="outline" onClick={savePreset}>
                <Save className="h-4 w-4" />
              </Button>
            </div>

            <div className="space-y-2">
              <label className="text-muted-foreground text-xs font-medium uppercase">
                Presets
              </label>
              <Select onValueChange={(v) => applyPreset(JSON.parse(v))}>
                <SelectTrigger>
                  <SelectValue placeholder="Select a preset" />
                </SelectTrigger>
                <SelectContent>
                  {presets.length === 0 && (
                    <SelectItem value="{}" disabled>
                      No presets saved
                    </SelectItem>
                  )}
                  {presets.map((p, idx) => (
                    <div
                      key={idx}
                      className="flex items-center justify-between p-1"
                    >
                      <SelectItem value={JSON.stringify(p)}>
                        {p.name}
                      </SelectItem>
                    </div>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Time</TableHead>
                  <TableHead>Action</TableHead>
                  <TableHead>Entity</TableHead>
                  <TableHead>Performed By</TableHead>
                  <TableHead className="w-[100px]">Details</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={5} className="h-24 text-center">
                      Loading audit logs...
                    </TableCell>
                  </TableRow>
                ) : data?.content.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} className="h-24 text-center">
                      No audit logs found matching the filters.
                    </TableCell>
                  </TableRow>
                ) : (
                  data?.content.map((a) => (
                    <TableRow key={a.id}>
                      <TableCell className="text-xs whitespace-nowrap">
                        {new Date(a.createdAt).toLocaleString()}
                      </TableCell>
                      <TableCell>
                        <Badge
                          variant="outline"
                          className="text-[10px] font-bold uppercase"
                        >
                          {a.action.replace(/_/g, " ")}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex flex-col gap-0.5">
                          <span className="text-muted-foreground text-[10px] font-medium uppercase">
                            {a.entityType}
                          </span>
                          <span
                            className="max-w-[120px] truncate font-mono text-xs"
                            title={a.entityId}
                          >
                            {a.entityId}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell
                        className="max-w-[120px] truncate font-mono text-xs"
                        title={a.performedBy}
                      >
                        {a.performedBy}
                      </TableCell>
                      <TableCell>
                        <Dialog>
                          <DialogTrigger asChild>
                            <Button
                              variant="ghost"
                              size="icon"
                              className="h-8 w-8"
                            >
                              <Eye className="h-4 w-4" />
                            </Button>
                          </DialogTrigger>
                          <DialogContent className="sm:max-w-[600px]">
                            <DialogHeader>
                              <DialogTitle>Audit Log Details</DialogTitle>
                            </DialogHeader>
                            <div className="space-y-4 py-4">
                              <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-1">
                                  <span className="text-muted-foreground text-xs font-medium uppercase">
                                    Log ID
                                  </span>
                                  <p className="font-mono text-sm">{a.id}</p>
                                </div>
                                <div className="space-y-1">
                                  <span className="text-muted-foreground text-xs font-medium uppercase">
                                    Correlation ID
                                  </span>
                                  <p className="font-mono text-sm">
                                    {a.correlationId || "N/A"}
                                  </p>
                                </div>
                              </div>
                              <div className="space-y-1">
                                <span className="text-muted-foreground text-xs font-medium uppercase">
                                  Full Details
                                </span>
                                <div className="bg-muted rounded p-4">
                                  <pre className="font-mono text-xs whitespace-pre-wrap">
                                    {a.details ||
                                      "No additional details available."}
                                  </pre>
                                </div>
                              </div>
                            </div>
                          </DialogContent>
                        </Dialog>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>

          <div className="mt-4 flex items-center justify-between">
            <div className="text-muted-foreground text-sm">
              Showing page {(data?.page ?? 0) + 1} of {data?.totalPages ?? 0} (
              {data?.totalElements ?? 0} total)
            </div>
            <div className="flex items-center space-x-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page <= 0 || isLoading}
              >
                <ChevronLeft className="mr-1 h-4 w-4" /> Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => p + 1)}
                disabled={
                  data ? page + 1 >= data.totalPages : true || isLoading
                }
              >
                Next <ChevronRight className="ml-1 h-4 w-4" />
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

"use client";

import { useState } from "react";
import { useForm, type Resolver } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogTrigger,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { useLimits, useCreateLimit } from "@/lib/hooks";
import {
  createLimitSchema,
  type CreateLimitFormValues,
} from "@/lib/validators";
import { toast } from "sonner";
import type { ErrorResponse } from "@/lib/types";
import { PlusCircle, Shield, Coins } from "lucide-react";

export default function LimitsPage() {
  const { data, isLoading, isError, refetch } = useLimits();
  const create = useCreateLimit();
  const [open, setOpen] = useState(false);

  const form = useForm<CreateLimitFormValues>({
    resolver: zodResolver(createLimitSchema) as Resolver<CreateLimitFormValues>,
    defaultValues: {
      role: "MAKER",
      currency: "TRY",
      maxSingleAmount: 1000,
      maxDailyAmount: 10000,
    },
  });

  if (isLoading)
    return <div className="p-8 text-center">Loading limits...</div>;
  if (isError)
    return (
      <div className="text-destructive p-8 text-center">
        Failed to load limits.
      </div>
    );

  function onSubmit(values: CreateLimitFormValues) {
    create.mutate(values, {
      onSuccess: () => {
        toast.success("Limit configuration created successfully");
        setOpen(false);
        form.reset();
        refetch();
      },
      onError: (err) => {
        const apiError = err as unknown as ErrorResponse;
        toast.error(apiError.message || "Failed to create limit");
      },
    });
  }

  return (
    <div className="flex-1 space-y-4 p-4 pt-6 md:p-8">
      <div className="flex items-center justify-between space-y-2">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Limits</h2>
          <p className="text-muted-foreground">
            Configure transaction limits per role and currency.
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
              <Button className="gap-2">
                <PlusCircle className="h-4 w-4" />
                New Limit
              </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-[425px]">
              <DialogHeader>
                <DialogTitle>Create New Limit Configuration</DialogTitle>
              </DialogHeader>

              <Form {...form}>
                <form
                  onSubmit={form.handleSubmit(onSubmit)}
                  className="space-y-4 pt-4"
                >
                  <div className="grid grid-cols-2 gap-4">
                    <FormField
                      control={form.control}
                      name="role"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>User Role</FormLabel>
                          <Select
                            onValueChange={field.onChange}
                            defaultValue={field.value}
                          >
                            <FormControl>
                              <SelectTrigger>
                                <SelectValue placeholder="Select role" />
                              </SelectTrigger>
                            </FormControl>
                            <SelectContent>
                              <SelectItem value="MAKER">Maker</SelectItem>
                              <SelectItem value="CHECKER">Checker</SelectItem>
                              <SelectItem value="ADMIN">Admin</SelectItem>
                            </SelectContent>
                          </Select>
                          <FormMessage />
                        </FormItem>
                      )}
                    />

                    <FormField
                      control={form.control}
                      name="currency"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Currency</FormLabel>
                          <Select
                            onValueChange={field.onChange}
                            defaultValue={field.value}
                          >
                            <FormControl>
                              <SelectTrigger>
                                <SelectValue placeholder="Select currency" />
                              </SelectTrigger>
                            </FormControl>
                            <SelectContent>
                              <SelectItem value="TRY">TRY</SelectItem>
                              <SelectItem value="USD">USD</SelectItem>
                              <SelectItem value="EUR">EUR</SelectItem>
                              <SelectItem value="GBP">GBP</SelectItem>
                            </SelectContent>
                          </Select>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                  </div>

                  <FormField
                    control={form.control}
                    name="maxSingleAmount"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Single Transaction Limit</FormLabel>
                        <FormControl>
                          <Input type="number" step="0.01" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name="maxDailyAmount"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Daily Total Limit</FormLabel>
                        <FormControl>
                          <Input type="number" step="0.01" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <DialogFooter className="pt-4">
                    <Button
                      type="submit"
                      className="w-full"
                      disabled={create.isPending}
                    >
                      {create.isPending ? "Creating..." : "Create Limit"}
                    </Button>
                  </DialogFooter>
                </form>
              </Form>
            </DialogContent>
          </Dialog>
        </div>
      </div>

      <Card>
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-[100px]">Role</TableHead>
                <TableHead>Currency</TableHead>
                <TableHead className="text-right">Single Limit</TableHead>
                <TableHead className="text-right">Daily Limit</TableHead>
                <TableHead className="text-center">Status</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data?.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} className="h-24 text-center">
                    No limit configurations found.
                  </TableCell>
                </TableRow>
              ) : (
                data?.map((l) => (
                  <TableRow key={l.id}>
                    <TableCell>
                      <div className="flex items-center gap-2 text-xs font-medium">
                        <Shield className="text-muted-foreground h-3 w-3" />
                        {l.role}
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2 text-xs">
                        <Coins className="text-muted-foreground h-3 w-3" />
                        {l.currency}
                      </div>
                    </TableCell>
                    <TableCell className="text-right font-mono">
                      {new Intl.NumberFormat("tr-TR", {
                        style: "currency",
                        currency: l.currency,
                      }).format(l.maxSingleAmount)}
                    </TableCell>
                    <TableCell className="text-right font-mono">
                      {new Intl.NumberFormat("tr-TR", {
                        style: "currency",
                        currency: l.currency,
                      }).format(l.maxDailyAmount)}
                    </TableCell>
                    <TableCell className="text-center">
                      {l.isActive ? (
                        <Badge className="border-green-500/20 bg-green-500/10 text-green-500 hover:bg-green-500/20">
                          Active
                        </Badge>
                      ) : (
                        <Badge variant="destructive">Inactive</Badge>
                      )}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}

"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useCreateAccount, useUpdateAccount } from "@/lib/hooks";
import {
  createAccountSchema,
  updateAccountSchema,
  type CreateAccountFormValues,
  type UpdateAccountFormValues,
} from "@/lib/validators";
import type { AccountResponse, ErrorResponse } from "@/lib/types";
import { DropdownMenuItem } from "@/components/ui/dropdown-menu";
import { PlusCircle } from "lucide-react";

type Props = {
  account?: AccountResponse;
  onSaved?: () => void;
  triggerLabel?: React.ReactNode;
  isDropdownItem?: boolean;
};

export default function AccountForm({
  account,
  onSaved,
  triggerLabel,
  isDropdownItem,
}: Props) {
  const isEdit = !!account;
  const [open, setOpen] = useState(false);

  const form = useForm<CreateAccountFormValues | UpdateAccountFormValues>({
    resolver: zodResolver(isEdit ? updateAccountSchema : createAccountSchema),
    defaultValues: isEdit
      ? {
          customerName: account?.customerName ?? "",
        }
      : {
          iban: "",
          customerName: "",
          currency: "TRY",
        },
  });

  const create = useCreateAccount();
  const update = useUpdateAccount();

  function onSubmit(values: CreateAccountFormValues | UpdateAccountFormValues) {
    if (isEdit && account) {
      update.mutate(
        { id: account.id, data: values as UpdateAccountFormValues },
        {
          onSuccess: () => {
            setOpen(false);
            toast.success("Account updated successfully");
            onSaved?.();
          },
          onError: (err) => {
            const apiError = err as unknown as ErrorResponse;
            toast.error(apiError.message || "Failed to update account");
          },
        },
      );
      return;
    }

    create.mutate(values as CreateAccountFormValues, {
      onSuccess: () => {
        setOpen(false);
        form.reset();
        toast.success("Account created successfully");
        onSaved?.();
      },
      onError: (err) => {
        const apiError = err as unknown as ErrorResponse;
        toast.error(apiError.message || "Failed to create account");
      },
    });
  }

  const trigger = isDropdownItem ? (
    <DropdownMenuItem
      onSelect={(e) => {
        e.preventDefault();
        setOpen(true);
      }}
    >
      {triggerLabel}
    </DropdownMenuItem>
  ) : (
    <Button
      variant={isEdit ? "ghost" : "default"}
      className={isEdit ? "" : "gap-2"}
    >
      {!isEdit && <PlusCircle className="h-4 w-4" />}
      {triggerLabel ?? (isEdit ? "Edit" : "Create Account")}
    </Button>
  );

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>
            {isEdit ? "Edit Account" : "Create New Account"}
          </DialogTitle>
        </DialogHeader>

        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit)}
            className="space-y-4 pt-4"
          >
            {!isEdit && (
              <FormField
                control={form.control}
                name="iban"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>IBAN</FormLabel>
                    <FormControl>
                      <Input placeholder="TR..." {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            )}

            <FormField
              control={form.control}
              name="customerName"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Customer Name</FormLabel>
                  <FormControl>
                    <Input placeholder="John Doe" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            {!isEdit && (
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
                        <SelectItem value="TRY">TRY (₺)</SelectItem>
                        <SelectItem value="USD">USD ($)</SelectItem>
                        <SelectItem value="EUR">EUR (€)</SelectItem>
                        <SelectItem value="GBP">GBP (£)</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            )}

            <DialogFooter className="pt-4">
              <Button
                type="submit"
                className="w-full"
                disabled={create.isPending || update.isPending}
              >
                {isEdit
                  ? update.isPending
                    ? "Updating..."
                    : "Save Changes"
                  : create.isPending
                    ? "Creating..."
                    : "Create Account"}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

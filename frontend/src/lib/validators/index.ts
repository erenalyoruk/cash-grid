import { z } from "zod/v4";

// ---- Auth ----

export const loginSchema = z.object({
  username: z.string().min(3, "Username must be at least 3 characters"),
  password: z.string().min(8, "Password must be at least 8 characters"),
});

export type LoginFormValues = z.infer<typeof loginSchema>;

export const registerSchema = z.object({
  username: z.string().min(3, "Username must be at least 3 characters"),
  email: z.email("Please enter a valid email address"),
  password: z.string().min(8, "Password must be at least 8 characters"),
  role: z.enum(["MAKER", "CHECKER", "ADMIN"]),
});

export type RegisterFormValues = z.infer<typeof registerSchema>;

// ---- Account ----

export const createAccountSchema = z.object({
  iban: z
    .string()
    .regex(/^TR\d{24}$/, "IBAN must be in Turkish format (TR + 24 digits)"),
  ownerName: z.string().min(2, "Owner name must be at least 2 characters"),
  currency: z.enum(["TRY", "USD", "EUR", "GBP"]),
  balance: z.coerce.number().min(0, "Balance must be non-negative"),
});

export type CreateAccountFormValues = z.infer<typeof createAccountSchema>;

export const updateAccountSchema = z.object({
  ownerName: z
    .string()
    .min(2, "Owner name must be at least 2 characters")
    .optional(),
  balance: z.coerce.number().min(0, "Balance must be non-negative").optional(),
});

export type UpdateAccountFormValues = z.infer<typeof updateAccountSchema>;

// ---- Payment ----

export const createPaymentSchema = z.object({
  sourceAccountId: z.coerce.number().positive("Source account is required"),
  targetAccountId: z.coerce.number().positive("Target account is required"),
  amount: z.coerce.number().positive("Amount must be greater than 0"),
  currency: z.enum(["TRY", "USD", "EUR", "GBP"]),
  description: z.string().min(1, "Description is required"),
});

export type CreatePaymentFormValues = z.infer<typeof createPaymentSchema>;

// ---- Limit ----

export const createLimitSchema = z.object({
  role: z.enum(["MAKER", "CHECKER", "ADMIN"]),
  currency: z.enum(["TRY", "USD", "EUR", "GBP"]),
  singleTransactionLimit: z.coerce
    .number()
    .positive("Single transaction limit must be positive"),
  dailyLimit: z.coerce.number().positive("Daily limit must be positive"),
});

export type CreateLimitFormValues = z.infer<typeof createLimitSchema>;

export const updateLimitSchema = z.object({
  singleTransactionLimit: z.coerce
    .number()
    .positive("Single transaction limit must be positive")
    .optional(),
  dailyLimit: z.coerce
    .number()
    .positive("Daily limit must be positive")
    .optional(),
});

export type UpdateLimitFormValues = z.infer<typeof updateLimitSchema>;

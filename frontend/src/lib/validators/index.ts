import { z } from "zod";

export const loginSchema = z.object({
  username: z.string().min(3, "Username must be at least 3 characters"),
  password: z.string().min(8, "Password must be at least 8 characters"),
});

export type LoginFormValues = z.infer<typeof loginSchema>;

export const registerSchema = z.object({
  username: z.string().min(3, "Username must be at least 3 characters"),
  email: z.string().email("Please enter a valid email address"),
  password: z.string().min(8, "Password must be at least 8 characters"),
  role: z.enum(["MAKER", "CHECKER", "ADMIN"]),
});

export type RegisterFormValues = z.infer<typeof registerSchema>;

export const updateUsernameSchema = z.object({
  username: z.string().min(3, "Username must be at least 3 characters"),
});

export type UpdateUsernameFormValues = z.infer<typeof updateUsernameSchema>;

export const updateEmailSchema = z.object({
  email: z.string().email("Please enter a valid email address"),
});

export type UpdateEmailFormValues = z.infer<typeof updateEmailSchema>;

export const updatePasswordSchema = z
  .object({
    currentPassword: z.string().min(1, "Current password is required"),
    newPassword: z
      .string()
      .min(8, "New password must be at least 8 characters"),
    confirmPassword: z.string().min(8, "Please confirm your new password"),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
  });

export type UpdatePasswordFormValues = z.infer<typeof updatePasswordSchema>;

export const createAccountSchema = z.object({
  iban: z
    .string()
    .regex(/^TR\d{24}$/, "IBAN must be in Turkish format (TR + 24 digits)"),
  customerName: z
    .string()
    .min(2, "Customer name must be at least 2 characters"),
  currency: z.enum(["TRY", "USD", "EUR", "GBP"]),
});

export type CreateAccountFormValues = z.infer<typeof createAccountSchema>;

export const updateAccountSchema = z.object({
  customerName: z
    .string()
    .min(2, "Customer name must be at least 2 characters"),
});

export type UpdateAccountFormValues = z.infer<typeof updateAccountSchema>;

export const createPaymentSchema = z.object({
  idempotencyKey: z.string().min(1, "Idempotency key is required"),
  sourceIban: z
    .string()
    .regex(
      /^TR\d{24}$/,
      "Source IBAN must be in Turkish format (TR + 24 digits)",
    ),
  targetIban: z
    .string()
    .regex(
      /^TR\d{24}$/,
      "Target IBAN must be in Turkish format (TR + 24 digits)",
    ),
  amount: z.coerce.number().positive("Amount must be greater than 0"),
  currency: z.enum(["TRY", "USD", "EUR", "GBP"]),
  description: z.string().max(255).optional(),
});

export type CreatePaymentFormValues = z.infer<typeof createPaymentSchema>;

export const createLimitSchema = z.object({
  role: z.enum(["MAKER", "CHECKER", "ADMIN"]),
  currency: z.enum(["TRY", "USD", "EUR", "GBP"]),
  maxSingleAmount: z.coerce
    .number()
    .positive("Max single amount must be positive"),
  maxDailyAmount: z.coerce
    .number()
    .positive("Max daily amount must be positive"),
});

export type CreateLimitFormValues = z.infer<typeof createLimitSchema>;

export const updateLimitSchema = z.object({
  maxSingleAmount: z.coerce
    .number()
    .positive("Max single amount must be positive")
    .optional(),
  maxDailyAmount: z.coerce
    .number()
    .positive("Max daily amount must be positive")
    .optional(),
});

export type UpdateLimitFormValues = z.infer<typeof updateLimitSchema>;

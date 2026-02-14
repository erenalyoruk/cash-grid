// ---- Auth ----

export type Role = "MAKER" | "CHECKER" | "ADMIN";

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  role: Role;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
}

export interface UserResponse {
  id: number;
  username: string;
  role: Role;
  createdAt: string;
}

// ---- Account ----

export type Currency = "TRY" | "USD" | "EUR" | "GBP";

export interface AccountResponse {
  id: number;
  iban: string;
  ownerName: string;
  currency: Currency;
  balance: number;
  deleted: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAccountRequest {
  iban: string;
  ownerName: string;
  currency: Currency;
  balance: number;
}

export interface UpdateAccountRequest {
  ownerName?: string;
  balance?: number;
}

// ---- Payment ----

export type PaymentStatus =
  | "PENDING"
  | "APPROVED"
  | "REJECTED"
  | "COMPLETED"
  | "FAILED";

export interface PaymentResponse {
  id: number;
  referenceNumber: string;
  sourceAccountId: number;
  targetAccountId: number;
  amount: number;
  currency: Currency;
  description: string;
  status: PaymentStatus;
  createdBy: string;
  approvedBy: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePaymentRequest {
  sourceAccountId: number;
  targetAccountId: number;
  amount: number;
  currency: Currency;
  description: string;
}

// ---- Limit ----

export interface LimitResponse {
  id: number;
  role: Role;
  currency: Currency;
  singleTransactionLimit: number;
  dailyLimit: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateLimitRequest {
  role: Role;
  currency: Currency;
  singleTransactionLimit: number;
  dailyLimit: number;
}

export interface UpdateLimitRequest {
  singleTransactionLimit?: number;
  dailyLimit?: number;
}

// ---- Audit ----

export type AuditAction =
  | "PAYMENT_CREATED"
  | "PAYMENT_APPROVED"
  | "PAYMENT_REJECTED"
  | "PAYMENT_COMPLETED"
  | "PAYMENT_FAILED";

export interface AuditLogResponse {
  id: number;
  paymentId: number;
  action: AuditAction;
  performedBy: string;
  details: string | null;
  correlationId: string | null;
  createdAt: string;
}

// ---- Common ----

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface ErrorResponse {
  status: number;
  message: string;
  timestamp: string;
  correlationId: string | null;
}

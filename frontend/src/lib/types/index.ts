export type Role = "MAKER" | "CHECKER" | "ADMIN";
export type Currency = "TRY" | "USD" | "EUR" | "GBP";

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
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
  id: string;
  username: string;
  email: string;
  role: Role;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateUsernameRequest {
  newUsername: string;
}

export interface UpdateEmailRequest {
  newEmail: string;
}

export interface UpdatePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface AccountResponse {
  id: string;
  customerName: string;
  iban: string;
  currency: Currency;
  balance: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAccountRequest {
  customerName: string;
  iban: string;
  currency: string;
}

export interface UpdateAccountRequest {
  customerName: string;
}

export type PaymentStatus =
  | "PENDING"
  | "APPROVED"
  | "REJECTED"
  | "COMPLETED"
  | "FAILED";

export interface PaymentResponse {
  id: string;
  idempotencyKey: string;
  sourceIban: string;
  targetIban: string;
  amount: number;
  currency: Currency;
  description: string | null;
  status: PaymentStatus;
  createdByUsername: string;
  approvedByUsername: string | null;
  rejectionReason: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePaymentRequest {
  idempotencyKey: string;
  sourceIban: string;
  targetIban: string;
  amount: number;
  currency: string;
  description?: string;
}

export interface RejectPaymentRequest {
  reason: string;
}

// ---- Limit ----
export interface LimitResponse {
  id: string;
  role: Role;
  maxSingleAmount: number;
  maxDailyAmount: number;
  currency: Currency;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateLimitRequest {
  role: Role;
  maxSingleAmount: number;
  maxDailyAmount: number;
  currency: string;
}

export interface UpdateLimitRequest {
  maxSingleAmount?: number;
  maxDailyAmount?: number;
}

export interface AuditLogResponse {
  id: string;
  entityType: string;
  entityId: string;
  action: string;
  performedBy: string;
  correlationId: string | null;
  details: string | null;
  createdAt: string;
}

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

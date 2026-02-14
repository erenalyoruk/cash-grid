"use client";

import { useAuthStore } from "@/lib/store/auth-store";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function DashboardPage() {
  const user = useAuthStore((s) => s.user);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Dashboard</h2>
        <p className="text-muted-foreground">Welcome back, {user?.username}!</p>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader>
            <CardDescription>Role</CardDescription>
            <CardTitle className="text-2xl">{user?.role}</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-muted-foreground text-sm">
              {user?.role === "MAKER" && "You can create payments."}
              {user?.role === "CHECKER" &&
                "You can approve or reject payments."}
              {user?.role === "ADMIN" &&
                "You have full access to manage the platform."}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardDescription>Quick Actions</CardDescription>
            <CardTitle className="text-2xl">Payments</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-muted-foreground text-sm">
              {user?.role === "MAKER" && "Create a new payment to get started."}
              {user?.role === "CHECKER" &&
                "Review pending payments awaiting approval."}
              {user?.role === "ADMIN" &&
                "Monitor all payment activity across the platform."}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardDescription>Account</CardDescription>
            <CardTitle className="text-2xl">{user?.username}</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-muted-foreground text-sm">
              Member since{" "}
              {user?.createdAt
                ? new Date(user.createdAt).toLocaleDateString()
                : "—"}
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

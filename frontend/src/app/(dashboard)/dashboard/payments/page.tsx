"use client";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function PaymentsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Payments</h2>
        <p className="text-muted-foreground">
          Create, view, and manage payments.
        </p>
      </div>
      <Card>
        <CardHeader>
          <CardTitle>Payment List</CardTitle>
          <CardDescription>
            This page will display all payments with approval workflow.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground text-sm">
            Coming soon — payment table with create, approve, and reject
            actions.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}

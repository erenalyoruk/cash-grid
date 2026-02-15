"use client";

import { Card, CardContent } from "@/components/ui/card";
import PaymentForm from "./components/payment-form";
import PaymentTable from "./components/payment-table";

export default function PaymentsPage() {
  return (
    <div className="flex-1 space-y-4 p-4 pt-6 md:p-8">
      <div className="flex items-center justify-between space-y-2">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Payments</h2>
          <p className="text-muted-foreground">
            Create, approve or reject payment requests.
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <PaymentForm />
        </div>
      </div>

      <Card className="bg-background/50 overflow-hidden border-none shadow-sm">
        <CardContent className="p-6">
          <PaymentTable />
        </CardContent>
      </Card>
    </div>
  );
}

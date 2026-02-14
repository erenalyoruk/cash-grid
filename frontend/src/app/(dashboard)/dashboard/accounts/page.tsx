"use client";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function AccountsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Accounts</h2>
        <p className="text-muted-foreground">
          Manage bank accounts and balances.
        </p>
      </div>
      <Card>
        <CardHeader>
          <CardTitle>Account List</CardTitle>
          <CardDescription>
            This page will display all accounts with CRUD operations.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground text-sm">
            Coming soon — account table with create, edit, and delete actions.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}

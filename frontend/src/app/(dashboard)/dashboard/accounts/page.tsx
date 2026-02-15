"use client";

import { Card, CardContent } from "@/components/ui/card";
import AccountForm from "./components/account-form";
import AccountTable from "./components/account-table";

export default function AccountsPage() {
  return (
    <div className="flex-1 space-y-4 p-4 pt-6 md:p-8">
      <div className="flex items-center justify-between space-y-2">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Accounts</h2>
          <p className="text-muted-foreground">
            Manage your bank accounts, monitor balances and currencies.
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <AccountForm />
        </div>
      </div>

      <Card className="bg-background/50 overflow-hidden border-none shadow-sm">
        <CardContent className="p-6">
          <AccountTable />
        </CardContent>
      </Card>
    </div>
  );
}

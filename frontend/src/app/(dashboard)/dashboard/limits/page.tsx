"use client";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function LimitsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Limits</h2>
        <p className="text-muted-foreground">
          Configure transaction limits per role and currency.
        </p>
      </div>
      <Card>
        <CardHeader>
          <CardTitle>Limit Configuration</CardTitle>
          <CardDescription>
            This page will display and manage transaction limits.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground text-sm">
            Coming soon — limit table with create and edit actions.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}

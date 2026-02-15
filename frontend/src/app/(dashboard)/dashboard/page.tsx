"use client";

import { useAuthStore } from "@/lib/store/auth-store";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  ShieldCheck,
  CreditCard,
  User,
  ArrowUpRight,
  CheckCircle2,
  Activity,
  Wallet,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";

export default function DashboardPage() {
  const user = useAuthStore((s) => s.user);

  return (
    <div className="flex-1 space-y-4 p-4 pt-6 md:p-8">
      <div className="flex items-center justify-between space-y-2">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Dashboard</h2>
          <p className="text-muted-foreground">
            Welcome back to your financial control center.
          </p>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">User Role</CardTitle>
            <ShieldCheck className="text-muted-foreground h-4 w-4" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{user?.role}</div>
            <p className="text-muted-foreground mt-1 text-xs">
              {user?.role === "MAKER" && "Authorized to create payments"}
              {user?.role === "CHECKER" && "Authorized to verify transactions"}
              {user?.role === "ADMIN" && "Full system administrative access"}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Username</CardTitle>
            <User className="text-muted-foreground h-4 w-4" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{user?.username}</div>
            <p className="text-muted-foreground mt-1 truncate text-xs">
              {user?.email}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Status</CardTitle>
            <Activity className="text-muted-foreground h-4 w-4" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {user?.isActive ? (
                <Badge className="border-green-500/20 bg-green-500/10 text-green-500 hover:bg-green-500/20">
                  Active
                </Badge>
              ) : (
                <Badge variant="destructive">Inactive</Badge>
              )}
            </div>
            <p className="text-muted-foreground mt-1 text-xs">
              Account is in good standing
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Member Since</CardTitle>
            <Wallet className="text-muted-foreground h-4 w-4" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {user?.createdAt ? new Date(user.createdAt).getFullYear() : "—"}
            </div>
            <p className="text-muted-foreground mt-1 text-xs">
              Joined{" "}
              {user?.createdAt
                ? new Date(user.createdAt).toLocaleDateString()
                : "—"}
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
        <Card className="col-span-4">
          <CardHeader>
            <CardTitle>System Overview</CardTitle>
          </CardHeader>
          <CardContent className="pl-2">
            <div className="space-y-4 p-4">
              <div className="flex items-center gap-4">
                <div className="bg-primary/10 flex h-10 w-10 items-center justify-center rounded-full">
                  <CreditCard className="text-primary h-5 w-5" />
                </div>
                <div>
                  <p className="text-sm leading-none font-medium">
                    Payments Managed
                  </p>
                  <p className="text-muted-foreground text-sm">
                    Monitor and control your money flow efficiently.
                  </p>
                </div>
                <ArrowUpRight className="text-muted-foreground ml-auto h-4 w-4" />
              </div>

              <div className="flex items-center gap-4">
                <div className="flex h-10 w-10 items-center justify-center rounded-full bg-green-500/10">
                  <CheckCircle2 className="h-5 w-5 text-green-500" />
                </div>
                <div>
                  <p className="text-sm leading-none font-medium">
                    Multi-Level Security
                  </p>
                  <p className="text-muted-foreground text-sm">
                    Maker-Checker process ensures transaction safety.
                  </p>
                </div>
                <ArrowUpRight className="text-muted-foreground ml-auto h-4 w-4" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="col-span-3">
          <CardHeader>
            <CardTitle>Next Steps</CardTitle>
            <CardDescription>Quick actions based on your role.</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {user?.role === "MAKER" && (
                <p className="text-muted-foreground text-sm italic">
                  &quot;Start by creating a new account or initiating a payment
                  request from the side menu.&quot;
                </p>
              )}
              {user?.role === "CHECKER" && (
                <p className="text-muted-foreground text-sm italic">
                  &quot;Head over to the Payments section to review and approve
                  pending requests.&quot;
                </p>
              )}
              {user?.role === "ADMIN" && (
                <p className="text-muted-foreground text-sm italic">
                  &quot;Manage system limits and monitor activity through Audit
                  Logs.&quot;
                </p>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

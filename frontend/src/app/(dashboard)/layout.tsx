"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useAuthStore } from "@/lib/store/auth-store";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";

const navigation = [
  { name: "Dashboard", href: "/dashboard" },
  { name: "Accounts", href: "/dashboard/accounts" },
  { name: "Payments", href: "/dashboard/payments" },
  { name: "Limits", href: "/dashboard/limits", roles: ["ADMIN"] },
  { name: "Audit Logs", href: "/dashboard/audit", roles: ["ADMIN"] },
];

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, isAuthenticated, isLoading, logout } = useAuthStore();

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <p className="text-muted-foreground">Loading...</p>
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    router.push("/login");
    return null;
  }

  const filteredNav = navigation.filter(
    (item) => !item.roles || item.roles.includes(user.role),
  );

  function handleLogout() {
    logout();
    router.push("/login");
  }

  return (
    <div className="flex min-h-screen">
      {/* Sidebar */}
      <aside className="bg-muted/40 flex w-64 flex-col border-r">
        <div className="p-6">
          <h1 className="text-xl font-bold">CashGrid</h1>
        </div>
        <Separator />
        <nav className="flex-1 space-y-1 px-3 py-4">
          {filteredNav.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className={`block rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                pathname === item.href
                  ? "bg-primary text-primary-foreground"
                  : "text-muted-foreground hover:bg-muted hover:text-foreground"
              }`}
            >
              {item.name}
            </Link>
          ))}
        </nav>
        <Separator />
        <div className="p-4">
          <div className="flex items-center gap-3">
            <Avatar className="h-8 w-8">
              <AvatarFallback>
                {user.username.slice(0, 2).toUpperCase()}
              </AvatarFallback>
            </Avatar>
            <div className="flex-1 truncate">
              <p className="truncate text-sm font-medium">{user.username}</p>
              <Badge variant="secondary" className="text-xs">
                {user.role}
              </Badge>
            </div>
          </div>
          <Button
            variant="ghost"
            className="text-muted-foreground mt-3 w-full justify-start"
            onClick={handleLogout}
          >
            Sign out
          </Button>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-auto">
        <div className="p-8">{children}</div>
      </main>
    </div>
  );
}

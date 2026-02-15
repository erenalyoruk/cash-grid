"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect } from "react";
import { useAuthStore } from "@/lib/store/auth-store";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import {
  LayoutDashboard,
  Wallet,
  Send,
  ShieldAlert,
  FileSearch,
  LogOut,
  Settings,
} from "lucide-react";
import { cn } from "@/lib/utils";

const navigation = [
  { name: "Dashboard", href: "/dashboard", icon: LayoutDashboard },
  { name: "Accounts", href: "/dashboard/accounts", icon: Wallet },
  { name: "Payments", href: "/dashboard/payments", icon: Send },
  {
    name: "Limits",
    href: "/dashboard/limits",
    roles: ["ADMIN"],
    icon: ShieldAlert,
  },
  {
    name: "Audit Logs",
    href: "/dashboard/audit",
    roles: ["ADMIN"],
    icon: FileSearch,
  },
  {
    name: "Settings",
    href: "/dashboard/settings",
    icon: Settings,
  },
];

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, isAuthenticated, isLoading, logout } = useAuthStore();

  useEffect(() => {
    if (!isLoading && (!isAuthenticated || !user)) {
      router.push("/login");
    }
  }, [isLoading, isAuthenticated, user, router]);

  if (isLoading) {
    return (
      <div className="bg-background flex min-h-screen items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <div className="border-primary h-12 w-12 animate-spin rounded-full border-4 border-t-transparent"></div>
          <p className="text-muted-foreground animate-pulse font-medium">
            Preparing your dashboard...
          </p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated || !user) {
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
    <div className="bg-muted/30 flex min-h-screen">
      {/* Sidebar */}
      <aside className="bg-background fixed inset-y-0 left-0 z-50 flex w-72 flex-col border-r shadow-xl lg:shadow-none">
        <div className="flex h-20 items-center px-8">
          <Link href="/dashboard" className="group flex items-center gap-3">
            <div className="bg-primary text-primary-foreground shadow-primary/20 flex h-10 w-10 items-center justify-center rounded-xl shadow-lg transition-transform group-hover:scale-110">
              <span className="text-2xl font-bold tracking-tighter">C</span>
            </div>
            <div className="flex flex-col">
              <span className="text-xl font-bold tracking-tight">CashGrid</span>
              <span className="text-muted-foreground -mt-1 text-[10px] font-semibold tracking-widest uppercase">
                Financial Ops
              </span>
            </div>
          </Link>
        </div>

        <div className="flex-1 space-y-2 overflow-y-auto px-4 py-6">
          <div className="mb-2 px-4">
            <p className="text-muted-foreground/60 text-[10px] font-bold tracking-widest uppercase">
              Main Menu
            </p>
          </div>
          {filteredNav.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "group flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition-all duration-300",
                pathname === item.href
                  ? "bg-primary text-primary-foreground shadow-primary/20 shadow-md"
                  : "text-muted-foreground hover:bg-muted hover:text-foreground",
              )}
            >
              <item.icon
                className={cn(
                  "h-5 w-5 transition-transform group-hover:scale-110",
                  pathname === item.href
                    ? "text-primary-foreground"
                    : "text-muted-foreground group-hover:text-foreground",
                )}
              />
              {item.name}
              {pathname === item.href && (
                <div className="bg-primary-foreground ml-auto h-1.5 w-1.5 rounded-full" />
              )}
            </Link>
          ))}
        </div>

        <div className="bg-muted/10 mt-auto border-t p-6">
          <div className="bg-background mb-6 flex items-center gap-4 rounded-2xl border p-3 shadow-sm">
            <Avatar className="border-muted h-12 w-12 border-2 shadow-inner">
              <AvatarFallback className="bg-primary/10 text-primary text-sm font-bold">
                {user.username.slice(0, 2).toUpperCase()}
              </AvatarFallback>
            </Avatar>
            <div className="flex flex-1 flex-col overflow-hidden">
              <p className="text-foreground truncate text-sm font-bold tracking-tight">
                {user.username}
              </p>
              <Badge
                variant="outline"
                className="border-primary/20 bg-primary/5 text-primary mt-1 w-fit px-2 py-0 text-[10px] font-bold uppercase"
              >
                {user.role}
              </Badge>
            </div>

            <Link href="/dashboard/settings">
              <Button
                variant="ghost"
                size="icon"
                className="hover:bg-muted h-8 w-8 rounded-full"
              >
                <Settings className="text-muted-foreground h-4 w-4" />
              </Button>
            </Link>
          </div>

          <Button
            variant="ghost"
            size="sm"
            className="text-muted-foreground hover:bg-destructive/10 hover:text-destructive group w-full justify-start rounded-xl px-4 transition-colors"
            onClick={handleLogout}
          >
            <LogOut className="mr-3 h-5 w-5 transition-transform group-hover:-translate-x-1" />
            <span className="text-sm font-semibold">Sign out</span>
          </Button>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 pl-72">
        <header className="bg-background/80 sticky top-0 z-40 flex h-20 items-center justify-between border-b px-10 backdrop-blur-xl transition-all">
          <div className="flex items-center gap-4">
            <h1 className="text-foreground text-xl font-bold tracking-tight capitalize">
              {pathname.split("/").pop()?.replace(/-/g, " ")}
            </h1>
          </div>
          <div className="flex items-center gap-6">
            <div className="hidden flex-col items-end md:flex">
              <span className="text-muted-foreground text-xs font-bold tracking-tighter uppercase">
                System Status
              </span>
              <div className="flex items-center gap-2">
                <span className="h-2 w-2 animate-pulse rounded-full bg-green-500" />
                <span className="text-sm font-semibold text-green-600">
                  Online
                </span>
              </div>
            </div>
            <Separator orientation="vertical" className="mx-2 h-10" />
            <Avatar className="border-background h-10 w-10 border-2 shadow-md">
              <AvatarFallback className="bg-muted text-xs font-bold">
                CG
              </AvatarFallback>
            </Avatar>
          </div>
        </header>
        <div className="min-h-[calc(100vh-80px)] p-10">
          <div className="animate-in fade-in slide-in-from-bottom-4 mx-auto max-w-7xl duration-500">
            {children}
          </div>
        </div>
      </main>
    </div>
  );
}

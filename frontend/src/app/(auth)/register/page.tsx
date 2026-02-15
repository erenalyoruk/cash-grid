"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

import { useRegister } from "@/lib/hooks";
import { registerSchema, type RegisterFormValues } from "@/lib/validators";
import type { ErrorResponse } from "@/lib/types";

import { User, Mail, Lock, ShieldCheck, UserPlus, Loader2 } from "lucide-react";

export default function RegisterPage() {
  const router = useRouter();
  const register = useRegister();

  const form = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: "",
      email: "",
      password: "",
      role: "MAKER",
    },
  });

  function onSubmit(values: RegisterFormValues) {
    register.mutate(values, {
      onSuccess: () => {
        toast.success("Account created successfully!");
        router.push("/dashboard");
      },
      onError: (error) => {
        const apiError = error as unknown as ErrorResponse;
        toast.error(apiError.message || "Registration failed");
      },
    });
  }

  return (
    <Card className="bg-background/80 gap-0 overflow-hidden border-none py-0 shadow-2xl backdrop-blur-sm">
      <CardHeader className="space-y-1 p-6">
        <CardTitle className="text-center text-xl">Create Account</CardTitle>
        <CardDescription className="text-center">
          Join CashGrid to manage your financial operations
        </CardDescription>
      </CardHeader>
      <CardContent className="p-6 pt-0">
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="username"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Username</FormLabel>
                  <FormControl>
                    <div className="relative">
                      <User className="text-muted-foreground absolute top-3 left-3 h-4 w-4" />
                      <Input
                        placeholder="username"
                        className="pl-9"
                        {...field}
                      />
                    </div>
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="email"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Email Address</FormLabel>
                  <FormControl>
                    <div className="relative">
                      <Mail className="text-muted-foreground absolute top-3 left-3 h-4 w-4" />
                      <Input
                        type="email"
                        placeholder="name@example.com"
                        className="pl-9"
                        {...field}
                      />
                    </div>
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="password"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Password</FormLabel>
                  <FormControl>
                    <div className="relative">
                      <Lock className="text-muted-foreground absolute top-3 left-3 h-4 w-4" />
                      <Input
                        type="password"
                        placeholder="••••••••"
                        className="pl-9"
                        {...field}
                      />
                    </div>
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="role"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>System Role</FormLabel>
                  <Select
                    onValueChange={field.onChange}
                    defaultValue={field.value}
                  >
                    <FormControl>
                      <div className="relative">
                        <ShieldCheck className="text-muted-foreground absolute top-3 left-3 z-10 h-4 w-4" />
                        <SelectTrigger className="pl-9">
                          <SelectValue placeholder="Select a role" />
                        </SelectTrigger>
                      </div>
                    </FormControl>
                    <SelectContent>
                      <SelectItem value="MAKER">
                        Maker (Payments Creator)
                      </SelectItem>
                      <SelectItem value="CHECKER">
                        Checker (Approver)
                      </SelectItem>
                      <SelectItem value="ADMIN">Administrator</SelectItem>
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button
              type="submit"
              className="mt-2 h-11 w-full gap-2"
              disabled={register.isPending}
            >
              {register.isPending ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Creating Account...
                </>
              ) : (
                <>
                  <UserPlus className="h-4 w-4" />
                  Register Now
                </>
              )}
            </Button>
          </form>
        </Form>
      </CardContent>
      <CardFooter className="bg-muted/50 flex flex-col border-t p-6">
        <div className="text-muted-foreground w-full text-center text-sm">
          Already have an account?{" "}
          <Link
            href="/login"
            className="text-primary font-semibold hover:underline"
          >
            Sign in instead
          </Link>
        </div>
      </CardFooter>
    </Card>
  );
}

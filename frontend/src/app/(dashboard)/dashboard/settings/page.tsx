"use client";

import { useAuthStore } from "@/lib/store/auth-store";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import {
  updateUsernameSchema,
  updateEmailSchema,
  updatePasswordSchema,
  type UpdateUsernameFormValues,
  type UpdateEmailFormValues,
  type UpdatePasswordFormValues,
} from "@/lib/validators";
import {
  useUpdateUsername,
  useUpdateEmail,
  useUpdatePassword,
} from "@/lib/hooks/use-auth";
import { toast } from "sonner";
import type { ErrorResponse } from "@/lib/types";
import { User, Mail, Lock, Check } from "lucide-react";

export default function SettingsPage() {
  const user = useAuthStore((s) => s.user);

  // Hooks
  const updateUsername = useUpdateUsername();
  const updateEmail = useUpdateEmail();
  const updatePassword = useUpdatePassword();

  // Forms
  const usernameForm = useForm<UpdateUsernameFormValues>({
    resolver: zodResolver(updateUsernameSchema),
    defaultValues: { username: user?.username ?? "" },
  });

  const emailForm = useForm<UpdateEmailFormValues>({
    resolver: zodResolver(updateEmailSchema),
    defaultValues: { email: user?.email ?? "" },
  });

  const passwordForm = useForm<UpdatePasswordFormValues>({
    resolver: zodResolver(updatePasswordSchema),
    defaultValues: {
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    },
  });

  // Submit Handlers
  function onUsernameSubmit(values: UpdateUsernameFormValues) {
    updateUsername.mutate(values.username, {
      onSuccess: () => {
        toast.success("Username updated successfully");
        usernameForm.reset({ username: values.username });
      },
      onError: (err) => {
        const apiError = err as unknown as ErrorResponse;
        toast.error(apiError.message || "Failed to update username");
      },
    });
  }

  function onEmailSubmit(values: UpdateEmailFormValues) {
    updateEmail.mutate(values.email, {
      onSuccess: () => {
        toast.success("Email updated successfully");
        emailForm.reset({ email: values.email });
      },
      onError: (err) => {
        const apiError = err as unknown as ErrorResponse;
        toast.error(apiError.message || "Failed to update email");
      },
    });
  }

  function onPasswordSubmit(values: UpdatePasswordFormValues) {
    updatePassword.mutate(
      {
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
      },
      {
        onSuccess: () => {
          toast.success("Password updated successfully");
          passwordForm.reset();
        },
        onError: (err) => {
          const apiError = err as unknown as ErrorResponse;
          toast.error(apiError.message || "Failed to update password");
        },
      },
    );
  }

  return (
    <div className="flex-1 space-y-8 p-4 pt-6 md:p-8">
      <div className="flex items-center justify-between space-y-2">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Settings</h2>
          <p className="text-muted-foreground">
            Manage your account settings and profile information.
          </p>
        </div>
      </div>

      <div className="grid gap-8 md:grid-cols-2">
        {/* Profile Settings */}
        <div className="space-y-8">
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <User className="text-primary h-5 w-5" />
                <CardTitle>Username</CardTitle>
              </div>
              <CardDescription>
                Change your display name on the platform.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Form {...usernameForm}>
                <form
                  onSubmit={usernameForm.handleSubmit(onUsernameSubmit)}
                  className="space-y-4"
                >
                  <FormField
                    control={usernameForm.control}
                    name="username"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Public Username</FormLabel>
                        <FormControl>
                          <Input placeholder="Enter new username" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <Button
                    type="submit"
                    disabled={updateUsername.isPending}
                    className="w-full gap-2"
                  >
                    {updateUsername.isPending ? (
                      "Saving..."
                    ) : (
                      <>
                        <Check className="h-4 w-4" /> Save Username
                      </>
                    )}
                  </Button>
                </form>
              </Form>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <Mail className="text-primary h-5 w-5" />
                <CardTitle>Email Address</CardTitle>
              </div>
              <CardDescription>
                Update the email associated with your account.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Form {...emailForm}>
                <form
                  onSubmit={emailForm.handleSubmit(onEmailSubmit)}
                  className="space-y-4"
                >
                  <FormField
                    control={emailForm.control}
                    name="email"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Email</FormLabel>
                        <FormControl>
                          <Input placeholder="Enter new email" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <Button
                    type="submit"
                    disabled={updateEmail.isPending}
                    className="w-full gap-2"
                  >
                    {updateEmail.isPending ? (
                      "Saving..."
                    ) : (
                      <>
                        <Check className="h-4 w-4" /> Save Email
                      </>
                    )}
                  </Button>
                </form>
              </Form>
            </CardContent>
          </Card>
        </div>

        {/* Password Settings */}
        <div>
          <Card className="h-full">
            <CardHeader>
              <div className="flex items-center gap-2">
                <Lock className="text-primary h-5 w-5" />
                <CardTitle>Password</CardTitle>
              </div>
              <CardDescription>
                Change your password to keep your account secure.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Form {...passwordForm}>
                <form
                  onSubmit={passwordForm.handleSubmit(onPasswordSubmit)}
                  className="space-y-4"
                >
                  <FormField
                    control={passwordForm.control}
                    name="currentPassword"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Current Password</FormLabel>
                        <FormControl>
                          <Input
                            type="password"
                            placeholder="••••••••"
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <div className="mt-4 border-t pt-2" />
                  <FormField
                    control={passwordForm.control}
                    name="newPassword"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>New Password</FormLabel>
                        <FormControl>
                          <Input
                            type="password"
                            placeholder="••••••••"
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <FormField
                    control={passwordForm.control}
                    name="confirmPassword"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Confirm New Password</FormLabel>
                        <FormControl>
                          <Input
                            type="password"
                            placeholder="••••••••"
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <Button
                    type="submit"
                    disabled={updatePassword.isPending}
                    className="mt-4 w-full gap-2"
                  >
                    {updatePassword.isPending ? (
                      "Updating..."
                    ) : (
                      <>
                        <Lock className="h-4 w-4" /> Update Password
                      </>
                    )}
                  </Button>
                </form>
              </Form>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}

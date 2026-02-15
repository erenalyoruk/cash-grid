export default function AuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="bg-muted/30 relative flex min-h-screen items-center justify-center overflow-hidden">
      {/* Decorative background elements */}
      <div className="bg-primary/5 absolute -top-24 -left-24 h-96 w-96 rounded-full blur-3xl" />
      <div className="bg-primary/10 absolute -right-24 -bottom-24 h-96 w-96 rounded-full blur-3xl" />

      <div className="animate-in fade-in zoom-in-95 relative z-10 w-full max-w-md px-6 duration-500">
        <div className="mb-8 flex flex-col items-center">
          <div className="bg-primary text-primary-foreground shadow-primary/20 mb-4 flex h-12 w-12 items-center justify-center rounded-2xl shadow-xl">
            <span className="text-3xl font-bold tracking-tighter">C</span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight">CashGrid</h1>
          <p className="text-muted-foreground text-sm">
            Financial Operations Reimagined
          </p>
        </div>
        {children}
      </div>
    </div>
  );
}

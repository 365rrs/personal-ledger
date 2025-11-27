@echo off
echo Starting Personal Ledger Development Environment...

echo Starting backend service...
start "Backend" cmd /k "cd personal-ledger-backend && mvn spring-boot:run"

timeout /t 5

echo Starting frontend development server...
start "Frontend" cmd /k "cd personal-ledger-frontend\cmb-bill-management && npm run dev"

echo Development environment started!
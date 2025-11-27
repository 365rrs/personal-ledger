@echo off
echo Building Personal Ledger Application...

echo Building backend...
cd personal-ledger-backend
call mvn clean package -DskipTests
cd ..

echo Building frontend...
cd personal-ledger-frontend\cmb-bill-management
call npm install
call npm run build
cd ..\..

echo Build completed successfully!
@echo off
echo Sauvegarde FreightOps sur GitHub...

cd /d "%~dp0\.."

echo.
echo [1/4] Verification de l'etat Git...
git status

echo.
echo [2/4] Ajout de tous les fichiers modifies...
git add .

echo.
echo [3/4] Creation du commit...
git commit -m "fix: Correction complete erreurs 404 - Tous modules FreightOps

- Ajout prefixe /api manquant dans tous les services frontend
- Correction AccountingReportController mapping
- Resolution erreur SQL H2 (colonne value -> goods_value)  
- Correction GlobalExceptionHandler (suppression builder inexistant)
- Ajout scripts memoire optimises pour backend
- Modules corriges: LTA, Manifests, Treasury, Accounting, Billing

Endpoints fonctionnels:
✅ /api/lta/stats
✅ /api/fret/manifests  
✅ /api/accounting/dashboard/stats
✅ /api/accounting/reports/balance-summary
✅ /api/invoices, /api/quotes, /api/payments, /api/taxes"

echo.
echo [4/4] Push vers GitHub...
git push origin main

echo.
echo ✅ Sauvegarde terminee sur GitHub !
pause

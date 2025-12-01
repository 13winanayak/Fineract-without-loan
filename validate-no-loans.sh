#!/bin/bash
echo "🔍 Validating no loan references remain..."

# Check for loan module references
echo "1. Checking settings.gradle..."
grep -i "loan" settings.gradle && echo "❌ Found loan in settings.gradle" || echo "✅ No loan in settings.gradle"

echo "2. Checking build.gradle files..."
find . -name "*.gradle" -exec grep -l "fineract-loan" {} \; && echo "❌ Found fineract-loan references" || echo "✅ No fineract-loan references"

echo "3. Checking Java files for Loan classes..."
find . -name "*.java" -exec grep -l "import.*Loan" {} \; | head -5
if [ $? -eq 0 ]; then
  echo "❌ Found Loan imports in Java files"
else
  echo "✅ No Loan imports found"
fi

echo "4. Checking if loan directories exist..."
if [ -d "fineract-loan" ]; then echo "❌ fineract-loan directory exists"; else echo "✅ fineract-loan removed"; fi
if [ -d "fineract-provider/src/main/java/org/apache/fineract/portfolio/loanaccount" ]; then echo "❌ loanaccount directory exists"; else echo "✅ loanaccount removed"; fi

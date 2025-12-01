# Fineract Without Loans

This is a customized version of Apache Fineract with ALL loan functionality removed.

## What's Removed
- ✅ `fineract-loan` module (complete removal)
- ✅ `fineract-investor` module (loan-dependent)
- ✅ `fineract-progressive-loan` module
- ✅ All loan packages from `fineract-core`
- ✅ All loan packages from `fineract-provider`
- ✅ All loan REST APIs and services
- ✅ All loan database entities
- ✅ All loan-related configurations

## What's Included
- Client management
- Savings accounts
- Group functionality
- Accounting system
- Charge management
- Organisation structure
- REST API framework

## Build Command
```bash
./gradlew build -x test
```

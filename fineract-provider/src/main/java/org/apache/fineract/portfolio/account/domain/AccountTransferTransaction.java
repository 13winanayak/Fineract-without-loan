/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;

@Entity
@Table(name = "m_account_transfer_transaction")
@Getter
public class AccountTransferTransaction extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "account_transfer_details_id", nullable = true)
    private AccountTransferDetails accountTransferDetails;

    @ManyToOne
    @JoinColumn(name = "from_savings_transaction_id", nullable = true)
    private SavingsAccountTransaction fromSavingsTransaction;

    @ManyToOne
    @JoinColumn(name = "to_savings_transaction_id", nullable = true)
    private SavingsAccountTransaction toSavingsTransaction;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed = false;

    @Column(name = "transaction_date")
    private LocalDate date;

    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "description", length = 100)
    private String description;

    public static AccountTransferTransaction savingsToSavingsTransfer(final AccountTransferDetails accountTransferDetails,
            final SavingsAccountTransaction withdrawal, final SavingsAccountTransaction deposit, final LocalDate transactionDate,
            final Money transactionAmount, final String description) {

        return new AccountTransferTransaction(accountTransferDetails, withdrawal, deposit, transactionDate, transactionAmount,
                description);
    }

    protected AccountTransferTransaction() {
        //
    }

    private AccountTransferTransaction(final AccountTransferDetails accountTransferDetails, final SavingsAccountTransaction withdrawal,
            final SavingsAccountTransaction deposit, final LocalDate transactionDate, final Money transactionAmount,
            final String description) {
        this.accountTransferDetails = accountTransferDetails;
        this.fromSavingsTransaction = withdrawal;
        this.toSavingsTransaction = deposit;
        this.date = transactionDate;
        this.currency = transactionAmount.getCurrency();
        this.amount = transactionAmount.getAmountDefaultedToNullIfZero();
        this.description = description;
    }

    public SavingsAccountTransaction getFromTransaction() {
        return this.fromSavingsTransaction;
    }


    public void reverse() {
        this.reversed = true;
    }

    public AccountTransferDetails accountTransferDetails() {
        return this.accountTransferDetails;
    }
    
    // Additional getters/setters if needed
    public AccountTransferDetails getAccountTransferDetails() {
        return accountTransferDetails;
    }
    
    public SavingsAccountTransaction getFromSavingsTransaction() {
        return fromSavingsTransaction;
    }
    
    public SavingsAccountTransaction getToSavingsTransaction() {
        return toSavingsTransaction;
    }
    
    public boolean isReversed() {
        return reversed;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public MonetaryCurrency getCurrency() {
        return currency;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public String getDescription() {
        return description;
    }
}
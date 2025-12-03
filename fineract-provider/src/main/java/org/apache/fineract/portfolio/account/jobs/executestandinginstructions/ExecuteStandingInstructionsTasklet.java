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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@RequiredArgsConstructor
public class ExecuteStandingInstructionsTasklet implements Tasklet {

    private final StandingInstructionReadPlatformService standingInstructionReadPlatformService;
    private final JdbcTemplate jdbcTemplate;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Collection<StandingInstructionData> instructionData = standingInstructionReadPlatformService
                .retrieveAll(StandingInstructionStatus.ACTIVE.getValue());
        List<Throwable> errors = new ArrayList<>();
        for (StandingInstructionData data : instructionData) {
            boolean isDueForTransfer = false;
            AccountTransferRecurrenceType recurrenceType = data.getRecurrenceType();
            LocalDate transactionDate = DateUtils.getBusinessLocalDate();
            BigDecimal transactionAmount = data.getAmount();
            
            // Check if this is a savings-to-savings transfer (only supported type after loan removal)
            if (!data.getFromAccountType().isSavingsAccount() || !data.getToAccountType().isSavingsAccount()) {
                log.warn("Skipping non-savings standing instruction ID: {}. Only savings-to-savings transfers are supported.", data.getId());
                continue;
            }

            if (recurrenceType.isPeriodicRecurrence()) {
                PeriodFrequencyType frequencyType = data.getRecurrenceFrequency();
                LocalDate startDate = data.getValidFrom();
                if (frequencyType.isMonthly()) {
                    startDate = startDate.withDayOfMonth(data.getRecurrenceOnDay());
                    if (DateUtils.isBefore(startDate, data.getValidFrom())) {
                        startDate = startDate.plusMonths(1);
                    }
                } else if (frequencyType.isYearly()) {
                    startDate = startDate.withDayOfMonth(data.getRecurrenceOnDay()).withMonth(data.getRecurrenceOnMonth());
                    if (DateUtils.isBefore(startDate, data.getValidFrom())) {
                        startDate = startDate.plusYears(1);
                    }
                }
                isDueForTransfer = isDateFallsInSchedule(frequencyType, data.getRecurrenceInterval(), startDate, transactionDate);
            }
            
            // Note: Dues-based transfers (for loan repayments) are no longer supported

            if (isDueForTransfer && transactionAmount != null && transactionAmount.compareTo(BigDecimal.ZERO) > 0) {
                final SavingsAccount fromSavingsAccount = null;
                final boolean isRegularTransaction = true;
                final boolean isExceptionForBalanceCheck = false;
                AccountTransferDTO accountTransferDTO = new AccountTransferDTO(transactionDate, transactionAmount,
                        data.getFromAccountType(), data.getToAccountType(), data.getFromAccount().getId(), data.getToAccount().getId(),
                        data.getName() + " Standing instruction transfer ", null, null, null, null, null, null,
                        data.getTransferType().getValue(), null, null, ExternalId.empty(), null, fromSavingsAccount,
                        isRegularTransaction, isExceptionForBalanceCheck);
                final boolean transferCompleted = transferAmount(errors, accountTransferDTO, data.getId());

                if (transferCompleted) {
                    final String updateQuery = "UPDATE m_account_transfer_standing_instructions SET last_run_date = ? where id = ?";
                    jdbcTemplate.update(updateQuery, transactionDate, data.getId());
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new JobExecutionException(errors);
        }
        return RepeatStatus.FINISHED;
    }

    private boolean isDateFallsInSchedule(PeriodFrequencyType frequencyType, Integer interval, 
            LocalDate startDate, LocalDate checkDate) {
        
        if (startDate == null || checkDate == null || interval == null || interval <= 0) {
            return false;
        }
        
        if (DateUtils.isBefore(checkDate, startDate)) {
            return false;
        }
        
        long daysBetween = ChronoUnit.DAYS.between(startDate, checkDate);
        
        switch (frequencyType) {
            case DAYS:
                return daysBetween % interval == 0;
            case WEEKS:
                return daysBetween % (7 * interval) == 0;
            case MONTHS:
                return isMonthlyDateMatch(startDate, checkDate, interval);
            case YEARS:
                return isYearlyDateMatch(startDate, checkDate, interval);
            default:
                return false;
        }
    }
    
    private boolean isMonthlyDateMatch(LocalDate startDate, LocalDate checkDate, Integer interval) {
        if (checkDate.getDayOfMonth() != startDate.getDayOfMonth()) {
            return false;
        }
        
        int monthsBetween = (checkDate.getYear() - startDate.getYear()) * 12 
                          + (checkDate.getMonthValue() - startDate.getMonthValue());
        
        return monthsBetween >= 0 && monthsBetween % interval == 0;
    }
    
    private boolean isYearlyDateMatch(LocalDate startDate, LocalDate checkDate, Integer interval) {
        if (checkDate.getDayOfMonth() != startDate.getDayOfMonth() 
                || checkDate.getMonthValue() != startDate.getMonthValue()) {
            return false;
        }
        
        int yearsBetween = checkDate.getYear() - startDate.getYear();
        return yearsBetween >= 0 && yearsBetween % interval == 0;
    }

    private boolean transferAmount(final List<Throwable> errors, final AccountTransferDTO accountTransferDTO, final Long instructionId) {
        boolean transferCompleted = true;
        StringBuilder errorLog = new StringBuilder();
        StringBuilder updateQuery = new StringBuilder(
                "INSERT INTO m_account_transfer_standing_instructions_history (standing_instruction_id, " + sqlGenerator.escape("status")
                        + ", amount,execution_time, error_log) VALUES (");
        try {
            accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        } catch (final PlatformApiDataValidationException e) {
            errors.add(new Exception("Validation exception while transferring funds for standing Instruction id" + instructionId + " from "
                    + accountTransferDTO.getFromAccountId() + " to " + accountTransferDTO.getToAccountId(), e));
            errorLog.append("Validation exception while transferring funds ").append(e.getDefaultUserMessage());
        } catch (final InsufficientAccountBalanceException e) {
            errors.add(new Exception("InsufficientAccountBalance Exception while transferring funds for standing Instruction id"
                    + instructionId + " from " + accountTransferDTO.getFromAccountId() + " to " + accountTransferDTO.getToAccountId(), e));
            errorLog.append("InsufficientAccountBalance Exception ");
        } catch (final AbstractPlatformServiceUnavailableException e) {
            errors.add(new Exception("Platform exception while transferring funds for standing Instruction id" + instructionId + " from "
                    + accountTransferDTO.getFromAccountId() + " to " + accountTransferDTO.getToAccountId(), e));
            errorLog.append("Platform exception while transferring funds ").append(e.getDefaultUserMessage());
        } catch (Exception e) {
            errors.add(new Exception("Unhandled System Exception while transferring funds for standing Instruction id" + instructionId
                    + " from " + accountTransferDTO.getFromAccountId() + " to " + accountTransferDTO.getToAccountId(), e));
            errorLog.append("Exception while transferring funds ").append(e.getMessage());
        }
        updateQuery.append(instructionId).append(",");
        if (errorLog.length() > 0) {
            transferCompleted = false;
            updateQuery.append("'failed'").append(",");
        } else {
            updateQuery.append("'success'").append(",");
        }
        updateQuery.append(accountTransferDTO.getTransactionAmount().doubleValue());
        updateQuery.append(", now(),");
        updateQuery.append("'").append(errorLog).append("')");
        jdbcTemplate.update(updateQuery.toString());
        return transferCompleted;
    }
}
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
package org.apache.fineract.infrastructure.configuration.domain;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.infrastructure.cache.domain.CacheType;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ConfigurationDomainServiceStub implements ConfigurationDomainService {

    @Override
    public boolean isMakerCheckerEnabledForTask(String taskPermissionCode) { return false; }

    @Override
    public List<String> getAllowedLoanStatusesForExternalAssetTransfer() { return Collections.emptyList(); }

    @Override
    public List<String> getAllowedLoanStatusesOfDelayedSettlementForExternalAssetTransfer() { return Collections.emptyList(); }

    @Override
    public boolean isSameMakerCheckerEnabled() { return false; }

    @Override
    public boolean isAmazonS3Enabled() { return false; }

    @Override
    public boolean isRescheduleFutureRepaymentsEnabled() { return false; }

    @Override
    public boolean isRescheduleRepaymentsOnHolidaysEnabled() { return false; }

    @Override
    public boolean allowTransactionsOnHolidayEnabled() { return false; }

    @Override
    public boolean allowTransactionsOnNonWorkingDayEnabled() { return false; }

    @Override
    public boolean isConstraintApproachEnabledForDatatables() { return false; }

    @Override
    public boolean isEhcacheEnabled() { return false; }

    @Override
    public void updateCache(CacheType cacheType) {}

    @Override
    public Long retrievePenaltyWaitPeriod() { return 0L; }

    @Override
    public boolean isPasswordForcedResetEnable() { return false; }

    @Override
    public Long retrievePasswordLiveTime() { return 0L; }

    @Override
    public Long retrieveGraceOnPenaltyPostingPeriod() { return 0L; }

    @Override
    public Long retrieveOpeningBalancesContraAccount() { return 0L; }

    @Override
    public boolean isSavingsInterestPostingAtCurrentPeriodEnd() { return false; }

    @Override
    public Integer retrieveFinancialYearBeginningMonth() { return 1; }

    @Override
    public Integer retrieveMinAllowedClientsInGroup() { return 0; }

    @Override
    public Integer retrieveMaxAllowedClientsInGroup() { return 0; }

    @Override
    public boolean isMeetingMandatoryForJLGLoans() { return false; }

    @Override
    public int getRoundingMode() { return 6; }

    @Override
    public boolean isBackdatePenaltiesEnabled() { return false; }

    @Override
    public boolean isOrganisationstartDateEnabled() { return false; }

    @Override
    public LocalDate retrieveOrganisationStartDate() { return LocalDate.now(); }

    @Override
    public boolean isPaymentTypeApplicableForDisbursementCharge() { return false; }

    @Override
    public boolean isInterestChargedFromDateSameAsDisbursementDate() { return false; }

    @Override
    public boolean isSkippingMeetingOnFirstDayOfMonthEnabled() { return false; }

    @Override
    public boolean isInterestToBeRecoveredFirstWhenGreaterThanEMI() { return false; }

    @Override
    public boolean isPrincipalCompoundingDisabledForOverdueLoans() { return false; }

    @Override
    public Long retreivePeriodInNumberOfDaysForSkipMeetingDate() { return 0L; }

    @Override
    public boolean isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled() { return false; }

    @Override
    public boolean isDailyTPTLimitEnabled() { return false; }

    @Override
    public Long getDailyTPTLimit() { return 0L; }

    @Override
    public void removeGlobalConfigurationPropertyDataFromCache(String propertyName) {}

    @Override
    public boolean isSMSOTPDeliveryEnabled() { return false; }

    @Override
    public boolean isEmailOTPDeliveryEnabled() { return false; }

    @Override
    public Integer retrieveOTPCharacterLength() { return 0; }

    @Override
    public Integer retrieveOTPLiveTime() { return 0; }

    @Override
    public boolean isSubRatesEnabled() { return false; }

    @Override
    public boolean isFirstRepaymentDateAfterRescheduleAllowedOnHoliday() { return false; }

    @Override
    public String getAccountMappingForPaymentType() { return ""; }

    @Override
    public String getAccountMappingForCharge() { return ""; }

    @Override
    public boolean isNextDayFixedDepositInterestTransferEnabledForPeriodEnd() { return false; }

    @Override
    public boolean retrievePivotDateConfig() { return false; }

    @Override
    public boolean isRelaxingDaysConfigForPivotDateEnabled() { return false; }

    @Override
    public Long retrieveRelaxingDaysConfigForPivotDate() { return 0L; }

    @Override
    public boolean isBusinessDateEnabled() { return false; }

    @Override
    public boolean isCOBDateAdjustmentEnabled() { return false; }

    @Override
    public boolean isReversalTransactionAllowed() { return false; }

    @Override
    public Long retrieveExternalEventsPurgeDaysCriteria() { return 0L; }

    @Override
    public Long retrieveProcessedCommandsPurgeDaysCriteria() { return 0L; }

    @Override
    public Long retrieveRepaymentDueDays() { return 0L; }

    @Override
    public Long retrieveRepaymentOverdueDays() { return 0L; }

    @Override
    public boolean isExternalIdAutoGenerationEnabled() { return false; }

    @Override
    public boolean isAddressEnabled() { return false; }

    @Override
    public boolean isCOBBulkEventEnabled() { return false; }

    @Override
    public Long retrieveExternalEventBatchSize() { return 0L; }

    @Override
    public String retrieveReportExportS3FolderName() { return ""; }

    @Override
    public String getAccrualDateConfigForCharge() { return ""; }

    @Override
    public String getNextPaymentDateConfigForLoan() { return ""; }

    @Override
    public boolean isImmediateChargeAccrualPostMaturityEnabled() { return false; }

    @Override
    public String getAssetOwnerTransferOustandingInterestStrategy() { return ""; }
}


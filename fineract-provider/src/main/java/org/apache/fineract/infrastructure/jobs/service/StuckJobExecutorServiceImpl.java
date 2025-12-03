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
package org.apache.fineract.infrastructure.jobs.service;

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.jobs.domain.JobExecutionRepository;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class StuckJobExecutorServiceImpl implements StuckJobExecutorService {

    private final JobExecutionRepository jobExecutionRepository;
    private final TransactionTemplate transactionTemplate;
    private final JobOperator jobOperator;

    @Override
    public void resumeStuckJob(String jobName) {
        List<Long> stuckJobIds = getStuckJobIds(jobName);
        if (isPartitionedJob(jobName) && areThereStuckJobs(jobName)) {
            restartPartitionedJobs(jobName, stuckJobIds);
        } else {
            restartTaskletJobs(stuckJobIds);
        }
    }

    private void restartTaskletJobs(List<Long> stuckJobIds) {
        stuckJobIds.forEach(this::handleStuckTaskletJob);
    }

    private void handleStuckTaskletJob(Long stuckJobId) {
        try {
            jobOperator.restart(stuckJobId);
        } catch (Exception e) {
            throw new RuntimeException("Exception while handling a stuck job", e);
        }
    }

    private void restartPartitionedJobs(String jobName, List<Long> stuckJobIds) {
        // Partitioned jobs are not currently supported after loan functionality removal
        // This method is kept for future extensibility if partitioned jobs are added for other modules
        log.warn("Partitioned job restart requested for job: {}, but partitioned jobs are not currently supported", jobName);
        throw new UnsupportedOperationException("Partitioned jobs are not supported. Job: " + jobName);
    }

    private boolean isPartitionedJob(String jobName) {
        // Since loan COB is removed, no partitioned jobs currently exist
        // Return false for all jobs - they will be treated as tasklet jobs
        // This can be extended in the future if partitioned jobs are added for other modules
        return false;
    }

    private String getPartitionerStepName(String name) {
        // Since partitioned jobs are not supported, throw exception
        throw new UnsupportedOperationException("Partitioned jobs are not supported. Cannot get partitioner step for: " + name);
    }

    private boolean areThereStuckJobs(String jobName) {
        Long stuckJobCount = jobExecutionRepository.getStuckJobCountByJobName(jobName);
        return stuckJobCount != 0L;
    }

    private List<Long> getStuckJobIds(String jobName) {
        return jobExecutionRepository.getStuckJobIdsByJobName(jobName);
    }

    private void handleStuckPartitionedJob(Long stuckJobId, String partitionerStepName) {
        // This method is no longer used since partitioned jobs are not supported
        // Kept for future extensibility
        throw new UnsupportedOperationException("Partitioned job handling is not supported. Job ID: " + stuckJobId);
    }

    private void waitUntilAllPartitionsFinished(Long stuckJobId, String partitionerStepName) throws InterruptedException {
        // This method is no longer used since partitioned jobs are not supported
        // Kept for future extensibility
        throw new UnsupportedOperationException("Partitioned job waiting is not supported. Job ID: " + stuckJobId);
    }

    private boolean areAllPartitionsCompleted(Long stuckJobId, String partitionerStepName) {
        // This method is no longer used since partitioned jobs are not supported
        // Kept for future extensibility
        throw new UnsupportedOperationException("Partitioned job completion check is not supported. Job ID: " + stuckJobId);
    }
}
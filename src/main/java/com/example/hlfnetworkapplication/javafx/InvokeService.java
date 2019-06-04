/* SPDX-License-Identifier: Apache-2.0 */
package com.example.hlfnetworkapplication.javafx;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ProposalResponse;
import com.example.hlfnetworkapplication.fabric.CommunicationHandler;

/**
 * Service class to invoke chaincode on the blockchain
 *
 * @author kehm
 */
public class InvokeService extends Service {

    private static final Logger LOG = Logger.getLogger(InvokeService.class);

    private final String chaincode; // name of chaincode
    private final String function; // name of chaincode function
    private final String[] args; // arguments to supply chaincode

    public InvokeService(String chaincode, String function, String[] args) {
        this.chaincode = chaincode;
        this.function = function;
        this.args = args;
    }

    @Override
    protected Task createTask() {
        return new Task<Collection<ProposalResponse>>() {
            @Override
            protected Collection<ProposalResponse> call() throws Exception {
                Collection<ProposalResponse> response = CommunicationHandler.transactionProposal(CommunicationHandler.getChannel(), chaincode, function, args);
                CompletableFuture cf = CommunicationHandler.getChannel().sendTransaction(response);
                if (cf == null) {
                    LOG.error("Inconsistent RW sets from endorsing peers. Transaction not orderered");
                    this.failed();
                }
                BlockEvent.TransactionEvent blockEvent = (BlockEvent.TransactionEvent) cf.get(60, TimeUnit.SECONDS);
                if (blockEvent.isValid()) {
                    LOG.info("Transaction is successful");
                } else {
                    LOG.error("Transaction failed");
                    this.failed();
                }
                return response;
            }
        };
    }
}

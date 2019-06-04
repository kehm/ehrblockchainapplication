/* SPDX-License-Identifier: Apache-2.0 */
package com.example.hlfnetworkapplication.util;

/**
 * Class to set constant strings
 *
 * @author kehm
 */
public final class Strings {

    public static final String CHANNEL_NAME = "providerschannel";
    public static final String RRC_CHAINCODE_NAME = "RecordRelationshipContract";
    public static final String RRC_CHAINCODE_FUNCTION_CREATE = "create";
    public static final String RRC_CHAINCODE_FUNCTION_LOG = "log";
    public static final String RRC_CHAINCODE_FUNCTION_UPDATE = "update";
    public static final String RRC_CHAINCODE_FUNCTION_QUERY = "query";
    public static final String INCENTIVE_CHAINCODE_NAME = "IncentiveMechanism";
    public static final String INCENTIVE_CHAINCODE_FUNCTION_ENDORSER = "selectEndorser";

    public static final String ENROLL_DIALOG_TITLE = "Enroll as User";
    public static final String ENROLL_DIALOG_HEADER = "Enter enrollment details";

    public static final String FORMAT_NATIONAL_ID_CHARACTER = "[0-9]+";
    public static final String FORMAT_NATIONAL_ID_LENGTH = "11";
    
    public static final String PROPOSAL_TIMEOUT = "120000";

    public static final String EVENT_READ = "READ";
    public static final String EVENT_WRITE = "WRITE";

    public static final String TRUSTSTORE_FILENAME = "certs.jks";
    public static final String TRUSTSTORE_PASS = "changeit";
    public static final String TRUSTSTORE_TYPE = "JKS";

    public static final String PEERS_FILENAME = "peers.txt";
    public static final String ORDERERS_FILENAME = "orderers.txt";
    public static final String AFFILIATIONS_FILENAME = "affiliations.txt";

    public static final String STATUS_TRANSACTION_ASSEMBLE = "Assembling transaction...";
    public static final String STATUS_TRANSACTION_SUCCESS = "Transaction is successful";
    public static final String STATUS_TRANSACTION_FAILED = "Transaction failed";
}

/* SPDX-License-Identifier: Apache-2.0 */
package com.example.hlfnetworkapplication.fabric;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Set;
import java.util.EnumSet;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.BlockchainInfo;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.TransactionRequest.Type;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.SDKUtils;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.RegistrationException;
import org.hyperledger.fabric.sdk.Channel.PeerOptions;
import org.hyperledger.fabric.sdk.Peer.PeerRole;
import org.hyperledger.fabric.sdk.ServiceDiscovery;
import org.hyperledger.fabric.sdk.exception.ServiceDiscoveryException;
import com.example.hlfnetworkapplication.util.Strings;

/**
 * Class to handle communication with the Fabric blockchain
 *
 * @author kehm
 */
public class CommunicationHandler {

    private static final Logger LOG = Logger.getLogger(CommunicationHandler.class);

    private static HFClient client; // Fabric client object for invoking operations on the channel
    private static HFCAClient caClient; // Fabric CA client for enrollment operations
    private static Channel channel; // Channel to invoke operations on

    Type CHAINCODE_TYPE = Type.JAVA;

    /**
     * Constructor for CommunicationHandler
     */
    public CommunicationHandler() {
    }

    /**
     * Prepare Fabric client object
     *
     * @throws IllegalAccessException if error from crypto factory
     * @throws InstantiationException if error from crypto factory
     * @throws ClassNotFoundException if error from crypto factory
     * @throws InvocationTargetException if error from crypto factory
     * @throws NoSuchMethodException if error from crypto factory
     * @throws CryptoException if Fabric client cannot use the selected crypto
     * suite
     * @throws InvalidArgumentException if Fabric client cannot use the selected
     * crypto suite
     */
    public static void prepareClient() throws IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, CryptoException, InvalidArgumentException {
        System.setProperty("javax.net.ssl.trustStore", CommunicationHandler.class.getClassLoader().getResource(Strings.TRUSTSTORE_FILENAME).getPath());
        System.setProperty("javax.net.ssl.trustStorePassword", Strings.TRUSTSTORE_PASS);
        System.setProperty("javax.net.ssl.trustStoreType", Strings.TRUSTSTORE_TYPE);
        client = HFClient.createNewInstance();
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
    }

    /**
     * Initialize channel object
     *
     * @return Channel length
     * @throws InvalidArgumentException if any argument is invalid
     * @throws TransactionException if channel initialization fails
     * @throws ProposalException if query for blockchain info fails
     * @throws FileNotFoundException if file path is invalid
     * @throws IOException if file stream cannot be opened
     */
    public static long initChannel() throws InvalidArgumentException, TransactionException, ProposalException, FileNotFoundException, IOException {
        channel = client.newChannel(Strings.CHANNEL_NAME);
        Peer discoveryPeer = client.newPeer("peer0.hospital1.example.com", "grpcs://172.18.0.40:7051");
        channel.addPeer(discoveryPeer, PeerOptions.createPeerOptions().setPeerRoles(EnumSet.of(PeerRole.SERVICE_DISCOVERY, PeerRole.LEDGER_QUERY, PeerRole.EVENT_SOURCE, PeerRole.CHAINCODE_QUERY)));
        channel.initialize();
        BlockchainInfo channelInfo = channel.queryBlockchainInfo();
        LOG.info("Channel '" + channel.getName() + "'. Length: " + channelInfo.getHeight());
        return channelInfo.getHeight();
    }

    /**
     * Enroll a user and set user context
     *
     * @param userName User name
     * @param caName Name of the CA
     * @param caUrl URL of the CA
     * @param registrarName Registrar name
     * @param registrarPass Registrar password
     * @param affiliation Associated affiliation
     * @param msp Associated msp
     * @throws MalformedURLException if URL is invalid
     * @throws InvalidArgumentException if user object is malformed
     * @throws EnrollmentException if the ca client cannot enroll
     * @throws Exception if an unspecified error occurs
     */
    public static void setContext(String userName, String caName, String caUrl, String registrarName, String registrarPass, String affiliation, String msp) throws MalformedURLException, InvalidArgumentException, EnrollmentException, Exception {
        caClient = HFCAClient.createNewInstance(caName, caUrl, null);
        caClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        ClientUser registrar = getClientUser(registrarName, null, registrarPass, affiliation, msp); // get user (for testing purposes, do not store user in production)
        ClientUser clientUser = getClientUser(userName, registrar, null, affiliation, msp); // get user (for testing purposes, do not store user in production)
        client.setUserContext(clientUser);
    }

    /**
     * Send transaction proposal request to endorser and evaluate the responses
     *
     * @param channel Channel to invoke the chaincode on
     * @param chaincodeName Name of chaincode
     * @param function Chaincode function to invoke
     * @param args Proposal arguments
     * @return Proposal responses
     * @throws InvalidArgumentException if arguments to the proposal are invalid
     * @throws ProposalException if query or transaction proposal fails
     */
    public static Collection<ProposalResponse> transactionProposal(Channel channel, String chaincodeName, String function, String[] args) throws InvalidArgumentException, ProposalException, ServiceDiscoveryException, IOException, ClassNotFoundException {
        TransactionProposalRequest request = client.newTransactionProposalRequest();
        ChaincodeID chaincode = ChaincodeID.newBuilder().setName(chaincodeName).build();
        request.setChaincodeID(chaincode);
        request.setFcn(function); // chaincode function to be called
        request.setArgs(args); // chaincode arguments to be passed
        request.setProposalWaitTime(Integer.valueOf(Strings.PROPOSAL_TIMEOUT));
        Collection<ProposalResponse> response;
        String endorser = CommunicationHandler.queryProposal(CommunicationHandler.getChannel(), Strings.INCENTIVE_CHAINCODE_NAME, Strings.INCENTIVE_CHAINCODE_FUNCTION_ENDORSER, new String[]{});
        LOG.info("Selected organization '" + endorser + "' for endorsing the transaction");
        if (endorser != null) {
            // send proposal to selected endorser organization
            EndorserSelector.setEndorsers(new String[]{endorser, client.getUserContext().getMspId().split("MSP")[0]});
            response = channel.sendTransactionProposalToEndorsers(request, Channel.DiscoveryOptions.createDiscoveryOptions().setEndorsementSelector(EndorserSelector.ENDORSEMENT_SELECTION_SIGNIFICANCE).setForceDiscovery(true));
        } else {
            // send proposal to random endorser organization
            response = channel.sendTransactionProposalToEndorsers(request, Channel.DiscoveryOptions.createDiscoveryOptions().setEndorsementSelector(ServiceDiscovery.EndorsementSelector.ENDORSEMENT_SELECTION_RANDOM).setForceDiscovery(true));
        }
        // check if any responses were received
        if (response.isEmpty()) {
            LOG.error("No responses received");
            return null;
        }
        // check if RW sets are consistent
        Collection<Set<ProposalResponse>> consistencySets = SDKUtils.getProposalConsistencySets(response);
        if (consistencySets.size() != 1) {
            LOG.error("Inconsistent RW sets. Transaction for chaincode '" + chaincodeName + "' not submitted.");
            return null;
        } else {
            LOG.info("Transaction for chaincode '" + chaincodeName + "' submitted");
            return response;
        }
    }

    /**
     * Query state database
     *
     * @param channel Channel to invoke
     * @param chaincodeName Name of chaincode
     * @param functionName Name of chaincode function
     * @param args Query arguments
     * @return Array of query results
     * @throws InvalidArgumentException if arguments to the proposal are invalid
     * @throws ProposalException if query proposal fails
     */
    public static String queryProposal(Channel channel, String chaincodeName, String functionName, String[] args) throws InvalidArgumentException, ProposalException {
        QueryByChaincodeRequest qpr = client.newQueryProposalRequest();
        ChaincodeID chaincode = ChaincodeID.newBuilder().setName(chaincodeName).build();
        qpr.setChaincodeID(chaincode);
        qpr.setFcn(functionName); // chaincode function to be called
        qpr.setArgs(args); // chaincode arguments to be passed
        qpr.setProposalWaitTime(Integer.valueOf(Strings.PROPOSAL_TIMEOUT));
        Collection<ProposalResponse> response = channel.queryByChaincode(qpr); // issue query proposal
        for (ProposalResponse pr : response) {
            try {
                return new String(pr.getChaincodeActionResponsePayload()); // return response payload
            } catch (InvalidArgumentException e) {
                LOG.info("Query returned no payload");
                return null;
            }
        }
        return null;
    }

    /**
     * Get ClientUser object from file or create new
     *
     * @param name Client name
     * @param registrar Registrar for user. Null if enrolling an admin.
     * @param pass Registrar password. Null if enrolling a user.
     * @param affiliation Client affiliation
     * @param msp Client MSP
     * @return User object
     * @throws EnrollmentException if ca client cannot enroll the registrar
     * @throws InvalidArgumentException if arguments to ca client is invalid
     * @throws RegistrationException if ca client cannot enroll the user
     * @throws FileNotFoundException if object cannot be written to file
     * @throws Exception if failed creating RegistrationRequest object
     */
    private static ClientUser getClientUser(String name, ClientUser registrar, String pass, String affiliation, String msp) throws EnrollmentException, InvalidArgumentException, RegistrationException, FileNotFoundException, Exception {
        ClientUser clientUser;
        // try to read user from file
        try {
            clientUser = (ClientUser) readObjectFromFile("users" + File.separator + affiliation + File.separator + name);
            LOG.info("Read user '" + clientUser.getName() + "' with affiliation '" + clientUser.getAffiliation() + "' from file");
            return clientUser;
        } catch (IOException | ClassNotFoundException ex) {
            LOG.info("Could not find user context file. Enrolling user with CA", ex);
        }
        // entroll new user with CA and save to file
        if (registrar == null) {
            // enroll admin
            Enrollment enrollment = caClient.enroll(name, pass);
            clientUser = new ClientUser(name, affiliation, enrollment, msp);
        } else {
            // enroll user
            RegistrationRequest request = new RegistrationRequest(name, affiliation);
            Enrollment userEnrollment = caClient.enroll(name, caClient.register(request, registrar));
            clientUser = new ClientUser(name, affiliation, userEnrollment, msp);
        }
        // write object to file
        File directory = new File("users" + File.separator + affiliation);
        if (!directory.exists()) {
            File subDirectory = new File("users");
            if (!subDirectory.exists()) {
                subDirectory.mkdir();
            }
            directory.mkdir();
        }
        writeObjectToFile(clientUser, "users" + File.separator + affiliation + File.separator + name); // write to disk
        String message = "Enrolled user '" + clientUser.getName() + "' with affiliation '" + clientUser.getAffiliation() + "'";
        LOG.info(message);
        LOG.info("Wrote user '" + name + "' to disk");
        return clientUser;
    }

    /**
     * Get channel object
     *
     * @return Channel object
     */
    public static synchronized Channel getChannel() {
        return CommunicationHandler.channel;
    }

    /**
     * Write Java Object to file
     *
     * @param object Object to write
     * @param path Write to path
     * @throws FileNotFoundException if file path is invalid
     * @throws IOException if file stream cannot be opened
     */
    private static void writeObjectToFile(Object object, String path) throws FileNotFoundException, IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(object);
        }
    }

    /**
     * Read Java Object from file
     *
     * @param path Read from path
     * @return Java Object
     * @throws ClassNotFoundException if class Object does not exist
     * @throws FileNotFoundException if file path is invalid
     * @throws IOException if file stream cannot be opened
     */
    private static Object readObjectFromFile(String path) throws ClassNotFoundException, FileNotFoundException, IOException {
        FileInputStream fileInputStream = new FileInputStream(path);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            return objectInputStream.readObject();
        }
    }
}

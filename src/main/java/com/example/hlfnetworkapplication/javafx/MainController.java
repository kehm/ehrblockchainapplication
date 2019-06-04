/* SPDX-License-Identifier: Apache-2.0 */
package com.example.hlfnetworkapplication.javafx;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import com.example.hlfnetworkapplication.fabric.CommunicationHandler;
import com.example.hlfnetworkapplication.util.JSONParser;
import com.example.hlfnetworkapplication.util.Strings;

public class MainController implements Initializable {

    private static final Logger LOG = Logger.getLogger(MainController.class);

    @FXML
    private Label userLabel;
    @FXML
    private TextArea outputArea;
    @FXML
    private TextField tab1Arg1;
    @FXML
    private TextField tab1Arg2;
    @FXML
    private TextField tab1Arg3;
    @FXML
    private TextField tab2Arg1;
    @FXML
    private TextField tab2Arg2;
    @FXML
    private TextArea tab1Area;
    @FXML
    private TextArea tab2Area;
    @FXML
    private TextArea tab3Area;
    @FXML
    private TextArea tab4Area;
    @FXML
    private TextField tab4Arg1;
    @FXML
    private TextField tab4Arg2;
    @FXML
    private TextField tab3Arg1;
    @FXML
    private TextField tab3Arg2;
    @FXML
    private HBox tab1Progress;
    @FXML
    private HBox tab2Progress;
    @FXML
    private HBox tab3Progress;
    @FXML
    private HBox tab4Progress;
    @FXML
    private Button registerBtn;
    @FXML
    private Button readBtn;
    @FXML
    private Button writeBtn;
    @FXML
    private Button editBtn;
    @FXML
    private RadioButton grantRadBtn;
    @FXML
    private RadioButton revokeRadBtn;
    @FXML
    private RadioButton clientRadBtn;
    @FXML
    private RadioButton mspRadBtn;
    @FXML
    private RadioButton readRadBtn;
    @FXML
    private RadioButton writeRadBtn;
    @FXML
    private CheckBox overrideBox;

    private ToggleGroup permissionsGroup;
    private ToggleGroup entityGroup;
    private ToggleGroup eventGroup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userLabel.setText("You are not enrolled!");
        initTab1();
        initTab2();
        initTab3();
        initTab4();
        try {
            CommunicationHandler.prepareClient();
            openEnrollDialog();
        } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException | CryptoException | InvalidArgumentException ex) {
            LOG.error(ex);
            output("ERROR: Could not initialize Fabric client");
        }
    }

    @FXML
    private void enroll(ActionEvent event) {
        try {
            openEnrollDialog();
        } catch (IOException ex) {
            LOG.error(ex);
        }
    }

    @FXML
    private void register(ActionEvent event) {
        if (tab1Arg1.getText().matches(Strings.FORMAT_NATIONAL_ID_CHARACTER) && tab1Arg1.getText().length() == Integer.valueOf(Strings.FORMAT_NATIONAL_ID_LENGTH)) {
            registerBtn.setDisable(true);
            tab1Area.clear();
            tab1Progress.setVisible(true);
            output(Strings.STATUS_TRANSACTION_ASSEMBLE);
            String ref = UUID.randomUUID().toString();
            final InvokeService service = new InvokeService(Strings.RRC_CHAINCODE_NAME, Strings.RRC_CHAINCODE_FUNCTION_CREATE, new String[]{ref, tab1Arg1.getText(), tab1Arg3.getText()});
            service.setOnSucceeded(e -> {
                // if service succeeds, hide progress indicator and indicate transaction success
                tab1Progress.setVisible(false);
                output(Strings.STATUS_TRANSACTION_SUCCESS);
                tab1Area.appendText("Registered new record with reference: \n" + ref);
                registerBtn.setDisable(false);
            });
            service.setOnFailed(e -> {
                // if service failes, hide progress indicator and indicate transaction fail
                tab1Progress.setVisible(false);
                output(Strings.STATUS_TRANSACTION_FAILED);
                registerBtn.setDisable(false);
            });
            service.restart();
        } else {
            output("ID must be " + Strings.FORMAT_NATIONAL_ID_CHARACTER + " only and of length " + Strings.FORMAT_NATIONAL_ID_LENGTH);
        }
    }

    @FXML
    private void edit(ActionEvent event) {
        editBtn.setDisable(true);
        tab2Area.clear();
        tab2Progress.setVisible(true);
        output(Strings.STATUS_TRANSACTION_ASSEMBLE);
        final InvokeService service = new InvokeService(Strings.RRC_CHAINCODE_NAME, Strings.RRC_CHAINCODE_FUNCTION_UPDATE, new String[]{tab2Arg1.getText(), tab2Arg2.getText(), permissionsGroup.getSelectedToggle().getUserData().toString(), entityGroup.getSelectedToggle().getUserData().toString(), eventGroup.getSelectedToggle().getUserData().toString()});
        service.setOnSucceeded(e -> {
            // if service succeeds, hide progress indicator and indicate transaction success
            tab2Progress.setVisible(false);
            output(Strings.STATUS_TRANSACTION_SUCCESS);
            tab2Area.appendText("ACL is updated");
            editBtn.setDisable(false);
        });
        service.setOnFailed(e -> {
            // if service failes, hide progress indicator and indicate transaction fail
            tab2Progress.setVisible(false);
            output(Strings.STATUS_TRANSACTION_FAILED);
            editBtn.setDisable(false);
        });
        service.restart();
    }

    @FXML
    private void read(ActionEvent event) {
        readBtn.setDisable(true);
        tab3Area.clear();
        tab3Progress.setVisible(true);
        output(Strings.STATUS_TRANSACTION_ASSEMBLE);
        String override = "0";
        if (overrideBox.isSelected()) {
            override = "1"; // set override flag true
        }
        final InvokeService service = new InvokeService(Strings.RRC_CHAINCODE_NAME, Strings.RRC_CHAINCODE_FUNCTION_QUERY, new String[]{tab3Arg1.getText(), override}
        );
        service.setOnSucceeded(e -> {
            // if service succeeds, hide progress indicator and indicate query success
            tab3Progress.setVisible(false);
            Collection<ProposalResponse> response = (Collection<ProposalResponse>) service.getValue();
            try {
                tab3Area.appendText("Query returned: \n" + JSONParser.getPrettyPrint(new String[]{new String(response.iterator().next().getChaincodeActionResponsePayload())}));
                output("Query is successful");
            } catch (InvalidArgumentException ex) {
                LOG.info(ex);
                output("Query returned no payload");
            }
            readBtn.setDisable(false);
        });
        service.setOnFailed(e -> {
            // if service failes, hide progress indicator and indicate transaction fail
            tab3Progress.setVisible(false);
            output(Strings.STATUS_TRANSACTION_FAILED);
            readBtn.setDisable(false);
        });
        service.restart();
    }

    @FXML
    private void write(ActionEvent event) {
        writeBtn.setDisable(true);
        tab4Area.clear();
        tab4Progress.setVisible(true);
        output(Strings.STATUS_TRANSACTION_ASSEMBLE);
        final InvokeService service = new InvokeService(Strings.RRC_CHAINCODE_NAME, Strings.RRC_CHAINCODE_FUNCTION_LOG, new String[]{tab4Arg1.getText(), Strings.EVENT_WRITE, tab4Arg2.getText()});
        service.setOnSucceeded(e -> {
            // if service succeeds, hide progress indicator and indicate transaction success
            tab4Progress.setVisible(false);
            output(Strings.STATUS_TRANSACTION_SUCCESS);
            tab4Area.appendText("Record updated with new entry: \n" + tab4Arg2.getText());
            writeBtn.setDisable(false);
        });
        service.setOnFailed(e -> {
            // if service failes, hide progress indicator and indicate transaction fail
            tab4Progress.setVisible(false);
            output(Strings.STATUS_TRANSACTION_FAILED);
            writeBtn.setDisable(false);
        });
        service.restart();
    }

    @FXML
    private void exit(ActionEvent event) {
        System.exit(0);
    }

    /**
     * Open dialog box to enroll a user
     */
    private void openEnrollDialog() throws FileNotFoundException, IOException {
        Dialog<Pair<String, String>> enrollDialog = new Dialog<>();
        enrollDialog.setTitle(Strings.ENROLL_DIALOG_TITLE);
        enrollDialog.setHeaderText(Strings.ENROLL_DIALOG_HEADER);
        ButtonType enrollButtonType = new ButtonType("Enroll", ButtonData.OK_DONE);
        enrollDialog.getDialogPane().getButtonTypes().addAll(enrollButtonType, ButtonType.CANCEL);
        // set grid layout in dialog box
        GridPane enrollGrid = new GridPane();
        enrollGrid.setHgap(10);
        enrollGrid.setVgap(10);
        enrollGrid.setPadding(new Insets(20, 150, 10, 10));
        TextField userName = new TextField();
        userName.setPromptText("Username");
        ComboBox affiliations = new ComboBox();
        ObservableList<String> options = FXCollections.observableArrayList();
        // fill list of affiliations with affiliations listed in config file
        FileReader fr = new FileReader(MainController.class.getClassLoader().getResource(Strings.AFFILIATIONS_FILENAME).getPath());
        try (BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                options.add(line.split(";")[0]);
            }
        }
        affiliations.getItems().addAll(options);
        affiliations.setValue(options.get(0));
        enrollGrid.add(new Label("Username:"), 0, 0);
        enrollGrid.add(userName, 1, 0);
        enrollGrid.add(new Label("Affiliation:"), 0, 1);
        enrollGrid.add(affiliations, 1, 1);
        enrollDialog.getDialogPane().setContent(enrollGrid);
        Platform.runLater(() -> userName.requestFocus());
        enrollDialog.setResultConverter((b) -> {
            if (b == enrollButtonType) {
                return new Pair<>(userName.getText(), affiliations.getValue().toString());
            }
            return null;
        });
        Optional<Pair<String, String>> result = enrollDialog.showAndWait();
        result.ifPresent(r -> {
            try {
                // read configuration details for the selected affiliation in config file and set user context
                FileReader fr2 = new FileReader(MainController.class.getClassLoader().getResource(Strings.AFFILIATIONS_FILENAME).getPath());
                try (BufferedReader br = new BufferedReader(fr2)) {
                    boolean configLocated = false;
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] args = line.split(";");
                        if (args[0].equalsIgnoreCase(r.getValue())) {
                            CommunicationHandler.setContext(r.getKey(), args[2], args[3], args[4], args[5], args[0], args[1]);
                            // set user label and enable GUI elements that require user context to be set
                            setUserLabel(r.getKey(), args[0]);
                            registerBtn.setDisable(false);
                            readBtn.setDisable(false);
                            writeBtn.setDisable(false);
                            editBtn.setDisable(false);
                            if (CommunicationHandler.getChannel() == null) {
                                // initialize channel object and print length
                                Long length = CommunicationHandler.initChannel();
                                output("Channel '" + CommunicationHandler.getChannel().getName() + "'. Length: " + length);
                            }
                            configLocated = true;
                            break;
                        }
                    }
                    if (!configLocated) {
                        String message = "ERROR: Could not find configuration for the selected affiliation";
                        LOG.error(message);
                        output(message);
                    }
                }
            } catch (Exception ex) {
                LOG.error(ex);
                output("ERROR: An enrollment error occured");
            }
        });
    }

    /**
     * Sets user label text indicating which user is enrolled and its
     * affiliation
     *
     * @param userName User name
     * @param affiliation User affiliation
     */
    private void setUserLabel(String userName, String affiliation) {
        tab1Arg2.setText(affiliation.split("\\.")[0]);
        String label = "Enrolled as '" + userName + "' with '" + affiliation + "'";
        userLabel.setText(label);
        output(label);
    }

    /**
     * Outputs message to the status log text field
     *
     * @param message Message to output
     */
    private void output(String message) {
        String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new java.util.Date());
        outputArea.appendText("\n" + timeStamp + ": " + message);
    }

    /**
     * Initialize GUI elements in the Register Record tab
     */
    private void initTab1() {
        tab1Progress.setVisible(false);
        tab1Arg1.setPromptText("Patient National ID");
        tab1Arg3.setPromptText("Initial Significance");
        tab1Arg2.setEditable(false);
    }

    /**
     * Initialize GUI elements in the Access Control tab
     */
    private void initTab2() {
        tab2Progress.setVisible(false);
        tab2Arg1.setPromptText("RRC reference");
        tab2Arg2.setPromptText("Client ID");
        grantRadBtn.setUserData(0);
        revokeRadBtn.setUserData(1);
        clientRadBtn.setUserData(0);
        mspRadBtn.setUserData(1);
        readRadBtn.setUserData(Strings.EVENT_READ);
        writeRadBtn.setUserData(Strings.EVENT_WRITE);
        permissionsGroup = new ToggleGroup();
        entityGroup = new ToggleGroup();
        eventGroup = new ToggleGroup();
        grantRadBtn.setToggleGroup(permissionsGroup);
        revokeRadBtn.setToggleGroup(permissionsGroup);
        clientRadBtn.setToggleGroup(entityGroup);
        mspRadBtn.setToggleGroup(entityGroup);
        readRadBtn.setToggleGroup(eventGroup);
        writeRadBtn.setToggleGroup(eventGroup);
        grantRadBtn.setSelected(true);
        clientRadBtn.setSelected(true);
        readRadBtn.setSelected(true);
    }

    /**
     * Initialize GUI elements in the Read tab
     */
    private void initTab3() {
        tab3Progress.setVisible(false);
        tab3Arg1.setPromptText("RRC reference");
        tab3Arg2.setPromptText("");
        tab3Arg2.setDisable(true);
    }

    /**
     * Initialize GUI elements in the Write tab
     */
    private void initTab4() {
        tab4Progress.setVisible(false);
        tab4Arg1.setPromptText("RRC reference");
        tab4Arg2.setPromptText("New entry");
    }
}

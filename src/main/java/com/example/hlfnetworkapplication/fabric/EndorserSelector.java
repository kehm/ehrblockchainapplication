/* SPDX-License-Identifier: Apache-2.0 */
package com.example.hlfnetworkapplication.fabric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.hyperledger.fabric.sdk.ServiceDiscovery;
import org.hyperledger.fabric.sdk.ServiceDiscovery.SDEndorser;
import org.hyperledger.fabric.sdk.ServiceDiscovery.SDEndorserState;
import org.hyperledger.fabric.sdk.ServiceDiscovery.SDLayout;

/**
 * Class for endorsement selector based on the significance concept
 *
 * @author kehm
 */
public class EndorserSelector implements ServiceDiscovery.EndorsementSelector {

    private static final Random RANDOM = new Random();
    private static String[] endorsers; // endorser organization

    /**
     * Select endorsement based on the amount of significance associated with
     * the member
     */
    public static final ServiceDiscovery.EndorsementSelector ENDORSEMENT_SELECTION_SIGNIFICANCE = (sdc) -> {
        System.out.println("Endorser is: " + endorsers[0] + " Invoker is: " + endorsers[1]);
        Map<String, SDEndorser> selectedEndorsers = new HashMap<>();
        SDLayout selectedLayout = null;
        List<SDLayout> layouts = new ArrayList<>(sdc.getLayouts());
        while (!layouts.isEmpty()) {
            SDLayout lay = layouts.get(RANDOM.nextInt(layouts.size()));
            // check if layout contains all selected endorsers and select one random peer from each endorser
            lay.getSDLGroups().stream().map((grp) -> new ArrayList<>(grp.getEndorsers()).get(RANDOM.nextInt(grp.getEndorsers().size()))).filter((end) -> (end.getMspid().split("MSP")[0].equalsIgnoreCase(endorsers[0]) || end.getMspid().split("MSP")[0].equalsIgnoreCase(endorsers[1]))).forEachOrdered((end) -> {
                selectedEndorsers.putIfAbsent(end.getEndpoint(), end);
            });
            // if layout contains all required endorsers, select the layout
            if (selectedEndorsers.size() == endorsers.length) {
                selectedLayout = lay;
                break;
            } else {
                layouts.remove(lay);
                selectedEndorsers.clear();
            }
        }
        final SDEndorserState sdEndorserState = new SDEndorserState();
        sdEndorserState.setPickedEndorsers(selectedEndorsers.values());
        sdEndorserState.setPickedLayout(selectedLayout);
        return sdEndorserState;
    };

    /**
     * Sets endorsing organizations
     *
     * @param endorsers Endorsing organizations
     */
    public static void setEndorsers(String[] endorsers) {
        EndorserSelector.endorsers = endorsers;
    }

    @Override
    public SDEndorserState endorserSelector(ServiceDiscovery.SDChaindcode sdc) {
        throw new UnsupportedOperationException("Method not implemented.");
    }
}

/* SPDX-License-Identifier: Apache-2.0 */
package com.example.hlfnetworkapplication.fabric;

import java.io.Serializable;
import java.util.Set;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

/**
 * Implementation of the Fabric SDK Java User interface
 *
 * @author kehm
 */
public class ClientUser implements User, Serializable {

    private String name;
    private Set<String> roles;
    private String account;
    private String affiliation;
    private Enrollment enrollment;
    private String mspId;

    /**
     * Constructor for ClientUser object
     */
    public ClientUser() {
    }

    /**
     * Constructor for ClientUser object
     *
     * @param name
     * @param roles
     * @param account
     * @param affiliation
     * @param enrollment
     * @param mspId
     */
    public ClientUser(String name, Set<String> roles, String account, String affiliation, Enrollment enrollment, String mspId) {
        this.name = name;
        this.roles = roles;
        this.account = account;
        this.affiliation = affiliation;
        this.enrollment = enrollment;
        this.mspId = mspId;
    }

    /**
     * Constructor for ClientUser object
     *
     * @param name
     * @param affiliation
     * @param enrollment
     * @param mspId
     */
    public ClientUser(String name, String affiliation, Enrollment enrollment, String mspId) {
        this.name = name;
        this.affiliation = affiliation;
        this.enrollment = enrollment;
        this.mspId = mspId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public String getAffiliation() {
        return affiliation;
    }

    @Override
    public Enrollment getEnrollment() {
        return enrollment;
    }

    @Override
    public String getMspId() {
        return mspId;
    }

    /**
     * Set client name
     *
     * @param name Client name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set roles
     *
     * @param roles Roles
     */
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    /**
     * Set account
     *
     * @param account Account
     */
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * Set affiliation
     *
     * @param affiliation Affiliation
     */
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    /**
     * Set enrollment
     *
     * @param enrollment Enrollment
     */
    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    /**
     * Set MSP ID
     *
     * @param mspId MSP ID
     */
    public void setMspId(String mspId) {
        this.mspId = mspId;
    }
}

package com.behaviosec.tree.restclient;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

interface BehavioSecAPIInterface {


    /**
     * RESTFull call to submit timing data and receive user report
     * TODO: add approprite links to the documentation
     *
     * @param userID        The ID of the user. Do not include userId when requesting Page Navigation results.
     * @param timing        JSON-formatted timing data string, please refer to Timing data format.
     * @param userAgent     The user agent string.
     * @param ip            The IP of the user.
     * @param reportFlags   Please refer to section regarding Report Flags.
     * @param operatorFlags Please refer to section regarding Operator Flags.
     * @param sessionID     The session ID that the transaction belongs to (null or empty to auto generate).
     * @param tenantID      Only applicable in a multi-tenant setup. Refer to multi-tenancy section for further information.
     * @param timeStamp     The timestamp of the event.
     * @param notes
     * @return Returns a JSON formatted report (a collection of reports) that contains the comparison of a timing behavior string to the given user ID.
     */
    BehavioSecReport getReport(String userID,
                               String timing,
                               String userAgent,
                               String ip,
                               int reportFlags,
                               int operatorFlags,
                               @Nullable String sessionID,
                               @Nullable String tenantID,
                               @Nullable Long timeStamp,
                               @Nullable String notes
    );
    HttpResponse getReport(List<NameValuePair> report) throws IOException;

    /**
     * Call to check health status of the server
     *
     * @return  True if database is accessible; false otherwise.
     */
    boolean getHealthCheck() throws IOException;

    /**
     * Get version of Behaviosec server and its components
     *
     * @return String containing version of BehavioSense and its components.
     */
    BehavioSecVersion getVersion() throws IOException;


    /**
     * Resets the matching sub-profiles of the user record. All variables must match for it to be reset.
     * Used then the password is reset
     *
     * @param userID The user ID to update.
     * @param target Target the target that we want to reset. Specify null or empty string (Ӕ) for all targets.
     * @param profileType ProfileType the field type that we want to reset ANONYMOUSTEXT, REGULARTEXT. null or “” to match all.
     * @param deviceType DeviceType the device type that we want to reset DESKTOP, MOBILE, IPHONE, IPAD, ANDROID, WINDOWSPHONE, etc. null or “” to match all.
     * @param tenantId Only applicable in a multi-tenant setup. Refer to multi-tenancy section for further information.
     * @param reason Reason for later traceability.
     *
     *
     * @return status code or error
     */
    boolean resetProfile(String userID,
                         @Nullable String target,
                         @Nullable String profileType,
                         @Nullable String deviceType,
                         @Nullable String tenantId,
                         @Nullable String reason
    );
    boolean resetProfile(List<NameValuePair> report) throws IOException;
}

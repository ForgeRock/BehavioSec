/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2017-2018 ForgeRock AS.
 */


package com.behaviosec.tree.nodes;

import com.behaviosec.isdk.client.APICall;
import com.behaviosec.isdk.client.Client;
import com.behaviosec.isdk.client.ClientConfiguration;
import com.behaviosec.isdk.client.RestClient;
import com.behaviosec.isdk.client.RestClientOktHttpImpl;
import com.behaviosec.isdk.config.BehavioSecException;
import com.behaviosec.isdk.config.NoBehavioSecReportException;
import com.behaviosec.isdk.entities.Report;
import com.behaviosec.isdk.entities.Response;
import com.behaviosec.tree.utils.Helper;
import com.google.common.hash.Hashing;
import com.sun.identity.shared.debug.Debug;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.AbstractDecisionNode;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.util.i18n.PreferredLocales;
import com.behaviosec.tree.config.Constants;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.inject.Inject;

/**
 * A node that send request to BehavioSense endpoint. Node expect to find behavior data in shared context
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class,
        configClass = BehavioSecAuthNode.Config.class)
public class BehavioSecAuthNode extends AbstractDecisionNode {

    private final static String DEBUG_NAME = "BehavioSecAuthNode";
    private final static Debug debug = Debug.getInstance(DEBUG_NAME);

    private final Config config;
    List operatorFlags = Arrays.asList (
            "0",
            "1",
            "2",
            "4",
            "8",
            "16",
            "32",
            "64",
            "128",
            "256",
            "512",
            "1024",
            "2048",
            "4096"
            );




    /**
     * Configuration for the node.
     */
    public interface Config {

        /**
         *
         * @return
         */
        @Attribute(order = 100)
        default String endpoint() {
            return "https://URL:OPTIONAL_PORT/";
        }

        @Attribute(order = 150)
        default String tenantID() {
            return "default_tenant";
        }

        @Attribute(order = 185)
        default String operatorFlag() {
            return "256";
        }

        @Attribute(order = 200)
        default boolean hashUserName() {
            return false;
        }

        @Attribute(order = 300)
        default boolean anonymizeIP() {
            return false;
        }

        @Attribute(order = 400)
        default boolean denyOnFail() {
            return false;
        }

    }
    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     * @throws NodeProcessException If the configuration was not valid.
     */
    @Inject
    public BehavioSecAuthNode(@Assisted Config config) {
        this.config = config;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        String timingData = context.sharedState.get(Constants.BEHAVIOSEC_TIMING_DATA).asString();
        if (timingData == null) {
            debug.error("Timing data is null in shared state", Constants.BEHAVIOSEC_TIMING_DATA);
            // We check for flag, and we either return deny or success
            if (config.denyOnFail()) {
                return goTo(false).build();
            } else {
                return goTo(true).build();
            }
        }

        if (!operatorFlags.contains(this.config.operatorFlag())) {
            debug.error("Invalid operator flag");
            throw new NodeProcessException("Invalid operator flag");
        }

        String username = context.sharedState.get(Constants.USER_NAME).asString();
        if (this.config.hashUserName()) {
            username = Hashing.sha256()
                    .hashString(
                            username,
                            StandardCharsets.UTF_8
                    )
                    .toString();
        }

        debug.error("Got timing data");
        debug.error(timingData);
        String userAgent = "";
        try {
            userAgent = context.request.headers.get("user-agent").get(0);
        } catch (IndexOutOfBoundsException e) {
            debug.error("sendRequest: Change of API for user-agent");
            throw new NodeProcessException("Could not get user-agent");
        }

        String userip = context.request.clientIp;
        if (this.config.anonymizeIP()){
            userip = userip.substring(0, userip.lastIndexOf(".")) +".000";
        }

        // Going to add UUID there and report
        JsonValue newSharedState = context.sharedState.copy();

        String sessionID = context.sharedState.get(Constants.BEHAVIOSEC_SID).asString();
        if(sessionID == null) {
            sessionID = UUID.randomUUID().toString();
            newSharedState.put(Constants.BEHAVIOSEC_SID, sessionID);
        }

        ClientConfiguration clientConfig = new ClientConfiguration(this.config.endpoint());

        RestClient client = new RestClientOktHttpImpl(clientConfig);

        APICall callReport = APICall.report()
                .tenantId(this.config.tenantID())
                .username(username)
                .userIP(userip)
                .userAgent(userAgent)
                .timingData(timingData)
                .timestamp()
                .sessionId(sessionID)
                .operatorFlags(com.behaviosec.isdk.config.Constants.FLAG_FINALIZE_SESSION)
                .notes("FR-V" + BehavioSecPlugin.currentVersion)
                .build();

        Response response = null;
        try {
            response = client.makeCall(callReport);
        } catch (BehavioSecException e) {
            e.printStackTrace();
        }

        if( response.hasReport()){
            newSharedState.remove(Constants.BEHAVIOSEC_TIMING_DATA);
            newSharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(response.getReport()));

            return goTo(true).replaceSharedState(newSharedState).build();
        }
        return goTo(false).build();

    }

    protected Action.ActionBuilder goTo(boolean outcome) {
        return Action.goTo(outcome ? Constants.TRUE_OUTCOME_ID : Constants.FALSE_OUTCOME_ID);
    }


}
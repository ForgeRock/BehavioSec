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


import com.behaviosec.isdk.client.*;
import com.behaviosec.isdk.config.BehavioSecException;
import com.behaviosec.isdk.entities.Response;
import com.google.common.hash.Hashing;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.behaviosec.tree.config.Constants;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import javax.inject.Inject;

/**
 * A node that send request to BehavioSense endpoint. Node expect to find behavior data in shared context
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class,
        configClass = BehavioSecAuthNode.Config.class)
public class BehavioSecAuthNode extends AbstractDecisionNode {

    private static final String TAG = BehavioSecAuthNode.class.getName();
    private static final Logger logger = LoggerFactory.getLogger(TAG);

    private final Config config;

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
            return "512";
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

        return sendRequest(context);
    }

    private Action sendRequest(TreeContext context) throws NodeProcessException {
        String username = context.sharedState.get(Constants.USERNAME).asString();
        // add config option for the session name
        if (this.config.hashUserName()) {
            username = Hashing.sha256()
                    .hashString(
                            username,
                            StandardCharsets.UTF_8
                    )
                    .toString();
        }
        String timingData = context.sharedState.get(Constants.DATA_FIELD).asString();
        if (timingData == null) {
            logger.error("Timing data is null in %s", Constants.DATA_FIELD);
            // We check for flag, and we either return deny or success
            if (config.denyOnFail()) {
                return goTo(false).build();
            } else {
                return goTo(true).build();
            }
        }
        logger.info("Got timing data");
        String userAgent = "";
        try {
            userAgent = context.request.headers.get("user-agent").get(0);
        } catch (IndexOutOfBoundsException e) {
            logger.error("sendRequest: Change of API for user-agent");
        }

        String userip = context.request.clientIp;
        if (this.config.anonymizeIP()){
            userip = userip.substring(0, userip.lastIndexOf(".")) +".000";
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
                .sessionId(UUID.randomUUID().toString())
                .operatorFlags(com.behaviosec.isdk.config.Constants.FLAG_FINALIZE_SESSION)
                .notes("FR-V" + BehavioSecPlugin.currentVersion)
                .build();

        Response respose = null;
        try {
            respose = client.makeCall(callReport);
        } catch (BehavioSecException e) {
            e.printStackTrace();
        }
        JsonValue newSharedState = context.sharedState.copy();
        if( respose.hasReport()){
            newSharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(respose.getReport()));
            return goTo(true).replaceSharedState(newSharedState).build();
        }

        return goTo(false).build();

    }


    private String getResponseString(HttpResponse resp) throws IOException {
        return EntityUtils.toString(resp.getEntity(), "UTF-8");
    }

    protected Action.ActionBuilder goTo(boolean outcome) {
        return Action.goTo(outcome ? Constants.TRUE_OUTCOME_ID : Constants.FALSE_OUTCOME_ID);
    }

    static final class OutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
        private static final String BUNDLE = BehavioSecAuthNode.class.getName().replace(".", "/");

        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE, OutcomeProvider.class.getClassLoader());
            return ImmutableList.of(
                    new Outcome(Constants.TRUE_OUTCOME_ID, bundle.getString("true")),
                    new Outcome(Constants.FALSE_OUTCOME_ID, bundle.getString("false")));
        }
    }

}
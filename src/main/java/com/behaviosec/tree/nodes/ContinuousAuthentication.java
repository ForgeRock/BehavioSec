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
import com.behaviosec.isdk.client.ClientConfiguration;
import com.behaviosec.isdk.client.RestClient;
import com.behaviosec.isdk.client.RestClientFactory;
import com.behaviosec.isdk.config.BehavioSecException;
import com.behaviosec.isdk.entities.Report;
import com.behaviosec.isdk.entities.Response;
import com.behaviosec.tree.config.Constants;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import org.apache.http.NameValuePair;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;

/**
 * A node that send request to BehavioSense endpoint. Node expect to find behavior data in shared context
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class,
        configClass = ContinuousAuthentication.Config.class, tags={"behavioral"})
public class ContinuousAuthentication extends AbstractDecisionNode {

    private static final String TAG = ContinuousAuthentication.class.getName();
    private static final Logger logger = LoggerFactory.getLogger(TAG);

    private final Config config;

    /**
     * Configuration for the node.
     */
    public interface Config {
        @Attribute(order = 100)
        default String endpoint() {
            return "https://URL:OPTIONAL_PORT/";
        }

        @Attribute(order = 150)
        default String tenantID() {
            return "default_tenant";
        }

        @Attribute(order = 175)
        default String replacementURL() {
            return "";
        }
    }

    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     */
    @Inject
    public ContinuousAuthentication(@Assisted Config config) {
        this.config = config;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        String sContext = context.toString();
        String sHeaders = context.request.headers.toString();
        String sCookies = context.request.cookies.toString();
        String sSharedState = context.sharedState.toString();
        String sTransientState = context.transientState.toString();


        return sendRequest(context);
    }

    private void debugMesssage (String message) {
        System.out.println(message);
    }

    private Action sendRequest(TreeContext context) {
        List<NameValuePair> nameValuePairs = new ArrayList<>(2);

        String username = context.sharedState.get(Constants.USERNAME).asString();
        String sessionID = context.request.cookies.get("com.behaviosec.api.check_cookie_param_sessionId");
        String journeyID = context.request.cookies.get("com.behaviosec.api.check_cookie_param_journeyId");

        String endPoint = config.endpoint();
        String tenantId = config.tenantID();

        int timeOutInMilliSeconds = 10000;
        int retries = 3;
        ClientConfiguration clientConfiguration = new ClientConfiguration(endPoint, tenantId, timeOutInMilliSeconds, retries);

        RestClient restClient = RestClientFactory.buildRestClient(clientConfiguration);

        APICall callHealth = APICall.healthBuilder().build();

        try {
            Response h = restClient.makeCall(callHealth);
            if (h.getResponseCode() == 200 || h.getResponseCode() == 0) {
                logger.debug("Health check result " + h.getResponseString());
            } else {
                System.out.println("Returning false - 0");
                return goTo(false).build();

            }
        } catch (BehavioSecException e) {
            logger.error("Error calling behaviosec " + e.getMessage());
            return goTo(false).build();
        }

        String replacementURL = this.config.replacementURL();

        if (replacementURL != null && !"".equals(replacementURL)) {
            logger.debug("Replacing URL: " + replacementURL);
        }

        APICall bindJourney = APICall.bindJourneyBuilder()
                .tenantId(tenantId)
                .username(username)
                .journeyId(journeyID)
                .sessionId(sessionID)
                .build();

        Response response;

        logger.debug("Calling bindJourney " + "tenantId(" + tenantId + ")" +
                "                .username(" + username + ")" +
                "                .journeyId(" + journeyID + ")" +
                "                .sessionId(" + sessionID + ")");
        try {
            response = restClient.makeCall(bindJourney);
        } catch (BehavioSecException e) {
            logger.error("Error calling bindjourney " + e.getMessage());
            return goTo(false).build();
        }

        JsonValue newSharedState = context.sharedState.copy();

        APICall getReport = APICall.session()
                .tenantId(tenantId)
                .username(username)
                .sessionId(sessionID)
                .reportFlags(0)
                .includeRawData(false)
                .build();

        try {
            response = restClient.makeCall(getReport);
        } catch (BehavioSecException e) {
            logger.error("Error calling getReport " + e.getMessage());
            return goTo(false).build();
        }

        if (response.hasReport()){
            Report report = response.getReport();
            newSharedState.put("SESSIONID", Collections.singletonList(sessionID));
            newSharedState.put("JOURNEYID", Collections.singletonList(journeyID));
            newSharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(response.getReport()));
            logger.debug("Returning from continuous authentication ");
            return goTo(true).replaceSharedState(newSharedState).build();
        }

        logger.error("No report found for session id " + sessionID);
        return goTo(false).replaceSharedState(newSharedState).build();
    }

    protected Action.ActionBuilder goTo(boolean outcome) {
        return Action.goTo(outcome ? Constants.TRUE_OUTCOME_ID : Constants.FALSE_OUTCOME_ID);
    }

    static final class OutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
        private static final String BUNDLE = ContinuousAuthentication.class.getName().replace(".", "/");

        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE, OutcomeProvider.class.getClassLoader());
            return ImmutableList.of(
                    new Outcome(Constants.TRUE_OUTCOME_ID, bundle.getString("true")),
                    new Outcome(Constants.FALSE_OUTCOME_ID, bundle.getString("false")));
        }
    }
}

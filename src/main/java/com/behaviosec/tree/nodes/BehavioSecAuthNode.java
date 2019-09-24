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


import com.google.common.hash.Hashing;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import com.behaviosec.tree.restclient.BehavioSecRESTClient;
import com.behaviosec.tree.restclient.BehavioSecReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
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
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Config config;
    private final BehavioSecRESTClient behavioSecRESTClient;

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
            return "https://IP:PORT/";
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
            return true;
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
        this.behavioSecRESTClient = new BehavioSecRESTClient(this.config.endpoint());
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {

        return sendRequest(context);
    }

    private Action sendRequest(TreeContext context) throws NodeProcessException {
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);
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
            nameValuePairs.add(new BasicNameValuePair(Constants.USER_ID, username));
            String timingData = context.sharedState.get(Constants.DATA_FIELD).asString();

            if (timingData != null) {
                nameValuePairs.add(new BasicNameValuePair(Constants.TIMING,
                                                          timingData));
            } else {
                logger.error("Timing data is null");
                // We check for flag, and we either return deny or success
                if (config.denyOnFail()) {
                    return goTo(false).build();
                } else {
                    return goTo(true).build();
                }
            }
            String userAgent = "";
            try {
                userAgent = context.request.headers.get("user-agent").get(0);
            } catch (IndexOutOfBoundsException e) {
                logger.error("sendRequest: Change of API for user-agent");
            }

            nameValuePairs.add(new BasicNameValuePair(Constants.USER_AGENT, userAgent));
            String userip = context.request.clientIp;
            if (this.config.anonymizeIP()){
                userip = userip.substring(0, userip.lastIndexOf(".")) +".000";
            }

            nameValuePairs.add(new BasicNameValuePair(Constants.IP, userip));
            nameValuePairs.add(new BasicNameValuePair(Constants.TIMESTAMP,
                                                      Long.toString(Calendar.getInstance().getTimeInMillis())));
            nameValuePairs.add(new BasicNameValuePair(Constants.SESSION_ID, UUID.randomUUID().toString()));
            nameValuePairs.add(
                    new BasicNameValuePair(Constants.NOTES, "FR-V" + BehavioSecPlugin.currentVersion));
            nameValuePairs.add(new BasicNameValuePair(Constants.REPORT_FLAGS, Integer.toString(0)));
            int operatorFlags = Constants.FLAG_GENERATE_TIMESTAMP + Constants.FINALIZE_DIRECTLY;
            nameValuePairs.add(new BasicNameValuePair(Constants.OPERATOR_FLAGS, Integer.toString(operatorFlags)));

            HttpResponse reportResponse = behavioSecRESTClient.getReport(nameValuePairs);
            int responseCode = reportResponse.getStatusLine().getStatusCode();

            if (responseCode == 200) {
                JsonValue newSharedState = context.sharedState.copy();

                BehavioSecReport bhsReport = objectMapper.readValue(EntityUtils.toString(reportResponse.getEntity()),
                                                                    BehavioSecReport.class);

                newSharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bhsReport));
                return goTo(true).replaceSharedState(newSharedState).build();
            } else if (responseCode == 400) {
                logger.error(TAG + " response 400  " + getResponseString(reportResponse));
            } else if (responseCode == 403) {
                logger.error(TAG + " response 400  " + getResponseString(reportResponse));
            } else if (responseCode == 500) {
                logger.error(TAG + " response 500  " + getResponseString(reportResponse));
            } else {
                logger.error(TAG + " response " + responseCode);
            }

        } catch (MalformedURLException e) {
            logger.error("MalformedURLException: " + e.toString());
            throw new NodeProcessException("MalformedURLException for " + config.endpoint());
        } catch (IOException e) {
            logger.error("IOException: " + e.toString());
            throw new NodeProcessException("IOException for " + e.toString());
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
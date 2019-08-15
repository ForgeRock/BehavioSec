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


package com.behaviosec.behaviosecAuthNode;


import com.behaviosec.client.BehavioSecRESTClient;
import com.behaviosec.client.BehavioSecReport;
import com.behaviosec.utils.Consts;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

/**
 * A node that checks to see if zero-page login headers have specified username and whether that username is in a group
 * permitted to use zero-page login headers.
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class,
        configClass = BehaviosecAuthNode.Config.class)
public class BehaviosecAuthNode extends AbstractDecisionNode {

    private final static String TRUE_OUTCOME_ID = "true";
    private final static String FALSE_OUTCOME_ID = "false";
    private static final String TAG = BehaviosecAuthNode.class.getName();

    private final Logger logger = LoggerFactory.getLogger(TAG);

    private final Config config;
    private BehavioSecRESTClient behavioSecRESTClient;

    /**
     * Configuration for the node.
     */
    public interface Config {

        @Attribute(order = 100)
        default String endpoint() {
            return "http://13.56.150.246:8080/";
        }

        @Attribute(order = 200)
        default int MinimumScore() {
            return 75;
        }

        @Attribute(order = 300)
        default String DataField() {
            return "bdata";
        }

        //TODO: verify that value exist?
        //TODO: how do validate configuration errors?
        //TODO: can we enforce dependencies?

        @Attribute(order = 400)
        default String Tenant() {
            return "TENANT_UUID";
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
    public BehaviosecAuthNode(@Assisted Config config) throws NodeProcessException {
        this.config = config;
        this.behavioSecRESTClient = new BehavioSecRESTClient(this.config.endpoint());
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {

        return sendRequest(context);
    }

    private Action sendRequest(TreeContext context) throws NodeProcessException {


        try {
            logger.error("Checking health " + behavioSecRESTClient.getHealthCheck());
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);
            String username = context.sharedState.get("username") + "_";
            logger.error("username: " + username);

            nameValuePairs.add(new BasicNameValuePair(Consts.USER_ID, username));
            String timingData = context.sharedState.get(config.DataField()).asString();
            nameValuePairs.add(new BasicNameValuePair(Consts.TIMING,
                    timingData));
            String userAgent = "";
            try{
                userAgent = context.request.headers.get("user-agent").get(0);
            } catch (IndexOutOfBoundsException e) {
                logger.error("sendRequest: Change in API for user-agent");
            }

            nameValuePairs.add(new BasicNameValuePair(Consts.USER_AGENT, userAgent));
            nameValuePairs.add(new BasicNameValuePair(Consts.IP, context.request.clientIp));
            nameValuePairs.add(new BasicNameValuePair(Consts.TIMESTAMP,
                    Long.toString(Calendar.getInstance().getTimeInMillis())));
            nameValuePairs.add(new BasicNameValuePair(Consts.SESSION_ID, UUID.randomUUID().toString()));
            nameValuePairs.add(new BasicNameValuePair(Consts.NOTES, "FR-V" + BehaviosecAuthNodePlugin.currentVersion));
            nameValuePairs.add(new BasicNameValuePair(Consts.REPORT_FLAGS, Integer.toString(0)));
            nameValuePairs.add(new BasicNameValuePair(Consts.OPERATOR_FLAGS, Integer.toString(0)));

            BehavioSecReport response = behavioSecRESTClient.getReport(nameValuePairs);
            logger.error("response " + response.toString());

            if (response.getScore() >= (double) config.MinimumScore() && !response.isTrained()) {
                return goTo(true).build();
            }
        } catch (MalformedURLException e) {
            logger.error("MalformedURLException: " + e.toString());
            e.printStackTrace();
            throw new NodeProcessException("MalformedURLException for " + config.endpoint());
        } catch (IOException e) {
            logger.error("IOException: " + e.toString());
            e.printStackTrace();
            throw new NodeProcessException("IOException for " + e.toString());
        }

        return goTo(false).build();

    }

    protected Action.ActionBuilder goTo(boolean outcome) {
        return Action.goTo(outcome ? TRUE_OUTCOME_ID : FALSE_OUTCOME_ID);
    }

    static final class OutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
        private static final String BUNDLE = BehaviosecAuthNode.class.getName().replace(".", "/");

        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE, OutcomeProvider.class.getClassLoader());
            return ImmutableList.of(
                    new Outcome(TRUE_OUTCOME_ID, bundle.getString("true")),
                    new Outcome(FALSE_OUTCOME_ID, bundle.getString("false")));
        }
    }

}
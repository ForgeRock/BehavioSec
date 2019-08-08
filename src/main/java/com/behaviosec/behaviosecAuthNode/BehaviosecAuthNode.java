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


import java.io.BufferedReader;
import java.io.IOException;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.AbstractDecisionNode;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.util.i18n.PreferredLocales;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A node that checks to see if zero-page login headers have specified username and whether that username is in a group
 * permitted to use zero-page login headers.
 */
@Node.Metadata(outcomeProvider  = AbstractDecisionNode.OutcomeProvider.class,
        configClass      = BehaviosecAuthNode.Config.class)
public class BehaviosecAuthNode extends AbstractDecisionNode {

    private final static String TRUE_OUTCOME_ID = "true";
    private final static String FALSE_OUTCOME_ID = "false";
    private final Logger logger = LoggerFactory.getLogger("amAuth"); //LoggerFactory.getLogger(BehaviosecAuthNode.class);

    private final Config config;

    /**
     * Configuration for the node.
     */
    public interface Config {

        @Attribute(order = 100)
        default String URL() {
            return "http://13.56.150.246:8080/BehavioSenseAPI/GetHealthCheck";
        }

        @Attribute(order = 200)
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
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.error("Process node");
        return sendRequest(context);
    }

    private Action sendRequest(TreeContext context) throws NodeProcessException{
        logger.error(" *************************************** BehaviosecAuthNode *************************** ");
        logger.error("CLIENT IP: " + context.request.clientIp);
        logger.error("ServerURL: " +context.request.serverUrl);
        logger.error("SSOTokenID: " +context.request.ssoTokenId);
        logger.error("Headers: " +context.request.headers.toString());
        logger.error("params: " +context.request.parameters.toString());
        logger.error(" ******************************************************************************** ");

        try{
            String getHealth = config.URL()+"/BehavioSenseAPI/GetHealthCheck";
            logger.error("Sending request to " + getHealth);
            //Build HTTP request
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost postRequest = new HttpPost("http://13.56.150.246:8080/BehavioSenseAPI/GetReport");
            HttpGet getRequest = new HttpGet("http://13.56.150.246:8080/BehavioSenseAPI/GetHealthCheck");
            String data = "userId=tutorial_user&userAgent=\"\"&ip=0.0.0.0&timing=[[\"w\",[{\"text#tutorial_username\": 2}],\"Tutorial/Login/\"],[\"f\",\"text#tutorial_username\",[[0,97,10030],[1,97,10342],[0,98,11412],[1,98,11792]]]]";
            StringEntity entity = new StringEntity(data);
            postRequest.setEntity(entity);
            postRequest.setHeader("Accept", "application/json");
            postRequest.setHeader("Content-type", "application/json");
            HttpResponse response = httpClient.execute(postRequest);
            logger.error("RESPONSE: " + response.toString());
            if (response.getStatusLine().getStatusCode() != 200) {
                return goTo(false).build();
//                throw new NodeProcessException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
            }
//            String json_string = EntityUtils.toString(response.getEntity());
//            logger.error("RESPONSE entity: " + json_string);
//
//            JSONArray temp1 = new JSONArray(json_string);
//            logger.error("RESPONSE JSON: " + temp1.toString());
            return goTo(true).build();
//            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
//            String output = br.readLine();
//            logger.debug("Server response: " + output);
//            if (output != null){
//                return goTo(true).build();
//            } else {
//                return goTo(false).build();
//
//            }
        } catch (MalformedURLException e) {
            logger.error("MalformedURLException: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("IOException: " + e.toString());
            e.printStackTrace();
        }
//        catch (JSONException e) {
//            logger.error("JSONException: " + e.toString());
//            e.printStackTrace();
//        }
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
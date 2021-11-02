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
import com.behaviosec.isdk.entities.Response;
import com.behaviosec.tree.config.Constants;
import com.behaviosec.tree.utils.PathHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A node that send request to BehavioSense endpoint. Node expect to find behavior data in shared context
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class,
        configClass = BehavioSecNode.Config.class, tags={"behavioral"})
public class BehavioSecNode extends AbstractDecisionNode {

    private static final String TAG = BehavioSecNode.class.getName();
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

//        @Attribute(order = 185)
//        default String searchAttribute() {
//            return "";
//        }
    }

    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     * @throws NodeProcessException If the configuration was not valid.
     */
    @Inject
    public BehavioSecNode(@Assisted Config config) {
        this.config = config;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        String sContext = context.toString();
        String sHeaders = context.request.headers.toString();
        String sCookies = context.request.cookies.toString();
        String sSharedState = context.sharedState.toString();
        String sTransientState = context.transientState.toString();
        String sLocales = context.request.locales.getLocales().toString();
        String sClientIp = context.request.clientIp;
        String sXForwardedFor = context.request.headers.get("X-Forwarded-For").toString();
        String sCallbacks = context.getAllCallbacks().toString();

        debugMesssage("*** CONTEXT ***\n" + sContext);
        debugMesssage("*** HEADERS ***\n" + sHeaders);
        debugMesssage("*** COOKIES ***\n" + sCookies);
        debugMesssage("*** SHARED_STATE ***\n" + sSharedState);
        debugMesssage("*** TRANSIENT STATE ***\n" + sTransientState);

        return sendRequest(context);
    }

    private void debugMesssage (String message) {
        System.out.println(message);
    }

    private Action sendRequest(TreeContext context) throws NodeProcessException {
        List<NameValuePair> nameValuePairs = new ArrayList<>(2);

        String username = context.sharedState.get(Constants.USERNAME).asString();
/*
        String searchAttribute = this.config.searchAttribute();

        // If the DS attribute to which the IDM Applicant ID is linked is not null...
        if (searchAttribute != null && !"".equals(searchAttribute.trim())) {
            System.out.println("searchAttribute " + searchAttribute);

            AMIdentity userIdentity =
                    Optional.ofNullable(
                                    findUser(
                                            context.sharedState.get(SharedStateConstants.REALM).asString(),
                                            username, searchAttribute
                                    ))
                            .orElseThrow(() -> new NodeProcessException("Could not find user identity"));
            username = userIdentity.getUniversalId();
            System.out.println("username " + username);
        }
*/
        logger.debug("sendRequest: " + nameValuePairs);

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

        if (timingData != null) {
            logger.debug("Timing data is present");
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

        String userip = context.request.clientIp;
        if (this.config.anonymizeIP()){
            userip = userip.substring(0, userip.lastIndexOf(".")) +".000";
        }

        String endPoint = config.endpoint();
        String tenantId = config.tenantID();

        if (endPoint != null && !endPoint.endsWith("/")) {
            endPoint += "/";
        }

        logger.debug("endpoint  " + endPoint);
        logger.debug("tenantID  " + tenantId);

        int timeOutInMilliSeconds = 100000;
        int retries = 3;
        ClientConfiguration clientConfiguration = new ClientConfiguration(endPoint, tenantId, timeOutInMilliSeconds, retries);

        RestClient restClient = RestClientFactory.buildRestClient(clientConfiguration);

        APICall callHealth = APICall.healthBuilder().build();

        try {
            Response h = restClient.makeCall(callHealth);
            if (h.hasReport()) {
                logger.debug("health report " + h.getReport().toString());
            } else {
                if (h.getResponseCode() == 200) {
                    logger.debug("Health check result " + h.getResponseString());
                }
            }
        } catch (BehavioSecException e) {
            e.printStackTrace();
            return goTo(false).build();
        }

        logger.debug("tenantId(this.config.tenantID()): " + this.config.tenantID());
        logger.debug("username): " + username);
        logger.debug("userip: " + userip);
        logger.debug("userAgent: " + userAgent);
        logger.debug("notes: " + "FR-V" + BehavioSecPlugin.currentVersion);
        logger.debug("operatorFlags: " + com.behaviosec.isdk.config.Constants.FLAG_FINALIZE_SESSION);
        logger.debug("timingData: " + timingData);

        String sessionId = UUID.randomUUID().toString();

        Map <String, String> sCookies = context.request.cookies;

        if (sCookies.containsKey("com.behaviosec.api.check_cookie_param_sessionId")) {
            sessionId = sCookies.get("com.behaviosec.api.check_cookie_param_sessionId");
            System.out.println("Session id from cookie" + sessionId);
        }
        /*
        if (sCookies.containsKey("PF")) {
            sessionId = sCookies.get("PF");
        }
        if (sCookies.containsKey("nextsession")) {
            sessionId += sCookies.get("nextsession");
        } else {
            sessionId += "1";
        } */

        String replacementURL = this.config.replacementURL();
        System.out.println("timingData\n\n" + timingData);


        if (replacementURL != null && !"".equals(replacementURL)) {
            if (!replacementURL.startsWith("/")) {
                replacementURL = "/" + replacementURL;
            }
            timingData = PathHelper.replacePath(timingData, replacementURL);
            logger.debug("Replacing URL: " + replacementURL);
        } else {
            logger.debug("Replacement URL empty");
        }

        if (userip.equals("127.0.0.1") && username.equals("mfanti")) {
            userip = "189.27.247.148";
        }
        APICall callReport = APICall.report()
               .tenantId(this.config.tenantID())
                .username(username)
                .userIP(userip)
                .userAgent(userAgent)
                .timingData(timingData)
                .timestamp()
                .sessionId(sessionId)
    //            .operatorFlags(com.behaviosec.isdk.config.Constants.F)
                .notes("FR-V" + BehavioSecPlugin.currentVersion) 
                .build();

        Response response = null;
        try {
            response = restClient.makeCall(callReport);
        } catch (BehavioSecException e) {
            e.printStackTrace();
            return goTo(true).build();
        }

        JsonValue newSharedState = context.sharedState.copy();

        if (response.hasReport()){
            newSharedState.put("SESSIONID", Collections.singletonList(sessionId));
            newSharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(response.getReport()));

            Action.ActionBuilder builder = Action.goTo(Constants.TRUE_OUTCOME_ID);
            builder.putSessionProperty(Constants.USERNAME, username);
            builder.putSessionProperty(Constants.SESSION_ID, sessionId);

            return goTo(true).replaceSharedState(newSharedState).build();
        }

        logger.debug("After call to getReport in sendRequest: " + nameValuePairs);
        return goTo(false).build();
    }

    private AMIdentity findUser(String realm, String applicantId, String searchAttribute) {
        // The onfidoApplicantIdAttribute is the DS attribute to which the IDM Onfido Applicant ID is linked
        Set<String> userSearchAttributes = new HashSet<String>() {{ add(searchAttribute); }};

        return IdUtils.getIdentity(applicantId, realm, userSearchAttributes);
    }

    private String getResponseString(HttpResponse resp) throws IOException {
        return EntityUtils.toString(resp.getEntity(), "UTF-8");
    }

    protected Action.ActionBuilder goTo(boolean outcome) {
        return Action.goTo(outcome ? Constants.TRUE_OUTCOME_ID : Constants.FALSE_OUTCOME_ID);
    }

    static final class OutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
        private static final String BUNDLE = BehavioSecNode.class.getName().replace(".", "/");

        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE, OutcomeProvider.class.getClassLoader());
            return ImmutableList.of(
                    new Outcome(Constants.TRUE_OUTCOME_ID, bundle.getString("true")),
                    new Outcome(Constants.FALSE_OUTCOME_ID, bundle.getString("false")));
        }
    }

}

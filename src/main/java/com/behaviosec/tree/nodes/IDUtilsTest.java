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

import com.behaviosec.tree.config.Constants;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

/**
 * A node that send request to BehavioSense endpoint. Node expect to find behavior data in shared context
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class,
        configClass = IDUtilsTest.Config.class, tags={"behavioral"})
public class IDUtilsTest extends AbstractDecisionNode {

    private static final String TAG = IDUtilsTest.class.getName();
    private static final Logger logger = LoggerFactory.getLogger(TAG);

    private final Config config;

    /**
     * Configuration for the node.
     */
    public interface Config {



        @Attribute(order = 185)
        default String searchAttribute() {
            return "";
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
    public IDUtilsTest(@Assisted Config config) {
        this.config = config;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {


        return sendRequest(context);
    }

    private void debugMesssage (String message) {
        System.out.println(message);
    }

    private Action sendRequest(TreeContext context) throws NodeProcessException {


        String username = context.sharedState.get(Constants.USERNAME).asString();

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
        private static final String BUNDLE = IDUtilsTest.class.getName().replace(".", "/");

        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE, OutcomeProvider.class.getClassLoader());
            return ImmutableList.of(
                    new Outcome(Constants.TRUE_OUTCOME_ID, bundle.getString("true")),
                    new Outcome(Constants.FALSE_OUTCOME_ID, bundle.getString("false")));
        }
    }

}

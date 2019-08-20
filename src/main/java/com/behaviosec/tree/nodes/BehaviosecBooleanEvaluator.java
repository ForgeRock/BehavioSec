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

import com.behaviosec.tree.restclient.BehavioSecReport;
import com.behaviosec.tree.config.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A node that checks to see if zero-page login headers have specified username and whether that username is in a group
 * permitted to use zero-page login headers.
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class,
        configClass = BehaviosecBooleanEvaluator.Config.class)
public class BehaviosecBooleanEvaluator extends AbstractDecisionNode {
    private static final String TAG = BehaviosecBooleanEvaluator.class.getName();
    private final Logger logger = LoggerFactory.getLogger(TAG);

    /**
     * Configuration for the node.
     */
    public interface Config {
        /**
         * Minimum score to to accept
         * @return the amount.
         */
        @Attribute(order = 100)
        default boolean allowRemoteAccess() {
            return false;
        }
        /**
         * Minimum score to to accept
         * @return the amount.
         */
        @Attribute(order = 200)
        default boolean allowReplay() {
            return false;
        }

        /**
         * Minimum score to to accept
         * @return the amount.
         */
        @Attribute(order = 300)
        default boolean allowBoot() {
            return false;
        }
        /**
         * Minimum score to to accept
         * @return the amount.
         */
        @Attribute(order = 400)
        default boolean allowTabAnomaly() {
            return true;
        }

        /**
         * Minimum score to to accept
         * @return the amount.
         */
        @Attribute(order = 500)
        default boolean allowNumpadAnomaly() {
            return true;
        }

        /**
         * Minimum score to to accept
         * @return the amount.
         */
        @Attribute(order = 600)
        default boolean allowDeviceChanged() {
            return true;
        }

    }

    private final Config config;

    /**
     * Guice constructor.
     * @param config The node configuration.
     * @throws NodeProcessException If there is an error reading the configuration.
     */
    @Inject
    public BehaviosecBooleanEvaluator(@Assisted Config config) throws NodeProcessException {
        this.config = config;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        JsonValue behavioSenseResponse = context.sharedState.get(Constants.DATA_FIELD);
        ObjectMapper objectMapper = new ObjectMapper();
        logger.error(TAG + " getReport " + behavioSenseResponse.toString());

        BehavioSecReport bhsReport = null;
        try {
            bhsReport = objectMapper.readValue(behavioSenseResponse.asString(), BehavioSecReport.class);
        } catch (IOException e) {
            e.printStackTrace();
            return goTo(false).build();
        }
        boolean evaluation = bhsReport.isIsbot() != config.allowBoot()
                || bhsReport.isReplay() != config.allowReplay();

        return goTo(evaluation).build();


    }


    protected Action.ActionBuilder goTo(boolean outcome) {
        return Action.goTo(outcome ? Constants.TRUE_OUTCOME_ID : Constants.FALSE_OUTCOME_ID);
    }

    static final class OutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
        private static final String BUNDLE = BehaviosecAuthNode.class.getName().replace(".", "/");

        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE, BehaviosecAuthNode.OutcomeProvider.class.getClassLoader());
            return ImmutableList.of(
                    new Outcome(Constants.TRUE_OUTCOME_ID, bundle.getString("true")),
                    new Outcome(Constants.FALSE_OUTCOME_ID, bundle.getString("false")));
        }
    }
}
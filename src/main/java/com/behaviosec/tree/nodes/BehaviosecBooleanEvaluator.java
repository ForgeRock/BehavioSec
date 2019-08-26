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
import com.behaviosec.tree.restclient.BehavioSecReport;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.ResourceBundle;

//TODO Add explanation of node

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
        default boolean allowBot() {
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
        default boolean allowInTraining() {
            return true;
        }
        /**
         * Minimum score to to accept
         * @return the amount.
         */

        @Attribute(order = 400)
        default boolean allowRemoteAccess() {
            return true;
        }

        /**
         * Minimum score to to accept
         * @return the amount.
         */
        @Attribute(order = 500)
        default boolean allowTabAnomaly() {
            return true;
        }

        /**
         * Minimum score to to accept
         * @return the amount.
         */
        @Attribute(order = 600)
        default boolean allowNumpadAnomaly() {
            return true;
        }

        /**
         * Minimum score to to accept
         * @return the amount.
         */
        @Attribute(order = 700)
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
    public Action process(TreeContext context) {
        //TODO: when to through NodeProcessException?
        //Get report from sharedState
        //TODO Duplicate code with BehaioSecScoreEvaluator, refactor out
        List<Object> shared = context.sharedState.get(Constants.BEHAVIOSEC_REPORT).asList();
        if (shared == null) {
            logger.error("context.sharedState.get(Constants.BEHAVIOSEC_REPORT) is null");
            return goTo(false).build();
        }
        if (shared.size() != 1) {
            logger.error("context.sharedState.get(Constants.BEHAVIOSEC_REPORT) list is larger than one");
            return goTo(false).build();
        }
        BehavioSecReport bhsReport = (BehavioSecReport) shared.get(0);

        if ( bhsReport.isIsbot()) {
            logger.error("Fail on bhsReport.isIsbot() " + bhsReport.isIsbot() + " " + config.allowBot());
            return goTo(config.allowBot()).build();
        }
        if (bhsReport.isRemoteAccess()) {
            logger.error("Fail on bhsReport.isRemoteAccess() "  + bhsReport.isRemoteAccess() + " " + config.allowRemoteAccess());
            return goTo(config.allowRemoteAccess()).build();
        }

        if ( bhsReport.isReplay() ) {
            logger.error("Fail on bhsReport.isReplay() "  + bhsReport.isReplay() + " " + config.allowReplay());
            return goTo(config.allowReplay()).build();
        }

        if ( !bhsReport.isTrained() ) {
            logger.error("Fail on bhsReport.isTrained()  "  + bhsReport.isTrained() + " " + config.allowInTraining());
            return goTo(config.allowInTraining()).build();
        }

        if ( bhsReport.isTabAnomaly()) {
            logger.error("Fail on bhsReport.isTabAnomaly() "  + bhsReport.isTabAnomaly() + " " + config.allowTabAnomaly());
            return goTo(config.allowTabAnomaly()).build();
        }

        if ( bhsReport.isDeviceChanged() ) {
            logger.error("Fail on bhsReport.isDeviceChanged() "  + bhsReport.isDeviceChanged() + " " + config.allowDeviceChanged());
            return goTo(config.allowDeviceChanged()).build();
        }
        if ( bhsReport.isNumpadAnomaly() ) {
            logger.error("Fail on bhsReport.isNumpadAnomaly() "  + bhsReport.isNumpadAnomaly() + " " + config.allowNumpadAnomaly());
            return goTo(config.allowNumpadAnomaly()).build();
        }
        // All good allow passing through
        logger.error("All good, resuming to true outcome");
        return goTo(true).build();


    }


    protected Action.ActionBuilder goTo(boolean outcome) {
        return Action.goTo(outcome ? Constants.TRUE_OUTCOME_ID : Constants.FALSE_OUTCOME_ID);
    }
}
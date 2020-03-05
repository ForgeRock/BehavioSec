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

import com.behaviosec.isdk.config.NoBehavioSecReportException;
import com.behaviosec.isdk.entities.Report;
import com.behaviosec.isdk.evaluators.BooleanEvaluator;
import com.behaviosec.isdk.evaluators.ScoreEvaluator;
import com.behaviosec.tree.utils.Helper;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.AbstractDecisionNode;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.behaviosec.tree.config.Constants;
import com.google.inject.assistedinject.Assisted;

import javax.inject.Inject;



/**
 * A node that what evaluates boolean flags of BehavioSec response
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class,
        configClass = BehavioSecBooleanEvaluator.Config.class)
public class BehavioSecBooleanEvaluator extends AbstractDecisionNode {
    private static final String TAG = BehavioSecBooleanEvaluator.class.getName();
    private final Logger logger = LoggerFactory.getLogger(TAG);
    private final Config config;

    /**
     * Configuration for the node.
     */
    public interface Config {
        /**
         * Toggle Bot flagged profiles to evaluate to true
         * @return allow bot.
         */
        @Attribute(order = 100)
        default boolean allowBot() {
            return false;
        }

        /**
         * Toggle Allow replay flagged profiles to evaluate to true
         * @return allow replay.
         */
        @Attribute(order = 200)
        default boolean allowReplay() {
            return false;
        }

        /**
         * Toggle in Training flagged profiles to evaluate to true
         * @return allow in training.
         */
        @Attribute(order = 300)
        default boolean allowInTraining() {
            return false;
        }

        /**
         * Toggle Remote Access flagged profiles to evaluate to true
         *
         * @return allow remote access.
         */
        @Attribute(order = 400)
        default boolean allowRemoteAccess() {
            return false;
        }

        /**
         * Toggle Tab anomaly flagged profiles to evaluate to true
         *
         * @return allow tab anomaly.
         */
        @Attribute(order = 500)
        default boolean allowTabAnomaly() {
            return true;
        }

        /**
         *  Toggle numpad anomaly flagged profiles to evaluate to true
         *
         * @return allow num pad anomaly.
         */
        @Attribute(order = 600)
        default boolean allowNumpadAnomaly() {
            return true;
        }

        /**
         *  Toggle device changed flagged profiles to evaluate to true
         *
         * @return device changed.
         */
        @Attribute(order = 700)
        default boolean allowDeviceChanged() {
            return true;
        }

        /**
         *  Toggle IP changed flagged profiles to evaluate to true
         *
         * @return device changed.
         */
        @Attribute(order = 800)
        default boolean allowIpChange() {
            return true;
        }

        /**
         *  Toggle when copy paste detected toggle will evaluate to true
         *
         * @return device changed.
         */
        @Attribute(order = 900)
        default boolean allowCopyPaste() {
            return true;
        }
    }

    /**
     * Guice constructor.
     *
     * @param config The node configuration.
     * @throws NodeProcessException If there is an error reading the configuration.
     */
    @Inject
    public BehavioSecBooleanEvaluator(@Assisted Config config) throws NodeProcessException {
        this.config = config;
    }

    @Override
    public Action process(TreeContext context) {
        //Get report from sharedState
        Report bhsReport = null;
        try {
            bhsReport = Helper.getReportFromContext(context);

            BooleanEvaluator booleanEvaluatorEvaluator = new BooleanEvaluator();
            booleanEvaluatorEvaluator.config.setAllowBot(config.allowBot());
            booleanEvaluatorEvaluator.config.setAllowReplay(config.allowReplay());
            booleanEvaluatorEvaluator.config.setAllowInTraining(config.allowInTraining());
            booleanEvaluatorEvaluator.config.setAllowRemoteAccess(config.allowRemoteAccess());
            booleanEvaluatorEvaluator.config.setAllowTabAnomaly(config.allowTabAnomaly());
            booleanEvaluatorEvaluator.config.setAllowNumpadAnomaly(config.allowNumpadAnomaly());
            booleanEvaluatorEvaluator.config.setAllowDeviceChanged(config.allowDeviceChanged());
            booleanEvaluatorEvaluator.config.setAllowIPChange(config.allowIpChange());
            booleanEvaluatorEvaluator.config.setAllowCopyOrPaste(config.allowCopyPaste());
            return goTo(booleanEvaluatorEvaluator.evaluate(bhsReport)).build();

        } catch (NoBehavioSecReportException e) {
            logger.error(TAG + " " + e.getMessage());

        }
        return goTo(false).build();
    }


    protected Action.ActionBuilder goTo(boolean outcome) {
        return Action.goTo(outcome ? Constants.TRUE_OUTCOME_ID : Constants.FALSE_OUTCOME_ID);
    }
}
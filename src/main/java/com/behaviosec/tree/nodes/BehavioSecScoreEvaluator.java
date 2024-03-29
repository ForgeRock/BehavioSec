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
import com.behaviosec.isdk.evaluators.ScoreEvaluator;
import com.behaviosec.tree.config.Constants;
import com.behaviosec.isdk.utils.Debug;
import com.behaviosec.tree.utils.Helper;
import com.google.inject.assistedinject.Assisted;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * A node that checks to see if zero-page login headers have specified username and whether that username is in a group
 * permitted to use zero-page login headers.
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class,
        configClass = BehavioSecScoreEvaluator.Config.class, tags={"behavioral"})
public class BehavioSecScoreEvaluator extends AbstractDecisionNode {
    private static final String TAG = BehavioSecScoreEvaluator.class.getName();
    private final Logger logger = LoggerFactory.getLogger(TAG);
    private final Config config;

    /**
     * Configuration for the node.
     */
    public interface Config {
        /**
         * Minimum score to to accept
         *
         * @return the amount.
         */
        @Attribute(order = 100)
        default int minScore() {
            return Constants.MIN_SCORE;
        }

        /**
         * Minimum score to to accept
         *
         * @return the amount.
         */
        @Attribute(order = 200)
        default int minConfidence() {
            return Constants.MIN_CONFIDENCE;
        }

        /**
         * Maximum acceptable risk
         *
         * @return the amount.
         */
        @Attribute(order = 300)
        default int maxRisk() {
            return Constants.MAX_RISK;
        }

        /**
         * Deprecated
         *
         */
        @Attribute(order = 400)
        default boolean allowInTraining() {
            return true;
        }
    }

    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     */
    @Inject
    public BehavioSecScoreEvaluator(@Assisted Config config){
        this.config = config;
    }

    @Override
    public Action process(TreeContext context) {
        //Get report from sharedState
        Report bhsReport;
        try {
            bhsReport = Helper.getReportFromContext(context);
            ScoreEvaluator scoreEvaluator = new ScoreEvaluator();
            scoreEvaluator.config.setMinScore(config.minScore());
            scoreEvaluator.config.setMinConfidence(config.minConfidence());
            scoreEvaluator.config.setMaxRisk(config.maxRisk());
            scoreEvaluator.config.setAllowInTraining(config.allowInTraining());
            boolean evaluation = scoreEvaluator.evaluateInTraining(bhsReport, false);
            Debug.printDebugMesssage("evaluation BehavioSecScoreEvaluator = " + evaluation);
            return goTo(evaluation).build();
        } catch (NoBehavioSecReportException e) {
            logger.error(TAG + " " + e.getMessage());
        }
        return goTo(false).build();
    }


    protected Action.ActionBuilder goTo(boolean outcome) {
        return Action.goTo(outcome ? Constants.TRUE_OUTCOME_ID : Constants.FALSE_OUTCOME_ID);
    }
}

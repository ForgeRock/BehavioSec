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
import com.behaviosec.tree.utils.Helper;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.shared.debug.Debug;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A node that checks to see if zero-page login headers have specified username and whether that username is in a group
 * permitted to use zero-page login headers.
 */
@Node.Metadata(outcomeProvider = BehavioSecPolicyEvaluator.BehavioSecPolicyOutcomeProvider.class,
        configClass = BehavioSecPolicyEvaluator.Config.class)
public class BehavioSecPolicyEvaluator extends AbstractDecisionNode {
    private final static String DEBUG_NAME = "BehavioSecPolicyEvaluator";
    private final static Debug debug = Debug.getInstance(DEBUG_NAME);
    private final Config config;

    /**
     * Configuration for the node.
     */
    public interface Config { }

    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     * @throws NodeProcessException If the configuration was not valid.
     */
    @Inject
    public BehavioSecPolicyEvaluator(@Assisted BehavioSecPolicyEvaluator.Config config) {
        this.config = config;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException{
        //Get report from sharedState
        Report bhsReport = null;
        try {
            bhsReport = Helper.getReportFromContext(context);
            return goTo(bhsReport.getPolicy()).build();

        } catch (NoBehavioSecReportException e) {
            debug.error( e.getMessage());
            throw  new NodeProcessException("Wasn't able to get policy outcome " + e.toString());
        }
    }


    protected Action.ActionBuilder goTo(String outcome) {
        return Action.goTo(outcome);
    }
    public static class BehavioSecPolicyOutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            return ImmutableList.of(
                    new Outcome("Red", "Red"),
                    new Outcome("Yellow", "Yellow"),
                    new Outcome("Green", "Green"),
                    new Outcome("Unexpected", "Unexpected"),
                    new Outcome("Training", "Training")
            );
        }
    }

}
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
import com.behaviosec.isdk.utils.Debug;
import com.behaviosec.tree.utils.Helper;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;

/**
 * A node that send request to BehavioSense endpoint. Node expect to find behavior data in shared context
 */
@Node.Metadata(outcomeProvider = BehavioSecInTrainingTestNode.BehavioSecInTrainingTestOutcomeProvider.class,
        configClass = BehavioSecInTrainingTestNode.Config.class, tags={"behavioral"})
public class BehavioSecInTrainingTestNode implements Node {

    private static final String TAG = BehavioSecInTrainingTestNode.class.getName();
    private static final Logger logger = LoggerFactory.getLogger(TAG);

    private final Config config;

    /**
     * Configuration for the node.
     */
    public interface Config {
    }

    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     */
    @Inject
    public BehavioSecInTrainingTestNode(@Assisted Config config) {
        this.config = config;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {


        String sContext = context.toString();

        InTrainingOutcome outcome = InTrainingOutcome.ERROR;

        Report bhsReport;
        try {
            bhsReport = Helper.getReportFromContext(context);
            if (bhsReport.isTrained()) {
                Debug.printDebugMesssage(TAG + " returning " + InTrainingOutcome.TRAINED_OUTCOME + " outcome");
                outcome = InTrainingOutcome.TRAINED_OUTCOME;
            } else {
                Debug.printDebugMesssage(TAG + " returning " + InTrainingOutcome.NOT_TRAINED_OUTCOME + " outcome");
                outcome = InTrainingOutcome.NOT_TRAINED_OUTCOME;
            }
            return goTo(outcome).build();
        } catch (NoBehavioSecReportException e) {
            logger.error(TAG + " " + e.getMessage());
        }
        Debug.printDebugMesssage(TAG + " returning " + InTrainingOutcome.ERROR + "outcome");
        return goTo(outcome).build();
    }

    private Action.ActionBuilder goTo(InTrainingOutcome outcome) {
        return Action.goTo(outcome.name());
    }

    /**
     * The possible outcomes for the BehavioSecNAFNode1.
     */
    public enum InTrainingOutcome {
        TRAINED_OUTCOME,
        NOT_TRAINED_OUTCOME,
        ERROR
    }

    /**
     * Defines the possible outcomes from this node.
     */
    public static class BehavioSecInTrainingTestOutcomeProvider implements OutcomeProvider {
        private static final String BUNDLE = BehavioSecInTrainingTestNode.class.getName().replace(".", "/");
        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE, OutcomeProvider.class.getClassLoader());
            return ImmutableList.of(
                    new Outcome(InTrainingOutcome.TRAINED_OUTCOME.name(), bundle.getString("trainedOutcome")),
                    new Outcome(InTrainingOutcome.NOT_TRAINED_OUTCOME.name(), bundle.getString("notTrainedOutcome")),
                    new Outcome(InTrainingOutcome.ERROR.name(), bundle.getString("errorOutcome")));
        }
    }
}

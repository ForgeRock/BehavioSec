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

import static org.forgerock.openam.auth.node.api.Action.send;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.SingleOutcomeNode;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.behaviosec.tree.config.Constants;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;
import javax.inject.Inject;
import javax.security.auth.callback.Callback;

/**
 * A node that checks to see if zero-page login headers have specified username and whether that username is in a group
 * permitted to use zero-page login headers.
 */
@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class,
        configClass = BehavioSecCollector.Config.class)
public class BehavioSecCollector extends SingleOutcomeNode {
    private static final String TAG = BehavioSecCollector.class.getName();
    //TODO Not logging anything in this class, either remove or add log statments
    private final Logger logger = LoggerFactory.getLogger(TAG);
    private final Config config;

    /**
     * Configuration for the node.
     */
    public interface Config {
        /**
         * The amount to increment/decrement the auth level.
         *
         * @return the amount.
         */
        @Attribute(order = 10)
        default String fileName() {
            return Constants.COLLECTOR_SCRIPT;
        }
    }

    /**
     * Guice constructor.
     *
     * @param config The node configuration.
     */
    @Inject
    public BehavioSecCollector(@Assisted Config config) {
        this.config = config;
    }

    private static String createClientSideScriptExecutorFunction(String script) {
        return String.format(
                "(function(output) {\n" +
                        "    %s\n" + // script
                        "}) (document);\n",
                script
        );
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {


        String myScript = getScriptAsString(config.fileName(), Constants.DATA_FIELD);

        Optional<String> result = context.getCallback(HiddenValueCallback.class).map(HiddenValueCallback::getValue).
                filter(scriptOutput -> !Strings.isNullOrEmpty(scriptOutput));
        if (result.isPresent() && !Constants.DATA_FIELD.equals(result.get())) {
            String resultValue = result.get();
            if ("undefined".equalsIgnoreCase(resultValue)) {
                resultValue = "Not set";
            }
            JsonValue newSharedState = context.sharedState.copy();
            newSharedState.put(Constants.DATA_FIELD, resultValue);
            return goToNext().replaceSharedState(newSharedState).build();
        } else {
            if (result.isPresent()) {
                return goToNext().build();
            }
            String clientSideScriptExecutorFunction = createClientSideScriptExecutorFunction(myScript);


            ScriptTextOutputCallback scriptAndSelfSubmitCallback =
                    new ScriptTextOutputCallback(clientSideScriptExecutorFunction);

            HiddenValueCallback hiddenValueCallback = new HiddenValueCallback(Constants.DATA_FIELD, "false");
            ImmutableList<Callback> callbacks = ImmutableList.of(scriptAndSelfSubmitCallback, hiddenValueCallback);
            return send(callbacks).build();
        }
    }

    private String getScriptAsString(String filename, String outputParameterId) {
        try {
            Reader paramReader = new InputStreamReader(getClass().getResourceAsStream(filename));

            StringBuilder data = new StringBuilder();
            BufferedReader objReader = new BufferedReader(paramReader);
            String strCurrentLine;
            while ((strCurrentLine = objReader.readLine()) != null) {
                data.append(strCurrentLine).append(System.lineSeparator());
            }
            return String.format(data.toString(), outputParameterId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
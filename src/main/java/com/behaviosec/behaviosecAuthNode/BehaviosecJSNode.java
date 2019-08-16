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

import com.behaviosec.utils.Consts;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Optional;

import static org.forgerock.openam.auth.node.api.Action.send;

/**
 * A node that checks to see if zero-page login headers have specified username and whether that username is in a group
 * permitted to use zero-page login headers.
 */
@Node.Metadata(outcomeProvider  = SingleOutcomeNode.OutcomeProvider.class,
        configClass      = BehaviosecJSNode.Config.class)
public class BehaviosecJSNode extends SingleOutcomeNode {
    private static final String TAG = BehaviosecJSNode.class.getName();
    private final Logger logger = LoggerFactory.getLogger(TAG);

    /**
     * Configuration for the node.
     */
    public interface Config {
        /**
         * The amount to increment/decrement the auth level.
         * @return the amount.
         */
        @Attribute(order = 10)
        default String fileName() {
            return Consts.COLLECTOR_SCRIPT;
        }
    }

    private final Config config;

    /**
     * Guice constructor.
     * @param config The node configuration.
     * @throws NodeProcessException If there is an error reading the configuration.
     */
    @Inject
    public BehaviosecJSNode(@Assisted Config config) throws NodeProcessException {
        this.config = config;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        String deb = "";
        List<? extends Callback>cb = context.getAllCallbacks();
        for (int i = 0; i < cb.size(); i++) {
            Callback c = cb.get(i);
            deb += c.toString() + " " ;
        }

        String scriptResult = Consts.DATA_FIELD;
        logger.error("1 - Processing script " + config.fileName() + ":" + context.toString() + "::" + deb);
        logger.error("    config.scriptResult() = " + scriptResult);
        String myScript = getScriptAsString(config.fileName(), scriptResult);

        Optional<String> result = context.getCallback(HiddenValueCallback.class).map(HiddenValueCallback::getValue).
                filter(scriptOutput -> !Strings.isNullOrEmpty(scriptOutput));
        logger.error("2 - Result = " + result);
        if (result.isPresent() && !scriptResult.equals(result.get())) {
            logger.error("3 - Result is present -> " + result.get());
            String resultValue = result.get();
            if ("undefined".equalsIgnoreCase(resultValue)) {
                resultValue = "Not set";
            }
            JsonValue newSharedState = context.sharedState.copy();
            logger.error("4 - newSharedState -> " + newSharedState);
            logger.error("Adding result to \"" + Consts.DATA_FIELD + "\"");
            newSharedState.put(Consts.DATA_FIELD, resultValue);
            logger.error("5 - newSharedState -> " + newSharedState);
            return goToNext().replaceSharedState(newSharedState).build();
        } else {
            if (result.isPresent() && scriptResult.equals(result.get())) {
                logger.error("6 doing nothing??");
                return goToNext().build();
            }
            logger.error("8 - Result not present yet");
            logger.error("9 - context.sharedState.toString() -> " + context.sharedState.toString());
            String clientSideScriptExecutorFunction = createClientSideScriptExecutorFunction(myScript ,
                    Consts.DATA_FIELD, true, context.sharedState.toString());

            logger.error("\n\n\n" + clientSideScriptExecutorFunction + "\n\n\n");

            ScriptTextOutputCallback scriptAndSelfSubmitCallback =
//                    new ScriptTextOutputCallback(myScript);
                    new ScriptTextOutputCallback(clientSideScriptExecutorFunction);

//            HiddenValueCallback hiddenValueCallback = new HiddenValueCallback(config.scriptResult());
            HiddenValueCallback hiddenValueCallback = new HiddenValueCallback(Consts.DATA_FIELD, "false");
            logger.error("10 - hiddenValueCallback -> " + hiddenValueCallback);

            ImmutableList<Callback> callbacks = ImmutableList.of(scriptAndSelfSubmitCallback, hiddenValueCallback);
            logger.error("11 - callbacks -> " + callbacks.toString());

            return send(callbacks).build();
        }
    }

    public String getScriptAsString(String filename, String outputParameterId) {
        logger.error("getScriptAsString: Filename " + filename);
        try {
            Reader paramReader = new InputStreamReader(getClass().getResourceAsStream(filename));

            String data = new String();
            BufferedReader objReader = new BufferedReader(paramReader);
            String strCurrentLine;
            while ((strCurrentLine = objReader.readLine()) != null) {
                data += strCurrentLine + System.lineSeparator();
            }
            return String.format(data, outputParameterId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String createClientSideScriptExecutorFunction(String script, String outputParameterId,
                                                                boolean clientSideScriptEnabled, String context) {

        return String.format(
//                spinningWheelScript +
                "(function(output) {\n" +
//                        "    var autoSubmitDelay = 0,\n" +
//                        "        submitted = false,\n" +
//                        "        context = %s;\n" + //injecting context in form of JSON
//                        "    function submit1() {\n" +
//                        "        console.log(\"submitted = \" + submitted)\n" +
//                        "        if (submitted) {\n" +
//                        "            return;\n" +
//                        "        }" +
//                        "        if (!(typeof $ == 'function')) {\n" + // Crude detection to see if XUI is not present.
//                        "            document.getElementById('loginButton_0').click();\n" +
//                        "        } else {\n" +
//                        "            $('input[type=submit]').click();\n" +
//                        "        }\n" +
//                        "        submitted = true;\n" +
//                        "    }\n" +
                        "    %s\n" + // script
//                        "    setTimeout(submit1, autoSubmitDelay);\n" +
                        "}) (document);\n", // outputParameterId
                script
                );
    }
}
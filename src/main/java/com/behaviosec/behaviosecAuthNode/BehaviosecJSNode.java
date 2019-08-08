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

import static org.forgerock.openam.auth.node.api.Action.send;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.SingleOutcomeNode;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;

/**
 * A node that checks to see if zero-page login headers have specified username and whether that username is in a group
 * permitted to use zero-page login headers.
 */
@Node.Metadata(outcomeProvider  = SingleOutcomeNode.OutcomeProvider.class,
               configClass      = BehaviosecJSNode.Config.class)
public class BehaviosecJSNode extends SingleOutcomeNode {
    private final Logger logger = LoggerFactory.getLogger("com.behaviosec");

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
            return "behaviosec.js";
        }
 
        @Attribute(order = 20)
        default String scriptResult() {
        	return "behaviosec";
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
     	String myScript = getScriptAsString(config.fileName());
    	
    	logger.error("Processing script " + config.fileName());
    	
        Optional<String> result = context.getCallback(HiddenValueCallback.class).map(HiddenValueCallback::getValue).filter(scriptOutput -> !Strings.isNullOrEmpty(scriptOutput));
        logger.error("Result = " + result);
        if (result.isPresent()) {
        	logger.error("Result is present");
            JsonValue newSharedState = context.sharedState.copy();
            newSharedState.put(config.scriptResult(), result.get());
            return goToNext().replaceSharedState(newSharedState).build();
        } else { 
        	logger.error("Result not present yet");
        	String clientSideScriptExecutorFunction = createClientSideScriptExecutorFunction(myScript , 
       			config.scriptResult(), true, context.sharedState.toString());
            ScriptTextOutputCallback scriptAndSelfSubmitCallback =
                    new ScriptTextOutputCallback(clientSideScriptExecutorFunction);

            HiddenValueCallback hiddenValueCallback = new HiddenValueCallback(config.scriptResult());

            ImmutableList<Callback> callbacks = ImmutableList.of(scriptAndSelfSubmitCallback, hiddenValueCallback);

            return send(callbacks).build();
       } 
    }
    
    public String getScriptAsString(String filename) {
    	logger.error("getScriptAsString: Filename " + filename);
    	if (filename == null) {
    		filename = "behaviosec.js";
    	}
		try {
			Reader paramReader = new InputStreamReader(getClass().getResourceAsStream(filename));

			String data = new String();
			BufferedReader objReader = new BufferedReader(paramReader);
			String strCurrentLine;
			while ((strCurrentLine = objReader.readLine()) != null) {
				data += strCurrentLine;
			}
	    	return data;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
    }

    public static String createClientSideScriptExecutorFunction(String script, String outputParameterId,
            boolean clientSideScriptEnabled, String context) {
        String collectingDataMessage = "";
        if (clientSideScriptEnabled) {
            collectingDataMessage = "messenger.messages.addMessage( message );\n";
        }

        String spinningWheelScript = "if (window.require) {\n" +
                "    var messenger = require(\"org/forgerock/commons/ui/common/components/Messages\"),\n" +
                "        spinner =  require(\"org/forgerock/commons/ui/common/main/SpinnerManager\"),\n" +
                "        message =  {message:\"Collecting Data...\", type:\"info\"};\n" +
                "    spinner.showSpinner();\n" +
                collectingDataMessage +
                "}";

        return String.format(
                spinningWheelScript +
                        "(function(output) {\n" +
                        "    var autoSubmitDelay = 0,\n" +
                        "        submitted = false,\n" +
                        "        context = %s;\n" + //injecting context in form of JSON
                        "    function submit() {\n" +
                        "        if (submitted) {\n" +
                        "            return;\n" +
                        "        }" +
                        "        if (!(typeof $ == 'function')) {\n" + // Crude detection to see if XUI is not present.
                        "            document.getElementById('loginButton_0').click();\n" +
                        "        } else {\n" +
                        "            $('input[type=submit]').click();\n" +
                        "        }\n" +
                        "        submitted = true;\n" +
                        "    }\n" +
                        "    %s\n" + // script
                        "    setTimeout(submit, autoSubmitDelay);\n" +
                        "}) (document.forms[0].elements['%s']);\n", // outputParameterId
                context,
                script,
                outputParameterId);
    }
}
package com.behaviosec.tree.nodes;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;
import static org.mockito.MockitoAnnotations.initMocks;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.behaviosec.tree.config.Constants;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;

import java.util.Collections;
import java.util.List;
import javax.security.auth.callback.Callback;


@Test
public class BehaviosecCollectorTest {


    private TestConfig config = new TestConfig();

    @BeforeMethod
    public void before() {
        initMocks(this);

    }

    @Test
    public void testProcessWithNoCallbacksInCaseOfMobile() {
        try {
            BehavioSecCollector node = new BehavioSecCollector(config);
            JsonValue sharedState = json(object(1));

            //WHEN
            Action result = node.process(getContext(sharedState,
                                                    emptyList()));
            //THEN
            assertThat(result.outcome).isEqualTo(null);
            assertThat(result.callbacks).hasSize(2);
            assertThat(result.callbacks.get(1)).isInstanceOf(HiddenValueCallback.class);
            assertThat(result.callbacks.get(0)).isInstanceOf(ScriptTextOutputCallback.class);
            assertThat((Object) result.sharedState).isNull();
        } catch (NodeProcessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testProcessWithCallbacksInCaseOfMobile() {
        try {
            BehavioSecCollector node = new BehavioSecCollector(config);
            JsonValue sharedState = json(object(1));
            HiddenValueCallback hiddenValueCallback = new HiddenValueCallback(Constants.DATA_FIELD, "false");
            hiddenValueCallback.setValue("================================================");
            //WHEN
            Action result = node.process(getContext(sharedState, Collections.singletonList(hiddenValueCallback)));
            //THEN
            assertThat(result.outcome).isEqualTo("outcome");
            assertThat(result.callbacks).isEmpty();
        } catch (NodeProcessException e) {
            e.printStackTrace();
        }
    }

    private TreeContext getContext(JsonValue sharedState, List<? extends Callback> callbacks) {
        return new TreeContext(sharedState, new ExternalRequestContext.Builder().build(), callbacks);
    }

    static class TestConfig implements BehavioSecCollector.Config {
        TestConfig() {

        }

        public String fileName() {
            return Constants.COLLECTOR_SCRIPT;
        }
    }

}
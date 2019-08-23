package com.behaviosec.tree.nodes;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.MockitoAnnotations.initMocks;
import static java.util.Collections.emptyList;

import com.behaviosec.tree.config.Constants;
import com.behaviosec.tree.restclient.BehavioSecReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mockito.stubbing.Answer;

import javax.security.auth.callback.Callback;
import java.io.File;
import java.io.IOException;
import java.util.*;


@Test
public class BehaviosecCollectorTest {


    class TestConfig implements BehaviosecCollector.Config {
        public TestConfig(){

        }
        public String fileName() {
            return Constants.COLLECTOR_SCRIPT;
        }
    }

    private TestConfig config = new TestConfig();

    @BeforeMethod
    public void before() {
        initMocks(this);

    }
    @Test
    public void testProcessWithNoCallbacksInCaseOfMobile() {
        try {
            BehaviosecCollector node = new BehaviosecCollector(config);
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
            BehaviosecCollector node = new BehaviosecCollector(config);
            JsonValue sharedState = json(object(1));
            List<Callback> cbList = new ArrayList<>();
            HiddenValueCallback hiddenValueCallback = new HiddenValueCallback(Constants.DATA_FIELD, "false");
            hiddenValueCallback.setValue("================================================");
            //WHEN
            Action result = node.process(getContext(sharedState,asList(hiddenValueCallback)));
            //THEN
            assertThat(result.outcome).isEqualTo("outcome");
            assertThat(result.callbacks.isEmpty());
//            assertThat(sharedState).isObject().contains(entry(Constants.DATA_FIELD, "================================================"));
        } catch (NodeProcessException e) {
            e.printStackTrace();
        }
    }

    private TreeContext getContext(JsonValue sharedState, List<? extends Callback> callbacks) {
        return new TreeContext(sharedState, new ExternalRequestContext.Builder().build(), callbacks);
    }

}
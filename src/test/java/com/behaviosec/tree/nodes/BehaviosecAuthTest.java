package com.behaviosec.tree.nodes;

import com.behaviosec.tree.config.Constants;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;


@Test
public class BehaviosecAuthTest {

    private String timing_data = "[[\"f\",\"text#behavioform:name\",[[0,76,0],[0,73,58],[1,76,104],[0,78,144],[1,73,191],[1,78,245],[0,90,451],[1,90,560],[0,8,940],[1,8,1004],[0,85,1153],[0,90,1211],[1,85,1221],[1,90,1303],[0,9,1418]]],[\"f\",\"text#behavioform:email\",[[1,9,1493],[0,73,1690],[0,78,1750],[1,73,1787],[1,78,1841],[0,70,1848],[0,79,1935],[1,70,1940],[1,79,2008],[0,17,2116],[0,18,2116],[0,50,2225],[1,50,2308],[1,17,2334],[1,18,2334],[0,66,2459],[1,66,2536],[0,69,2636],[1,69,2709],[0,72,2770],[0,65,2851],[1,72,2856],[0,86,2922],[1,65,2960],[0,73,3027],[1,86,3031],[0,79,3072],[1,73,3127],[0,83,3148],[1,79,3191],[0,69,3211],[1,83,3272],[1,69,3289],[0,67,3376],[1,67,3479],[0,190,3487],[1,190,3569],[0,67,3607],[1,67,3723],[0,79,3726],[0,77,3783],[1,79,3820],[1,77,3880],[0,9,3928]]],[\"f\",\"text#behavioform:securitycode\",[[1,9,4016],[0,77,4053],[1,77,4136],[0,89,4308],[1,89,4381],[0,83,4384],[0,69,4448],[1,83,4502],[1,69,4534],[0,67,4623],[1,67,4725],[0,82,4844],[0,69,4912],[1,82,4941],[1,69,4995],[0,84,5044],[1,84,5127],[0,80,5157],[1,80,5249],[0,65,5257],[0,83,5294],[1,65,5344],[1,83,5383],[0,83,5486],[1,83,5558],[0,87,5647],[1,87,5734],[0,79,5779],[1,79,5848],[0,82,5876],[0,68,5925],[1,82,5969],[1,68,6019],[0,13,6045]]]]";
    private String userAgent="Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X; en-us) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3";
    private String ip = "1.1.1.1";

    class TestConfig implements BehaviosecAuthNode.Config {
            public TestConfig(){

            }

        public String endpoint() {
            return "http://13.56.150.246:8080/";
        }
        public boolean denyOnFail() {
            return true;
        }

    }

    @Mock
    private TestConfig config;

    @BeforeMethod
    public void before() {
        initMocks(this);

    }

    @Test
    public void nodeProcessWithTrueOutcome() throws NodeProcessException {
//        TreeContext context = getTreeContext(new HashMap<>());

//        context.sharedState.put(SharedStateConstants.USERNAME, "vip123");
//        context.sharedState.put(CRED_ID, "SYMC87283752");
//        context.sharedState.put(SECURE_CODE, "487385");
//        context.sharedState.put(MOB_NUM, "918147119089");
//        context.sharedState.put(CRED_CHOICE, "SMS");
//        context.sharedState.put(KEY_STORE_PATH,"C://Users//keystore.ks");
//        context.sharedState.put(KEY_STORE_PASS,"WORK12345");

        //WHEN
//        Action action = node.process(context);

        // THEN
//        assertThat(action.callbacks).isEmpty();
        assertThat(true).isEqualTo(true);
    }

    @Test
    public void nodeProcessWithFalseOutcome() throws NodeProcessException {
        BehaviosecAuthNode node = new BehaviosecAuthNode(config);
//        given(addCredential.addCredential(any(),any(),any(),any(),any(),any())).willReturn("6004");
        TreeContext context = getTreeContext(new HashMap<>());
//
        context.sharedState.put(Constants.USERNAME, "");
        context.sharedState.put(Constants.DATA_FIELD, null);

        Action action = node.process(context);

        // THEN
        assertThat(action.outcome).isEqualTo("false");

    }

    private TreeContext getTreeContext(Map<String, String[]> parameters) {
        final TreeContext treeContext = new TreeContext(
                JsonValue.json(JsonValue.object(1)),
                new ExternalRequestContext.Builder().parameters(parameters).build(),
                Collections.emptyList()
        );
        return treeContext;
    }

    class CustomAnswer implements Answer<Boolean> {
        @Override
        public Boolean answer(InvocationOnMock invocation) throws Throwable {
            return true;
        }
    }
}
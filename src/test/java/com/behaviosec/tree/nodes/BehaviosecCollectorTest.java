package com.behaviosec.tree.nodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.json.JsonValue.object;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.MockitoAnnotations.initMocks;

@Test
public class BehaviosecCollectorTest {

    @BeforeMethod
    public void before() {
        node = null;
        initMocks(this);

    }

    @Test
    public void nodeProcessWithTrueOutcome() throws NodeProcessException {
        given(addCredential.addCredential(any(),any(),any(),any(),any(),any())).willReturn("0000");
        TreeContext context = getTreeContext(new HashMap<>());

        context.sharedState.put(SharedStateConstants.USERNAME, "vip123");
        context.sharedState.put(CRED_ID, "SYMC87283752");
        context.sharedState.put(SECURE_CODE, "487385");
        context.sharedState.put(MOB_NUM, "918147119089");
        context.sharedState.put(CRED_CHOICE, "SMS");
        context.sharedState.put(KEY_STORE_PATH,"C://Users//keystore.ks");
        context.sharedState.put(KEY_STORE_PASS,"WORK12345");

        //WHEN
        Action action = node.process(context);

        // THEN
        assertThat(action.callbacks).isEmpty();
        assertThat(action.outcome).isEqualTo("TRUE");

    }
    @Test
    public void nodeProcessWithFalseOutcome() throws NodeProcessException {
        given(addCredential.addCredential(any(),any(),any(),any(),any(),any())).willReturn("6004");
        TreeContext context = getTreeContext(new HashMap<>());

        context.sharedState.put(SharedStateConstants.USERNAME, "vip123");
        context.sharedState.put(CRED_ID, "SYMC87283752");
        context.sharedState.put(SECURE_CODE, "487385");
        context.sharedState.put(MOB_NUM, "918147119089");
        context.sharedState.put(CRED_CHOICE, "SMS");
        context.sharedState.put(KEY_STORE_PATH,"C://Users//keystore.ks");
        context.sharedState.put(KEY_STORE_PASS,"WORK12345");

        //WHEN
        Action action = node.process(context);

        // THEN
        assertThat(action.callbacks).isEmpty();
        assertThat(action.outcome).isEqualTo("FALSE");

    }

    private TreeContext getTreeContext(Map<String, String[]> parameters) {
        return new TreeContext(JsonValue.json(object(1)),
                new ExternalRequestContext.Builder().parameters(parameters).build(), emptyList());
    }

    class CustomAnswer implements Answer<Boolean> {
        @Override
        public Boolean answer(InvocationOnMock invocation) throws Throwable {
            return true;
        }
    }
}
package com.behaviosec.tree.nodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.behaviosec.tree.config.Constants;
import com.behaviosec.tree.restclient.BehavioSecReport;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Test
public class BehaviosSecBooleanEvaluatorTest {


    @BeforeMethod
    public void before() {
        initMocks(this);

    }

    @Test
    public void nodeProcessCleanTrueOutcome() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        TestConfig config = new TestConfig();
        BehaviosecBooleanEvaluator node = new BehaviosecBooleanEvaluator(config);
        BehavioSecReport bre = getReport();

        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        //WHEN
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("true");
    }

    @Test
    public void nodeProcessIsBotFalseOutcome() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        TestConfig config = new TestConfig();
        BehaviosecBooleanEvaluator node = new BehaviosecBooleanEvaluator(config);
        BehavioSecReport bre = getReport();
        bre.setIsbot(true);

        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        //WHEN
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("false");
    }

    @Test
    public void nodeProcessIsBotTrueOutcome() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        TestConfig config = new TestConfig();
        config.setAllowBot();
        BehaviosecBooleanEvaluator node = new BehaviosecBooleanEvaluator(config);
        BehavioSecReport bre = getReport();
        bre.setIsbot(true);

        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        //WHEN
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("true");
    }

    /**
     * Test replay
     *
     * @throws NodeProcessException
     */
    @Test
    public void nodeProcessReplayFalseOutcome() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        TestConfig config = new TestConfig();
        BehaviosecBooleanEvaluator node = new BehaviosecBooleanEvaluator(config);
        BehavioSecReport bre = getReport();
        bre.setReplay(true);

        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        //WHEN
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("false");
    }

    @Test
    public void nodeProcessReplayTrueOutcome() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        TestConfig config = new TestConfig();
        config.setAllowReplay(true);
        BehaviosecBooleanEvaluator node = new BehaviosecBooleanEvaluator(config);
        BehavioSecReport bre = getReport();
        bre.setReplay(true);

        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        //WHEN
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("true");
    }

    /**
     * Test remote access
     *
     * @throws NodeProcessException
     */
    @Test
    public void nodeProcessRemoteAccessFalseOutcome() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        TestConfig config = new TestConfig();
        config.setAllowRemoteAccess(false);
        BehaviosecBooleanEvaluator node = new BehaviosecBooleanEvaluator(config);
        BehavioSecReport bre = getReport();
        bre.setRemoteAccess(true);

        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        //WHEN
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("false");
    }

    @Test
    public void nodeProcessRemoteAccessTrueOutcome() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        TestConfig config = new TestConfig();
        BehaviosecBooleanEvaluator node = new BehaviosecBooleanEvaluator(config);
        BehavioSecReport bre = getReport();
        bre.setRemoteAccess(true);

        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        //WHEN
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("true");
    }

    /**
     * allowTabAnomaly
     *
     * @throws NodeProcessException
     */

    @Test
    public void nodeProcessAllowTabAnomalyFalseOutcome() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        TestConfig config = new TestConfig();
        config.setAllowTabAnomaly(false);
        BehaviosecBooleanEvaluator node = new BehaviosecBooleanEvaluator(config);
        BehavioSecReport bre = getReport();
        bre.setIsTabAnomaly(true);

        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        //WHEN
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("false");
    }

    @Test
    public void nodeProcessAllowTabAnomalyTrueOutcome() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        TestConfig config = new TestConfig();
        BehaviosecBooleanEvaluator node = new BehaviosecBooleanEvaluator(config);
        BehavioSecReport bre = getReport();
        bre.setIsTabAnomaly(true);

        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        //WHEN
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("true");
    }

    /**
     * allowNumpadAnomaly
     *
     * @throws NodeProcessException
     */
    @Test
    public void nodeProcessAllowNumpadAnomalyFalseOutcome() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        TestConfig config = new TestConfig();
        config.setAllowNumpadAnomaly(false);
        BehaviosecBooleanEvaluator node = new BehaviosecBooleanEvaluator(config);
        BehavioSecReport bre = getReport();
        bre.setIsNumpadAnomaly(true);

        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        //WHEN
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("false");
    }

    @Test
    public void nodeProcessAllowNumpadAnomalyTrueOutcome() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        TestConfig config = new TestConfig();
        BehaviosecBooleanEvaluator node = new BehaviosecBooleanEvaluator(config);
        BehavioSecReport bre = getReport();
        bre.setIsNumpadAnomaly(true);
        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        //WHEN
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("true");
    }

    /**
     * allowDeviceChanged
     *
     * @throws NodeProcessException
     */
    @Test
    public void nodeProcessAllowDeviceChangedFalseOutcome() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        TestConfig config = new TestConfig();
        config.setAllowDeviceChanged(false);
        BehaviosecBooleanEvaluator node = new BehaviosecBooleanEvaluator(config);
        BehavioSecReport bre = getReport();
        bre.setDeviceChanged(true);

        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        //WHEN
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("false");
    }

    @Test
    public void nodeProcessAllowDeviceChangedTrueOutcome() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        TestConfig config = new TestConfig();
        BehaviosecBooleanEvaluator node = new BehaviosecBooleanEvaluator(config);
        BehavioSecReport bre = getReport();
        bre.setDeviceChanged(true);

        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        //WHEN
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("true");
    }

    /**
     * No report
     *
     * @throws NodeProcessException
     */
    @Test
    public void nodeProcessNoReportOutcomeFalse() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        TestConfig config = new TestConfig();
        BehaviosecBooleanEvaluator node = new BehaviosecBooleanEvaluator(config);
        // test no bhs repport
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("false");
    }

    private BehavioSecReport getReport() {
        ObjectMapper mapper = new ObjectMapper();
        BehavioSecReport bre = null;
        try {
            bre = mapper.readValue(new File("src/test/java/com/behaviosec/tree/nodes/sample_return" +
                                                    ".json"),
                                   BehavioSecReport.class);
            bre.setScore(0.9);
            bre.setConfidence(0.9);
            bre.setRisk(0.2);
            bre.setIsTrained(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bre;
    }

    private TreeContext getTreeContext(Map<String, String[]> parameters) {
        return new TreeContext(
                JsonValue.json(JsonValue.object(1)),
                new ExternalRequestContext.Builder().parameters(parameters).build(),
                Collections.emptyList()
        );
    }

    static class TestConfig implements BehaviosecBooleanEvaluator.Config {
        boolean allowBot = false;
        boolean allowReplay = false;
        boolean allowInTraining = true;
        boolean allowRemoteAccess = true;
        boolean allowTabAnomaly = true;
        boolean allowNumpadAnomaly = true;
        boolean allowDeviceChanged = true;
        TestConfig() {

        }

        public boolean allowBot() {
            return allowBot;
        }

        public boolean allowReplay() {
            return allowReplay;
        }

        public boolean allowInTraining() {
            return allowInTraining;
        }

        public boolean allowRemoteAccess() {
            return allowRemoteAccess;
        }

        public boolean allowTabAnomaly() {
            return allowTabAnomaly;
        }

        public boolean allowNumpadAnomaly() {
            return allowNumpadAnomaly;
        }

        public boolean allowDeviceChanged() {
            return allowDeviceChanged;
        }

        void setAllowBot() {
            this.allowBot = true;
        }

        void setAllowReplay(boolean allowReplay) {
            this.allowReplay = allowReplay;
        }

        void setAllowRemoteAccess(boolean allowRemoteAccess) {
            this.allowRemoteAccess = allowRemoteAccess;
        }

        void setAllowTabAnomaly(boolean allowTabAnomaly) {
            this.allowTabAnomaly = allowTabAnomaly;
        }

        void setAllowNumpadAnomaly(boolean allowNumpadAnomaly) {
            this.allowNumpadAnomaly = allowNumpadAnomaly;
        }

        void setAllowDeviceChanged(boolean allowDeviceChanged) {
            this.allowDeviceChanged = allowDeviceChanged;
        }

    }

}
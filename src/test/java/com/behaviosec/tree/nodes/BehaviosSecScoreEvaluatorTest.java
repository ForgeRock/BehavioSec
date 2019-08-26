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
import java.util.Objects;


@Test
public class BehaviosSecScoreEvaluatorTest {

    private TestConfig config = new TestConfig();

    @BeforeMethod
    public void before() {
        initMocks(this);

    }

    @Test
    public void nodeProcessHighScoreTrueOutcome() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        BehavioSecScoreEvaluator node = new BehavioSecScoreEvaluator(config);
        BehavioSecReport bre = getReport();
        Objects.requireNonNull(bre).setScore(0.9);
        bre.setConfidence(0.9);
        bre.setRisk(0.2);
        bre.setIsTrained(true);
        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        //WHEN
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("true");
    }

    @Test
    public void nodeProcessNoReportOutcomeFalse() throws NodeProcessException {

        TreeContext context = getTreeContext(new HashMap<>());
        BehavioSecScoreEvaluator node = new BehavioSecScoreEvaluator(config);
        // test no bhs repport
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("false");
    }

    @Test
    public void nodeProcessLowScoreOutcomeFalse() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        BehavioSecScoreEvaluator node = new BehavioSecScoreEvaluator(config);
        System.out.println("Config" + config.allowInTraining());
        BehavioSecReport bre = getReport();
        Objects.requireNonNull(bre).setScore(0.1);
        bre.setConfidence(0.1);
        bre.setRisk(0);
        bre.setIsTrained(true);
        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        // WHEN
        Action action = node.process(context);
        System.out.println("nodeProcessLowScoreOutcomeFalse " + action.outcome);
        // THEN
        assertThat(action.outcome).isEqualTo("false");

    }

    @Test
    public void nodeProcessHighRiskOutcomeFalse() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        BehavioSecScoreEvaluator node = new BehavioSecScoreEvaluator(config);
        BehavioSecReport bre = getReport();
        Objects.requireNonNull(bre).setScore(0.1);
        bre.setConfidence(0.1);
        bre.setRisk(0.6);
        bre.setIsTrained(true);
        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        // WHEN
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("false");
    }

    @Test
    public void nodeProcessNotAllowInTrainingOutcomeFalse() throws NodeProcessException {
        TreeContext context = getTreeContext(new HashMap<>());
        config.setAllowInTraining(false);
        BehavioSecScoreEvaluator node = new BehavioSecScoreEvaluator(config);
        BehavioSecReport bre = getReport();
        Objects.requireNonNull(bre).setScore(0.9);
        bre.setConfidence(0.9);
        bre.setRisk(0);
        bre.setIsTrained(false);
        context.sharedState.put(Constants.BEHAVIOSEC_REPORT, Collections.singletonList(bre));
        //WHEN
        Action action = node.process(context);
        // THEN
        assertThat(action.outcome).isEqualTo("false");

    }

    private BehavioSecReport getReport() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File("src/test/java/com/behaviosec/tree/nodes/sample_return.json"),
                                    BehavioSecReport.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private TreeContext getTreeContext(Map<String, String[]> parameters) {
        return new TreeContext(
                JsonValue.json(JsonValue.object(1)),
                new ExternalRequestContext.Builder().parameters(parameters).build(),
                Collections.emptyList()
        );
    }

    static class TestConfig implements BehavioSecScoreEvaluator.Config {
        private boolean allowInTraining = true;

        TestConfig() {

        }

        public int minScore() {
            return Constants.MIN_SCORE;
        }

        public int minConfidence() {
            return Constants.MIN_CONFIDENCE;
        }

        public int maxRisk() {
            return Constants.MAX_RISK;
        }

        public boolean allowInTraining() {
            return allowInTraining;
        }

        void setAllowInTraining(boolean allowInTraining) {
            this.allowInTraining = allowInTraining;
        }
    }

}
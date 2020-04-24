package com.behaviosec.tree.utils;

import com.behaviosec.isdk.config.NoBehavioSecReportException;
import com.behaviosec.isdk.entities.Report;
import com.behaviosec.tree.config.Constants;
import com.sun.identity.shared.debug.Debug;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.TreeContext;

import java.util.List;

public class Helper {
    private final static String DEBUG_NAME = "Helper";
    private final static Debug debug = Debug.getInstance(DEBUG_NAME);

    public static JsonValue clearTimingFromShareState(JsonValue context){
        debug.message("Removing timing data");
        context.remove(Constants.BEHAVIOSEC_TIMING_DATA);
        return context;
    }

    public static Report getReportFromContext(TreeContext context) throws NoBehavioSecReportException {
        List<Object> shared = context.sharedState.get(Constants.BEHAVIOSEC_REPORT).asList();
        if (shared == null) {
            throw new NoBehavioSecReportException("context.sharedState.get(Constants.BEHAVIOSEC_REPORT) is null");
        }
        if (shared.size() != 1) {
            throw new NoBehavioSecReportException("context.sharedState.get(Constants.BEHAVIOSEC_REPORT) list is larger than one");
        }
        return (Report) shared.get(0);
    }
}

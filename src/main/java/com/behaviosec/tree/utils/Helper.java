package com.behaviosec.tree.utils;

import com.behaviosec.isdk.config.NoBehavioSecReportException;
import com.behaviosec.tree.config.Constants;
import com.behaviosec.isdk.entities.Report;

import org.forgerock.openam.auth.node.api.TreeContext;

import java.util.List;

public class Helper {
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

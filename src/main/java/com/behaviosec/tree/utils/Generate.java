package com.behaviosec.tree.utils;

import java.util.UUID;

public class Generate {
    public static String createSessionId() {
        return "sid-" + UUID.randomUUID().toString().substring(0, 10);
    }

    public static String createJourneyId() {
        return "jid-" + UUID.randomUUID().toString().substring(0, 10);
    }

}

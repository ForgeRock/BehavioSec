package com.behaviosec.tree.nodes;

import com.behaviosec.tree.utils.Generate;

public class TestGenerate {
    public static void main(String [] args) {
        String a = Generate.createSessionId();
        System.out.println(a);
        String b = Generate.createJourneyId();
        System.out.println(b);
    }
}
// jid-d5d7976c-5f98-42c1-0000
// jid-a3f237b8-b2e5-4e8c-0000-000

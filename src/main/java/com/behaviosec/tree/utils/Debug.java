package com.behaviosec.tree.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Debug {
    private enum Type {
        LOG, PRINTLN, BOTH, ERROR
    }

    private static final String TAG = Debug.class.getName();
    private static final Logger logger = LoggerFactory.getLogger(TAG);
    private static Type debugType = Type.LOG;

    public static void printDebugMesssage (String message) {
        if (debugType == Type.PRINTLN) {
            System.out.println(message);
        } else if (debugType == Type.LOG) {
            logger.debug(message);
        } else if (debugType == Type.ERROR) {
            System.out.println(message);
            logger.error(message);
        } else {
            logger.debug(message);
            System.out.println(message);
        }
    }
}

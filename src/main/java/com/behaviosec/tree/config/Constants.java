package com.behaviosec.tree.config;

/**
 * Class for constants through the application
 */
public class Constants {
    //Default score settings
    public static final int MIN_SCORE = 70;
    public static final int MIN_CONFIDENCE = 60;
    public static final int MAX_RISK = 100;

    //report naming
    public static final String BEHAVIOSEC_REPORT = "bhs_report";
    public static final String BEHAVIOSEC_SID = "bhs_sid";
    public static final String COLLECTOR_URL = "http://URL_TO_COLLECTOR/";

    //username from from openam
    public static String USER_NAME = "username";

    // data collector field
    public static String BEHAVIOSEC_TIMING_DATA = "bdata";
    public static String COLLECTOR_SCRIPT= "collector.min.js";

    // Node configuration
    public final static String TRUE_OUTCOME_ID = "true";
    public final static String FALSE_OUTCOME_ID = "false";


}

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
    //username from from openam
    public static String USERNAME = "username";
    // request header
    public static String ACCEPT_HEADER = "application/json";
    public static String SEND_HEADER = "application/json";


    // data collector field
    public static String DATA_FIELD = "bdata";
    public static String COLLECTOR_SCRIPT= "collector.min.js";

    // request body
    public static String USER_ID = "userId";
    public static String TIMING = "timing";
    public static String USER_AGENT = "userAgent";
    public static String IP = "ip";
    public static String TIMESTAMP = "timestamp";
    public static String NOTES = "notes";
    public static String REPORT_FLAGS = "reportflags";
    public static String OPERATOR_FLAGS = "operatorflags";
    public static String SESSION_ID = "sessionId";


    //request action
    private static String API_BASE_URL               = "BehavioSenseAPI/";



    // Node configuration
    public final static String TRUE_OUTCOME_ID = "true";
    public final static String FALSE_OUTCOME_ID = "false";
    //Operator flags
    public final static int FINALIZE_DIRECTLY = 512;
    public final static int FLAG_GENERATE_TIMESTAMP = 2;

}

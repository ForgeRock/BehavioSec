package com.behaviosec.tree.config;

public class Constants {
    public static final int MIN_SCORE = 60;
    public static final int MIN_CONFIDENCE = 40;
    public static final int MAX_RISK = 60;
    public static final String BEHAVIOSEC_REPORT = "bhs_report";
    // request header
    public static String ACCEPT_HEADER = "application/json";
    public static String SEND_HEADER = "application/json";


    // data collector field
    public static String DATA_FIELD = "application/json";
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
    public static String RESET_REASON = "reason";

    //request action
    public static String API_BASE_URL               = "BehavioSenseAPI/";
    public static String FINALIZE_SESSION   = API_BASE_URL + "FinalizeSession";
    public static String GET_HEALTH_STATUS  = API_BASE_URL+ "GetHealthCheck";
    public static String GET_REPORT         = API_BASE_URL + "GetReport";
    public static String GET_VERSION        = API_BASE_URL + "GetVersion";
    public static String REMOVE_USER        = API_BASE_URL + "RemoveUser";
    public static String RESET_PROFILE      = API_BASE_URL + "ResetProfile";


    // Node configuration
    public final static String TRUE_OUTCOME_ID = "true";
    public final static String FALSE_OUTCOME_ID = "false";
    //Operator flags
    public final static int FINALIZE_DIRECTLY = 256;
    public final static int FLAG_GENERATE_TIMESTAMP = 2;

    //log(1=error, 2=warning, 3=message)
    public static int DEBUG_LEVEL = 3;

    // Http Status Code
    public static int OK = 200;
    public static int BAD_REQUEST = 400;

    // Error codes
    public static int IP_MISSING_TIMING_CODE = 1001;
    public static String IP_MISSING_TIMING_MESSAGE = "Invalid parameter: missing timing.";

    public static int IP_MISSING_USER_AGENT_CODE = 1002;
    public static String IP_MISSING_USER_AGENT_MESSAGE = "Invalid parameter: missing useragent.";
    public static String USERNAME = "username";

    // TODO: implement rest of the error messages
//    public static int IP_MISSING_TIMING_CODE = 1001;
//    public static String IP_MISSING_TIMING_MESSAGE = "Invalid parameter: missing timing.";
//
//    public static int IP_MISSING_TIMING_CODE = 1001;
//    public static String IP_MISSING_TIMING_MESSAGE = "Invalid parameter: missing timing.";
//
//    public static int IP_MISSING_TIMING_CODE = 1001;
//    public static String IP_MISSING_TIMING_MESSAGE = "Invalid parameter: missing timing.";
//
//    public static int IP_MISSING_TIMING_CODE = 1001;
//    public static String IP_MISSING_TIMING_MESSAGE = "Invalid parameter: missing timing.";

}

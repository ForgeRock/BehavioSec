package com.behaviosec.utils;

public class Consts {

    public static final int DEFAULT_TIMEOUT = 5000;

    // JsonPath
    public static String SCORE = "score";

    // request header
    static String APPLICATION_JSON = "application/json";

    // request body
    public static String CUSTOMER_ID = "customerID";
    public static String ACTION = "action";
    public static String UUID = "uuid";
    public static String SESSION_ID = "sessionId";
    public static String TENANT_ID = "tenantId";

    //request action
    public static String BASE               = "BehavioSenseAPI/";
    public static String FINALIZE_SESSION   = BASE + "FinalizeSession";
    public static String GET_HEALTH_STATUS  = BASE+ "GetHealthCheck";
    public static String GET_REPORT         = BASE + "GetReport";
    public static String GET_VERSION        = BASE + "GetVersion";
    public static String REMOVE_USER        = BASE + "RemoveUser";
    public static String RESET_PROFILE      = BASE + "ResetProfile";





    //log(1=error, 2=warning, 3=message)
    public static int DEBUG_LEVEL = 3;

    // Http Status Code
    public static int OK = 200;
    public static int BAD_REQUEST = 400;

    // Error codes
    public static int IP_MISSING_TIMMING_CODE = 1001;
    public static String IP_MISSING_TIMMING_MESSAGE = "Invalid parameter: missing timing.";

    public static int IP_MISSING_USER_AGENT_CODE = 1002;
    public static String IP_MISSING_USER_AGENT_MESSAGE = "Invalid parameter: missing useragent.";

    // TODO: implement rest of the error messages
//    public static int IP_MISSING_TIMMING_CODE = 1001;
//    public static String IP_MISSING_TIMMING_MESSAGE = "Invalid parameter: missing timing.";
//
//    public static int IP_MISSING_TIMMING_CODE = 1001;
//    public static String IP_MISSING_TIMMING_MESSAGE = "Invalid parameter: missing timing.";
//
//    public static int IP_MISSING_TIMMING_CODE = 1001;
//    public static String IP_MISSING_TIMMING_MESSAGE = "Invalid parameter: missing timing.";
//
//    public static int IP_MISSING_TIMMING_CODE = 1001;
//    public static String IP_MISSING_TIMMING_MESSAGE = "Invalid parameter: missing timing.";

}

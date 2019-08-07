package com.behaviosec.utils;

import org.forgerock.http.util.Json;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.json.JSONObject;
import com.sun.identity.shared.debug.Debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.annotation.Nullable;


public class RestConnector implements BehavioSecRESTAPI{
    private String endPoint;
    private JSONObject json;
    private final Debug debug = Debug.getInstance("BehavioSec");


    private static RestConnector single_instance = null;


    public static RestConnector getInstance(int cacheExpirationTime) {
        if (single_instance == null)
            single_instance = new RestConnector();

        return single_instance;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }


    @Override
    public Json getReport(String userID,
                          String timing,
                          String userAgent,
                          String ip,
                          int reportFlags,
                          int operatorFlags,
                          @Nullable String sessionID,
                          @Nullable String tenantID,
                          @Nullable Long timeStamp,
                          @Nullable String notes) {
        return null;
    }

    @Override
    public boolean getHealthCheck() {
        return false;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public boolean resetProfile(String userID,
                                @Nullable String target,
                                @Nullable String profileType,
                                @Nullable String deviceType,
                                @Nullable String tenantId,
                                @Nullable String reason) {
        return false;
    }
}

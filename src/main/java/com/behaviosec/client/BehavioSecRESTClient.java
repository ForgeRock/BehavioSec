package com.behaviosec.client;


import com.behaviosec.utils.Consts;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class BehavioSecRESTClient implements BehavioSecAPIInterface {

    private static final String TAG = BehavioSecRESTClient.class.getName();
    private static final Logger LOGGER = LoggerFactory.getLogger(TAG);
    private String endPoint;
    private HttpClient httpClient;


    public BehavioSecRESTClient(String endPoint) {
        this.endPoint =endPoint;
        LOGGER.error(TAG + " BehavioSecRESTClient: " + this.endPoint);

        httpClient = HttpClientBuilder.create().build();
    }

    private HttpPost makePost(String path) {
        String uri = endPoint + path;
        LOGGER.error(TAG + " makePost " + uri);
        HttpPost postRequest = new HttpPost(uri);
        postRequest.setHeader("Accept", Consts.ACCEPT_HEADER);
        postRequest.setHeader("Content-type", Consts.SEND_HEADER);
        LOGGER.error(TAG + " makePost postRequest " + postRequest.toString());

        return postRequest;
    }

    private HttpGet makeGet(String path){
        String uri = endPoint + path;
        LOGGER.error(TAG + " makeGet " + uri);
        HttpGet httpGet = new HttpGet(uri);
        //TODO: move that to builder
        httpGet.setHeader("Accept", Consts.ACCEPT_HEADER);
        httpGet.setHeader("Content-type", Consts.SEND_HEADER);
        return httpGet;
    }

    private HttpResponse getResponse(org.apache.http.client.methods.HttpRequestBase request) throws IOException {
        HttpResponse response =  this.httpClient.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();
        LOGGER.error(TAG + " getResponse RESPONSE CODE: " + responseCode);
        if (responseCode == 200) {
            return response;
        } else if (responseCode == 400 ) {
            LOGGER.error(TAG + " getResponse CODE 400: " + response.getEntity().getContent());
            throw new IOException("Response 400");
        } else if (responseCode == 403) {
            LOGGER.error(TAG + " getResponse RESPONSE CODE: " + response.getEntity().toString());
            throw new IOException("Response 403");
        } else {
            LOGGER.error(TAG + " getResponse RESPONSE CODE: " + response.getEntity().toString());
            throw new IOException("Response unknown error");
        }
    }

    private void handleError(HttpResponse httpResponse) throws IOException {
        throw new IOException("HTTP response error: " + httpResponse.getStatusLine().getStatusCode());
    }

    @Override
    public  BehavioSecReport getReport(
            String userID,
            String timing,
            String userAgent,
            String ip,
            int reportFlags,
            int operatorFlags,
            @Nullable String sessionID,
            @Nullable String tenantID,
            @Nullable Long timeStamp,
            @Nullable String notes) {

        HttpPost post = makePost(Consts.GET_REPORT);
        return new BehavioSecReport();
    }

    public BehavioSecReport getReport(List<NameValuePair> report){
        //TODO :  return entity from get request
        LOGGER.error(TAG + " getReport ");
        HttpPost post = makePost(Consts.GET_REPORT);
        BehavioSecReport bhsReport = null;
        HttpResponse reportResponse = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(report));
            reportResponse = this.getResponse(post);
            LOGGER.error(TAG + " getReport " + reportResponse.toString());

            HttpEntity reportHttpEntity = reportResponse.getEntity();
            String retSrc = EntityUtils.toString(reportHttpEntity);
            ObjectMapper objectMapper = new ObjectMapper();
            LOGGER.error(TAG + " getReport " + reportResponse.toString());

            bhsReport = objectMapper.readValue(retSrc, BehavioSecReport.class);

        } catch (IOException e) {

            LOGGER.error(TAG + " getReport IOException" + e.getMessage());
            e.printStackTrace();
        }
        LOGGER.error(TAG + " getReport " + bhsReport.toString());

        return bhsReport;

    }

    @Override
    public boolean getHealthCheck() throws IOException {
        HttpResponse health = this.getResponse(this.makeGet(Consts.GET_HEALTH_STATUS));
        if (health != null) {
            if (health.getStatusLine().getStatusCode() == 200) {
                HttpEntity httpEntity = health.getEntity();
                Boolean healthStatus = Boolean.valueOf(EntityUtils.toString(httpEntity));
                LOGGER.info(TAG + " " + healthStatus.toString() );
                return healthStatus;
            } else {
                LOGGER.info(TAG + "Response is not 200" );

                this.handleError(health);
            }
        } else {
            throw new IOException("Got null response");
        }
        throw new IOException("Got null response");
    }

    @Override
    public BehavioSecVersion getVersion() throws IOException {
        HttpResponse version = this.getResponse(this.makeGet(Consts.GET_VERSION));
        return new BehavioSecVersion(EntityUtils.toString(version.getEntity()));
    }

    @Override
    public boolean resetProfile(
            String userID,
            @Nullable String target,
            @Nullable String profileType,
            @Nullable String deviceType,
            @Nullable String tenantId,
            @Nullable String reason
    ) {
        return false;
    }

    public boolean resetProfile(List<NameValuePair> report) throws IOException {
        HttpResponse version = this.getResponse(this.makePost(Consts.GET_VERSION));
        version.setEntity(new UrlEncodedFormEntity(report));
        return Boolean.valueOf(EntityUtils.toString(version.getEntity()));
    }
}

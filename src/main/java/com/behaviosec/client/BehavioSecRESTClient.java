package com.behaviosec.client;

import com.behaviosec.utils.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class BehavioSecRESTClient implements BehavioSecAPIInterface {

    private static final String TAG = BehavioSecRESTClient.class.getName();
    private static final Logger LOGGER = Logger.getLogger(BehavioSecRESTClient.class.getName());
    private String endPoint;
    HttpClient httpClient;


    public BehavioSecRESTClient(String endPoint) {
        this.endPoint =endPoint;
        httpClient = HttpClientBuilder.create().build();
    }

    private HttpPost makePost(String path) {
        HttpPost postRequest = new HttpPost(endPoint + path);
        postRequest.setHeader("Accept", "application/json");
        postRequest.setHeader("Content-type", "application/json");

        return postRequest;
    }

    private HttpGet makeGet(String path){
        return new HttpGet(endPoint + path);
    }

    private HttpResponse getResponse(org.apache.http.client.methods.HttpRequestBase request) throws IOException {
        HttpResponse response =  this.httpClient.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode == 200) {
            return response;
        } else if (responseCode == 400 ) {
            throw new IOException("Response 400");
        } else if (responseCode == 403) {
            throw new IOException("Response 403");
        } else {
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

    public BehavioSecReport getReport(List<NameValuePair> report) throws IOException {
        HttpPost post = makePost(Consts.GET_REPORT);
        post.setEntity(new UrlEncodedFormEntity(report));

        return new BehavioSecReport();
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

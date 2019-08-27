package com.behaviosec.tree.restclient;


import com.behaviosec.tree.config.Constants;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * REST client implementation for connectivity with BehavioSec endpoint
 */
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
        postRequest.setHeader("Accept", Constants.ACCEPT_HEADER);
        postRequest.setHeader("Content-type", Constants.SEND_HEADER);
        LOGGER.error(TAG + " makePost postRequest " + postRequest.toString());
        return postRequest;
    }

    private HttpResponse getResponse(org.apache.http.client.methods.HttpRequestBase request) throws IOException {
        HttpResponse response =  this.httpClient.execute(request);
        LOGGER.error(TAG + " getResponse RESPONSE CODE: " + response.getStatusLine().getStatusCode());
        return response;
    }


    /**
     * Submit behavior data and get evaluation
     * @param report List<NameValuePair>
     * @return server response
     * @throws IOException
     */
    public HttpResponse getReport(List<NameValuePair> report) throws IOException {
        //TODO :  return entity from get request
        LOGGER.error(TAG + " getReport ");
        HttpPost post = makePost(Constants.GET_REPORT);
        post.setEntity(new UrlEncodedFormEntity(report));
        return this.getResponse(post);
    }
}

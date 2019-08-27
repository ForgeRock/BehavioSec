package com.behaviosec.tree.restclient;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

interface BehavioSecAPIInterface {


    /**
     * RESTFull call to submit timing data and receive user report
     * Documentation https://developer.behaviosec.com/docapi/5.1/#getreport
     *
     * @return Returns a JSON formatted report (a collection of reports) that contains the comparison of a timing behavior string to the given user ID.
     */

    HttpResponse getReport(List<NameValuePair> report) throws IOException;
}

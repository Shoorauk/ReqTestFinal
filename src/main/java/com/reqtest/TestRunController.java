package com.reqtest;

import com.backend.Constants;
import com.reqtest.Entities.Response.CreateTestRunResponse;
import com.reqtest.Entities.Response.GetContentsResponse;
import com.backend.RequestUtils;
import com.google.gson.Gson;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TestRunController {

    Gson gson = new Gson();

    RequestUtils requestUtils = new RequestUtils();

    public CreateTestRunResponse createNewTestRun(String url, Map<String, ?> headers, String body){
        log.info("Body : " + body);
        Response response = requestUtils.sendPostWithBodyAndPath(url, headers, getProjectIdParam(), body);
        String responseString = response.getBody().asString();
        log.info("Response : " + responseString);
        Assert.assertEquals(response.getStatusCode(), 200);
        return gson.fromJson(responseString, CreateTestRunResponse.class);
    }

    public void addTestcase(String url, Map<String, ?> headers, String body, String testRunId){
        log.info("Body : " + body);
        Response response = requestUtils.sendPostWithBodyAndPath(url, headers, getProjectIdAndTestRunIdParam(testRunId), body);
        log.info("Response : " + response.getBody().asString());
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    public GetContentsResponse getTestRunContents(String url, Map<String, ?> headers, String testRunId){
        Response response = requestUtils.sendGetWithPathParams(url, headers, getProjectIdAndTestRunIdParam(testRunId));
        String responseString = response.getBody().asString();
        log.info("Response : " + responseString);
        Assert.assertEquals(response.getStatusCode(), 200);
        return gson.fromJson(responseString, GetContentsResponse.class);
    }

    public void executeContent(String url, Map<String, ?> headers, Map<String, ?> qParams, String testRunId, String body){
        Response response = requestUtils.sendPostWithBodyPathAndQuery(url, headers,
                getProjectIdAndTestRunIdParam(testRunId), qParams, body);
        String responseString = response.getBody().asString();
        log.info("Response : " + responseString);
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    public void uploadAttachment(String url, Map<String, ?> headers, String testRunId, long contentId, long itemId, File file){
        Response response = requestUtils.sendPostWithPathAndFormData(url, headers,
                getProjectTestRunContentAndItemIdParam(testRunId, contentId, itemId), file);
        String responseString = response.getBody().asString();
        log.info("Response : " + responseString);
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    private Map<String, ?> getProjectIdParam(){
        Map<String, String> params = new HashMap<>();
        params.put("projectId", Constants.PROJECT_ID);
        return params;
    }

    private Map<String, ?> getProjectIdAndTestRunIdParam(String testRunId){
        Map<String, String> params = (Map<String, String>) getProjectIdParam();
        params.put("testRunId", testRunId);
        return params;
    }

    private Map<String, ?> getProjectTestRunContentAndItemIdParam(String testRunId, long contentId, long itemId){
        Map<String, Object> params = (Map<String, Object>) getProjectIdAndTestRunIdParam(testRunId);
        params.put("contentId", contentId);
        params.put("itemId", itemId);
        return params;
    }


}

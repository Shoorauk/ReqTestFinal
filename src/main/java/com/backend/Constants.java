package com.backend;

public interface Constants {
    String CONFIG_PATH = "src/main/resources/config/";
    String REQTEST = "REQTEST";
    String CREATE_TEST_RUN = "/{projectId}/testruns";
    String REQTEST_PAT = "ve/XjVx8aQJtyJoMU12KNEZQclqtiIxwHL1d4BNCVs8Ml74oySlJUeD9y+Td0xOK";
    String PROJECT_ID = "70766";
    String ADD_TEST_CASE = "/{projectId}/testruns/{testRunId}/contents/add-testcases";
    String GET_CONTENTS = "/{projectId}/testruns/{testRunId}/contents";
    String EXECUTE_CONTENT = "/{projectId}/testruns/{testRunId}/contents/execute";
    String UPLOAD_ATTACHMENT = "/{projectId}/testruns/{testRunId}/contents/{contentId}/steps/{itemId}/attachments";
    String USERS = "/users";
    String REQRES = "REQRES";
}

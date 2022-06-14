package com.ui.cucumber;


import com.backend.StepDetails;
import com.reqtest.Entities.Response.Step;
import com.ui.cucumber.Repo.HomePage;
import com.ui.cucumber.Repo.SettingsPage;
import com.backend.Constants;
import com.google.gson.Gson;
import com.reqtest.Entities.Request.CreateTestRun;
import com.reqtest.Entities.Request.Fields;
import com.reqtest.Entities.Request.Name;
import com.reqtest.Entities.Response.Content;
import com.reqtest.Entities.Response.CreateTestRunResponse;
import com.reqtest.Entities.Response.GetContentsResponse;
import com.reqtest.TestRunController;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;
import io.cucumber.java.Status;
import lombok.Builder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

/**
 * Created by mac on 25/08/17.
 */
public class Browser {

    private WebDriver driver;
    private Properties prop;
    private HomePage homePage;
    private SettingsPage settings;
    TestRunController testRunController = new TestRunController();
    String base_url;
    Gson gson = new Gson();
    static boolean bSuite = false;
    public static String testRunId;


    @Before
    public void setUp(){

        loadProperties();

        String env = Reporter.getCurrentTestResult() == null ? "chrome" : Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("env");

        switch (env){
            case "chrome" :
                System.out.println("Chromedriver path " + prop.getProperty("driverExecutable") + "/chromedriver");
                System.setProperty("webdriver.chrome.driver",prop.getProperty("driverExecutable") + "/chromedriver");
                driver = new ChromeDriver();
                break;

            case "firefox" :
                System.setProperty("webdriver.gecko.driver",prop.getProperty("driverExecutable") + "/geckodriver");
                driver = new FirefoxDriver();
                break;

            case "safari" :
                driver = new SafariDriver();
                break;

            default:
                break;

        }

        homePage = new HomePage(driver);
        settings = new SettingsPage(driver);

        if(!bSuite){
            String executionTitle = "Suite Execution - " + RandomUtils.nextInt();
            CreateTestRunResponse response = testRunController.createNewTestRun(
                    getApiPath(Constants.REQTEST, Constants.CREATE_TEST_RUN),
                    getReqtestHeaders(),
                    getCreateTestRunBody(executionTitle));
            testRunId = Long.toString(response.getResult().getId());
            bSuite = true;
        }

    }

    public WebDriver getDriver(){
        return driver;
    }

    public Properties getProp(){
        return prop;
    }

    public HomePage getHomePage(){
        return homePage;
    }

    public SettingsPage getSettingsPage(){
        return settings;
    }

    @After
    public void tearDown(Scenario scenario) {
        String testResult = scenario.getStatus() == Status.PASSED ? "OK" : "Failed";

        String reqTestCaseId = scenario.getSourceTagNames().toArray()[0].toString().substring(1);

        //add test case to test run
        testRunController.addTestcase(
                getApiPath(Constants.REQTEST, Constants.ADD_TEST_CASE),
                getReqtestHeaders(),
                getArrayBody(reqTestCaseId),
                testRunId);

        GetContentsResponse contentsResponse = testRunController.getTestRunContents(
                getApiPath(Constants.REQTEST, Constants.GET_CONTENTS),
                getReqtestHeaders(),
                testRunId);

        Predicate<Content> findWithTestCaseName = e -> e.getName().equals(scenario.getName());
        long contentId = contentsResponse.getResult().getContents().stream()
                .filter(findWithTestCaseName).findFirst().get().getId();

        testRunController.executeContent(
                getApiPath(Constants.REQTEST, Constants.EXECUTE_CONTENT),
                getReqtestHeaders(),
                getResultQueryParams(testResult),
                testRunId,
                getArrayBody(String.valueOf(contentId)));

        if(testResult.equals("Failed")) onTestFailure(scenario);
        if(driver != null) driver.quit();

    }

    public String getApiPath(String url, String path){
        loadProperties();
        base_url = prop.getProperty(url + "_BASE_URL");
        return base_url + path;
    }

    public Map<String, ?> getReqtestHeaders(){
        return getReqtestHeaders("application/json");
    }

    public Map<String, ?> getReqtestHeaders(String contentType){
        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "application/json");
        headers.put("Content-Type", contentType);
        headers.put("reqtest-pat", Constants.REQTEST_PAT);
        return headers;
    }



    public Map<String, ?> getReqresHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private String getCreateTestRunBody(String testRunTitle){
        Name name = Name.builder().value(testRunTitle).build();
        Fields fields = Fields.builder().Name(name).build();
        CreateTestRun testRun = CreateTestRun.builder().fields(fields).build();
        return gson.toJson(testRun);
    }

    private String getArrayBody(String id){
        return "[ " + id + " ]";
    }

    private Map<String, String> getResultQueryParams(String result){
        Map<String, String> qParams = new HashMap<>();
        qParams.put("result", result);
        return qParams;
    }

    private void loadProperties() {
        FileInputStream f = null;
        try{
            f = new FileInputStream(new File("src/test/Resources/com/ui/cucumber/Config.properties"));
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        prop = new Properties();
        try{
            prop.load(f);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void onTestFailure(Scenario scenario) {
        final byte[] file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        GetContentsResponse contentsResponse = testRunController.getTestRunContents(
                getApiPath(Constants.REQTEST, Constants.GET_CONTENTS),
                getReqtestHeaders(),
                Browser.testRunId);
        Predicate<Content> findWithTestCaseName = e -> e.getName().equals(scenario.getName());
        long contentId = contentsResponse.getResult().getContents().stream()
                .filter(findWithTestCaseName).findFirst().get().getId();
        Predicate<Step> findWithStepName = e -> e.getStepDescription().equals(StepDetails.stepName);
        long itemId = contentsResponse.getResult().getContents().stream()
                .filter(findWithTestCaseName).findFirst().get().getSteps()
                .stream().filter(findWithStepName).findFirst().get().getId();
        testRunController.uploadAttachment(
                getApiPath(Constants.REQTEST, Constants.UPLOAD_ATTACHMENT),
                getReqtestHeaders("multipart/form-data"),
                Browser.testRunId, contentId, itemId, Hooks.writeByte(file));
    }


}

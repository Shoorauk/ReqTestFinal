package com.ui.cucumber;


import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Created by mac on 25/05/17.
 */


@CucumberOptions(tags = "@ui",plugin = {"com.backend.StepDetails"})
public class RunCukesTestUI extends AbstractTestNGCucumberTests {
}

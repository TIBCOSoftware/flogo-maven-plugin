package com.tibco.flogo.maven.coverage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibco.flogo.maven.coverage.dto.Data;
import com.tibco.flogo.maven.coverage.dto.Link;
import com.tibco.flogo.maven.coverage.dto.App;
import com.tibco.flogo.maven.coverage.dto.Task;
import com.tibco.flogo.maven.test.dto.*;
import com.tibco.flogo.maven.utils.FileHelper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class AppParser {


    AppCoverageStatistics appCoverageStatistics;
    public void parse(String appPath, Root root) throws Exception {
        AppCoverage coverage = new AppCoverage();

        populateAppData(appPath, coverage);
        populateExecutedData( coverage, root);

        System.out.println(coverage);
    }

    private void populateExecutedData( AppCoverage coverage, Root root ) throws Exception {

        for ( int i =0; i < root.report.suites.size(); i++ ) {
           Suite suite = root.report.suites.get(i);
           for ( int j =0 ; j < suite.testCases.size(); j++ ) {
               TestCase testCase = suite.testCases.get(j);
               FlowCoverage flowCoverage = null;
               if (coverage.getFlowMap().containsKey(testCase.flowName)) {
                   flowCoverage = coverage.getFlowMap().get(testCase.flowName);
               } else {
                   throw new Exception( "Flow not found in the flow map");
               }
               addActivitiesExecuted( flowCoverage, testCase.activities);
               addActivitiesErrorHandlerExecuted( flowCoverage, testCase.errorHandler.activities);
               addExecutedLinks( flowCoverage, testCase.links);
               addExecutedErrorHandlerLinks( flowCoverage, testCase.errorHandler.links);

               if (testCase.subFlow != null && !testCase.subFlow.isEmpty()) {
                   for (Map.Entry<String, TestCase> entry : testCase.subFlow.entrySet()) {
                        TestCase subTestCase = entry.getValue();
                        processSubFlowData( coverage, subTestCase);
                   }
               }
           }

        }

    }

    private void processSubFlowData( AppCoverage coverage, TestCase testCase) throws Exception {
        FlowCoverage flowCoverage = null;
        if (coverage.getFlowMap().containsKey(testCase.flowName)) {
            flowCoverage = coverage.getFlowMap().get(testCase.flowName);
        } else {
            throw new Exception( "Flow not found in the flow map");
        }
        addActivitiesExecuted( flowCoverage, testCase.activities);
        addActivitiesErrorHandlerExecuted( flowCoverage, testCase.errorHandler.activities);
        addExecutedLinks( flowCoverage, testCase.links);
        addExecutedErrorHandlerLinks( flowCoverage, testCase.errorHandler.links);

        if (testCase.subFlow != null && !testCase.subFlow.isEmpty()) {
            for (Map.Entry<String, TestCase> entry : testCase.subFlow.entrySet()) {
                TestCase subTestCase = entry.getValue();
                processSubFlowData( coverage, subTestCase);
            }
        }
    }

    private void populateAppData(String appPath, AppCoverage coverage) throws IOException {
        String app = FileHelper.readFile(appPath, Charset.defaultCharset() );
        ObjectMapper mapper = new ObjectMapper();
        App root = mapper.readValue(app, App.class);


        for( int i =0; i < root.resources.size(); i++ ) {
            Data resource = root.resources.get(i).data;
            FlowCoverage flowCoverage = null;
            if (coverage.getFlowMap().containsKey(resource.name)) {
                flowCoverage = coverage.getFlowMap().get(resource.name);
            } else {
                flowCoverage = new FlowCoverage();
                coverage.getFlowMap().put(resource.name, flowCoverage);
            }
            addLinks(flowCoverage, resource.links);
            if ( resource.errorHandler != null  && resource.errorHandler.links != null ) {
                addLinksErrorHandler(flowCoverage, resource.errorHandler.links);

            }
            addActivities( flowCoverage, resource.tasks);
            if ( resource.errorHandler != null  && resource.errorHandler.tasks != null ) {
                addActivitiesErrorHandler( flowCoverage, resource.errorHandler.tasks);
            }
        }
    }


    private void addExecutedLinks(FlowCoverage coverage, List<com.tibco.flogo.maven.test.dto.Link> links) {
        if ( links == null ) {
            return;
        }
        for ( int i =0; i < links.size(); i++ ) {
            com.tibco.flogo.maven.test.dto.Link link = links.get(i);
            coverage.getTransitionExec().add( link.linkName);
        }
    }

    private void addExecutedErrorHandlerLinks(FlowCoverage coverage, List<com.tibco.flogo.maven.test.dto.Link> links) {
        if ( links == null ) {
            return;
        }
        for ( int i =0; i < links.size(); i++ ) {
            com.tibco.flogo.maven.test.dto.Link link = links.get(i);
            coverage.getErrorHandlerTransitionExec().add( link.linkName);
        }
    }

    private void addActivitiesExecuted( FlowCoverage coverage, List<Activity> activities) {
        if (activities == null) {
            return;
        }
        for ( int i =0; i < activities.size(); i++ ) {
            Activity activity = activities.get(i);
            coverage.getActivitiesExec().add( activity.name);
        }
    }

    private void addActivitiesErrorHandlerExecuted( FlowCoverage coverage, List<Activity> activities) {
        if (activities == null) {
            return;
        }
        for ( int i =0; i < activities.size(); i++ ) {
            Activity activity = activities.get(i);
            coverage.getErrorHandlerActivitiesExec().add( activity.name);
        }
    }

    private void addLinks( FlowCoverage coverage, List<Link> links) {
        for ( int i =0; i < links.size(); i++ ) {
            Link link = links.get(i);
            coverage.getTransitions().add( link.label);
        }
    }

    private void addLinksErrorHandler( FlowCoverage coverage, List<Link> links) {
        for ( int i =0; i < links.size(); i++ ) {
            Link link = links.get(i);
            coverage.getErrorHandlerTransitions().add( link.label);
        }
    }

    private void addActivities( FlowCoverage coverage, List<Task> activities) {
        for ( int i =0; i < activities.size(); i++ ) {
            Task activity = activities.get(i);
            coverage.getActivities().add( activity.name);
        }
    }

    private void addActivitiesErrorHandler( FlowCoverage coverage, List<Task> activities) {
        for ( int i =0; i < activities.size(); i++ ) {
            Task activity = activities.get(i);
            coverage.getErrorHandlerActivities().add( activity.name);
        }
    }

    public AppCoverageStatistics getAppCoverageStatistics() {
        return appCoverageStatistics;
    }

    public void setAppCoverageStatistics(AppCoverageStatistics appCoverageStatistics) {
        this.appCoverageStatistics = appCoverageStatistics;
    }


    public class AppCoverageStatistics {

        AppCoverage appCoverage;

        public

        public String getActivityStat()
        {
            return "";
        }

        public String getTransitionStats() {
            return "";
        }
    }
}


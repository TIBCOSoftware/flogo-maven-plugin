package com.tibco.flogo.maven.coverage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibco.flogo.maven.coverage.dto.*;
import com.tibco.flogo.maven.coverage.dto.Link;
import com.tibco.flogo.maven.test.dto.*;
import com.tibco.flogo.maven.utils.FileHelper;
import com.tibco.flogo.maven.utils.Utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class AppParser {

    AppCoverage coverage = new AppCoverage();
    AppCoverageStatistics appCoverageStatistics;
    public void parse(String appPath, Root root) throws Exception {


        populateAppData(appPath, coverage);
        populateExecutedData( coverage, root);

        appCoverageStatistics = new AppCoverageStatistics(coverage);
        appCoverageStatistics.generate();

    }

    private void populateExecutedData( AppCoverage coverage, Root root ) throws Exception {

        for ( int i =0; i < root.report.suites.size(); i++ ) {
           Suite suite = root.report.suites.get(i);
            TestSuiteIOCoverage testSuiteCoverage = new TestSuiteIOCoverage();
            coverage.getTestIOMap().put(suite.suiteName, testSuiteCoverage);

           for ( int j =0 ; j < suite.testCases.size(); j++ ) {
               TestCase testCase = suite.testCases.get(j);
               FlowCoverage flowCoverage = null;
               if (coverage.getFlowMap().containsKey(testCase.flowName)) {
                   flowCoverage = coverage.getFlowMap().get(testCase.flowName);
                   flowCoverage.setFlowExecuted( true);
               } else {
                   throw new Exception( "Flow not found in the flow map");
               }
               TestIOCoverage testIOCoverage = new TestIOCoverage();
               testSuiteCoverage.getTestIOCoverageMap().put(testCase.testName, testIOCoverage);

               FlowIOCoverage flowIOCoverage = new FlowIOCoverage();
               flowIOCoverage.setStartingFlow( true);
               testIOCoverage.getFlowIOCoverageMap().put(testCase.flowName, flowIOCoverage);

               addActivitiesExecuted( flowCoverage, testCase.activities, flowIOCoverage);
               addActivitiesErrorHandlerExecuted( flowCoverage, testCase.errorHandler.activities, flowIOCoverage);
               addExecutedLinks( flowCoverage, testCase.links);
               addExecutedErrorHandlerLinks( flowCoverage, testCase.errorHandler.links);

               if (testCase.subFlow != null && !testCase.subFlow.isEmpty()) {
                   for (Map.Entry<String, TestCase> entry : testCase.subFlow.entrySet()) {
                        TestCase subTestCase = entry.getValue();
                        processSubFlowData( coverage, subTestCase , testIOCoverage);
                   }
               }
           }

        }

    }

    private void processSubFlowData( AppCoverage coverage, TestCase testCase, TestIOCoverage testIOCoverage) throws Exception {
        FlowCoverage flowCoverage = null;
        if (coverage.getFlowMap().containsKey(testCase.flowName)) {
            flowCoverage = coverage.getFlowMap().get(testCase.flowName);
            flowCoverage.setFlowExecuted( true);
        } else {
            throw new Exception( "Flow not found in the flow map");
        }
        FlowIOCoverage flowIOCoverage = new FlowIOCoverage();
        testIOCoverage.getFlowIOCoverageMap().put(testCase.flowName, flowIOCoverage);
        addActivitiesExecuted( flowCoverage, testCase.activities, flowIOCoverage);
        addActivitiesErrorHandlerExecuted( flowCoverage, testCase.errorHandler.activities, flowIOCoverage);
        addExecutedLinks( flowCoverage, testCase.links);
        addExecutedErrorHandlerLinks( flowCoverage, testCase.errorHandler.links);

        if (testCase.subFlow != null && !testCase.subFlow.isEmpty()) {
            for (Map.Entry<String, TestCase> entry : testCase.subFlow.entrySet()) {
                TestCase subTestCase = entry.getValue();
                processSubFlowData( coverage, subTestCase, testIOCoverage);
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
                flowCoverage.setFlowName( resource.name);
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

    private void addActivitiesExecuted( FlowCoverage coverage, List<Activity> activities , FlowIOCoverage flowIOCoverage) throws Exception {
        if (activities == null) {
            return;
        }
        for ( int i =0; i < activities.size(); i++ ) {
            Activity activity = activities.get(i);
            coverage.getActivitiesExec().add( activity.name);
            String input = new ObjectMapper().writeValueAsString( activity.input);
            String output = new ObjectMapper().writeValueAsString( activity.output);
            flowIOCoverage.getActivityList().add( new ActivityIO( activity.name, input, output, "" ));
        }
    }

    private void addActivitiesErrorHandlerExecuted( FlowCoverage coverage, List<Activity> activities , FlowIOCoverage flowIOCoverage) throws Exception {
        if (activities == null) {
            return;
        }
        for ( int i =0; i < activities.size(); i++ ) {
            Activity activity = activities.get(i);
            coverage.getErrorHandlerActivitiesExec().add( activity.name);
            String input = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString( activity.input);
            String output = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString( activity.output);
            flowIOCoverage.getActivityErrorList().add( new ActivityIO( activity.name, input, output, "" ));
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

    public AppCoverage getCoverage() {
        return coverage;
    }

    public AppCoverageStatistics getAppCoverageStatistics() {
        return appCoverageStatistics;
    }

    public void setAppCoverageStatistics(AppCoverageStatistics appCoverageStatistics) {
        this.appCoverageStatistics = appCoverageStatistics;
    }


    public class AppCoverageStatistics {

        AppCoverage appCoverage;
        Map<String,FlowStats> map = new HashMap<>();
        public AppCoverageStatistics( AppCoverage coverage) {
            this.appCoverage = coverage;
        }

        public void generate() throws Exception {
            for (Map.Entry<String, FlowCoverage> entry : appCoverage.getFlowMap().entrySet()) {
                FlowStats flowStats = new FlowStats();
                if (entry.getValue() == null ) {
                    throw new Exception( "No statistics found for flow " +  entry.getKey() );
                }
                flowStats.generateFlowStats( entry.getValue() );
                map.put( entry.getKey(), flowStats );
            }
        }

        public List<String> getFlows() {
            List<String> flows = new ArrayList<>( this.appCoverage.getFlowMap().keySet() );
            Collections.sort(flows, String.CASE_INSENSITIVE_ORDER);
            return flows;
        }

        public String getAppFlowStats() {
            int total = 0;
            int nonExecuted = 0;
            for ( FlowStats flowStats : map.values() ) {
                total++;
                if  (!flowStats.executed ) {
                    nonExecuted++;
                }
            }

            return Utils.getPercentage( total, nonExecuted );
        }

        public String getNonExecutedFlows() {
            List<String> nonExecFlows = new ArrayList<>();
            for ( FlowStats flowStats : map.values() ) {
                if  (!flowStats.executed ) {
                    nonExecFlows.add( flowStats.flowName);
                }
            }
            return String.join( "," , nonExecFlows );
        }

        public String getAppFlowActivityStats() {
                int total = 0;
                int nonExecuted = 0;
                for ( FlowStats flowStats : map.values() ) {
                    total += ( flowStats.totalMainActivities + flowStats.totalErrorActivities);
                    nonExecuted += ( (flowStats.totalMainActivities - flowStats.executedMainActivities) + (flowStats.totalErrorActivities - flowStats.executedErrorActivities));
                }
                return Utils.getPercentage( total, nonExecuted );
        }

        public String getAppFlowLinkStats() {
            int total = 0;
            int nonExecuted = 0;
            for ( FlowStats flowStats : map.values() ) {
                total += ( flowStats.totalMainLinks + flowStats.totalErrorLinks);
                nonExecuted += ( (flowStats.totalMainLinks - flowStats.executedMainLinks) + (flowStats.totalErrorLinks - flowStats.executedErrorLinks));
            }
            return Utils.getPercentage( total, nonExecuted );
        }


        public FlowStats getFlowStatistics( String flowName) throws Exception {
            return map.get( flowName);
        }
    }

    public class FlowStats {
        public String flowName;
        public int totalMainActivities;
        public int executedMainActivities;
        public int totalMainLinks;
        public int executedMainLinks;
        public int totalErrorActivities;
        public int executedErrorActivities;
        public int totalErrorLinks;
        public int executedErrorLinks;
        public String totalMainActivitiesList;
        public String nonExecutedMainActivitiesList;
        public String totalMainLinksList;
        public String nonExecutedMainLinksList;
        public String totalErrorActivitiesList;
        public String nonExecutedErrorActivitiesList;
        public String totalErrorLinksList;
        public String nonExecutedErrorLinksList;
        public boolean executed;
        public boolean hasErrorHandler;

        public void generateFlowStats( FlowCoverage coverage) {
            flowName = coverage.getFlowName();
            executed = coverage.isFlowExecuted();

            totalMainActivities = coverage.getActivities().size();
            executedMainActivities = coverage.getActivitiesExec().size();

            totalMainLinks = coverage.getTransitions().size();
            executedMainLinks = coverage.getTransitionExec().size();

            totalErrorActivities = coverage.getErrorHandlerActivities().size();
            executedErrorActivities = coverage.getErrorHandlerActivitiesExec().size();

            totalErrorLinks = coverage.getErrorHandlerTransitions().size();
            executedErrorLinks = coverage.getErrorHandlerTransitionExec().size();

            Set<String> nonExecSet = new TreeSet<>( coverage.getActivities());
            totalMainActivitiesList = String.join( ",", nonExecSet);
            nonExecSet.removeAll( coverage.getActivitiesExec());
            if (nonExecSet.size() > 0) {
                nonExecutedMainActivitiesList = String.join( ",", nonExecSet);
            } else  {
                nonExecutedMainActivitiesList = "N/A";
            }


            Set<String> nonExecLinkSet = new TreeSet<>( coverage.getTransitions());
            totalMainLinksList = String.join( "," , nonExecLinkSet);

            nonExecLinkSet.removeAll( coverage.getTransitionExec());
            if (nonExecLinkSet.size() > 0) {
                nonExecutedMainLinksList = String.join( ",", nonExecLinkSet);
            } else  {
                nonExecutedMainLinksList = "N/A";
            }

            Set<String> nonExecErrorSet = new TreeSet<>( coverage.getErrorHandlerActivities());
            totalErrorActivitiesList = String.join( "," , nonExecErrorSet);
            nonExecErrorSet.removeAll( coverage.getErrorHandlerActivitiesExec());
            if (nonExecErrorSet.size() > 0) {
                nonExecutedErrorActivitiesList = String.join( ",", nonExecErrorSet);
            } else {
                nonExecutedErrorActivitiesList = "N/A";
            }

            Set<String> nonExecLinkErrorSet = new TreeSet<>( coverage.getErrorHandlerTransitions());
            totalErrorLinksList = String.join( "," , nonExecLinkErrorSet);
            nonExecLinkErrorSet.removeAll( coverage.getErrorHandlerTransitionExec());
            if (nonExecLinkErrorSet.size() > 0) {
                nonExecutedErrorLinksList = String.join( ",", nonExecLinkErrorSet);
            } else  {
                nonExecutedErrorLinksList = "N/A";
            }


        }



        public String getMainFlowActivityPercentage() {
            if ( !executed ) {
                return "N/A";
            }
            return Utils.getPercentage( totalMainActivities, totalMainActivities - executedMainActivities) ;
        }
        public String getMainFlowLinkPercentage() {
            if ( !executed ) {
                return "N/A";
            }
            return Utils.getPercentage( totalMainLinks, totalMainLinks - executedMainLinks) ;
        }
        public String getErrorFlowActivityPercentage() {
            return Utils.getPercentage( totalErrorActivities, totalErrorActivities - executedErrorActivities) ;
        }
        public String getErrorFlowLinkPercentage() {
            return Utils.getPercentage( totalErrorLinks, totalErrorLinks - executedErrorLinks) ;
        }

        public boolean hasErrorHandler() {
            return hasErrorHandler;
        }

    }
}


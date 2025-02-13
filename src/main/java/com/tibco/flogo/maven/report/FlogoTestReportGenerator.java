package com.tibco.flogo.maven.report;

import com.tibco.flogo.maven.test.dto.*;
import com.tibco.flogo.maven.utils.Utils;
import org.apache.maven.doxia.markup.HtmlMarkup;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.doxia.sink.impl.SinkEventAttributeSet;
import org.apache.maven.doxia.util.DoxiaUtils;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;

import static org.apache.maven.doxia.markup.HtmlMarkup.A;
import static org.apache.maven.doxia.sink.Sink.JUSTIFY_LEFT;
import static org.apache.maven.doxia.sink.SinkEventAttributes.NAME;

public class FlogoTestReportGenerator {
    private static final int LEFT = JUSTIFY_LEFT;

    private static final Object[] TAG_TYPE_START = {HtmlMarkup.TAG_TYPE_START};

    private static final Object[] TAG_TYPE_END = {HtmlMarkup.TAG_TYPE_END};

    Root report;

    private static String toHtmlId(String id) {
        return DoxiaUtils.isValidId(id) ? id : DoxiaUtils.encodeId(id);
    }

    private static void sinkIcon(String type, Sink sink) {
        sink.figure();
        if (type.startsWith("junit.framework") || "skipped".equals(type)) {
            sink.figureGraphics("images/icon_warning_sml.gif");
        } else if (type.startsWith("success")) {
            sink.figureGraphics("images/icon_success_sml.gif");
        } else {
            sink.figureGraphics("images/icon_error_sml.gif");
        }
        sink.figure_();
    }

    private static void sinkLineBreak(Sink sink) {
        sink.lineBreak();
    }

    private static void sinkHeader(Sink sink, String header) {
        sink.tableHeaderCell();
        sink.text(header);
        sink.tableHeaderCell_();
    }

    private static void sinkCell(Sink sink, String text) {
        sink.tableCell();
        sink.text(text);
        sink.tableCell_();
    }

    private static void sinkCellBold(Sink sink, String text) {
        sink.tableCell();
        sink.bold();
        sink.text(text);
        sink.bold();
        sink.tableCell_();
    }

    private static void sinkCellMonospaced(Sink sink, String text) {
        sink.tableCell();
        sink.monospaced();
        sink.text(text);
        sink.monospaced_();
        sink.tableCell_();
    }

    private static void sinkLink(Sink sink, String text, String link) {
        sink.link(link);
        sink.text(text);
        sink.link_();
    }

    private static void sinkCellLink(Sink sink, String text, String link) {
        sink.tableCell();
        sinkLink(sink, text, link);
        sink.tableCell_();
    }

    private static void sinkAnchor(Sink sink, String anchor) {
        // Dollar '$' for nested classes is not valid character in sink.anchor() and therefore it is ignored
        // https://issues.apache.org/jira/browse/SUREFIRE-1443
        sink.unknown(A.toString(), TAG_TYPE_START, new SinkEventAttributeSet(NAME, anchor));
        sink.unknown(A.toString(), TAG_TYPE_END, null);
    }

    private static String javascriptToggleDisplayCode() {

        // the javascript code is emitted within a commented CDATA section
        // so we have to start with a newline and comment the CDATA closing in the end

        return "\n" + "function toggleDisplay(elementId) {\n"
                + " var elm = document.getElementById(elementId + '-error');\n"
                + " if (elm == null) {\n"
                + "  elm = document.getElementById(elementId + '-failure');\n"
                + " }\n"
                + " if (elm && typeof elm.style != \"undefined\") {\n"
                + "  if (elm.style.display == \"none\") {\n"
                + "   elm.style.display = \"\";\n"
                + "   document.getElementById(elementId + '-off').style.display = \"none\";\n"
                + "   document.getElementById(elementId + '-on').style.display = \"inline\";\n"
                + "  } else if (elm.style.display == \"\") {"
                + "   elm.style.display = \"none\";\n"
                + "   document.getElementById(elementId + '-off').style.display = \"inline\";\n"
                + "   document.getElementById(elementId + '-on').style.display = \"none\";\n"
                + "  } \n"
                + " } \n"
                + " }\n"
                + "//";
    }

    public void generateReport(Root report, Sink sink) {
        this.report = report;

        sink.head();

        sink.title();
        sink.text("Flogo Unit Test Report");
        sink.title_();

        sink.head_();

        sink.body();

        SinkEventAttributeSet atts = new SinkEventAttributeSet();
        atts.addAttribute("type", "text/javascript");
        sink.unknown("script", new Object[]{HtmlMarkup.TAG_TYPE_START}, atts);
        sink.unknown("cdata", new Object[]{HtmlMarkup.CDATA_TYPE, javascriptToggleDisplayCode()}, null);
        sink.unknown("script", new Object[]{HtmlMarkup.TAG_TYPE_END}, null);

        sink.section1();
        sink.sectionTitle1();
        sink.text("Flogo Test Report");
        sink.sectionTitle1_();
        sink.section1_();

        //constructSummarySection(sink);

        constructSuiteListSection(sink);
        contructTestCaseSection(sink);
        sink.body_();

        sink.flush();

        sink.close();

    }

   /* private static void sinkCellAnchor( Sink sink, String text, String anchor )
    {
        sink.tableCell();
        sinkAnchor( sink, anchor );
        sink.text( text );
        sink.tableCell_();
    }*/

    private void constructAssertionDetailsSection(Sink sink, Suite suite) {
        sink.section2();
        sink.sectionTitle3();
        sink.text( " Failed Assertions in suite  " + suite.suiteName);
        sink.sectionTitle3_();

        sinkLineBreak(sink);
        sink.table();
        sink.tableRows(new int[]{LEFT, LEFT, LEFT, LEFT, LEFT, LEFT}, true);
        sink.tableRow();
        sinkHeader(sink, "Flow Name");
        sinkHeader(sink, "Test Case Name");
        sinkHeader(sink, "Activity Name");
        sinkHeader(sink, "Assertion Name");
        sinkHeader(sink, "Assertion Expression");
        sinkHeader(sink, "Evaluated expression");
        sink.tableRow_();
        for ( int i=0; i < suite.testCases.size(); i++ ) {
            TestCase testCase = suite.testCases.get(i);
            if ( testCase.testResult.skippedAssertions == 0 && testCase.testResult.failedAssertions == 0 ) {
                continue;
            }
            for ( int j=0; j < testCase.activities.size(); j++ ) {

                Activity activity = testCase.activities.get(j);
                if (activity.assertionResult == null) {
                    continue;
                }
                for ( int k=0; k < activity.assertionResult.size(); k++ ) {
                    AssertionResult assertionResult = activity.assertionResult.get(k);
                    if ( !assertionResult.status.equals( "fail" ) ) {
                        continue;
                    }

                    sink.tableRow( ) ;
                    sinkCell( sink, testCase.flowName);
                    sinkCell( sink, testCase.testName);
                    sinkCell( sink, activity.name);
                    sinkCell( sink, assertionResult.name) ;
                    sinkCellMonospaced( sink, assertionResult.expression);
                    sinkCellMonospaced( sink, assertionResult.expressionEvaluated);

                    sink.tableRow_();
//                    SinkEventAttributeSet event = new SinkEventAttributeSet();
//                    event.addAttribute(SinkEventAttributes.ROWSPAN, "6");
//                    sink.tableRow( event);
//                    sink.tableRow_();

                }

            }

        }
        sink.table_();
        sink.section2_();
    }
    private void contructTestCaseSection(Sink sink) {

        sink.section1();


        Collections.sort( this.report.report.suites);
        for (int i = 0; i < this.report.report.suites.size(); i++) {

            Suite suite = this.report.report.suites.get(i);
            boolean containsFailed = false;
            sink.section1();
            sink.sectionTitle2();
            sink.text(suite.suiteName);
            sink.sectionTitle2_();
            sink.table();
            sink.tableRows(new int[]{LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT}, true);
            sink.tableRow();
            sinkHeader(sink, "");
            sinkHeader(sink, "Flow Name");
            sinkHeader(sink, "Test Case Name");
            sinkHeader(sink, "Total Assertions");
            sinkHeader(sink, "Success Assertions");
            sinkHeader(sink, "Failure Assertions");
            sinkHeader(sink, "Skipped Assertions");
            sinkHeader(sink, "Test Case Status");
            sink.tableRow_();
            Collections.sort(suite.testCases);
            for (int j = 0; j < suite.testCases.size(); j++) {
                TestCase testCase = suite.testCases.get(j);
                sink.tableRow();
                sink.tableCell();
                sink.link( "#test" );
                if (testCase.testResult.failedAssertions > 0 || testCase.testResult.skippedAssertions > 0) {
                    sinkIcon("failure", sink);
                } else {
                    sinkIcon("success", sink);
                }
                sink.link_();

                sink.tableCell_();
                sinkCell(sink, testCase.flowName);
                sinkCell(sink, testCase.testName);
                sinkCell(sink, String.valueOf(testCase.testResult.totalAssertions));
                sinkCell(sink, String.valueOf(testCase.testResult.successAssertions));
                if (testCase.testResult.failedAssertions > 0) {
                    sinkCellBold(sink, String.valueOf(testCase.testResult.failedAssertions));
                } else {
                    sinkCell(sink, String.valueOf(testCase.testResult.failedAssertions));
                }

                if (testCase.testResult.skippedAssertions > 0) {
                    sinkCellBold(sink, String.valueOf(testCase.testResult.skippedAssertions));
                } else {
                    sinkCell(sink, String.valueOf(testCase.testResult.skippedAssertions));
                }

                if ( testCase.testResult.failedAssertions > 0 || testCase.testResult.skippedAssertions > 0) {
                    sinkCellBold (sink, "failed");
                    containsFailed = true;
                } else {
                    sinkCell (sink, "passed");
                }
                sink.tableRow_();
            }
            sink.table_();
            if (containsFailed) {
                constructAssertionDetailsSection( sink, suite );
            }


        }
        sink.section1_();


    }

    private void constructSuiteListSection(Sink sink) {
        sink.section1();

        sink.sectionTitle1();
        sink.text("Test Suites Summary");
        sink.sectionTitle1_();

        sinkLineBreak(sink);

        sink.table();

        sink.tableRows(new int[]{LEFT, LEFT, LEFT, LEFT, LEFT, LEFT}, true);

        sinkHeader(sink, "Test Suite Name");
        sinkHeader(sink, "Success Rate");


        sinkHeader(sink, "Tests");


        sinkHeader(sink, "Success");

        sinkHeader(sink, "Failure");

        sinkHeader(sink, "Errors");


        sink.tableRow_();

        Collections.sort( this.report.report.suites);

        for (int i = 0; i < this.report.report.suites.size(); i++) {

            Suite suite = this.report.report.suites.get(i);
            sink.tableRow();
            sinkCell(sink, suite.suiteName);
            sinkCell(sink, Utils.getPercentage(suite.suiteResult.totalTests, suite.suiteResult.failedTests));
            sinkCell(sink, String.valueOf(suite.suiteResult.totalTests));
            sinkCell(sink, String.valueOf(suite.suiteResult.totalTests - suite.suiteResult.failedTests));
            sinkCell(sink, String.valueOf(suite.suiteResult.failedTests));
            sinkCell(sink, String.valueOf(suite.suiteResult.errorFailed));
            sink.tableRow_();
        }


        sink.table_();


        sinkLineBreak(sink);

        sink.section1_();
    }

    private void constructSummarySection(Sink sink) {


        sink.section1();
        sink.sectionTitle2();
        sink.text("Test Suites Summary");
        sink.sectionTitle2_();

        sinkAnchor(sink, "Suites Summary");

        constructHotLinks(sink);

        sinkLineBreak(sink);

        sink.table();

        sink.tableRows(new int[]{LEFT, LEFT, LEFT, LEFT}, true);

        sink.tableRow();

        sinkHeader(sink, "Suites");

        sinkHeader(sink, "Success");

        sinkHeader(sink, "Failures");

        sinkHeader(sink, "Success Rate");

        sink.tableRow_();

        sink.tableRow();

        sinkCell(sink, String.valueOf(report.result.totalSuites));

        sinkCell(sink, String.valueOf(report.result.totalSuites - report.result.failedSuites));

        sinkCell(sink, String.valueOf(report.result.failedSuites));

        sinkCell(sink, Utils.getPercentage(report.result.totalSuites, report.result.failedSuites));

        sink.tableRow_();

        sink.tableRows_();

        sink.table_();


        sinkLineBreak(sink);

        sink.section1_();
    }


    /*private static void sinkLink( Sink sink, String href )
    {
        // The "'" argument in this JavaScript function would be escaped to "&apos;"
        // sink.link( "javascript:toggleDisplay('" + toHtmlId( testCase.getFullName() ) + "');" );
        sink.unknown( A.toString(), TAG_TYPE_START, new SinkEventAttributeSet( HREF, href ) );
    }*/

    /*@SuppressWarnings( "checkstyle:methodname" )
    private static void sinkLink_( Sink sink )
    {
        sink.unknown( A.toString(), TAG_TYPE_END, null );
    }*/

    private void constructHotLinks(Sink sink) {
        sink.paragraph();

//        sink.text("[");
//        sinkLink(sink, "Summary", "#Summary");
//        sink.text("]");
//        if (null != BWTestConfig.INSTANCE.getTestSuiteName() && !BWTestConfig.INSTANCE.getTestSuiteName().isEmpty()) {
//            sink.text(" [");
//            sinkLink(sink, "Test Suite List", "#TestSuite_List");
//            sink.text("]");
//        } else {
//            sink.text(" [");
//            sinkLink(sink, "Package List", "#Package_List");
//            sink.text("]");
//        }
//
//        sink.text(" [");
//        sinkLink(sink, "Test Cases", "#Test_Cases");
//        sink.text("]");
//
//        if (bwTestParser.isShowFailureDetails()) {
//            sink.text(" [");
//            sinkLink(sink, "Failure Details", "#Failure_Details");
//            sink.text("]");
//        }


        sink.paragraph_();
    }

}


package com.tibco.flogo.maven.report;

import com.tibco.flogo.maven.coverage.AppParser;
import com.tibco.flogo.maven.test.dto.*;
import org.apache.maven.doxia.markup.HtmlMarkup;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.impl.SinkEventAttributeSet;
import org.apache.maven.doxia.util.DoxiaUtils;

import static org.apache.maven.doxia.markup.HtmlMarkup.A;
import static org.apache.maven.doxia.sink.Sink.JUSTIFY_LEFT;
import static org.apache.maven.doxia.sink.SinkEventAttributes.NAME;

public class FlogoCoverageReportGenerator {
    private static final int LEFT = JUSTIFY_LEFT;

    private static final Object[] TAG_TYPE_START = {HtmlMarkup.TAG_TYPE_START};

    private static final Object[] TAG_TYPE_END = {HtmlMarkup.TAG_TYPE_END};

    private AppParser report;

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

    public void generateReport(AppParser parser, Sink sink) throws Exception {
        this.report = parser;
        sink.head();

        sink.title();
        sink.text("Flogo Coverage Report");
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
        sink.text("Flogo Coverage Report");
        sink.sectionTitle1_();
        sink.section1_();

        constructSummarySection(sink);
        contructCoverageByFlowSection(sink);
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

    private void contructCoverageByFlowSection(Sink sink) throws Exception {

        sink.section1();
        sink.sectionTitle1();
        sink.text("Coverage BreakDown By Flow");
        sink.sectionTitle1_();
        sinkLineBreak(sink);

        sink.table();
        sink.tableRows(new int[]{LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT}, true);
        sink.tableRow();
        sinkHeader(sink, "Flow Name");
        sinkHeader(sink, "Flow Executed");
        sinkHeader(sink, "Main Flow Activities Executed %");
        sinkHeader(sink, "Non  Executed Activities");
        sinkHeader(sink, "Error Handler Activities Executed %");
        sinkHeader(sink, "Non  Executed Error Handler Activities");
        sinkHeader(sink, "Main Flow Transition Executed %");
        sinkHeader(sink, "Non Executed Transitions");
        sinkHeader(sink, "Error Handler Transition Executed %");
        sinkHeader(sink, "Non Executed Error Handlers Transition");

        sink.tableRow_();

        for ( String flow : report.getAppCoverageStatistics().getFlows()) {
            AppParser.FlowStats stats = report.getAppCoverageStatistics().getFlowStatistics( flow);
            sink.tableRow();
            sinkCell( sink, stats.flowName);
            sinkCell( sink, String.valueOf(stats.executed));
            sinkCell( sink, stats.getMainFlowActivityPercentage());
            sinkCell( sink, stats.nonExecutedMainActivitiesList);
            sinkCell( sink, stats.getErrorFlowActivityPercentage());
            sinkCell( sink , stats.nonExecutedErrorActivitiesList);
            sinkCell( sink , stats.getMainFlowLinkPercentage());
            sinkCell( sink , stats.nonExecutedMainLinksList);
            sinkCell( sink, stats.getErrorFlowLinkPercentage());
            sinkCell( sink, stats.nonExecutedErrorLinksList);
            sink.tableRow_();
        }
        sink.table_();
        sink.section1_();

    }


    private void constructSummarySection(Sink sink) {


        sink.section1();
        sink.sectionTitle1();
        sink.text("Coverage  Summary");
        sink.sectionTitle1_();
        sinkLineBreak(sink);

        sink.table();

        sink.tableRows(new int[]{ LEFT, LEFT, LEFT}, true);

        sink.tableRow();

        sinkHeader(sink, "Flow %");
        sinkHeader(sink, "Non Executed Flows");
        sinkHeader(sink, "Activity %");
        sinkHeader(sink, "Transition %");


        sink.tableRow_();

        sink.tableRow();

        sinkCell(sink, report.getAppCoverageStatistics().getAppFlowStats());

        sinkCell( sink, report.getAppCoverageStatistics().getNonExecutedFlows());
        sinkCell(sink, report.getAppCoverageStatistics().getAppFlowActivityStats());

        sinkCell(sink, report.getAppCoverageStatistics().getAppFlowLinkStats());

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


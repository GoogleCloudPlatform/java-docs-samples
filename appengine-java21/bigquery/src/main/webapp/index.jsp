<%@ page import="com.example.appengine.bigquerylogging.BigQueryHome" %>

<html>
  <title>An example of using BigQuery and StackDriver Logging on AppEngine Standard</title>
  <body>
    <h3>Run query</h3>
    <form action="bigquery/run" method="POST">
      <input id="submit"  type="submit" value="Start Run" />
    </form>
    <h3> Most Recent Run Results </h3>
    <table border="1" cellpadding="10">
      <tr>
      <th>URL</th>
      <th>View Count</th>
      </tr>
      <%= BigQueryHome.getMostRecentRun() %>
    </table>
    <h3>Run Metric Values</h3>
    <table border="1" cellpadding="10">
      <tr>
      <th>Metric Type</th>
      <th>Count</th>
      <th>Most Recent End Time</th>
      <th>Most Recent Value</th>
      <th>Average/Values</th>
      </tr>
      <%= BigQueryHome.getMetricAverages() %>
    </table>
  </body>
</html>

<%@page import="org.apache.http.client.HttpClient"%>
<%@page import="org.apache.commons.lang3.StringEscapeUtils"%>
<%@page import="javax.ws.rs.core.Response"%>
<%@page import="org.edits.rest.EngineManager"%>
<%@page import="org.edits.rest.RestService"%>
<%@page import="org.edits.etaf.EntailmentPair"%>
<%@page import="org.edits.engines.distance.DistanceEntailmentEngine"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%

	String question="Explain why you got a voltage reading of 1.5 for terminal 4 and the positive terminal.";
	String result=null;
	String teacher="Terminal 4 and the positive terminal are separated by the gap";
	String student="the positive battery terminal is seperated by a gap from Terminal 4";
	
	if (request.getParameter("question")!=null){
		question=request.getParameter("question");
		teacher=request.getParameter("teacher");
		student=request.getParameter("student");

		HttpClient client = new DefaultHttpClient();
		  HttpGet request = new HttpGet('');
		  HttpResponse response = client.execute(request);
		  BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
		  String line = '';
		  while ((line = rd.readLine()) != null) {
		    System.out.println(line);
		  }
		
		RestService rs=new RestService();
		Response r=rs.evaluate(EngineManager.DEFAULT_ENTAILMENT_ENGINE, teacher, student);
		
		result=r.getEntity().toString();
	}
%>
<html>
<head></head>
<style type="text/css"> 
.centered {
    margin: 0 auto;
    text-align: left;
}
body {
  background-color:#F5F5F5;
  background-position:initial initial;
  background-repeat:initial initial;
  color:#1A04A5;
  font-family:helvetica, arial, sans-serif;
  font-weight:normal;
  margin-left:10%;
  margin-right:10%;
}
html { padding: 0px; }
</style>
<body>
<div class="centered">
 <h2>Student Response Evaluation Demo</h2>
 <form action="index.jsp">
 <table>
 <tbody>
 	<tr><td>Question</td><td><input type="text" name="question" size="100" value="<%=question%>"/></td></tr>
 	<tr><td>Reference<br/>Answer</td><td><input type="text" size="100" name="teacher" value="<%=teacher%>"/></td></tr>
 	<tr><td>Student<br/>Answer</td><td><input type="text" size="100" name="student" value="<%=student%>"/></td></tr>
 	<tr><td><input type="submit"/></td><td></td></tr>
 </tbody>
 </table>
 </form>

<% if (result!=null) { %>
	<h3>Result:</h3>
 	<%=StringEscapeUtils.escapeHtml4(result) %>
 <% } %>
 </div>
</body>
</html>

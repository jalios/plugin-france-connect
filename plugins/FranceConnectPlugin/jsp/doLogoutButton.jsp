<%@ page contentType="text/html; charset=UTF-8"%><%
%><%@ include file='/jcore/doInitPage.jspf'%><%

if (loggedMember!=null && loggedMember.getLogin().startsWith("franceconnect")) {
	
	jcmsContext.addJavaScript("https://fcp.integ01.dev-franceconnect.fr/js/franceconnect.js");
%>
<style>
#fconnect-profile > a {
   
    color: #000000;
       padding: 0px 0 0px 25px;
    
    margin-right: 2px;
    font-size: 12px;
    
    background-size: 20px;
}
</style>
<div id="fconnect-profile" data-fc-logout-url="<%= ResourceHelper.getLogout() %>" class="topbar-item" ><a href="#" title="FranceConnect"><span class="glyph-alt">FranceConnect</span></a></div>

<%} %>

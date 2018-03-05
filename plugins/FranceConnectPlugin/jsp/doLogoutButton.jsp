<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file='/jcore/doInitPage.jspf'%>

<%

if(loggedMember!=null && loggedMember.getLogin().startsWith("franceconnect")){
	
	jcmsContext.addJavaScript("http://fcp.integ01.dev-franceconnect.fr/js/franceconnect.js");
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
<div id="fconnect-profile" data-fc-logout-url="plugins/FranceConnectPlugin/jsp/doLogout.jsp" class="topbar-item" ><a href="#" > Se déconnecter</a></div>

<%} %>

<%@ page import="bzh.jcmsplugin.fc.handlers.FranceConnectAuthenticationHandler" %><%
%><%@ page contentType="text/html; charset=UTF-8"%><%
%><%@ include file='/jcore/doInitPage.jspf'%><%

final String fcKitURL = channel.getProperty("jcmsplugin.franceconnect.kit.url", null);
final boolean isFcKitEnabled = channel.getBooleanProperty("jcmsplugin.franceconnect.kit.enabled", false);
final boolean isFcUser = isLogged && FranceConnectAuthenticationHandler.isFranceConnectSession(session);

if (isFcKitEnabled && Util.notEmpty(fcKitURL) && isFcUser) {
	jcmsContext.addJavaScript(fcKitURL);
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

<% } %>
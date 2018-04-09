<%@ page import="bzh.jcmsplugin.fc.handlers.FranceConnectAuthenticationHandler" %><%
%><%@ page contentType="text/html; charset=UTF-8"%><%
%><%@ include file='/jcore/doInitPage.jspf'%><%

final String fcKitURL = channel.getProperty("jcmsplugin.franceconnect.kit.url", null);
final boolean isFcKitEnabled = channel.getBooleanProperty("jcmsplugin.franceconnect.kit.enabled", false);
final boolean isFcUser = isLogged && FranceConnectAuthenticationHandler.isFranceConnectSession(session);

if (isFcKitEnabled && Util.notEmpty(fcKitURL) && isFcUser) {
	jcmsContext.addJavaScript(fcKitURL);
	final String css = channel.getProperty("jcmsplugin.franceconnect.kit.css." + JcmsInfo.RELEASE_MAJOR, channel.getProperty("jcmsplugin.franceconnect.kit.css", ""));
  if (Util.notEmpty(css)) {
    jcmsContext.addCSSHeader(css);
  }
%>
<div id="fconnect-profile" data-fc-logout-url="<%= ResourceHelper.getLogout() %>" class="topbar-item topbar-item-wrapper" ><a href="#" title="FranceConnect"><span class="glyph-alt">FranceConnect</span></a></div>
<% } %>
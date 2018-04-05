<%@page import="bzh.jcmsplugin.fc.FranceConnectUtils"%><%
%><%@page import="com.jalios.jcms.handler.EditMemberHandler"%><%
%><%@ page contentType="text/html; charset=UTF-8"%><%
%><%@ include file='/jcore/doInitPage.jspf'%><%

Object formHandler = request.getAttribute("formHandler");
if (!(formHandler instanceof EditMemberHandler)) {
  return;
}

EditMemberHandler memberFormHandler = (EditMemberHandler) formHandler;
Member editedMember = memberFormHandler.getMember();
if (!FranceConnectUtils.isFranceConnectUser(editedMember)) {
  return;
}

%>
<jalios:javascript>

// Disable modification of fields retrieved in FranceConnect
jQuery('.widget-name-salutation').addClass('disabled').find('SELECT').prop('disabled', 'disabled');
jQuery('.widget-name-login, .widget-name-name, .widget-name-firstName').addClass('disabled').find('INPUT' ).prop('disabled', 'disabled');

// Hide password fields 
jQuery('.widget-name-password0, .widget-name-password1, .widget-name-password2').hide();

</jalios:javascript>
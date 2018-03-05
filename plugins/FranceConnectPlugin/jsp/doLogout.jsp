<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file='/jcore/doInitPage.jspf'%>
<%@ page import="bzh.jcmsplugin.fc.oauth.*"%>
<%@ page import="com.jalios.jcmsplugin.socialauth.*"%>
<%@ page import="com.jalios.jcmsplugin.oauth.*"%>
<%@ page import="org.scribe.oauth.*"%>
<%@ page import="org.scribe.utils.*"%>
<%@ page import="org.scribe.model.*"%>
<%@ page import="org.json.*"%>

<%
	OAuthProviders<SocialAuthOAuthProvider> providers = OAuthUtil.loadProviders(
			"jcmsplugin.socialauth.provider", "plugins/SocialAuthenticationPlugin/jsp/oauthcallback.jsp",
			SocialAuthOAuthProvider.class);
	SocialAuthOAuthProvider localSocialAuthOAuthProvider = (SocialAuthOAuthProvider) providers
			.getProviderFromSession(session);
	String redirect = "front/logout.jsp";
	
	if (localSocialAuthOAuthProvider != null) {
		if (logger.isDebugEnabled()) {
			logger.debug("Request OAuth logout on " + localSocialAuthOAuthProvider.getName());
		}

		OAuthService ohs = localSocialAuthOAuthProvider.getService();
		Token localToken = (Token) session.getAttribute("SC_TOKEN");
		if (Util.notEmpty(localToken)) {
		
			String raw = localToken.getRawResponse();

			String idTokenValue = "";
			try {
				JSONObject jsonRoot = new JSONObject(raw);

				if (jsonRoot.has("id_token")) {
					idTokenValue = jsonRoot.getString("id_token");
					// this value comes back in the id token and is checked
					// there

					String state = (String) session
							.getAttribute(FranceConnectEntreprisesApi.STATE_SESSION_VARIABLE);
					String logoutUrl = "";
					if(ohs.getClass().getName().startsWith("bzh.jcmsplugin.fc.oauth.FranceConnectEntreprisesApi"))
						logoutUrl = channel.getProperty("jcmsplugin.socialauth.provider.franceconnectentreprises.logoutUrl");
					else if(ohs.getClass().getName().startsWith("bzh.jcmsplugin.fc.oauth.FranceConnectParticuliersApi"))
						
						logoutUrl = channel.getProperty("jcmsplugin.socialauth.provider.franceconnectparticuliers.logoutUrl");
					
					if(Util.notEmpty(logoutUrl))
					 redirect = String.format(
							 logoutUrl + "?id_token_hint=%s&post_logout_redirect_uri=%s",
							new Object[] { idTokenValue,OAuthEncoder.encode(ServletUtil.getBaseUrl(request)+"front/logout.jsp") });
					sendRedirect(redirect);
					return;

				}
			} catch (Exception e) {
				logger.info("No id token for FranceConnect logout", e);
			} finally {
				sendRedirect(redirect);
				return;
			}
		}
		else sendRedirect(redirect);
		return;

	}
	else sendRedirect(redirect);
	return;
%>
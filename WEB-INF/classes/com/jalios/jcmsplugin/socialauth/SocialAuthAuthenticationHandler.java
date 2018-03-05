package com.jalios.jcmsplugin.socialauth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;

import com.jalios.io.IOUtil;
import com.jalios.jcms.ControllerStatus;
import com.jalios.jcms.HttpUtil;
import com.jalios.jcms.JcmsUtil;
import com.jalios.jcms.Member;
import com.jalios.jcms.ResourceHelper;
import com.jalios.jcms.authentication.AuthenticationContext;
import com.jalios.jcms.authentication.AuthenticationHandler;
import com.jalios.jcms.context.JcmsContext;
import com.jalios.jcms.context.JcmsJspContext;
import com.jalios.jcms.context.JcmsMessage;
import com.jalios.jcms.dbmember.DBMember;
import com.jalios.jcms.plugin.Plugin;
import com.jalios.jcmsplugin.oauth.OAuthProviders;
import com.jalios.jcmsplugin.oauth.OAuthUtil;
import com.jalios.util.ServletUtil;
import com.jalios.util.Util;

import bzh.jcmsplugin.fc.oauth.FranceConnectEntreprisesApi;

public class SocialAuthAuthenticationHandler extends AuthenticationHandler {
	public static final String PROVIDER_PARAM = "oauthprovider";
	public static final String SOCIALAUTH_MEMBER_CREATION = "socialauth.member-creation";
	public static final String OAUTH_CALLBACK_URL = "plugins/SocialAuthenticationPlugin/jsp/oauthcallback.jsp";
	public static final String UPLOAD_DIR = "upload";
	public static final String PHOTO_DIR = "upload/photos/";
	public static final String MBR_PHOTO_DIR = "upload/photos/";
	private static Logger logger = Logger.getLogger(SocialAuthAuthenticationHandler.class);
	public static SocialAuthAuthenticationHandler instance;
	OAuthProviders<SocialAuthOAuthProvider> providers;

	public static SocialAuthAuthenticationHandler getInstance() {
		return instance;
	}

	public boolean createNewAccount = true;
	public boolean createDBMember = true;

	public boolean init(Plugin paramPlugin) {
		instance = this;
		logger = paramPlugin.getLogger();
		loadProperties();

		return true;

	}

	public void loadProperties() {
		this.providers = OAuthUtil.loadProviders("jcmsplugin.socialauth.provider",
				"plugins/SocialAuthenticationPlugin/jsp/oauthcallback.jsp", SocialAuthOAuthProvider.class);

		this.createNewAccount = channel.getBooleanProperty("jcmsplugin.socialauth.create-new-account", true);
		this.createDBMember = channel.getBooleanProperty("jcmsplugin.socialauth.create-db-member", true);
	}

	public Set<SocialAuthOAuthProvider> getProviderSet() {
		return this.providers.getProviderSet();
	}

	public void login(AuthenticationContext paramAuthenticationContext) throws IOException {
		if (requestSocialAuthentication(paramAuthenticationContext)) {
			return;
		}
		HttpServletRequest localHttpServletRequest = paramAuthenticationContext.getRequest();
		String str1 = ServletUtil.getResourcePath(localHttpServletRequest);
		if ("plugins/SocialAuthenticationPlugin/jsp/oauthcallback.jsp".equals(str1)) {
			Member localMember = checkSocialAuthenticationResponse(paramAuthenticationContext);
			String str2;
			if (localMember != null) {
				logger.debug("Authentication processed using OAuth");
				paramAuthenticationContext.setLoggedMember(localMember);
				str2 = "index.jsp";
			} else {
				str2 = ResourceHelper.getLogin();
			}
			paramAuthenticationContext.sendRedirect(str2);

			return;
		}
		paramAuthenticationContext.doChain();
	}

	public boolean requestSocialAuthentication(AuthenticationContext paramAuthenticationContext) throws IOException {
		HttpServletRequest localHttpServletRequest = paramAuthenticationContext.getRequest();
		HttpServletResponse localHttpServletResponse = paramAuthenticationContext.getResponse();
		SocialAuthOAuthProvider localSocialAuthOAuthProvider = (SocialAuthOAuthProvider) this.providers
				.getProvider(localHttpServletRequest.getParameter("oauthprovider"));
		if (localSocialAuthOAuthProvider == null) {
			return false;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Request OAuth authentication on " + localSocialAuthOAuthProvider.getName());
		}
		try {
			OAuthUtil.requestOAuthAuthentication(localSocialAuthOAuthProvider, localHttpServletRequest,
					localHttpServletResponse);

			OAuthProviders.storeProviderInSession(localSocialAuthOAuthProvider, localHttpServletRequest.getSession());
			
			return true;
		} catch (OAuthException localOAuthException) {
			logger.warn("OAuth authentication on " + localSocialAuthOAuthProvider.getName() + " could not be processed",
					localOAuthException);
		}
		return false;
	}

	public Member checkSocialAuthenticationResponse(AuthenticationContext paramAuthenticationContext) {
		HttpServletRequest localHttpServletRequest = paramAuthenticationContext.getRequest();
		HttpServletResponse localHttpServletResponse = paramAuthenticationContext.getResponse();
		HttpSession session = localHttpServletRequest.getSession();
		SocialAuthOAuthProvider localSocialAuthOAuthProvider = (SocialAuthOAuthProvider) this.providers
				.getProviderFromSession(localHttpServletRequest.getSession());
		String str1 = localSocialAuthOAuthProvider.getId();
		String str2 = localSocialAuthOAuthProvider.getName();

		logger.debug("Verify OAuth token...");
		Token localToken;
		try {
			localToken = OAuthUtil.checkOAuthResponse(localSocialAuthOAuthProvider, localHttpServletRequest,
					localHttpServletResponse);
			session.setAttribute("SC_TOKEN",localToken);
			
		} catch (Exception localException1) {
			logger.warn("Could not authenticate user through " + str2 + " OAuth provider.", localException1);
			String localObject2 = JcmsUtil.glp(paramAuthenticationContext.getUserLang(),
					"jcmsplugin.socialauth.msg.login-failure", new Object[] { str2 });
			JcmsJspContext.addMsgSession(localHttpServletRequest,
					new JcmsMessage(JcmsMessage.Level.WARN, (String) localObject2));
			return null;
		}
		if (localToken == null) {
			logger.debug("OAuthAuthentication failed.");
			String localObject1 = JcmsUtil.glp(paramAuthenticationContext.getUserLang(),
					"jcmsplugin.socialauth.msg.login-failure", new Object[] { str2 });
			JcmsJspContext.addMsgSession(localHttpServletRequest,
					new JcmsMessage(JcmsMessage.Level.WARN, (String) localObject1));
			return null;
		}
		logger.debug("OAuthAuthentication was successful, retrieve user informations...");
		Object localObject1 = localSocialAuthOAuthProvider.getUserInfos(localToken);
		if (localObject1 == null) {
			logger.debug("User informations could not be retrieved, OAuth fail...");
			String localObject2 = JcmsUtil.glp(paramAuthenticationContext.getUserLang(),
					"jcmsplugin.socialauth.msg.userinfos-notretrieved", new Object[] { str2 });
			JcmsJspContext.addMsgSession(localHttpServletRequest,
					new JcmsMessage(JcmsMessage.Level.WARN, (String) localObject2));
			return null;
		}
		logger.debug("User informations successfully retrieved, looking for matching JCMS Member...");
		if (logger.isTraceEnabled()) {
			logger.trace("User informations : " + localObject1);
		}
		Member localObject2 = channel.getMemberFromLogin(str1 + "." + ((UserInfos) localObject1).getLogin());
		if (localObject2 != null) {
			boolean bool = Util.toBoolean(((Member) localObject2).getExtraDBData("oauth"), false);
			String localObject4 = Util.getString(((Member) localObject2).getExtraDBData("oauth-provider"), "");
			if ((!bool) || (!((String) localObject4).equals(str1))) {
				JcmsUtil.logSecurityIssue("Member '" + localObject2 + "' (" + ((Member) localObject2).getId()
						+ ") was found for OAuth authentication of '" + ((UserInfos) localObject1).getLogin() + "' on "
						+ str2
						+ ". But this account was not created using OAuth plugin with the same provider. Authentication will be forbidden");
				String localObject5 = JcmsUtil.glp(paramAuthenticationContext.getUserLang(),
						"jcmsplugin.socialauth.msg.login-unauthorized",
						new Object[] { str2, ((UserInfos) localObject1).getLogin() });
				JcmsJspContext.addMsgSession(localHttpServletRequest,
						new JcmsMessage(JcmsMessage.Level.WARN, (String) localObject5));
				return null;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Logging with existing member '" + localObject2 + "' (" + ((Member) localObject2).getId()
						+ ") whose login matches '" + ((UserInfos) localObject1).getLogin() + "'");
			}
			return localObject2;
		}
		if (logger.isDebugEnabled()) {
			logger.debug(
					"No member found matching OAuth login '" + ((UserInfos) localObject1).getLogin() + "'. "
							+ (this.createNewAccount
									? "Creating new " + (this.createDBMember ? "DBMember" : "Member") + "..."
									: "Account creation is disabled, refuse authentication request..."));
		}
		if (!this.createNewAccount) {
			String localObject3 = JcmsUtil.glp(paramAuthenticationContext.getUserLang(),
					"jcmsplugin.socialauth.msg.create-unauthorized",
					new Object[] { str2, ((UserInfos) localObject1).getLogin() });
			JcmsJspContext.addMsgSession(localHttpServletRequest,
					new JcmsMessage(JcmsMessage.Level.WARN, (String) localObject3));
			return null;
		}
		localObject2 = this.createDBMember ? new DBMember() : new Member();
		((Member) localObject2).setLogin(str1 + "." + ((UserInfos) localObject1).getLogin());
		((Member) localObject2).setName(((UserInfos) localObject1).getLastname());
		((Member) localObject2).setFirstName(((UserInfos) localObject1).getFirstName());
		((Member) localObject2).setEmail(((UserInfos) localObject1).getEmail());
		((Member) localObject2).setPassword("*");
		((Member) localObject2).setExtraDBData("oauth", "true");
		((Member) localObject2).setExtraDBData("oauth-provider", str1);
		((Member) localObject2).setEmail(((UserInfos) localObject1).getEmail());
		((Member) localObject2).setPhoto(((UserInfos) localObject1).getPicture());
		Object localObject3 = channel.getDefaultAdmin();
		Object localObject4 = Util.getHashMap("socialauth.member-creation", Boolean.TRUE);
		Object localObject5 = ((Member) localObject2).checkCreate((Member) localObject3, (Map) localObject4);
		if (((ControllerStatus) localObject5).isOK()) {
			((Member) localObject2).performCreate(channel.getDefaultAdmin(), (Map) localObject4);
			try {
				File localFile1 = new File(((UserInfos) localObject1).getPicture());
				File localFile2 = new File(channel.getUploadParentPath());
				String str4 = JcmsUtil.getNewDirectory(localFile2, "upload/photos/");
				File localFile3 = new File(localFile2, str4);
				String str5 = IOUtil.getExtension(localFile1);
				if (StringUtils.isEmpty(str5)) {
					str5 = "png";
				}
				if (str5.indexOf("?") != -1) {
					str5 = str5.substring(0, str5.indexOf("?"));
				}
				File localFile4 = new File(localFile3, "mbr_" + ((Member) localObject2).getId() + "." + str5);
				saveImage(((UserInfos) localObject1).getPicture(), localFile4.getAbsolutePath());
				((Member) localObject2)
						.setPhoto(JcmsUtil.getNewDirectory(localFile2, "upload/photos/") + localFile4.getName());
				((Member) localObject2).checkAndPerformUpdate((Member) localObject2);
			} catch (Exception localException2) {
				logger.debug(localException2.getMessage());
			}
			return localObject2;
		}
		String str3 = JcmsUtil.glp(paramAuthenticationContext.getUserLang(), "jcmsplugin.socialauth.msg.create-failed",
				new Object[] { str2,
						((ControllerStatus) localObject5).getMessage(paramAuthenticationContext.getUserLang()) });
		JcmsJspContext.addMsgSession(localHttpServletRequest, new JcmsMessage(JcmsMessage.Level.WARN, str3));
		return null;
	}

	private void saveImage(String paramString1, String paramString2) throws IOException {
		URL localURL = new URL(paramString1);
		InputStream localInputStream = localURL.openStream();
		FileOutputStream localFileOutputStream = new FileOutputStream(paramString2);

		byte[] arrayOfByte = new byte[2048];
		int i;
		while ((i = localInputStream.read(arrayOfByte)) != -1) {
			localFileOutputStream.write(arrayOfByte, 0, i);
		}
		localInputStream.close();
		localFileOutputStream.close();
	}

	

	

}

package bzh.jcmsplugin.fc.oauth;

import java.math.BigInteger;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.security.SecureRandom;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

import com.jalios.jcms.Channel;
import com.jalios.jcmsplugin.socialauth.SocialAuthAuthenticationHandler;
import com.jalios.util.JProperties;
import com.jalios.util.JPropertiesListener;
import com.jalios.util.Util;

import bzh.jcmsplugin.fc.extractors.FranceConnectJsonTokenExtractor;

public class FranceConnectParticuliersApi extends DefaultApi20 implements JPropertiesListener {
	private static final Logger logger = Logger.getLogger(FranceConnectParticuliersApi.class);
	public final static String NONCE_SESSION_VARIABLE = "nonce";
	private String scope = "";
	private String fcTokenUrl = "";
	private String fcAuthorizeUrl = "";
	private String apiSecret = "";
	private String fcLogoutUrl = "";
	
	private FranceConnectJsonTokenExtractor franceConnectJsonTokenExtractor= null;

	private Channel channel = Channel.getChannel();

	public FranceConnectParticuliersApi() {
		super();
		initProperties();
		initSSLProxy();
	}

	public void initSSLProxy() {
		Channel channel = Channel.getChannel();
		boolean bool = channel.getBooleanProperty("http.proxy.enabled",
				Util.notEmpty(channel.getProperty("http.proxyHost", "")));

		if (bool)
			System.setProperty("https.proxyHost", channel.getProperty("http.proxyHost"));
		System.setProperty("https.proxyPort", channel.getProperty("http.proxyPort"));
		final String login = channel.getProperty("http.proxy.login");
		if (Util.notEmpty(login)) {
			final String pwd = channel.getProperty("http.proxy.password");
			Authenticator.setDefault(new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {

					return new PasswordAuthentication(login, Util.reveal(pwd).toCharArray());
				}
			});

		}
	}

	public void propertiesChange(JProperties paramJProperties) {
		initProperties();
	}

	private void initProperties() {
		
		this.scope = channel.getProperty("jcmsplugin.socialauth.provider.franceconnectparticuliers.scope");
		this.fcTokenUrl = channel.getProperty("jcmsplugin.socialauth.provider.franceconnectparticuliers.tokenUrl");
		this.fcAuthorizeUrl = channel.getProperty("jcmsplugin.socialauth.provider.franceconnectparticuliers.authorizeUrl");
		this.apiSecret = channel.getProperty("jcmsplugin.socialauth.provider.franceconnectparticuliers.apiSecret");
		this.franceConnectJsonTokenExtractor = new FranceConnectJsonTokenExtractor(this.apiSecret);
		this.fcLogoutUrl = channel.getProperty("jcmsplugin.socialauth.provider.franceconnectparticuliers.logoutUrl");
		
	}

	public String getAccessTokenEndpoint() {
		return this.fcTokenUrl;
	}

	public String getAuthorizationUrl(OAuthConfig config) {
		Preconditions.checkValidUrl(config.getCallback(),
				"Must provide a valid url as callback. FranceConnect does not support OOB");
		HttpServletRequest req = channel.getCurrentServletRequest();

		HttpSession session = req.getSession();
		// this value comes back in the id token and is checked there
		String nonce = createNonce(session);

		return String.format(
				this.fcAuthorizeUrl + "?client_id=%s&response_type=code&scope=" + scope + "&nonce="
						+ nonce + "&redirect_uri=%s",
				new Object[] { config.getApiKey(), OAuthEncoder.encode(config.getCallback()) });
	}

	public AccessTokenExtractor getAccessTokenExtractor() {
		return this.franceConnectJsonTokenExtractor;
	}

	@Override
	public Verb getAccessTokenVerb() {
		return Verb.POST;
	}

	/**
	 * Get the named stored session variable as a string. Return null if not
	 * found or not a string.
	 * 
	 * @param session
	 * @param key
	 * @return
	 */
	private static String getStoredSessionString(HttpSession session, String key) {
		Object o = session.getAttribute(key);
		if (o != null && o instanceof String) {
			return o.toString();
		} else {
			return null;
		}
	}

	/**
	 * Create a cryptographically random nonce and store it in the session
	 * 
	 * @param session
	 * @return
	 */
	protected static String createNonce(HttpSession session) {
		String nonce = new BigInteger(50, new SecureRandom()).toString(16);
		session.setAttribute(NONCE_SESSION_VARIABLE, nonce);

		return nonce;
	}

	/**
	 * Get the nonce we stored in the session
	 * 
	 * @param session
	 * @return
	 */
	protected static String getStoredNonce(HttpSession session) {
		return getStoredSessionString(session, NONCE_SESSION_VARIABLE);
	}

	@Override
	public OAuthService createService(final OAuthConfig config) {
		return new OAuth20ServiceImpl(this, config) {
			@Override
			public Token getAccessToken(Token requestToken, Verifier verifier) {

				OAuthRequest request = new OAuthRequest(getAccessTokenVerb(), getAccessTokenEndpoint());

				// basic auth
				request.addBodyParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.GRANT_TYPE_AUTHORIZATION_CODE);
				request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
				request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());

				request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
				request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());

				Response response = request.send();
				String body = response.getBody();
				return getAccessTokenExtractor().extract(body);
			}
		};
	}

}
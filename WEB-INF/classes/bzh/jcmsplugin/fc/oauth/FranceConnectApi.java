package bzh.jcmsplugin.fc.oauth;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Verb;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

import com.jalios.jcms.Channel;
import com.jalios.util.JProperties;
import com.jalios.util.JPropertiesListener;

import bzh.jcmsplugin.fc.extractors.FranceConnectJsonTokenExtractor;

public class FranceConnectApi extends DefaultApi20 implements JPropertiesListener {
	private static final Logger logger = Logger.getLogger(FranceConnectApi.class);
	public final static String STATE_SESSION_VARIABLE = "state";
	public final static String NONCE_SESSION_VARIABLE = "nonce";
	private String scope = "";
	private String fcTokenUrl = "";
	private String fcAuthorizeUrl = "";

	private Channel channel = Channel.getChannel();

	public FranceConnectApi() {
		super();
		initProperties();
	}

	public void propertiesChange(JProperties paramJProperties) {
		initProperties();
	}

	private void initProperties() {
		this.scope = channel.getProperty("jcmsplugin.socialauth.provider.franceconnect.scope");
		this.fcTokenUrl = channel.getProperty("jcmsplugin.socialauth.provider.franceconnect.tokenUrl");
		this.fcAuthorizeUrl = channel.getProperty("jcmsplugin.socialauth.provider.franceconnect.authorizeUrl");

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

		// this value comes back in the auth code response
		String state = createState(session);

		return String.format(
				this.fcAuthorizeUrl + "?client_id=%s&response_type=code&scope=" + scope + "&state=" + state + "&nonce="
						+ nonce + "&redirect_uri=%s",
				new Object[] { config.getApiKey(), OAuthEncoder.encode(config.getCallback()) });
	}
	
	

	public AccessTokenExtractor getAccessTokenExtractor() {
		return new FranceConnectJsonTokenExtractor();
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

	/**
	 * Create a cryptographically random state and store it in the session
	 * 
	 * @param session
	 * @return
	 */
	protected static String createState(HttpSession session) {
		String state = new BigInteger(50, new SecureRandom()).toString(16);
		session.setAttribute(STATE_SESSION_VARIABLE, state);

		return state;
	}

	/**
	 * Get the state we stored in the session
	 * 
	 * @param session
	 * @return
	 */
	protected static String getStoredState(HttpSession session) {
		return getStoredSessionString(session, STATE_SESSION_VARIABLE);
	}

}
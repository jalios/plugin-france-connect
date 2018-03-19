package bzh.jcmsplugin.fc.oauth;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
import com.jalios.util.JProperties;
import com.jalios.util.JPropertiesListener;

import bzh.jcmsplugin.fc.FranceConnectType;
import bzh.jcmsplugin.fc.extractors.FranceConnectJsonTokenExtractor;

/**
 * FranceConnect implementation of Scribe API. 
 * 
 * @since fc-1.9
 */
public abstract class AbstractFranceConnectApi extends DefaultApi20 implements JPropertiesListener {
	
	/**
	 * Name of session attribute used to store the OAuth nonce value.
	 */
	public final static String NONCE_SESSION_VARIABLE = "bzh.jcmsplugin.fc.oauth.nonce";

  private Channel channel = Channel.getChannel();
  
	private String scope = "";
	private String fcTokenUrl = "";
	private String fcAuthorizeUrl = "";
	
	private String apiSecret = "";
	
	private FranceConnectJsonTokenExtractor franceConnectJsonTokenExtractor= null;

  //-------------------------------------------------------------------
  // Abstract methods that must be implemented by provider implementation.
  //-------------------------------------------------------------------
  
  /**
   * Retrieve the type of FranceConnect implementation.
   * @return a FranceConnectType enum, must not return null.
   */
  abstract protected FranceConnectType getType();

  //-------------------------------------------------------------------
  // Constructor
  //-------------------------------------------------------------------
  
	public AbstractFranceConnectApi() {
		super();
		initProperties();
	}

  //-------------------------------------------------------------------
  // Properties
  //-------------------------------------------------------------------
	
	public void propertiesChange(JProperties paramJProperties) {
		initProperties();
	}

	private void initProperties() {
		this.scope = channel.getProperty("jcmsplugin.socialauth.provider.franceconnect"+getType().getSuffix()+".scope");
		
		this.fcTokenUrl = channel.getProperty("jcmsplugin.socialauth.provider.franceconnect"+getType().getSuffix()+".tokenUrl");
		this.fcAuthorizeUrl = channel.getProperty("jcmsplugin.socialauth.provider.franceconnect"+getType().getSuffix()+".authorizeUrl");
		
		this.apiSecret = channel.getProperty("jcmsplugin.socialauth.provider.franceconnect"+getType().getSuffix()+".apiSecret");
		this.franceConnectJsonTokenExtractor = new FranceConnectJsonTokenExtractor(this.apiSecret);
	}

  //-------------------------------------------------------------------
  // OAuth logic
  //-------------------------------------------------------------------
	
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
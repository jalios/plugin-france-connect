package bzh.jcmsplugin.fc.extractors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.scribe.exceptions.OAuthException;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.Token;
import org.scribe.utils.Preconditions;

import com.jalios.jcms.Channel;
import com.jalios.jcmsplugin.socialauth.SocialAuthAuthenticationHandler;
import com.jalios.util.Util;

import bzh.jcmsplugin.fc.oauth.AbstractFranceConnectApi;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class FranceConnectJsonTokenExtractor extends JsonTokenExtractor  {

	private static final Logger logger = Logger.getLogger(FranceConnectJsonTokenExtractor.class);
	private int timeSkewAllowance = 300;

	private String apiSecret;

	public FranceConnectJsonTokenExtractor(String apiSecret) {
		this.apiSecret = apiSecret;
	}


	@Override
	public Token extract(String response) {
		
		
		Preconditions.checkEmptyString(response, "Cannot extract a token from a null or empty String");
		String idTokenValue;

		try {
			JSONObject jsonRoot = new JSONObject(response);

			if (jsonRoot.has("id_token")) {
				idTokenValue = jsonRoot.getString("id_token");
			} else {
				logger.error("Token Endpoint did not return an id_token");
				throw new OAuthException("Token Endpoint did not return an id_token");
			}

			String storedState = getStoredState();
			HttpServletRequest req = Channel.getChannel().getCurrentServletRequest();
			String state = req.getParameter("state");
			if(state==null || !state.equals(storedState) ){
				logger.error("Possible attack detected! The comparison of the state in the returned "
						+ "ID Token to the session " + SocialAuthAuthenticationHandler.OAUTH_STATE + " failed. Expected "
						+ storedState + " got " + state + ".");

				throw new OAuthException("Possible  attack detected! The comparison of the state in the returned "
						+ "ID Token to the session " + SocialAuthAuthenticationHandler.OAUTH_STATE + " failed. Expected "
						+ storedState + " got " + state + ".");
			}
			

			// on parse avec jwts ce qui va févirier l'encryption du jwt, le
			// issuer et l'expiration automatiquement
			Claims claims = Jwts.parser().setAllowedClockSkewSeconds(timeSkewAllowance)
					.setSigningKey(this.apiSecret.getBytes("UTF-8"))
					.parseClaimsJws(idTokenValue).getBody();

			
			// compare the nonce to our stored claim
			String nonce = (String) claims.get("nonce");
			if (Util.isEmpty(nonce)) {

				logger.error("ID token did not contain a nonce claim.");

				throw new OAuthException("ID token did not contain a nonce claim.");
			}

			String storedNonce = getStoredNonce();
			if (!nonce.equals(storedNonce)) {
				logger.error("Possible replay attack detected! The comparison of the nonce in the returned "
						+ "ID Token to the session " + AbstractFranceConnectApi.NONCE_SESSION_VARIABLE + " failed. Expected "
						+ storedNonce + " got " + nonce + ".");

				throw new OAuthException("Possible replay attack detected! The comparison of the nonce in the returned "
						+ "ID Token to the session " + AbstractFranceConnectApi.NONCE_SESSION_VARIABLE + " failed. Expected "
						+ storedNonce + " got " + nonce + ".");
			}

			return super.extract(response);
		} catch (Exception e) {
			throw new OAuthException("Couldn't parse idToken: ", e);
		}
	
	}

	protected static String getStoredNonce() {
		return getStoredSessionString(AbstractFranceConnectApi.NONCE_SESSION_VARIABLE);
	}

  /**
   * Get the state that was stored in the session by the SocialAuthHandler
   * 
   * @param session
   * @return
   */
  protected static String getStoredState() {
    return getStoredSessionString(SocialAuthAuthenticationHandler.OAUTH_STATE);
  }

	private static String getStoredSessionString(String key) {
		HttpServletRequest req = Channel.getChannel().getCurrentServletRequest();

		HttpSession session = req.getSession();

		Object o = session.getAttribute(key);
		if (o != null && o instanceof String) {
			return o.toString();
		} else {
			return null;
		}
	}


}

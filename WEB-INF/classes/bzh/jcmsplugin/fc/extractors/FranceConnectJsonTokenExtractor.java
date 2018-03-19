package bzh.jcmsplugin.fc.extractors;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.scribe.exceptions.OAuthException;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.Token;
import org.scribe.utils.Preconditions;

import com.fasterxml.jackson.databind.ObjectMapper;
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
		

		try {
		  final String idTokenValue = getIdToken(response);
			if (Util.isEmpty(idTokenValue)) {
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

	/**
	 * Read JSON response as java HashMap.
	 * @param json a JSON content, may be null
	 * @return a Map, may return null.
	 * @since fc-1.9
	 */
  public static Map<?,?> json2map(String json) {
    try {
      if (json == null) {
        return null;
      }
      // Process empty JSON in a faster way 
      if ("{}".equals(json)) {
        return new HashMap<Object,Object>();
      }
      return new ObjectMapper().readValue(json, HashMap.class);
    } catch (Exception ex) {
      logger.debug("json2map: An exception occured with json: " + json, ex);
      return null;
    }
  }
  
	/**
	 * Parse the specified JSON response to read the id_token.
	 * @param jsonResponse the JSON response 
	 * @return an id_token value, may return null
   * @since fc-1.9
	 */
  public static String getIdToken(String jsonResponse) { 
	  Map<?,?> map = json2map(jsonResponse);

	  if (map != null) {
	    return Util.getString(map.get("id_token"), null);
	  }
	  
	  return null;
  }
}

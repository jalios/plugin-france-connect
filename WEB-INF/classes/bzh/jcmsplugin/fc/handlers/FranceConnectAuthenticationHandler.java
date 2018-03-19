package bzh.jcmsplugin.fc.handlers;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;

import com.jalios.jcms.authentication.AuthenticationContext;
import com.jalios.jcms.authentication.AuthenticationHandler;
import com.jalios.jcmsplugin.socialauth.SocialAuthAuthenticationHandler;
import com.jalios.jcmsplugin.socialauth.SocialAuthOAuthProvider;
import com.jalios.util.ServletUtil;
import com.jalios.util.Util;

/**
 * Provides logout for FranceConnect (FC).
 * <p>
 * The following behavior is expected by FCspecification : 
 * <ol>
 *  <li>User is loggued out (disconnected) from local JCMS site,</li>
 *  <li>Then user is redirected to FC logout which informs user disconnect was successful, 
 *   and propose logging out from france connect, or continuing.</li>
 *  <li>User is redirected to the final logout URL specified to FC callback (which may be the site or not)</li>
 * </ol> 
 * 
 * cf https://partenaires.franceconnect.gouv.fr/fournisseur-service#acceptance
 */
public class FranceConnectAuthenticationHandler extends AuthenticationHandler {

  private static final Logger logger = Logger.getLogger(FranceConnectAuthenticationHandler.class);

  /**
   * Order used by the FranceConnectAuthenticationHandler
   * 
   * Very low value (-100) to ensure : 
   *  - chain and all other authentication can be invoked before this one
   *  - this handler can perform the last redirect operation (to guarantee redirection to FranceConnect is performed) 
   */
  public static final int ORDER_FC_HANDLER = -1000;

  //-----------------------------------------------
  // Constructor 
  //-----------------------------------------------

  public FranceConnectAuthenticationHandler() {
    logger.info("FranceConnectAuthenticationHandler init");
    setOrder(ORDER_FC_HANDLER);
  }
  
  //-----------------------------------------------
  // AuthenticationHandler implementation 
  //-----------------------------------------------

  /**
   * This methods is called when users logout from JCMS. <br>
   * <b>It may not be called if user simply close its browser. Don't rely on this for
   * critical operation</b><br>
   * <br>
   * Default implementation is to invoke the next handler in the chain.
   * @param ctxt the {@link AuthenticationContext} used for this login
   * @throws IOException 
   */
  @Override
  public void logout(AuthenticationContext ctxt) throws IOException {
    HttpSession session = ctxt.getRequest().getSession();

    SocialAuthOAuthProvider localSocialAuthOAuthProvider = SocialAuthAuthenticationHandler.getInstance().getProviders().getProviderFromSession(session);
    Token localToken = (Token) session.getAttribute(SocialAuthAuthenticationHandler.SOCIALAUTH_MEMBER_TOKEN);

    ctxt.doChain();

    if (localSocialAuthOAuthProvider != null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Request OAuth logout on " + localSocialAuthOAuthProvider.getName());
      }

      OAuthService ohs = localSocialAuthOAuthProvider.getService();
      if (Util.notEmpty(localToken)) {

        String raw = localToken.getRawResponse();

        String idTokenValue = "";
        try {
          JSONObject jsonRoot = new JSONObject(raw);

          if (jsonRoot.has("id_token")) {
            idTokenValue = jsonRoot.getString("id_token");
            // this value comes back in the id token and is checked
            // there

            String logoutUrl = "";
            if (ohs.getClass().getName().startsWith("bzh.jcmsplugin.fc.oauth.FranceConnectEntreprisesApi")) {
              logoutUrl = channel.getProperty("jcmsplugin.socialauth.provider.franceconnectentreprises.logoutUrl");
            }
            else if (ohs.getClass().getName().startsWith("bzh.jcmsplugin.fc.oauth.FranceConnectParticuliersApi")) {
              logoutUrl = channel.getProperty("jcmsplugin.socialauth.provider.franceconnectparticuliers.logoutUrl");              
            }

            if (Util.notEmpty(logoutUrl)) {
              String redirect = String.format(logoutUrl + "?id_token_hint=%s&post_logout_redirect_uri=%s",
                                              idTokenValue, 
                                              OAuthEncoder.encode(ServletUtil.getBaseUrl(ctxt.getRequest())));
              ctxt.sendRedirect(redirect);
            }

          }
        } catch (Exception e) {
          logger.warn("No id token for FranceConnect logout", e);
        }

      }
    }
  }

}

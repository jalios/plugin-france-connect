package bzh.jcmsplugin.fc.handlers;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.scribe.model.Token;
import org.scribe.utils.OAuthEncoder;

import com.jalios.jcms.authentication.AuthenticationContext;
import com.jalios.jcms.authentication.AuthenticationHandler;
import com.jalios.jcmsplugin.socialauth.SocialAuthAuthenticationHandler;
import com.jalios.jcmsplugin.socialauth.SocialAuthOAuthProvider;
import com.jalios.util.ServletUtil;
import com.jalios.util.Util;

import bzh.jcmsplugin.fc.extractors.FranceConnectJsonTokenExtractor;
import bzh.jcmsplugin.fc.oauth.AbstractFranceConnectProvider;

/**
 * Provides logout for FranceConnect (FC).
 * <p>
 * The following behavior is expected by FC specification : 
 * <ol>
 *  <li>User is logged out (disconnected) from local JCMS site,</li>
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
   * @param ctxt the {@link AuthenticationContext} used for this logout
   * @throws IOException 
   */
  @Override
  public void logout(AuthenticationContext ctxt) throws IOException {
    HttpSession session = ctxt.getRequest().getSession();

    SocialAuthOAuthProvider socialAuthProvider = SocialAuthAuthenticationHandler.getInstance().getProviders().getProviderFromSession(session);
    Token memberToken = (Token) session.getAttribute(SocialAuthAuthenticationHandler.SOCIALAUTH_MEMBER_TOKEN);

    ctxt.doChain();

    if (socialAuthProvider instanceof AbstractFranceConnectProvider) {
      final AbstractFranceConnectProvider fcProvider = (AbstractFranceConnectProvider) socialAuthProvider;
      doFranceConnectLogout(ctxt, fcProvider, memberToken);      
    }
  }
  
  /**
   * Logout user from FranceConnect if all condition required were met.
   * @param ctxt 
   * @param ctxt the {@link AuthenticationContext} used for this logout
   * @param fcProvider the FranceConnectProvider instance for which logout is requested
   * @param memberToken the Scribe Member token stored during authentication  
   * @throws IOException in case of error during redirection
   */
  private void doFranceConnectLogout(AuthenticationContext ctxt, final AbstractFranceConnectProvider fcProvider, final Token memberToken) throws IOException {
    
    // Check member token exists
    if (Util.isEmpty(memberToken)) {
      if (logger.isDebugEnabled()) {
        logger.debug("Invalid member token for current session. Cannot logout from " + fcProvider.getName());
      }
      return;
    }

    // Check logout URL configured
    final String logoutUrl = fcProvider.getLogoutUrl();
    if (Util.isEmpty(logoutUrl)) {
      if (logger.isDebugEnabled()) {
        logger.debug("No logout URL configured. Cannot logout from " + fcProvider.getName());
      }
      return;
    }

    // Extract ID token from member token
    final String idTokenValue = FranceConnectJsonTokenExtractor.getIdToken(memberToken.getRawResponse());
    if (Util.isEmpty(idTokenValue)) {
      if (logger.isDebugEnabled()) {
        logger.debug("Missing id_token. Cannot logout from " + fcProvider.getName());
      }
      return;
    }
    
    // Everything is fine, redirect to logout URL
    String redirect = String.format(logoutUrl + "?id_token_hint=%s&post_logout_redirect_uri=%s",
                                    idTokenValue, 
                                    OAuthEncoder.encode(ServletUtil.getBaseUrl(ctxt.getRequest())));
    if (logger.isDebugEnabled()) {
      logger.debug("Logout from " + fcProvider.getName() + ". Redirected user to " + redirect);
    }
    ctxt.sendRedirect(redirect);
  }

  
  /**
   * Check if the current session is a session where the user signed in using FranceConnect.
   * @param session the current HttpSession
   * @return true if current session is a FranceConnect session, false otherwise
   * @since fc-1.9
   */
  public static boolean isFranceConnectSession(HttpSession session) {
    if (session == null) {
      return false;
    }
    SocialAuthOAuthProvider socialAuthProvider = SocialAuthAuthenticationHandler.getInstance().getProviders().getProviderFromSession(session);
    return socialAuthProvider instanceof AbstractFranceConnectProvider;
  }
}

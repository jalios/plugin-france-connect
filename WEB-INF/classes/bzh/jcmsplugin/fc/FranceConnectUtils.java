package bzh.jcmsplugin.fc;

import com.jalios.jcms.Member;
import com.jalios.util.Util;

/**
 * Utility class for FranceConnect implementation.
 * @since fc-1.9
 */
public class FranceConnectUtils {

  /**
   * Check if the specified Member was created by SocialAuthentication
   * for a FranceConnect user.  
   * @param mbr the member being verified
   * @return true if specified member is a FranceConnect user, false otherwise
   */
  public static boolean isFranceConnectUser(Member mbr) {
    if (mbr == null) {
      return false;
    }
    final String login = Util.getString(mbr.getLogin(), "");
    final boolean isEntrepriseUser = login.startsWith("franceconnect" + FranceConnectType.ENTREPRISES.getSuffix() + ".");
    final boolean isParticulierUser = login.startsWith("franceconnect" + FranceConnectType.PARTICULIERS.getSuffix() + ".");
    final boolean isAgentUser = login.startsWith("franceconnect" + FranceConnectType.AGENTS.getSuffix() + ".");
    return isEntrepriseUser || isParticulierUser || isAgentUser;
  }

}

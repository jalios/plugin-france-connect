package bzh.jcmsplugin.fc.controllers;

import java.util.Map;

import com.jalios.jcms.BasicDataController;
import com.jalios.jcms.ControllerStatus;
import com.jalios.jcms.Data;
import com.jalios.jcms.JcmsUtil;
import com.jalios.jcms.Member;
import com.jalios.jcms.db.HibernateUtil;
import com.jalios.jcms.dbmember.DBMember;
import com.jalios.util.Util;

import bzh.jcmsplugin.fc.FranceConnectType;

/**
 * DataController used to restrict update operation on FranceConnect Member.
 * 
 * @since fc-1.9
 */
public class FranceConnectMemberDataController extends BasicDataController {
  
  @Override
  public ControllerStatus checkWrite(Data data, int op, Member opAuthor, boolean checkIntegrity, @SuppressWarnings("rawtypes") Map context) {
    if (op != OP_UPDATE) {
      return ControllerStatus.OK;
    }
    
    if (!(data instanceof Member)) {
      return ControllerStatus.OK;
    }
    
    final Member mbr = (Member) data;
    final Member original;
    if (mbr instanceof DBMember) {
      original = (Member) HibernateUtil.getCurrentData(mbr.getId());
    } else {
      original = channel.getMember(mbr.getId());
    }
    
    if (!isFranceConnectUser(original)) {
      return ControllerStatus.OK;
    }

    // For FranceConnect user, if following field are retrieved from the identity provider,
    // their modification must NOT be authorized :
    // Civilité, nom, prénoms, date et lieu de naissance.
    final boolean lastNameFieldModified = !Util.isSameContent(original.getLastName(), mbr.getLastName());
    final boolean firstNameFieldModified = !Util.isSameContent(original.getFirstName(), mbr.getFirstName());
    if (lastNameFieldModified || firstNameFieldModified) {
      final ControllerStatus status = new ControllerStatus(CTRL_TOPIC_WRITE);
      status.setProp("jcmsplugin.franceconnect.status.field-op." + (JcmsUtil.isSameId(mbr, opAuthor) ? "self" : "other")); 
      return status;
    }

    return ControllerStatus.OK;
  }
  
  /**
   * Check if the specified Member was created by SocialAuthentication
   * for a FranceConnect user.  
   */
  static private boolean isFranceConnectUser(Member mbr) {
    if (mbr == null) {
      return false;
    }
    final String login = Util.getString(mbr.getLogin(), "");
    final boolean isEntrepriseUser = login.startsWith("franceconnect" + FranceConnectType.ENTREPRISES.getSuffix() + ".");
    final boolean isParticulierUser = login.startsWith("franceconnect" + FranceConnectType.PARTICULIERS.getSuffix() + ".");
    return isEntrepriseUser || isParticulierUser;
  }
  
}

package bzh.jcmsplugin.fc.dc;

import java.util.Map;

import org.apache.log4j.Logger;

import com.jalios.jcms.BasicDataController;
import com.jalios.jcms.Data;
import com.jalios.jcms.Member;
import com.jalios.jcmsplugin.socialauth.SocialAuthAuthenticationHandler;
import com.jalios.jcmsplugin.socialauth.UserInfos;
import com.jalios.util.Util;


//import bzh.jcmsplugin.cxm.mgr.CXMManager;
//import generated.GroupProfile;


/**
 * Controller pour alimenter le siret à partir d'un UserInfos
 * 
 * @author 09914
 *
 */
public class FranceConnectMemberDataController extends BasicDataController {

  private static final Logger logger = Logger.getLogger(FranceConnectMemberDataController.class);

  @Override
  public void beforeWrite(Data data, int op, Member opMember, Map ctx) {
    if (data instanceof Member && (op == OP_CREATE || op == OP_UPDATE)) {
      Member mbr = (Member) data;
      if (mbr.getLogin().startsWith("franceconnect")) {
        UserInfos userInfos = (ctx != null) ? (UserInfos) ctx.get(SocialAuthAuthenticationHandler.SOCIALAUTH_USER_INFOS) : null;
        String siret = (userInfos != null) ? userInfos.getData("siret") : null;
        if (Util.notEmpty(siret)) {

          logger.info("Member creation/update through FranceConnect Enterprise, siret = " + siret);
          
          // si compte de test/demo FC, on plaque celui de Jalios;
          if (siret.equals("73282932000074")) {
            siret = "44012603500029";
          }

          // A personnaliser
          /*
          CXMManager cxmhg = CXMManager.getInstance();
          GroupProfile gp = cxmhg.getGroupProfile(siret, true);
          if (Util.notEmpty(gp)) {
            Group g = gp.getGroupe();
            mbr.addGroup(g);
          } else {
            // on flaque le groupe par défaut pour les demandeurs
            mbr.addGroup(cxmhg.getGroupATitreParticulier());
          }
          */

        }
      }

    }
  }
  
}

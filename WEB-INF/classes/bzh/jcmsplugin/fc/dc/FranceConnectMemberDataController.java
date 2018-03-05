package bzh.jcmsplugin.fc.dc;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.jalios.jcms.BasicDataController;
import com.jalios.jcms.Channel;
import com.jalios.jcms.Data;
import com.jalios.jcms.Group;
import com.jalios.jcms.Member;
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

	@Override
	public void beforeWrite(Data data, int op, Member opMember, Map ctx) {
		if (data instanceof Member && (op == super.OP_CREATE || op == super.OP_UPDATE)) {
			Member fcMember = (Member) data;
			if (fcMember.getLogin().startsWith("franceconnect")) {
				HttpServletRequest req = Channel.getChannel().getCurrentServletRequest();
				
				Member m = (Member) fcMember.getUpdateInstance();
			//	CXMManager cxmhg = CXMManager.getInstance();
				String nom = fcMember.getLastName();
				if (nom.contains(".siret")) {
					String newNom = nom.substring(0,nom.indexOf(".siret"));
					m.setLastName(newNom);
					String siret = nom.substring(nom.indexOf(".siret")+6);
					// A personnaliser
			/**
					GroupProfile gp = cxmhg.getGroupProfile(siret, true);
					if (Util.notEmpty(gp)) {
						Group g = gp.getGroupe();
						m.addGroup(g);

					}
					
					
					*/
					
					
					
					

				}
				
				
				// on flaque le groupe par défaut pour les demandeurs
			//	m.addGroup(cxmhg.getGroupATitreParticulier());
			}
		}

	}

}

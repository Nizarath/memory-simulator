import java.awt.*;
import java.util.Random;
/* additional tools */
class util {
	final static Random rnd = new Random();
}
class gui_util {
	static void make_grd(Container cnt, Component[] cmps, int[][] grd_cnfg) {
		GridBagLayout layout = new GridBagLayout();
		cnt.setLayout(layout);
		GridBagConstraints prms = new GridBagConstraints();
		prms.ipadx = 3;
		prms.ipady = 3;
		for (int i = 0; i < cmps.length; i++) {
			prms.gridx = grd_cnfg[i][0];
			prms.gridy = grd_cnfg[i][1];
			prms.gridwidth = grd_cnfg[i][2];
			prms.gridheight = grd_cnfg[i][3];
			prms.anchor = grd_cnfg[i][4];
			layout.setConstraints(cmps[i],prms);
			cnt.add(cmps[i]);
		}
	}
	static void swtch_pnl(Container cnt, boolean flg) {
		Component[] cmps = cnt.getComponents();
		for (int i = 0; i < cmps.length; i++)
			cmps[i].setEnabled(flg);
	}
}

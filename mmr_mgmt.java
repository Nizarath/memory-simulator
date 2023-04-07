/*
 * memory management real-time simulator
 */

/* ----- packages ----- */
import javax.swing.*;
import static javax.swing.JOptionPane.*;
class mmr_mgmt {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
					} catch(Exception exc) {
						showMessageDialog(null, "Error while setting style");
					}
					new cnfg_gui();
				}
			});
	}
}

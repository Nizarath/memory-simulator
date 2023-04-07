import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import static java.awt.GridBagConstraints.*;
import static java.lang.Integer.parseInt;
import static javax.swing.BorderFactory.*;
import static javax.swing.JOptionPane.*;
import static javax.swing.ScrollPaneConstants.*;

class cnfg_gui implements ActionListener {
	/* cnfg-filter */
	class cnfg_fltr extends FileFilter {
		public boolean accept(File fd){
			return fd.getName().endsWith(".mmr_cnfg") || fd.isDirectory();
		}
		public String getDescription(){
			return "memory management simulator parameters (\".mmr_cnfg\")";
		}
	}
	/* config */
	cnfg cnfg;
	/* gui-components */
	/* containers */
	JFrame frm0;			/* main window */
	/* menu system */
	JMenuBar mbar0;			/* main menu */
	JMenu file_menu,about_menu;	/* sections: File, About */
	JMenuItem rd_itm,wr_itm,exit_itm;    /* points: config read/write, exit */
	/* config window */
	JPanel cnfg_wnd;
	/* low-level panels (process, timer, usage, memory stuctures) */
	JPanel prc_pnl,tmr_pnl,btn_pnl,mngr_pnl,
		fix_mngr_pnl,din_mngr_pnl,page_mngr_pnl;
	/* 0. process config */
	JTextField  max_prc_fld,	/* maximum count field */
		deq_size_fld,		/* queue size field */
		min_prc_mmr_fld,	/* lower boundary of address space */
		max_prc_mmr_fld;	/* upper boundary of address space */
	JCheckBox   is_max_prc_cbx,     /* limited process count on/off */
		is_rstrct_mmr_cbx,	/* limited address space on/off */
		is_prc_tree_cbx,	/* process families on/off */
		is_prc_prrt_cbx;	/* process priorities on/off */
	/* 1. timer config */
	JTextField  tmr_prd_fld,	/* period field */
		tmr_prt_fld;		/* request field */
	JCheckBox tmr_on_cbx;		/* timer on/off */
	JComboBox tmr_prc_name_cmbx;	/* template of new process name */
	/* 2. manager selection */
	JCheckBox   fix_mngr_on_cbx,    /* fixed on/off */
		din_mngr_on_cbx,	/* din on/off */
		page_mngr_on_cbx;	/* paged on/off */
	/* 3. fixed manager config */
	JTextField  fix_blk_cnt_fld;    /* block count field */
	JComponent[][] fix_mmr_map_cmp; /* memory map */
	JPanel fix_mmr_map_pnl;         /* memory map panel */
	JScrollPane fix_mmr_map_scp;    /* memory map scrolling */
	JButton fix_updt_btn;           /* memory map update button */
	/* 4. din maneger config */
	JTextField  din_dfrg_fld,       /* defragmentation limit field */
		din_mmr_fld;		/* memory size field */
	JCheckBox   din_dfrg_cbx,       /* defragmentation on/off */
		din_add_cbx;		/* insert mode flag */
	/* 5. paged manager config */
	JTextField  page_size_fld,      /* page size field */
		page_cnt_fld;		/* page count field */
	/* 6. panel of global changes & start working */
	JButton cnfg_confirm_btn,       /* confirm all button */
		cnfg_reset_btn,         /* reset all button */
		cnfg_read_btn,          /* read all from file */
		cnfg_write_btn,         /* write all to file */
		cnfg_exit_btn;          /* exit */

	/* gui creation */
	cnfg_gui() {
		/* config */
		cnfg = new cnfg();
		/* high-level container */
		frm0 = new JFrame("MEMORY MANAGEMENT SIMULATION");
		frm0.setIconImage(new ImageIcon("ttl_icn.png").getImage());
		frm0.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm0.setResizable(false);
		/* manu panel */
		make_menu_bar();
		/* config windows */
		make_cnfg_wnd();
		/* panels & menu -> frame */
		frm0.getContentPane().add(cnfg_wnd);
		frm0.setJMenuBar(mbar0);
		/* show */
		frm0.pack();
		frm0.setVisible(true);
	}

	/* MAIN WINDOW */
	void make_menu_bar(){
		/* panel creation */
		mbar0 = new JMenuBar();
		/* manu creation */
		file_menu = new JMenu("File");            /* file operations & exit */
		file_menu.setMnemonic(KeyEvent.VK_F);
		about_menu = new JMenu("About");          /* info about program */
		about_menu.setMnemonic(KeyEvent.VK_A);
		/* File menu items */
		/* read config */
		file_menu.add(rd_itm = new JMenuItem("Read config"));
		rd_itm.setMnemonic(KeyEvent.VK_R);
		rd_itm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
		/* write config */
		file_menu.add(wr_itm = new JMenuItem("Write config"));
		wr_itm.setMnemonic(KeyEvent.VK_W);
		wr_itm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,InputEvent.CTRL_MASK));
		file_menu.add(new JSeparator());
		/* exit */
		file_menu.add(exit_itm = new JMenuItem("Exit"));
		exit_itm.setMnemonic(KeyEvent.VK_E);
		exit_itm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,InputEvent.CTRL_MASK));
		/* actions */
		rd_itm.addActionListener(this);
		wr_itm.addActionListener(this);
		exit_itm.addActionListener(this);
		/* adding */
		mbar0.add(file_menu);
		mbar0.add(about_menu);
	}
	/* CONFIG WINDOW */
	void make_cnfg_wnd(){
		/* config window creation */
		cnfg_wnd = new JPanel(new GridLayout(1, 3),true);

		/* low-level panels creation & adding */
		Box box0 = Box.createVerticalBox();
		box0.setBorder(createEmptyBorder(2, 2, 2, 2));
		box0.add(mngr_pnl = make_mngr_pnl());
		box0.add(fix_mngr_pnl = make_fix_mngr_pnl());
		box0.add(din_mngr_pnl = make_din_mngr_pnl());
		box0.add(page_mngr_pnl = make_page_mngr_pnl());
		
		Box box1 = Box.createVerticalBox();
		box1.setBorder(createEmptyBorder(2, 2, 2, 2));
		box1.add(prc_pnl = make_prc_pnl());
		box1.add(tmr_pnl = make_tmr_pnl());
		box1.add(btn_pnl = make_btn_pnl());

		/* adding boxes to config window */
		cnfg_wnd.add(box0);
		cnfg_wnd.add(box1);
	}
	JPanel make_prc_pnl(){
		/* process panel creation */
		(prc_pnl = new JPanel(true)).setBorder(createEtchedBorder());

		/* create components */
		(max_prc_fld = new JTextField(5)).
			setHorizontalAlignment(SwingConstants.CENTER);
		max_prc_fld.setEnabled(false);
		(deq_size_fld = new JTextField(5)).
			setHorizontalAlignment(SwingConstants.CENTER);
		(min_prc_mmr_fld = new JTextField(4)).
			setHorizontalAlignment(SwingConstants.CENTER);
		min_prc_mmr_fld.setEnabled(false);
		(max_prc_mmr_fld = new JTextField(4)).
			setHorizontalAlignment(SwingConstants.CENTER);
		max_prc_mmr_fld.setEnabled(false);
		(is_max_prc_cbx = new JCheckBox("unlimited", true)).
			addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					max_prc_fld.setEnabled(!is_max_prc_cbx.isSelected());
				}
			});
		(is_rstrct_mmr_cbx = new JCheckBox("memory limits:")).
			addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					boolean flg = is_rstrct_mmr_cbx.isSelected();
					min_prc_mmr_fld.setEnabled(flg);
					max_prc_mmr_fld.setEnabled(flg);
				}
			});
		is_prc_tree_cbx = new JCheckBox("process trees");
		is_prc_prrt_cbx = new JCheckBox("process priorities");

		/* grid params */
		Component[] cmps = {
			new JLabel("PROCESS PARAMS"),
			new JLabel("MAX: "),
			new JLabel("QUEUE SIZE: "),
			max_prc_fld,
			deq_size_fld,
			min_prc_mmr_fld,
			max_prc_mmr_fld,
			is_max_prc_cbx,
			is_rstrct_mmr_cbx,
			is_prc_tree_cbx,
			is_prc_prrt_cbx
		};
		int[][] grd_cnfg = {
			{ 0, 0, 2, 1, CENTER }, 
			{ 0, 1, 1, 2, WEST },
			{ 0, 3, 1, 1, WEST },
			{ 1, 2, 1, 1, WEST },
			{ 1, 3, 1, 1, WEST },
			{ 0, 7, 1, 1, WEST }, 
			{ 1, 7, 1, 1, WEST },
			{ 1, 1, 1, 1, WEST },
			{ 0, 6, 2, 1, WEST },
			{ 0, 4, 2, 1, WEST },
			{ 0, 5, 2, 1, WEST }
		};

		/* add components */
		gui_util.make_grd(prc_pnl, cmps, grd_cnfg);
		return prc_pnl;
	}
	JPanel make_tmr_pnl(){
		/* timer panel creation */
		(tmr_pnl = new JPanel(true)).
			setBorder(createEtchedBorder());

		/* component creation */
		(tmr_prd_fld = new JTextField(5)).
			setHorizontalAlignment(SwingConstants.CENTER);
		(tmr_prt_fld = new JTextField(5)).
			setHorizontalAlignment(SwingConstants.CENTER);
		(tmr_on_cbx = new JCheckBox("generator on", true)).
			addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					gui_util.swtch_pnl(tmr_pnl, tmr_on_cbx.isSelected());
					tmr_on_cbx.setEnabled(true);
				}
			});
		String[] tmp_names = { "X", "process ", "prc" };
		tmr_prc_name_cmbx = new JComboBox(tmp_names);

		/* layout params */
		Component[] cmps = {
			new JLabel("PROCESS GENERATOR"),
			new JLabel("timer period (ms):"),
			new JLabel("[remove requests]/[all] (%):"),
			new JLabel("process template: "),
			tmr_prd_fld,
			tmr_prt_fld,
			tmr_on_cbx,
			tmr_prc_name_cmbx
		};
		int[][] grd_cnfg = { 
			{ 0, 0, 2, 1, CENTER },
			{ 0, 2, 1, 1, WEST },
			{ 0, 3, 1, 1, WEST },
			{ 0, 4, 1, 1, WEST },
			{ 1, 2, 1, 1, WEST }, 
			{ 1, 3, 1, 1, WEST },
			{ 0, 1, 2, 1, WEST },
			{ 1, 4, 1, 1, WEST }
		 };

		/* add components */
		gui_util.make_grd(tmr_pnl, cmps, grd_cnfg);
		return tmr_pnl;
	}
	JPanel make_mngr_pnl(){
		/* panel creation */
		(mngr_pnl = new JPanel(true)).
			setBorder(createEtchedBorder());
		/* add components */
		Component[] cmps = {
			new JLabel("USE MANAGERS:"),
			fix_mngr_on_cbx = new JCheckBox("fixed", true), 
			din_mngr_on_cbx = new JCheckBox("dynamic", true), 
			page_mngr_on_cbx = new JCheckBox("paged", true)
		};
		int[][] grd_cnfg = {
			{ 0, 0, 2, 1, CENTER },
			{ 0, 1, 2, 1, WEST },
			{ 0, 2, 2, 1, WEST },
			{ 0, 3, 2, 1, WEST }
		 };
		gui_util.make_grd(mngr_pnl, cmps, grd_cnfg);
		/* actions */
		fix_mngr_on_cbx.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					boolean flg = fix_mngr_on_cbx.isSelected();
					gui_util.swtch_pnl(fix_mngr_pnl, flg);
					gui_util.swtch_pnl(fix_mmr_map_scp, flg);
					gui_util.swtch_pnl(fix_mmr_map_pnl, flg);
				}
			});
		din_mngr_on_cbx.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					gui_util.swtch_pnl(din_mngr_pnl,
							   din_mngr_on_cbx.isSelected());
				}
			});
		page_mngr_on_cbx.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					gui_util.swtch_pnl(page_mngr_pnl,
							   page_mngr_on_cbx.isSelected());
				}
			});
		return mngr_pnl;
	}
	JPanel make_fix_mngr_pnl() {
		/* panel creation */
		(fix_mngr_pnl = new JPanel(true)).
			setBorder(createEtchedBorder());

		/* component creation */
		(fix_blk_cnt_fld = new JTextField(6)).
			setHorizontalAlignment(SwingConstants.CENTER);
		fix_updt_btn = new JButton("update");
		(fix_mmr_map_scp = new JScrollPane(fix_mmr_map_pnl = new JPanel(true),VERTICAL_SCROLLBAR_AS_NEEDED,
						 HORIZONTAL_SCROLLBAR_NEVER)).setVisible(false);

		/* actions handlers */
		fix_updt_btn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						fix_mmr_map_scp.setVisible(true);
						fix_mmr_map_cmp = new JComponent[parseInt(fix_blk_cnt_fld.getText())][2];
						fix_mmr_map_pnl.removeAll();
						fix_mmr_map_pnl.setLayout(new GridLayout(fix_mmr_map_cmp.length, 2));
						for (int i = 0; i < fix_mmr_map_cmp.length; i++) {
							fix_mmr_map_pnl.add(fix_mmr_map_cmp[i][0] = new JLabel("block " + i));
							fix_mmr_map_pnl.add(fix_mmr_map_cmp[i][1] = new JTextField(7));
							((JLabel) fix_mmr_map_cmp[i][0]).setHorizontalAlignment(SwingConstants.CENTER);
							((JTextField) fix_mmr_map_cmp[i][1]).setHorizontalAlignment(SwingConstants.CENTER);
						}
						/* SHIT, NEEDS TO BE REDONE */
						if (fix_mmr_map_cmp.length < 4)
							frm0.pack();
						else
							fix_mmr_map_scp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
					} catch(Exception exc) {
						JOptionPane.showMessageDialog(cnfg_wnd, "Memory map length must be integer > 0!",
									      "Input error",JOptionPane.ERROR_MESSAGE);
					}
				}
			});

		/* layout params */
		Component[] cmps = {
			new JLabel("FIXED MEMORY"),
			new JLabel("block count: "),
			new JLabel("<html>memory map <br>(block-size) "),
			fix_mmr_map_scp,
			fix_blk_cnt_fld,
			fix_updt_btn
		};
		int[][] grd_cnfg = { 
			{ 0, 0, 2, 1, CENTER },
			{ 0, 1, 1, 1, WEST },
			{ 0, 2, 1, 1, WEST }, 
			{ 0, 3, 2, 3, CENTER },
			{ 1, 1, 1, 1, CENTER },
			{ 1, 2, 1, 1, CENTER }
		 };

		/* add components */
		gui_util.make_grd(fix_mngr_pnl, cmps, grd_cnfg);
		return fix_mngr_pnl;
	}
	JPanel make_din_mngr_pnl() {
		/* create panel */
		(din_mngr_pnl = new JPanel(true)).setBorder(createEtchedBorder());

		/* individual component layouts */
		Component[] cmps = {
			new JLabel("DYNAMIC MEMORY"),
			new JLabel("total size: "),
			din_mmr_fld = new JTextField(6),
			new JLabel("holes/all memory: "),
			din_dfrg_fld = new JTextField(4),
			din_dfrg_cbx = new JCheckBox("defragmentate at:"),
			din_add_cbx = new JCheckBox("optimal fit algorythm")
		};
		din_mmr_fld.setHorizontalAlignment(SwingConstants.CENTER);
		din_dfrg_fld.setHorizontalAlignment(SwingConstants.CENTER);
		int[][] grd_cnfg = {
			{ 0, 0, 2, 1, CENTER },
			{ 0, 1, 1, 1, WEST },
			{ 1, 1, 1, 1, WEST },
			{ 0, 3, 1, 1, WEST }, 
			{ 1, 3, 1, 1, WEST },
			{ 0, 2, 2, 1, WEST },
			{ 0, 4, 2, 1, WEST }
		 };

		/* add components */
		gui_util.make_grd(din_mngr_pnl, cmps, grd_cnfg);
		return din_mngr_pnl;
	}
	JPanel make_page_mngr_pnl() {
		/* create panel */
		(page_mngr_pnl=new JPanel(true)).setBorder(createEtchedBorder());

		/* layout params */
		Component[] cmps = {
			new JLabel("PAGED MEMORY"),
			new JLabel("page size: "),
			new JLabel("page count: "),
			page_size_fld=new JTextField(10),
			page_cnt_fld=new JTextField(10)
		};
		page_size_fld.setHorizontalAlignment(SwingConstants.CENTER);
		page_cnt_fld.setHorizontalAlignment(SwingConstants.CENTER);
		int[][] grd_cnfg = {
			{0, 0, 2, 1, CENTER},
			{0, 1, 1, 1, WEST},
			{0, 2, 1, 1, WEST},
			{1, 1, 1, 1, WEST},
			{1, 2, 1, 1, WEST}
		};

		/* add components */
		gui_util.make_grd(page_mngr_pnl, cmps, grd_cnfg);
		return page_mngr_pnl;
	}
	JPanel make_btn_pnl(){
		/* create panel */
		btn_pnl = new JPanel(new GridLayout(2, 3),true);

		/* add components */
		btn_pnl.add(cnfg_confirm_btn = new JButton("CREATE"));
		btn_pnl.add(cnfg_reset_btn = new JButton("RESET"));
		btn_pnl.add(cnfg_exit_btn = new JButton("EXIT"));
		btn_pnl.add(cnfg_read_btn = new JButton("READ"));
		btn_pnl.add(cnfg_write_btn = new JButton("WRITE"));

		/* actions */
		cnfg_confirm_btn.addActionListener(this);
		cnfg_reset_btn.addActionListener(this);
		cnfg_exit_btn.addActionListener(this);
		cnfg_read_btn.addActionListener(this);
		cnfg_write_btn.addActionListener(this);
		return btn_pnl;
	}
	public void actionPerformed(ActionEvent ae) {
		Object src = ae.getSource();
		try {
			if (src == cnfg_confirm_btn) {
				if (!(fix_mngr_on_cbx.isSelected() || din_mngr_on_cbx.isSelected() ||
				      page_mngr_on_cbx.isSelected())) {
					showMessageDialog(frm0, "At least one manager must be used",
							  "Incorrect config", ERROR_MESSAGE);
					return;
				}
				make_cnfg();
				frm0.dispose();
				new wrk_gui(cnfg);
			} else if (src == cnfg_exit_btn || src == exit_itm) {
				if (showConfirmDialog(cnfg_wnd, "Want to quit?", "Quit confirm", YES_NO_OPTION)
				    == YES_OPTION)
					System.exit(0);
			} else if (src == cnfg_read_btn || src == rd_itm) {
				JFileChooser fch0 = new JFileChooser();
				fch0.setFileFilter(new cnfg_fltr());
				if (fch0.showOpenDialog(cnfg_wnd) == JFileChooser.APPROVE_OPTION)
					if (rd_cnfg(fch0.getSelectedFile().getAbsolutePath()))
						make_gui_cnfg();
			} else if (src == cnfg_write_btn || src == wr_itm) {
				JFileChooser fch0 = new JFileChooser();
				fch0.setFileFilter(new cnfg_fltr());
				if (fch0.showSaveDialog(cnfg_wnd) == JFileChooser.APPROVE_OPTION) {
					make_cnfg();
					wr_cnfg(fch0.getSelectedFile().getAbsolutePath());
				}
			} else if (src == cnfg_reset_btn) {
				frm0.dispose();
				new cnfg_gui();
			}
		} catch(NumberFormatException exc) {
			showMessageDialog(cnfg_wnd, "Some parameters are lost/set incorrectly",
					  "Incorrect config", ERROR_MESSAGE);
		}
	}
	/* SESSION CONFIG */
	/* configuring by hand */
	void make_cnfg(){
		/* process config */
		cnfg.wrk_max = is_max_prc_cbx.isSelected() ? cnfg.INF : parseInt(max_prc_fld.getText());/* max working count */
		cnfg.deq_size = parseInt(deq_size_fld.getText());     /* queue size */
		cnfg.is_tree = is_prc_tree_cbx.isSelected();          /* process families */
		cnfg.is_prrt = is_prc_prrt_cbx.isSelected();          /* priorities */
		if (is_rstrct_mmr_cbx.isSelected()) {                 /* memory limits (if set on): */
			cnfg.min_prc_mmr = parseInt(min_prc_mmr_fld.getText());   /* min */
			cnfg.max_prc_mmr = parseInt(max_prc_mmr_fld.getText());   /* max */
		} else {
			cnfg.min_prc_mmr = 1;
			cnfg.max_prc_mmr = cnfg.INF;
		}
		/* request generator config (if set on) */
		if (cnfg.tmr_on=tmr_on_cbx.isSelected()) {
			cnfg.tmr_prd = parseInt(tmr_prd_fld.getText()); /* period */
			cnfg.del_req_prt = parseInt(tmr_prt_fld.getText()); /* layout of requst types */
			cnfg.prc_name_tmp = (String)tmr_prc_name_cmbx.getSelectedItem();  /* process name template */
		}
		/* fixed memory config (if set on) */
		if (cnfg.use_fix = fix_mngr_on_cbx.isSelected()) {
			cnfg.mmr_map = new int[parseInt(fix_blk_cnt_fld.getText())];    /* map size */
			for(int i = 0; i < cnfg.mmr_map.length; i++)
				cnfg.mmr_map[i] = parseInt(((JTextField)fix_mmr_map_cmp[i][1]).getText());
		}
		/* dynamic memory config (if set on) */
		if (cnfg.use_din = din_mngr_on_cbx.isSelected()) {
			cnfg.din_mmr = parseInt(din_mmr_fld.getText());   /* memory size */
			if (cnfg.cond_dfrg = din_dfrg_cbx.isSelected())            /* defragmentation mode */
				cnfg.dfrg_prt = parseInt(din_dfrg_fld.getText());   /* start defragmentation at: */
			cnfg.add_in_min = din_add_cbx.isSelected();       /* insert mode */
		}
		/* paged memory config (if set on) */
		if (cnfg.use_page = page_mngr_on_cbx.isSelected()) {
			cnfg.page_cnt = parseInt(page_cnt_fld.getText());     /* page count */
			cnfg.page_size = parseInt(page_size_fld.getText());   /* page size */
		}
	}
	/* read from/write to config file */
	void make_gui_cnfg() {
		/* compare flag to avoid recalculations */
		boolean comp;

		/* process config */
		/* max working count, if limited */
		comp = cnfg.wrk_max != cnfg.INF;        /* FLAG = PROCESS COUNT LIMITS ON */
		is_max_prc_cbx.setSelected(!comp);
		max_prc_fld.setEnabled(comp);
		if (comp)
			max_prc_fld.setText(String.valueOf(cnfg.wrk_max));
		/* queue size */
		deq_size_fld.setText(String.valueOf(cnfg.deq_size));
		/* families on */
		is_prc_tree_cbx.setSelected(cnfg.is_tree);
		/* priorities on */
		is_prc_prrt_cbx.setSelected(cnfg.is_prrt);
		/* memory limits, if limited */
		comp = (!((cnfg.min_prc_mmr == 1) && (cnfg.max_prc_mmr == cnfg.INF))); /* FLAG=PROCESS MEMORY LIMITS ON */
		is_rstrct_mmr_cbx.setSelected(comp);
		min_prc_mmr_fld.setEnabled(comp);
		max_prc_mmr_fld.setEnabled(comp);
		if (comp) {
			min_prc_mmr_fld.setText(String.valueOf(cnfg.min_prc_mmr));
			max_prc_mmr_fld.setText(String.valueOf(cnfg.max_prc_mmr));
		}

		/* request generator config (if on) */
		gui_util.swtch_pnl(tmr_pnl, cnfg.tmr_on);
		tmr_on_cbx.setEnabled(true);
		tmr_on_cbx.setSelected(cnfg.tmr_on);
		if (cnfg.tmr_on) {
			tmr_prd_fld.setText(String.valueOf(cnfg.tmr_prd));
			tmr_prt_fld.setText(String.valueOf(cnfg.del_req_prt));
			tmr_prc_name_cmbx.setSelectedItem(cnfg.prc_name_tmp);
		}

		/* memory manager selection config */
		fix_mngr_on_cbx.setSelected(cnfg.use_fix);
		din_mngr_on_cbx.setSelected(cnfg.use_din);
		page_mngr_on_cbx.setSelected(cnfg.use_page);

		/* fixed memory config (if selected) */
		gui_util.swtch_pnl(fix_mngr_pnl, cnfg.use_fix);
		if (cnfg.use_fix) {
			fix_blk_cnt_fld.setText(String.valueOf(cnfg.mmr_map.length));
			fix_mmr_map_cmp = new JComponent[cnfg.mmr_map.length][2];
			fix_mmr_map_pnl.removeAll();
			fix_mmr_map_pnl.setLayout(new GridLayout(fix_mmr_map_cmp.length, 2));
			for (int i = 0; i < fix_mmr_map_cmp.length; i++) {
				fix_mmr_map_pnl.add(fix_mmr_map_cmp[i][0] = new JLabel("block " + i));
				fix_mmr_map_pnl.add(fix_mmr_map_cmp[i][1] = new JTextField(6));
				((JLabel)fix_mmr_map_cmp[i][0]).setHorizontalAlignment(SwingConstants.CENTER);
				((JTextField)fix_mmr_map_cmp[i][1]).setHorizontalAlignment(SwingConstants.CENTER);
				((JTextField)fix_mmr_map_cmp[i][1]).setText(String.valueOf(cnfg.mmr_map[i]));
			}
			/* SHIT, NEEDS TO BE REDONE */
			if (fix_mmr_map_cmp.length < 4)
				frm0.pack();
			else
				fix_mmr_map_scp.setVerticalScrollBarPolicy
					(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		}

		/* dynamic memory config (if selected) */
		gui_util.swtch_pnl(din_mngr_pnl, cnfg.use_din);
		if (cnfg.use_din) {
			din_mmr_fld.setText(String.valueOf(cnfg.din_mmr));
			din_dfrg_cbx.setSelected(cnfg.cond_dfrg);
			din_dfrg_fld.setEnabled(cnfg.cond_dfrg);
			if (cnfg.cond_dfrg)din_dfrg_fld.setText(String.valueOf(cnfg.dfrg_prt));
			din_add_cbx.setSelected(cnfg.add_in_min);
		}

		/* paged memory config (if selected) */
		gui_util.swtch_pnl(page_mngr_pnl, cnfg.use_page);
		if (cnfg.use_page) {
			page_cnt_fld.setText(String.valueOf(cnfg.page_cnt));
			page_size_fld.setText(String.valueOf(cnfg.page_size));
		}

		/* redraw config window */
		frm0.pack();
	}
	boolean rd_cnfg(String fin_abs_path) {
		DataInputStream fin = null;
		try {
			fin = new DataInputStream(new FileInputStream(fin_abs_path));
			cnfg.wrk_max = fin.readInt();
			cnfg.deq_size = fin.readInt();
			cnfg.is_tree = fin.readBoolean();
			cnfg.is_prrt = fin.readBoolean();
			cnfg.min_prc_mmr = fin.readInt();
			cnfg.max_prc_mmr = fin.readInt();
			if (cnfg.tmr_on = fin.readBoolean()) {
				cnfg.tmr_prd = fin.readInt();
				cnfg.del_req_prt = fin.readInt();
				cnfg.prc_name_tmp = fin.readUTF();
			}
			if (cnfg.use_fix = fin.readBoolean()) {
				cnfg.mmr_map = new int[fin.readInt()];
				for (int i = 0; i < cnfg.mmr_map.length; i++)
					cnfg.mmr_map[i] = fin.readInt();
			}
			if (cnfg.use_din = fin.readBoolean()) {
				cnfg.din_mmr = fin.readInt();
				cnfg.cond_dfrg = fin.readBoolean();
				cnfg.add_in_min = fin.readBoolean();
				cnfg.dfrg_prt = fin.readInt();
			}
			if (cnfg.use_page = fin.readBoolean()) {
				cnfg.page_size = fin.readInt();
				cnfg.page_cnt = fin.readInt();
			}
			return true;
		} catch (FileNotFoundException exc) {
			JOptionPane.showMessageDialog(cnfg_wnd,"File not found!",
						      "File not found error",JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(cnfg_wnd,"Read error!",
						      "Read file error",JOptionPane.ERROR_MESSAGE);
			return false;
		} finally {
			try {
				if (fin != null)
					fin.close();
			} catch(IOException exc) {
				JOptionPane.showMessageDialog(cnfg_wnd,"Close error!",
							      "Close file error",JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
	}
	boolean wr_cnfg(String fout_abs_path) {
		DataOutputStream fout = null;
		try {
			fout = new DataOutputStream(new FileOutputStream(fout_abs_path,false));
			fout.writeInt(cnfg.wrk_max);
			fout.writeInt(cnfg.deq_size);
			fout.writeBoolean(cnfg.is_tree);
			fout.writeBoolean(cnfg.is_prrt);
			fout.writeInt(cnfg.min_prc_mmr);
			fout.writeInt(cnfg.max_prc_mmr);
			fout.writeBoolean(cnfg.tmr_on);
			if (cnfg.tmr_on) {
				fout.writeInt(cnfg.tmr_prd);
				fout.writeInt(cnfg.del_req_prt);
				fout.writeUTF(cnfg.prc_name_tmp);
			}
			fout.writeBoolean(cnfg.use_fix);
			if (cnfg.use_fix) {
				fout.writeInt(cnfg.mmr_map.length);
				for (int i = 0; i < cnfg.mmr_map.length; i++)
					fout.writeInt(cnfg.mmr_map[i]);
			}
			fout.writeBoolean(cnfg.use_din);
			if (cnfg.use_din) {
				fout.writeInt(cnfg.din_mmr);
				fout.writeBoolean(cnfg.cond_dfrg);
				fout.writeBoolean(cnfg.add_in_min);
				fout.writeInt(cnfg.dfrg_prt);
			}
			fout.writeBoolean(cnfg.use_page);
			if (cnfg.use_page) {
				fout.writeInt(cnfg.page_size);
				fout.writeInt(cnfg.page_cnt);
			}
			return true;
		} catch(FileNotFoundException exc) {
			JOptionPane.showMessageDialog(cnfg_wnd,"File not found!",
						      "File not found error",JOptionPane.ERROR_MESSAGE);
			return false;
		} catch(IOException exc) {
			JOptionPane.showMessageDialog(cnfg_wnd,"Write error!",
						      "Write file error",JOptionPane.ERROR_MESSAGE);
			return false;
		} finally {
			try {
				if (fout != null)
					fout.close();
			} catch(IOException exc) {
				JOptionPane.showMessageDialog(cnfg_wnd,"Close error!",
							      "Close file error",JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
	}
}

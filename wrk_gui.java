import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;

import static java.awt.GridBagConstraints.*;
import static java.lang.Integer.parseInt;
import static java.lang.Math.abs;
import static java.lang.System.currentTimeMillis;
import static javax.swing.BorderFactory.*;
import static javax.swing.JOptionPane.*;
import static org.jfree.chart.ChartFactory.createXYLineChart;

class wrk_gui implements ActionListener {
	/* table model for dynamic memory manager */
	class din_tbl_mdl extends AbstractTableModel {
		String[] hdrs = { "block size", "process" };
		din_mmr_mngr mngr;
		din_tbl_mdl(din_mmr_mngr mngr) {
			this.mngr = mngr;
		}
		public String getColumnName(int col) {
			return hdrs[col];
		}
		public int getRowCount() {
			return mngr.get_blk_cnt();
		}
		public int getColumnCount() {
			return hdrs.length;
		}
		public Object getValueAt(int row, int col) {
			din_mmr_mngr.mmr_blk blk = mngr.get_blk(row);
			return col == 0 ? blk.mmr : (blk.prc == null ? "FREE" : blk.prc.name);
		}
	}
	/* memory managers */
	mmr_mngr[] mngr;
	/* params */
	cnfg cnfg;			/* simulation config */
	Timer tmr;			/* timer */
	long strt_time;			/* start time in ms */
	final static int min_tmr_prd = 50,   /* timer constants */
		max_tmr_prd = 2000;
	/* GUI-components */
	/* containers */
	JFrame frm0;
	JPanel wrk_wnd;
	/* working window layout */
	JTabbedPane mngr_pnl,		/* panel with memory managers' tabs */
		chrt_pnl,		/* panel with plot tabs */
		prc_stat_pnl;		/* panel with prc tabs */
	JPanel  prc_pnl,		/* add/remove prc panel */
		rule_pnl;		/* global control panel */
	/* 0. panel with tabs */
	JTable[]  mngr_tbl,stat_tbl;	/* memory map & statistics */
	JTextArea[] mngr_log;		/* request history */
	/* 1. plot panels */
	XYSeries[]  mmr_stat,		/* memory statistics */
		prc_stat,		/* process statistics */
		req_stat;		/* request statistics */
	/* 2. process panel */
	JList[] wrk_lst,wait_lst;	/* process lists (working, waiting) */
	DefaultListModel[] wrk_lst_mdl,wait_lst_mdl;/* list models */
	JScrollPane[] wrk_lst_scp,wait_lst_scp;     /* their scrolling */
	/* 3. process control panel */
	JTextField  prc_name_fld,	/* name */
		prc_mmr_fld;		/* memory */
	JButton prc_add_btn,		/* add button */
		prc_del_btn;		/* remove button */
	/* 4. global control panel */
	JSlider tmr_sldr;		/* simulation period */
	JButton pause_btn;		/* timer on/off */
	JButton reset_btn, stop_btn;	/* reset/stop */

	/* entry point & GUI creation */
	wrk_gui(cnfg cnfg) {
		this.cnfg = cnfg;       /* additional config */
		/* timer params */
		if (cnfg.tmr_on)
			tmr = new Timer(cnfg.tmr_prd, this);   /* timer was set manually */
		else {                                         /* timer template */
			tmr = new Timer((min_tmr_prd + max_tmr_prd) / 2, this);
			this.cnfg.del_req_prt = 25;
			this.cnfg.prc_name_tmp = "prc";
		}
		/* memory managers creation */
		mngr = new mmr_mngr[(cnfg.use_fix ? 1 : 0) + (cnfg.use_din ? 1 : 0) + (cnfg.use_page ? 1 : 0)];
		byte top  =  0;
		if (cnfg.use_fix)
			mngr[top++] = new fix_mmr_mngr(cnfg.mmr_map, cnfg.deq_size, cnfg.is_prrt);
		if (cnfg.use_din)
			mngr[top++] = new din_mmr_mngr(cnfg.din_mmr, cnfg.deq_size, cnfg.is_prrt, cnfg.add_in_min, 
						       cnfg.cond_dfrg, cnfg.dfrg_prt / 100.0f);
		if (cnfg.use_page)
			mngr[top] = new page_mmr_mngr(cnfg.page_size, cnfg.page_cnt, cnfg.deq_size, cnfg.is_prrt);
		make_frm();		/* high-level container (frame) */
		make_wrk_wnd();         /* working window */
		/* add panels & menu -> frame */
		frm0.getContentPane().add(wrk_wnd);
		/* show */
		frm0.pack();
		frm0.setVisible(true);
		/* START AUTOSIMULATION HERE (if needed, else - wait for button pressing) */
		if (cnfg.tmr_on) {
			showMessageDialog(wrk_wnd, "Press to start simulation", "Simulation", INFORMATION_MESSAGE);   /* info */
			tmr.start();    /* go! */
		}
		strt_time = currentTimeMillis();  /* ~ simulation start time */
	}
	/* main window */
	void make_frm() {
		/* high-level container (frame) */
		frm0 = new JFrame("MEMORY MANAGEMENT SIMULATION");
		frm0.setIconImage(new ImageIcon("ttl_icn.png").getImage());
		frm0.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm0.setResizable(false);
	}
	/* working window */
	void make_wrk_wnd() {
		/* create simulation window */
		wrk_wnd = new JPanel(true);
		/* layout map */
		Box box = Box.createHorizontalBox();
		box.add(prc_stat_pnl = make_prc_stat_pnl());
		box.add(prc_pnl = make_prc_pnl());
		box.add(rule_pnl = make_rule_pnl());
		Component[] cmps = {
			mngr_pnl = make_mngr_pnl(), chrt_pnl = make_chrt_pnl(), box
		};
		int[][] grd_cnfg = {
			{ 0, 0, 1, 2, NORTH }, 
			{ 1, 0, 2, 1, CENTER }, 
			{ 1, 1, 2, 1, CENTER }
		};
		/* add to window */
		gui_util.make_grd(wrk_wnd, cmps, grd_cnfg);
	}
	/* low-level panels creation */
	JTabbedPane make_mngr_pnl() {
		/* panel creation */
		(mngr_pnl = new JTabbedPane()).addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					int i = mngr_pnl.getSelectedIndex();
					prc_stat_pnl.setComponentAt(0, wrk_lst_scp[i]);
					prc_stat_pnl.setComponentAt(1, wait_lst_scp[i]);
				}
			});
		JPanel[] mngr_tab = new JPanel[mngr.length];
		mngr_tbl = new JTable[mngr.length];
		stat_tbl = new JTable[mngr.length];
		mngr_log = new JTextArea[mngr.length];
		mmr_stat = new XYSeries[mngr.length];
		prc_stat = new XYSeries[mngr.length];
		req_stat = new XYSeries[mngr.length];
		/* memory for every component */
		for (int i = 0; i < mngr.length; i++) {
			/* requst log */
			JScrollPane log_scp = new JScrollPane(mngr_log[i] = new JTextArea());
			log_scp.setPreferredSize(new Dimension(230, 400));
			log_scp.setBorder(createTitledBorder(createEtchedBorder(), "LOG",
							     TitledBorder.CENTER, TitledBorder.TOP));
			/* table of statistics */
			Object[][] data = {
				{ "all memory", mngr[i].size }, 
				{ "free memory", mngr[i].size }, 
				{ "non-free memory", 0 }, 
				{ "used memory", 0 }, 
				{ "working processes", 0 }, 
				{ "waiting processes", 0 }, 
				{ "all requests", 0 }, 
				{ "confirmed", 0 }, 
				{ "to queue", 0 }, 
				{ "rejected", 0 }
			};
			JScrollPane stat_pnl = new JScrollPane
				(stat_tbl[i] = new JTable
				 (data, new String[]{ "parameter", "value" }));
			stat_pnl.setPreferredSize(new Dimension(230, 210));
			stat_pnl.setBorder(createTitledBorder(createEtchedBorder(), "STATISTICS",
							      TitledBorder.CENTER, TitledBorder.TOP));
			/* memory map table */
			if (mngr[i].toString().equals("fixed memory")) {
				fix_mmr_mngr fm = (fix_mmr_mngr)mngr[i];
				data = new Object[fm.max_cnt][2];
				for (int j = 0; j < fm.max_cnt; j++) {
					data[j][0] = fm.mmr_map[j];
					data[j][1] = (fm.prc_lst[j] == null) ? "FREE" : fm.prc_lst[j].name;
				}
				mngr_tbl[i] = new JTable(data,new String[]{ "block size", "process" });
			} else if (mngr[i].toString().equals("dynamic memory"))
				mngr_tbl[i] = new JTable(new din_tbl_mdl((din_mmr_mngr)mngr[i]));
			else if (mngr[i].toString().equals("paged memory")) {
				page_mmr_mngr pm = (page_mmr_mngr)mngr[i];
				data = new Object[pm.page_cnt][2];
				for (int j = 0; j < pm.page_cnt; j++) {
					data[j][0] = pm.page_size;
					data[j][1] = (pm.prc_lst[j] == null) ? "FREE" : pm.prc_lst[j].name;
				}
				mngr_tbl[i] = new JTable(data, new String[]{ "page size", "process" });
			}
			mngr_tbl[i].setPreferredScrollableViewportSize(new Dimension(230, 570));
			JScrollPane mngr_scp = new JScrollPane(mngr_tbl[i]);
			mngr_scp.setBorder(createTitledBorder(createEtchedBorder(), "MEMORY MAP",
							      TitledBorder.CENTER, TitledBorder.TOP));
			/* create panel & add components */
			Component[] cmps = {
				log_scp, stat_pnl, mngr_scp
			};
			int[][] grd_cnfg = {
				{ 0, 0, 1, 1, CENTER }, 
				{ 0, 1, 1, 1, CENTER }, 
				{ 1, 0, 1, 2, CENTER }
			};
			gui_util.make_grd(mngr_tab[i] = new JPanel(true), cmps, grd_cnfg);
			mngr_pnl.addTab(mngr[i].toString(), mngr_tab[i]);
		}
		return mngr_pnl;
	}
	JTabbedPane make_chrt_pnl() {
		/* panel creation */
		chrt_pnl = new JTabbedPane();
		/* plots' data */
		XYSeriesCollection mmr_stat_set = new XYSeriesCollection(),
			prc_stat_set = new XYSeriesCollection(),
			req_stat_set = new XYSeriesCollection();
		for (int i = 0; i < mngr.length; i++) {
			mmr_stat_set.addSeries(mmr_stat[i] = new XYSeries(mngr[i].toString()));
			prc_stat_set.addSeries(prc_stat[i] = new XYSeries(mngr[i].toString()));
			req_stat_set.addSeries(req_stat[i] = new XYSeries(mngr[i].toString()));
		}
		/* -----add plots -> tabs */
		JFreeChart[] chrt = new JFreeChart[3];
		/* memory usage plot */
		chrt_pnl.addTab("MEMORY",
				new ChartPanel(chrt[0] = createXYLineChart("MEMORY USAGE", "TIME (S)", "NON-FREE (%)", 
									   mmr_stat_set, PlotOrientation.VERTICAL, true, true, false)));
		/* process loading plot */
		chrt_pnl.addTab("PROCESSES",
				new ChartPanel(chrt[1] = createXYLineChart("PROCESS LOADING", "TIME (S)", "WORKING PROCESSES (%)",
									   prc_stat_set, PlotOrientation.VERTICAL, true, true, false)));
		/* request execution plot */
		chrt_pnl.addTab("REQUESTS",
				new ChartPanel(chrt[2] = createXYLineChart("REQUEST EXECUTION", "TIME (S)", "CONFIRMED REQUESTS (%)",
									   req_stat_set, PlotOrientation.VERTICAL, true, true, false)));
		/* GUI */
		XYPlot plt;
		for (int i = 0; i < chrt.length; i++) {
			chrt[i].setBackgroundPaint(Color.GRAY);
			chrt[i].getLegend().setBackgroundPaint(Color.LIGHT_GRAY);
			plt = (XYPlot)chrt[i].getPlot();
			plt.setBackgroundPaint(Color.DARK_GRAY);
			plt.setDomainGridlinePaint(Color.BLACK);
			plt.setRangeGridlinePaint(Color.BLACK);
		}
		return chrt_pnl;
	}
	JTabbedPane make_prc_stat_pnl() {
		/* panel creation */
		(prc_stat_pnl = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT)).
			setPreferredSize(new Dimension(175, 185));
		/* component arrays creation */
		/* working processes */
		wrk_lst_scp = new JScrollPane[mngr.length];
		wrk_lst = new JList[mngr.length];
		wrk_lst_mdl = new DefaultListModel[mngr.length];
		/* waiting processes */
		wait_lst_scp = new JScrollPane[mngr.length];
		wait_lst = new JList[mngr.length];
		wait_lst_mdl = new DefaultListModel[mngr.length];
		/* creation of models, lists, scroll panels -- working & waiting */
		for (int i = 0; i < mngr.length; i++) {
			wrk_lst_scp[i] = new JScrollPane
				(wrk_lst[i] = new JList(wrk_lst_mdl[i] = new DefaultListModel()));
			wait_lst_scp[i] = new JScrollPane
				(wait_lst[i] = new JList(wait_lst_mdl[i] = new DefaultListModel()));
		}
		/* firstly first manager's lists are in tabs */
		prc_stat_pnl.addTab("work", new JScrollPane(wrk_lst_scp[0]));
		prc_stat_pnl.addTab("wait", new JScrollPane(wait_lst_scp[0]));
		return prc_stat_pnl;
	}
	JPanel make_prc_pnl() {
		/* panel for process adding creation */
		(prc_pnl = new JPanel(true)).setBorder(createTitledBorder(createEtchedBorder(), 
									  "PROCESS", TitledBorder.CENTER,  TitledBorder.TOP));
		/* layout params */
		Component[] cmps = {
			new JLabel("NAME: "),
			new JLabel("MEMORY: "),
			prc_name_fld = new JTextField(7),
			prc_mmr_fld = new JTextField(7),
			prc_add_btn = new JButton("ADD"),
			prc_del_btn = new JButton("REMOVE")
		};
		prc_add_btn.setPreferredSize(new Dimension(100, 25));
		prc_del_btn.setPreferredSize(new Dimension(100, 25));
		int[][] grd_cnfg = {
			{ 0, 0, 1, 1, CENTER },
			{ 0, 1, 1, 1, CENTER },
			{ 1, 0, 1, 1, CENTER },
			{ 1, 1, 1, 1, CENTER },
			{ 0, 2, 2, 1, CENTER },
			{ 0, 3, 2, 1, CENTER }
		};
		/* add components */
		gui_util.make_grd(prc_pnl, cmps, grd_cnfg);
		/* handle actions */
		prc_add_btn.addActionListener(this);    /* add process */
		prc_del_btn.addActionListener(this);    /* remove process */
		return prc_pnl;
	}
	JPanel make_rule_pnl() {
		/* create panel */
		(rule_pnl = new JPanel(true)).setBorder(createTitledBorder(createEtchedBorder(),
									   "SIMULATION CONTROL", TitledBorder.CENTER, TitledBorder.TOP));
		/* timer period slider */
		(tmr_sldr = new JSlider(JSlider.HORIZONTAL, min_tmr_prd, max_tmr_prd, tmr.getDelay())).
			setBorder(createEmptyBorder(0, 0, 0, 5));
		tmr_sldr.setFocusable(false);
		/* markers */
		tmr_sldr.setMajorTickSpacing((max_tmr_prd - min_tmr_prd) / 5);
		tmr_sldr.setPaintTicks(true);
		/* labels */
		tmr_sldr.setLabelTable(tmr_sldr.createStandardLabels((max_tmr_prd - min_tmr_prd) / 5));
		tmr_sldr.setPaintLabels(true);
		/* timer control */
		tmr_sldr.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					if (tmr_sldr.getValueIsAdjusting())
						return;
					tmr.setDelay(tmr_sldr.getValue());
				}
			});
		/* STOP button */
		(pause_btn = new JButton(cnfg.tmr_on ? "PAUSE" : "CONTINUE")).setPreferredSize(new Dimension(120, 25));
		pause_btn.addActionListener(this);
		/* RESTART button */
		(reset_btn = new JButton("RESTART")).setPreferredSize(new Dimension(120, 25));
		reset_btn.addActionListener(this);
		/* EXIT button */
		(stop_btn = new JButton("EXIT")).setPreferredSize(new Dimension(120, 25));
		stop_btn.addActionListener(this);
		/* layout params */
		Component[] cmps = {
			new JLabel("TIMER PERIOD (ms):"),
			tmr_sldr,
			pause_btn,
			reset_btn,
			stop_btn
		};
		int[][] grd_cnfg = { 
			{ 0, 0, 1, 1, CENTER },
			{ 0, 1, 1, 2, CENTER },
			{ 1, 0, 1, 1, CENTER },
			{ 1, 1, 1, 1, CENTER },
			{ 1, 2, 1, 1, CENTER }
		};
		/* add components */
		gui_util.make_grd(rule_pnl, cmps, grd_cnfg);
		return rule_pnl;
	}

	/*------------ SIMULATION -----------*/
	/* timer "interrupts" + simulation by hand */
	public void actionPerformed(ActionEvent ae) {
		Object src = ae.getSource();  /* action source */
		/* ----------TIMER'S "INTERRUPT"----------- */
		if (src == tmr) {
			for (int i = 0; i < mngr.length; i++) {
				/* add process (probability is determined by configs) */
				if (mngr[i].wrk_cnt != 0 && abs(util.rnd.nextInt()) % 100 < cnfg.del_req_prt) {
					make_req(i, mngr[i].get_wrk_prc()[abs(util.rnd.nextInt()) % mngr[i].wrk_cnt], false);
					return;
				}
				/* XOR remove process */
				make_req(i, new prc(cnfg.prc_name_tmp+mngr[i].all_req,
						    abs(util.rnd.nextInt()) % (cnfg.max_prc_mmr - cnfg.min_prc_mmr + 1) + cnfg.min_prc_mmr), true);
			}
			/* --------------PAUSE BUTTON------------ */
		} else if (src==pause_btn) {
			if (tmr.isRunning()) {
				tmr.stop();
				pause_btn.setText("CONTINUE");
			} else {
				pause_btn.setText("PAUSE");
				tmr.start();
			}
			/* ------------STOP BUTTON--------- */
		} else if (src == stop_btn) {
			if (showConfirmDialog(frm0, "Want to quit?", "Exit confirmation", YES_NO_OPTION) == YES_OPTION)
				System.exit(0);
			/* --------PROCESS ADD BUTTON----- */
		} else if (src == prc_add_btn) {
			try {
				make_req(mngr_pnl.getSelectedIndex(),
					 new prc(prc_name_fld.getText(), parseInt(prc_mmr_fld.getText())), true);
			} catch(NumberFormatException exc) {
				showMessageDialog(frm0, "Process name must be a string, memory - integer number",
						  "Incorrect params of new process", ERROR_MESSAGE);
			}
			/* --------PROCESS REMOVE BUTTON------- */
		} else if (src == prc_del_btn) {
			try {
				int i = mngr_pnl.getSelectedIndex();
				make_req(i, (prc)wrk_lst[i].getSelectedValue(), false);
			} catch(NullPointerException exc) {
				showMessageDialog(frm0, "Choose process to remove from working list",
						  "Process to remove isn't specified", ERROR_MESSAGE);
			}
			/* ------------RESTART BUTTON----------- */
		} else if (src == reset_btn) {
			if (showConfirmDialog(frm0, "Restart simulation?", "Restart confirmation",
					      YES_NO_OPTION,QUESTION_MESSAGE) == YES_OPTION) {
				frm0.dispose();
				new wrk_gui(cnfg);
			}
		}
	}
	/* request [MANAGER][PROCESS][ADD|REMOVE] + update GUI */
	void make_req(int i, prc prc, boolean add) {
		/* make add/remove requst & store result */
		int rslt = add ? mngr[i].prc_add(prc) : mngr[i].prc_del(prc);
		/* update log */
		mngr_log[i].append("\n" + prc.name + ", size " + prc.mmr + /* record: "process [name] [memory] [action]" */
				   (rslt == mmr_mngr.DELETED ? " remove" :
				    rslt == mmr_mngr.CONFIRMED ? " added" :
				    rslt == mmr_mngr.WAIT ? " added to queue" :
				    rslt == mmr_mngr.REPLACED ? " removed & replaced from queue" : " rejected"));
		/* update table */
		if (mngr[i].toString().equals("fixed memory")) {
			fix_mmr_mngr fm = (fix_mmr_mngr)mngr[i];           /* for less code size */
			for (int j = 0; j < fm.max_cnt; j++)
				mngr_tbl[i].setValueAt(fm.prc_lst[j] == null ? "FREE" : fm.prc_lst[j].name, j, 1);
		} else if (mngr[i].toString().equals("dynamic memory"))
			mngr_tbl[i].setModel(new din_tbl_mdl((din_mmr_mngr)mngr[i])); /* memory map with new size */
		else if (mngr[i].toString().equals("paged memory")) {
			page_mmr_mngr pm = (page_mmr_mngr)mngr[i];          /* for less code size */
			for (int j = 0; j < pm.page_cnt; j++)
				mngr_tbl[i].setValueAt(pm.prc_lst[j] == null ? "FREE" : pm.prc_lst[j].name, j, 1);
		}
		/* update plots */
		double cur_sec = (currentTimeMillis() - strt_time) / 1000.0;
		mmr_stat[i].add(cur_sec, 100.0 * mngr[i].nfree_size / mngr[i].size);
		prc_stat[i].add(cur_sec, 100.0 * mngr[i].wrk_cnt / (mngr[i].wrk_cnt + mngr[i].wait_cnt));
		req_stat[i].add(cur_sec, 100.0 * mngr[i].acc_req / mngr[i].all_req);
		/* update process lists */
		switch (rslt) {
		case mmr_mngr.CONFIRMED:
			wrk_lst_mdl[i].addElement(prc);
			break;
		case mmr_mngr.WAIT:
			wait_lst_mdl[i].addElement(prc);
			break;
		case mmr_mngr.DELETED:
			wrk_lst_mdl[i].removeElement(prc);
			break;
		case mmr_mngr.REPLACED:
			wrk_lst_mdl[i].clear();
			wait_lst_mdl[i].clear();
			prc[] wrk = mngr[i].get_wrk_prc();
			for (prc p : wrk)
				wrk_lst_mdl[i].addElement(p);
			if (mngr[i].wait_cnt != 0) {
				prc[] wait = mngr[i].get_wait_prc();
				for (prc p : wait)
					wait_lst_mdl[i].addElement(p);
			}
		}
		/* update table of statistics */
		long[] stat = {
			mngr[i].size,
			mngr[i].free_size,
			mngr[i].nfree_size,
			0,
			mngr[i].wrk_cnt,
			mngr[i].wait_cnt,
			mngr[i].all_req,
			mngr[i].acc_req,
			mngr[i].deq_req,
			mngr[i].nacc_req
		};
		if (mngr[i].wrk_cnt != 0) {
			prc[] wrk = mngr[i].get_wrk_prc();
			for (prc p : wrk)
				stat[3] += p.mmr;
		}
		for (int j = 0; j < 10; j++)
			stat_tbl[i].setValueAt(stat[j], j, 1);
	}
}

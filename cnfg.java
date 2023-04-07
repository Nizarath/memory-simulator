class cnfg {
	/* processes */
	int wrk_max;            /* max count */
	final static int INF=1000000;   /* imitate infinite count */
	int deq_size;           /* queue size */
	boolean is_tree;        /* process families on/off */
	boolean is_prrt;        /* priorities on/off */
	int min_prc_mmr;        /* min memory size */
	int max_prc_mmr;        /* max memory size */
	/* request generator */
	boolean tmr_on;         /* timer on/off */
	int tmr_prd;            /* timer period (ms) */
	int del_req_prt;        /* value [remove req.] / [add req.] */
	String prc_name_tmp;    /* template of process name */
	/* chosen memory managers */
	boolean use_fix;        /* fixed on/off */
	boolean use_din;        /* dynamic on/off */
	boolean use_page;       /* paged on/off */
	/* fixed memory layout */
	int[] mmr_map;          /* memory map */
	/* dynamic memory layout */
	int din_mmr;            /* memory size */
	boolean cond_dfrg;      /* defragmentation mode */
	boolean add_in_min;     /* insert mode */
	int dfrg_prt;           /* defragmentation border */
	/* paged memory layout */
	int page_size;          /* page size */
	int page_cnt;           /* page count */
}

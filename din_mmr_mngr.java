/* dynamic memory manager */
class din_mmr_mngr extends mmr_mngr {
	/* fields */
	/* memory map */
	class mmr_blk {
		prc prc;            /* process */
		int mmr;            /* memory size */
		mmr_blk prev, next;  /* neighbour blocks */
		/* constructor */
		mmr_blk(prc prc, int mmr, mmr_blk prev, mmr_blk next) {
			this.prc = prc;
			this.mmr = mmr;
			this.prev = prev;
			this.next = next;
		}
	}
	mmr_blk fst_blk;
	/* working modes */
	boolean add_in_min, /* optimal fit (true) or first fit (false) */
		cond_dfrg;  /* defragmentation after each removing (false) or after hole accumulating (true) */
	float dfrg_prt;     /* value [holes] / [all memory], at which defragmentation starts */

	/* manager creation */
	din_mmr_mngr(int mmr, int deq_size, boolean prrt_mode, boolean add_in_min, boolean cond_dfrg, float dfrg_prt) {
		/* memory & working prc map */
		fst_blk = new mmr_blk(null, mmr, null, null);    /* first block (full mem) */
		/* awaiting processes */
		prc_deq = new prc_deq(deq_size, prrt_mode);    /* awaiting queue */
		/* working modes */
		this.add_in_min = add_in_min;     /* insert algorythm flag */
		this.cond_dfrg = cond_dfrg;       /* deframentation flag */
		this.dfrg_prt = dfrg_prt;         /* defragmentation border (for 1 of algorythms) */

		/* memory statistics */
		size = free_size = mmr;         /* all memory is free yet */
		nfree_size = 0;
		/* process statistics */
		wrk_cnt = wait_cnt = 0;         /* no working nor waiting processes yet */
		/* request statistics */
		all_req = acc_req = nacc_req = deq_req = 0; /* no requests yet */
	}

	/* methods */
	int prc_add(prc new_prc) {
		all_req++;          /* total requests++ */
		/* firstly try to add in hole */
		mmr_blk cur_blk = fst_blk;    /* go from first block */
		while (cur_blk != null) {
			if (cur_blk.prc == null)   /* empty block */
				/* bigger size -> need add hole of difference */
				if (cur_blk.mmr>new_prc.mmr) {
					cur_blk.next = new mmr_blk(null, cur_blk.mmr - new_prc.mmr, cur_blk, cur_blk.next);
					cur_blk.prc = new_prc;
					cur_blk.mmr = new_prc.mmr;
					if (cur_blk.next.next != null)
						cur_blk.next.next.prev = cur_blk.next;
					break;
					/* equal size -> add prc without adding block */
				}else if (cur_blk.mmr == new_prc.mmr) {
					cur_blk.prc = new_prc;
					break;
				}/* lower size -> continue search */
			/* non empty block -> continue search */
			cur_blk = cur_blk.next;
		}
		/* no needed block available -> add to queue */
		if (cur_blk == null) {
			prc_deq.add(new_prc);
			return WAIT;
		}
		/* prc was added -- update statistics */
		/* update prc stat */
		wrk_cnt++;
		acc_req++;
		/* update mem stat */
		free_size -= new_prc.mmr;
		nfree_size += new_prc.mmr;
		return CONFIRMED;
	}
	int prc_del(prc del_prc) {       /* optimize */
		/* look for block from start */
		mmr_blk cur_blk = fst_blk;
		while (cur_blk.prc != del_prc)
			cur_blk = cur_blk.next;
		/* now we are staying at block beeing freeing */
		cur_blk.prc = null;   /* remove process */
		/* if possible, unite with prev. hole */
		if (cur_blk.prev != null && cur_blk.prev.prc == null) {
			cur_blk.prev.next = cur_blk.next;
			cur_blk.prev.mmr += cur_blk.mmr;
			cur_blk = cur_blk.prev;
			if (cur_blk.next != null)
				cur_blk.next.prev = cur_blk;
		}
		/* if possible, unite with next hole */
		if (cur_blk.next != null && cur_blk.next.prc == null) {
			cur_blk.mmr += cur_blk.next.mmr;
			cur_blk.next = cur_blk.next.next;
			if (cur_blk.next != null)
				cur_blk.next.prev = cur_blk;
		}
		/* update mem stat */
		free_size += del_prc.mmr;
		nfree_size -= del_prc.mmr;
		/* update prc stat */
		wrk_cnt--;

		/*
		 * do defragmentation according to mode;
		 * cur_blk points to united hole
		 */
		if (cur_blk.next != null)     /* hole isn't at memory end (where it must be) */
			if (cond_dfrg) {                  /* defrag mode with accumulation */
				/* counting holes */
				cur_blk = fst_blk;
				int empty_cnt = 0;
				/* the last one is allowed to be hole, don't count it */
				while (cur_blk.next != null) {
					if (cur_blk.prc == null)
						empty_cnt++;   /* accumulate hole count */
					cur_blk = cur_blk.next;
				}
				/* [holes] / [all blocks] >= max -> defragmentation */
				if ((float)empty_cnt / (empty_cnt + wrk_cnt) >= dfrg_prt)
					dfrg();
			} else dfrg();                   /* regular defragmentation mode */
		/* try to get from queue */
		return deq_to_mmr() == REPLACED ? REPLACED : DELETED;
	}
	/* defragmentation */
	void dfrg() {
		int empty = 0;                /* total holes memory */
		if (fst_blk.prc == null) {      /* if 1st block is empty, repoint first block pointer */
			empty += fst_blk.mmr;
			(fst_blk = fst_blk.next).prev = null;
		}
		mmr_blk cur_blk = fst_blk;    /* go from start */
		while (cur_blk.next != null) {
			if (cur_blk.prc == null) {  /* block is empty */
				empty += cur_blk.mmr; /* update total holes size */
				if (cur_blk.prev != null)
					cur_blk.prev.next = cur_blk.next;	/* repoint prev */
				cur_blk.next.prev = cur_blk.prev;		/* repoint next */
			}
			cur_blk = cur_blk.next;
		}
		/* are staying at the last block */
		if (cur_blk.prc == null) {
			empty += cur_blk.mmr;     /* update total hole size */
			cur_blk = cur_blk.prev;   /* cur_b.prev != null isn't checked, because isn't possible at defrag */
		}
		/* move all holes to end */
		cur_blk.next = new mmr_blk(null, empty, cur_blk, null);
	}
	prc[] get_wrk_prc() {
		if (wrk_cnt == 0)
			return null;
		prc[] wrk_lst = new prc[wrk_cnt];
		int top = 0;
		mmr_blk cur_blk = fst_blk;
		while (top != wrk_cnt) {
			if (cur_blk.prc != null)
				wrk_lst[top++] = cur_blk.prc;
			cur_blk = cur_blk.next;
		}
		return wrk_lst;
	}
	mmr_blk get_blk(int pos) {
		if (pos < 0 || pos >= get_blk_cnt())
			return null;
		mmr_blk cur_blk = fst_blk;
		while (pos-- != 0)
			cur_blk = cur_blk.next;
		return cur_blk;
	}
	int get_blk_cnt() {
		int cnt = 1;
		mmr_blk cur_blk = fst_blk;
		while ((cur_blk = cur_blk.next) != null)
			cnt++;
		return cnt;
	}
	public String toString() {
		return "dynamic memory";
	}
}

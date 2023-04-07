/* paged memory manager */
class page_mmr_mngr extends mmr_mngr {
	/* fields */
	int page_size, page_cnt;     /* page size, page count */
	prc[] prc_lst;              /* process list corresponding to pages */

	/* manager creation */
	page_mmr_mngr(int page_size, int page_cnt, int deq_size, boolean prrt_mode) {
		/* memory params */
		this.page_cnt = page_cnt;             /* page count */
		this.page_size = page_size;           /* page size */
		/* memory map & working processes */
		prc_lst = new prc[page_cnt];
		/* awaiting processes */
		prc_deq = new prc_deq(deq_size, prrt_mode);    /* waiting queue */
		/* memory statistics */
		free_size = size = page_size * page_cnt;  /* all memory is free yet */
		nfree_size = 0;
		/* process statistics */
		wrk_cnt = wait_cnt = 0;                 /* no working nor waiting processes yet */
		/* request statistics */
		all_req = acc_req = nacc_req = deq_req = 0; /* no requests yet */
	}

	/* methods */
	int prc_add(prc new_prc) {
		all_req++;      /* total requests++ */
		/* reject if need > memory than machine has */
		if (new_prc.mmr > size) {
			nacc_req++;
			return REJECTED;
		}
		/* if no free memory available for prc -- to queue */
		if (new_prc.mmr > free_size) {
			prc_deq.add(new_prc);
			return WAIT;
		}
		/* otherwise can be placed right now */
		int pages = new_prc.mmr / page_size + (new_prc.mmr % page_size == 0 ? 0 : 1); /* process page count */
		/* update memory stat (before loop, because it decreeses pages) */
		free_size -= pages * page_size;
		nfree_size += pages * page_size;
		/* load process to memory pages */
		for (int i = 0; pages != 0; i++)
			if (prc_lst[i] == null) {
				prc_lst[i] = new_prc;
				pages--;
			}
		/* update prc stat */
		wrk_cnt++;
		acc_req++;
		return CONFIRMED;
	}
	int prc_del(prc del_prc) {
		/* get process page count */
		int pages = del_prc.mmr / page_size + (del_prc.mmr % page_size == 0 ? 0 : 1);
		/* update mem stat (before loop, because it decreeses pages) */
		free_size += pages * page_size;
		nfree_size -= pages * page_size;
		/* free process' pages */
		for (int i = 0; pages != 0; i++)
			if (prc_lst[i] == del_prc) {
				prc_lst[i] = null;
				pages--;
			}
		/* update prc stat */
		wrk_cnt--;
		/* try to get from queue */
		return deq_to_mmr() == REPLACED ? REPLACED : DELETED;
	}
	prc[] get_wrk_prc() {
		if (wrk_cnt == 0)
			return null;
		prc[] wrk_lst = new prc[wrk_cnt];
        mmr_ccl:
		for (int i = 0, top = 0; top != wrk_cnt; i++) {
			if (prc_lst[i] == null)
				continue;
			for (int j = 0; j < top; j++)
				if (prc_lst[i] == wrk_lst[j])
					continue mmr_ccl;
			wrk_lst[top++] = prc_lst[i];
		}
		return wrk_lst;
	}
	public String toString() {
		return "paged memory";
	}
}

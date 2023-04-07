/* fixed memory manager */
class fix_mmr_mngr extends mmr_mngr{
	/* fields */
	int max_cnt;                /* section count ( =  =  max prc count) */
	int[] mmr_map;              /* list of block sizes */
	prc[] prc_lst;              /* lise of processes in blocks */

	/* manager creation */
	fix_mmr_mngr(int[] mmr, int deq_size, boolean prrt_mode) {
		/* memory map */
		mmr_map = new int[max_cnt = mmr.length];
		for(int i = 0; i < max_cnt; i++)
			size += (mmr_map[i] = mmr[i]);
		/* working processes */
		prc_lst = new prc[max_cnt];   /* list of future processes */
		/* awaiting processes */
		prc_deq = new prc_deq(deq_size, prrt_mode);   /* process queue */

		/* memory statistics */
		free_size = size;             /* all memory is free yet  */
		nfree_size = 0;
		/* prc statistics */
		wrk_cnt = wait_cnt = 0;         /* no working nor waiting processes yet */
		/* request statistics */
		all_req = acc_req = nacc_req = deq_req = 0; /* no requests yet */
	}

	/* methods */
	int prc_add(prc new_prc) {
		all_req++;                      /* total++ */
		/* reject for too big prc (which principally cann't work in this OS) */
        too_big:
		{
			for (int i = 0; i < max_cnt; i++)
				if (mmr_map[i] >= new_prc.mmr)
					break too_big;
			nacc_req++;
			return REJECTED;
		}
		/* add to queue if working count == max */
		if (wrk_cnt == max_cnt) {
			prc_deq.add(new_prc);
			return WAIT;
		}
		/*
		 * main: load process to memory;
		 * we search for minimal block, which can hold process' addr space
		 * (+ first fit (according to cnfg))
		 */
		int min_mmr = -1;
		/* look for enough big block */
		for (int i = 0; i < max_cnt; i++)
			if (prc_lst[i] == null && mmr_map[i] >= new_prc.mmr) {
				min_mmr = i;
				break;
			}
		/* if no -- add to waiting queue */
		if (min_mmr == -1) {
			prc_deq.add(new_prc);
			return WAIT;
		}
		/* if yes -- find minimal & put process there */
		for (int i = min_mmr + 1; i < max_cnt; i++)
			if (prc_lst[i] == null && mmr_map[i] >= new_prc.mmr &&
			    mmr_map[i] < mmr_map[min_mmr])
				min_mmr = i;
		prc_lst[min_mmr] = new_prc;
		/* update memory statistics */
		free_size -= mmr_map[min_mmr];
		nfree_size += mmr_map[min_mmr];
		/* update process statistics */
		wrk_cnt++;
		acc_req++;
		return CONFIRMED;            /* process has been added */
	}
	int prc_del(prc del_prc) {
		/* remove process */
		/* remove from memory */
		for (int i = 0; i < max_cnt; i++)
			if (prc_lst[i] == del_prc) {
				/* remove */
				prc_lst[i] = null;
				/* update memory stat */
				free_size += mmr_map[i];
				nfree_size -= mmr_map[i];
				break;
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
		for (int i = 0, top = 0; top != wrk_cnt; i++)
			if (prc_lst[i] != null)
				wrk_lst[top++] = prc_lst[i];
		return wrk_lst;
	}
	public String toString() {
		return "fixed memory";
	}
}

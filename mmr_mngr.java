/* common memory manager (doesn't specify memory structure) */
abstract class mmr_mngr{
	/* -------- fields -------- */
	/* memory statistics */
	long size, free_size, nfree_size;   /* all, free, non-free (byte) */
	/* request statistics */
	int all_req, acc_req, nacc_req, deq_req; /* all, confirmed, rejected, to waiting queue */
	/* process statistics */
	int wrk_cnt, wait_cnt;               /* working & waiting prc count */
	/* waiting process queue */
	class prc_deq{
		/* fields */
		prc[] prc_deq;              /* processes */
		int head, tail;             /* head (get) & tail (put) */
		boolean prrt_mode;          /* priority mode on/off */

		/* create empty queue */
		prc_deq(int deq_size, boolean prrt_mode) {
			prc_deq = new prc[deq_size];
			head = 0;
			tail = -1;
			this.prrt_mode = prrt_mode;
		}

		/* methods */
		void add(prc new_prc) {
			/* add prc to queue end */
			/* tail cann't grow: */
			if (tail == prc_deq.length - 1) {
				/* possible head cases: */
				/* head is at start -> queue is fill */
				if (head == 0) {
					nacc_req++; /* REJECT */
					return;
				}
				/* head is in middle -> have space, but need push to start */
				int i;		/* here for speed */
				for (i = 0; i <= tail - head; i++)
					prc_deq[i] = prc_deq[i + head];
				/* update head & tail */
				head = 0;
				tail = i - 1;       /* here i (== tail - head + 1) is before loop (don't touch) */
			}
			/* tail can grow, not touch head */
			prc_deq[++tail] = new_prc;
			deq_req++;
			wait_cnt++;
			/* move process to head end if priorities on */
			if (prrt_mode)
				for (int i = head; i < tail; i++)  /* process is at tail */
					if (prc_deq[i].prrt < new_prc.prrt) {   /* need move from tail to i */
						for (int j = tail; j > i; j--)
							prc_deq[j] = prc_deq[j-1];
						prc_deq[i] = new_prc;
						break;
					}
		}
		prc get() {
			/* queue is empty */
			if (head > tail)
				return null;
			/* >= 1 element, get first (head) */
			prc get_prc = prc_deq[head++];
			if (head > tail) {    /* got last one, */
				head = 0;     /* reset pointers to start */
				tail = -1;
			}
			wait_cnt--;
			return get_prc;
		}
		boolean is_empty() {
			return head > tail;
		}
	}
	prc_deq prc_deq;
	/* add/remove request possible results */
	final static int
		REPLACED = 3,     /* deleted & replaced with some from queue */
		DELETED = 2,      /* deleted */
		CONFIRMED = 1,    /* loaded to memory */
		WAIT = 0,         /* added to queue */
		REJECTED = -1,    /* rejected & forgotten */
		EMPTY_DEQ = -2,   /* queue is empty */
		FAIL_DEQ = -3;    /* cann't get from queue */

	/* ------- methods ------- */
	/* memory management */
	abstract int prc_add(prc new_prc);          /* loading process to memory */
	abstract int prc_del(prc del_prc);          /* removing process from memory */
	int deq_to_mmr() {                          /* loading process to memory from queue */
		/* if queue isn't empty try to load from it */
		if (!prc_deq.is_empty()) {
			/*
			 * priority mode, can try only once
			 * (because next cann't be before)
			 */
			if (prc_deq.prrt_mode)
				return prc_add(prc_deq.get()) == CONFIRMED ? REPLACED : FAIL_DEQ;
			/*
			 * non-priority mode, loop all queue, until
			 * reach end OR find process to load
			 */
			for (int i = 0; i < wait_cnt; i++)
				if (prc_add(prc_deq.get()) == CONFIRMED)
					return REPLACED;
			return FAIL_DEQ;
		}
		/* queue is empty */
		return EMPTY_DEQ;
	}
	/* get working processes list (for external purposes) */
	abstract prc[] get_wrk_prc();
	/* get waiting processes list (for external purposes) */
	prc[] get_wait_prc() {
		prc[] wait_lst = new prc[wait_cnt];
		for (int i = 0; i < wait_cnt; i++)
			wait_lst[i] = prc_deq.prc_deq[i + prc_deq.head];
		return wait_lst;
	}
}

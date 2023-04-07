/* ---------- processes ----------- */
class prc {
	/* fields */
	String name;    /* name */
	int mmr;        /* memory */
	byte prrt;      /* priority */
	prc prnt;       /* parent */
	boolean strict; /* child dependency (cascade/restrict) */
	/* creation */
	prc(String name, int mmr) {
		this.name = name;
		this.mmr = mmr;
		this.prrt = 0;
	}
	prc(String name, int mmr, byte prrt) {
		this.name = name;
		this.mmr = mmr;
		this.prrt = prrt;
	}
	prc(String name, int mmr, byte prrt, prc prnt, boolean strict) {
		this.name = name;
		this.mmr = mmr;
		this.prrt = prrt;
		this.prnt = prnt;
		this.strict = strict;
	}
	/* methods */
	public String toString() {
		return name;
	}
	boolean run() {
		/*
		 * executable process code;
		 * true if correct finishing,
		 * false if incorrect finishing
		 */
		return true;
	}
}

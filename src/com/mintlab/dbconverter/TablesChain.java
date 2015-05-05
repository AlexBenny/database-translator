package com.mintlab.dbconverter;

import java.util.LinkedList;


public class TablesChain extends LinkedList<Relation> {

	private Constraint constraint;
	private TableDescriptor table;

	public void setConstraint(Constraint constraint) {
		this.constraint = constraint;	
	}

	public Constraint getConstraint() {
		return constraint;
	}

	public boolean contains(TableDescriptor td) {
		for (Relation r : this) {
			if (r.getTableDestDescriptor() == td || r.getTableSrcDescriptor() == td) return true;
		}
		return false;
	}

	public void setFocusTable(TableDescriptor td) {
		table = td;
	}
	
	public TableDescriptor getFocusTable() {
		return table;
	}
	
}

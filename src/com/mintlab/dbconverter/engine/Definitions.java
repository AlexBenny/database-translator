package com.mintlab.dbconverter.engine;

import com.mintlab.dbconverter.Relation;
import com.mintlab.dbconverter.TableDescriptor;


public class Definitions {
	
	@DefaultRelationAnnotation
	public static Relation defaultRelation(TableDescriptor td, TableDescriptor td2) {
		if (td.has(td2.getName()+"_pk")) {
			String sql = td.getName()+"."+td2.getName()+"_pk"+" = "+td2.getName()+".pk";
			return new Relation(td,td2,sql);
		}
		return null;
	}
	
}

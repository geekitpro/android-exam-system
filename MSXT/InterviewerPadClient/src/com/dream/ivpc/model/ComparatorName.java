package com.dream.ivpc.model;

import java.util.Comparator;

public	class ComparatorName implements Comparator<Object> {
	public int compare(Object arg0, Object arg1) {
		CandiateBean cb1 = (CandiateBean) arg0;
		CandiateBean cb2 = (CandiateBean) arg1;
		return cb1.getName().compareTo(cb2.getName());
	}
}

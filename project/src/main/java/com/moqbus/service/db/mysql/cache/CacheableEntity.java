package com.moqbus.service.db.mysql.cache;

public abstract class CacheableEntity  {
	public abstract Integer getId();
	public abstract String getCacheKeyVal();
	

	@Override
	public boolean equals(Object e) {
		
		if (e instanceof CacheableEntity) {
			return (((CacheableEntity)e).getId().equals(getId()));
		}
		
		return false;
	}
	
}

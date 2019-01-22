package com.moqbus.service.db.mysql.cache;

public abstract class CacheableEntity  {
	public abstract Integer getId();
	public abstract String getCacheKeyVal();
	public boolean equals(CacheableEntity e) {
		return (e.getId().equals(getId()));
	}
	
}

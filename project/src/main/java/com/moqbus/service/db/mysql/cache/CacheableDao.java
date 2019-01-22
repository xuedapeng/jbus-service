package com.moqbus.service.db.mysql.cache;

import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

import com.moqbus.service.db.mysql.ConnectionPool;
import com.moqbus.service.db.mysql.dao.BaseDao;

public abstract  class CacheableDao<E extends CacheableEntity> extends BaseDao {

	public CacheableDao(Connection _conn) {
		super(_conn);
		
	}

	public CacheableDao() {
		this.conn = ConnectionPool.getInstance();
	}
	
	abstract public List<E>  findAll();
	abstract public List<Integer>  findAllIds();
	abstract public Long countAll();
	abstract public Integer findMaxId();
	abstract public List<E> findAfterTime(Date time);
	abstract public Long countAfterTime(Date time);
	
//	 public Class<E> getClassE()
//	    {
//	        @SuppressWarnings("unchecked")
//			Class<E> tClass = (Class<E>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
//	        return tClass;
//	    }
}

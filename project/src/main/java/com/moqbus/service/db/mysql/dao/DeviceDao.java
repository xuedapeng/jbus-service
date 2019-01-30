package com.moqbus.service.db.mysql.dao;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.log4j.Logger;

import com.moqbus.service.db.mysql.ConnectionPool;
import com.moqbus.service.db.mysql.bean.DeviceEntity;
import com.moqbus.service.db.mysql.cache.CacheableDao;
import com.moqbus.service.db.mysql.cache.CacheableEntity;

public class DeviceDao< E extends CacheableEntity> extends CacheableDao<E> {

	static Logger LOG = Logger.getLogger(DeviceDao.class);
	
	public DeviceDao(Connection _conn) {
		super(_conn);
	}

	public DeviceDao() {
		this.conn = ConnectionPool.getInstance();
	}
	
	@SuppressWarnings("unchecked")
	public List<E> findAll() {

		try {
	        //创建SQL执行工具   
	        QueryRunner qRunner = new QueryRunner();  
	        
			String sql = "select * from t_device ";
			sql += " where status = 1";

			BeanListHandler<E> bh = new BeanListHandler<E>((Class<? extends E>) DeviceEntity.class);
			 
			List<E> list = (List<E>) qRunner.query(conn, sql, bh);
	        
	        return list != null? list:new ArrayList<E>();
	        
		} catch (SQLException e) {
			
			LOG.error("", e);
			return null;
		} 
	}

	@SuppressWarnings("unchecked")
	public List<E> findAfterTime(Date time) {

		try {
	        //创建SQL执行工具   
	        QueryRunner qRunner = new QueryRunner();  
	        
			String sql = "select * from t_device ";
			sql += " where status = 1";
			sql += " and updateTime > ?";

			BeanListHandler<E> bh = new BeanListHandler<E>((Class<? extends E>) DeviceEntity.class);
			 
			List<E> list = (List<E>) qRunner.query(conn, sql, bh, time);
	        
	        return list != null? list:new ArrayList<E>();
	        
		} catch (SQLException e) {
			
			LOG.error("", e);
			return null;
		} 
	}
	
	public Long countAfterTime(Date time) {

		try {
	        //创建SQL执行工具   
	        QueryRunner qRunner = new QueryRunner();  
	        
			String sql = "select count(id) from t_device ";
			sql += " where status = 1";
			sql += " and updateTime > ?";

			ScalarHandler<BigInteger> bh = new ScalarHandler<BigInteger>();
			 
			Object count =  qRunner.query(conn, sql, bh, time);

	        return (Long)count;
	        
		} catch (Exception e) {
			
			LOG.error(e.getMessage(), e);
			return 0L;
		} 
	}

	public Long countAll() {

		try {
	        //创建SQL执行工具   
	        QueryRunner qRunner = new QueryRunner();  
	        
			String sql = "select count(id) from t_device ";
			sql += " where status = 1";

			ScalarHandler<BigInteger> bh = new ScalarHandler<BigInteger>();
			 
			Object count =  qRunner.query(conn, sql, bh);

	        return (Long)count;
	        
		} catch (Exception e) {
			
			LOG.error(e.getMessage(), e);
			return 0L;
		} 
	}

	@Override
	public Integer findMaxId() {

		try {
	        //创建SQL执行工具   
	        QueryRunner qRunner = new QueryRunner();  
	        
			String sql = "select max(id) from t_device ";
			sql += " where status = 1";

			ScalarHandler<BigInteger> bh = new ScalarHandler<BigInteger>();
			 
			Object maxId =  qRunner.query(conn, sql, bh);

	        return (Integer)maxId;
	        
		} catch (Exception e) {
			
			LOG.error(e.getMessage(), e);
			return 0;
		} 
	}

	@Override
	public List<Integer> findAllIds() {

		try {
	        //创建SQL执行工具   
	        QueryRunner qRunner = new QueryRunner();  
	        
			String sql = "select id from t_device ";
			sql += " where status = 1";

			ArrayListHandler bh = new ArrayListHandler();
			 
			List<Object[]> list = (List<Object[]>) qRunner.query(conn, sql, bh);
	        
	        return list != null? list.stream().map(o->(Integer)o[0]).collect(Collectors.toList()):new ArrayList<Integer>();
	        
		} catch (SQLException e) {
			
			LOG.error("", e);
			return null;
		} 
	}

}

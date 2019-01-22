//package com.moqbus.service.db.mysql.dao;
//
//import java.math.BigInteger;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import org.apache.commons.dbutils.QueryRunner;
//import org.apache.commons.dbutils.handlers.BeanListHandler;
//import org.apache.commons.dbutils.handlers.ScalarHandler;
//
//import com.moqbus.service.db.mysql.bean.VScheduleEntity;
//
//public class VScheduleDao extends BaseDao {
//
//	public VScheduleDao(Connection _conn) {
//		super(_conn);
//	}
//	
//	public List<VScheduleEntity> findAll() {
//
//		try {
//	        //创建SQL执行工具   
//	        QueryRunner qRunner = new QueryRunner();  
//	        
//			String sql = "select * from v_schedule ";
//			sql += " where status = 1";
//			sql += " order by max_updateTime desc ";
//
//			BeanListHandler<VScheduleEntity> bh = new BeanListHandler<VScheduleEntity>(VScheduleEntity.class);
//			 
//			List<VScheduleEntity> list = (List<VScheduleEntity>) qRunner.query(conn, sql, bh);
//	        
//	        return list != null? list:new ArrayList<VScheduleEntity>();
//	        
//		} catch (SQLException e) {
//			
//			LOG.error("", e);
//			return null;
//		} 
//	}
//
//	public Long countAfterTime(String max_updateTime) {
//
//		try {
//	        //创建SQL执行工具   
//	        QueryRunner qRunner = new QueryRunner();  
//	        
//			String sql = "select count(id) from v_schedule ";
//			sql += " where status = 1";
//			sql += " and max_updateTime > ?";
//
//			ScalarHandler<BigInteger> bh = new ScalarHandler<BigInteger>();
//			 
//			Object count =  qRunner.query(conn, sql, bh, max_updateTime);
//
//	        return (Long)count;
//	        
//		} catch (Exception e) {
//			
//			LOG.error(e.getMessage(), e);
//			return null;
//		} 
//	}
//
//
//}

package com.moqbus.service.db.mysql.dao;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.moqbus.service.db.mysql.ConnectionPool;
import com.moqbus.service.db.mysql.bean.LastDataEntity;

public class LastDataDao extends BaseDao {

	public LastDataDao(Connection _conn) {
		super(_conn);
	}
	
	static Connection conn() {
		return ConnectionPool.getInstance();
	}
	
	public static LastDataEntity findBySno(String deviceSn, String sensorNo) {

		try {
	        //创建SQL执行工具   
	        QueryRunner qRunner = new QueryRunner();  
	        
			String sql = "select * from t_last_data ";
			sql += " where deviceSn = ?";
			sql += " and sensorNo = ?";

			BeanHandler<LastDataEntity> bh = new BeanHandler<LastDataEntity>(LastDataEntity.class);
			 
			LastDataEntity bean = (LastDataEntity) qRunner.query(conn(), sql, bh, deviceSn, sensorNo);
	        
	        return bean;
	        
		} catch (SQLException e) {
			
			LOG.error("", e);
			return null;
		} 
	}

	public static Long update(String deviceSn, String sensorNo, String message) {

		try {
	        //创建SQL执行工具   
	        QueryRunner qRunner = new QueryRunner();  
	        
			String sql = "update t_last_data ";
			sql += " set message = ?, updateTime = ?";
			sql += " where deviceSn = ? and sensorNo = ?";
				  
			qRunner.update(conn(), sql, 
					message,
					new Date(),
					deviceSn,
					sensorNo
					);

			BigInteger id= (BigInteger) qRunner.query(conn(), SQL_GET_ID,new ScalarHandler<BigInteger>());
			
	        return id.longValue();
	        
		} catch (SQLException e) {

			LOG.error("", e);
			return null;
		} 
	}
	
	public static Long insert(LastDataEntity bean) {

		try {
	        //创建SQL执行工具   
	        QueryRunner qRunner = new QueryRunner();  
	        
			String sql = "insert into t_last_data( ";
			sql += " deviceSn, sensorNo, dsKey, message,  createTime, updateTime";
			sql += " ) values( ";
			sql += " ?, ?, ?, ?, ?, ?";
			sql += " ) ";
				  
			qRunner.update(conn(), sql, 
					bean.getDeviceSn(),
					bean.getSensorNo(),
					bean.getDsKey(),
					bean.getMessage(),
					bean.getCreateTime(),
					new Date()
					);

			BigInteger id= (BigInteger) qRunner.query(conn(), SQL_GET_ID,new ScalarHandler<BigInteger>());
			
	        return id.longValue();
	        
		} catch (SQLException e) {

			LOG.error("", e);
			return null;
		} 
	}
}

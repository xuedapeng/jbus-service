package com.moqbus.service.db.mysql.dao;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.moqbus.service.db.mysql.ConnectionPool;
import com.moqbus.service.db.mysql.bean.EventEntity;

public class EventDao extends BaseDao {

	public EventDao(Connection _conn) {
		super(_conn);
	}
	
	static Connection conn = ConnectionPool.getInstance();
	
	public static Long insert(EventEntity bean) {

		try {
	        //创建SQL执行工具   
	        QueryRunner qRunner = new QueryRunner();  
	        
			String sql = "insert into t_event( ";
			sql += " deviceId, deviceSn, event, time, memo ";
			sql += " ) values( ";
			sql += " ?, ?, ?, ?, ?";
			sql += " ) ";
				  
			qRunner.update(conn, sql, 
					bean.getDeviceId(),
					bean.getDeviceSn(),
					bean.getEvent(),
					bean.getTime(),
					bean.getMemo()
					);

			BigInteger id= (BigInteger) qRunner.query(conn, SQL_GET_ID,new ScalarHandler<BigInteger>());
			
	        return id.longValue();
	        
		} catch (SQLException e) {

			LOG.error("", e);
			return null;
		} 
	}
}

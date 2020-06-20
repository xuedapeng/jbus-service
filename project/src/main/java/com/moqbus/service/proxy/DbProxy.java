package com.moqbus.service.proxy;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.google.common.collect.ImmutableMap;
import com.mongodb.client.MongoCollection;
import com.moqbus.service.common.conf.ZSystemConfig;
import com.moqbus.service.common.constant.JbusConst;
import com.moqbus.service.common.helper.DateHelper;
import com.moqbus.service.common.helper.HexHelper;
import com.moqbus.service.common.helper.JsonHelper;
import com.moqbus.service.db.MongoUtil;

public class DbProxy {

	static Logger LOG = Logger.getLogger(DbProxy.class);
	
	static String _dbName = ZSystemConfig.getProperty("mdb_dbname");
	static String _collCmd = "cmd";
	static String _collDat = "dat";
	static String _collSts = "sts";
	static String _collMsglog = "msglog";
	
	static Map<String, String> _topicType2Coll = ImmutableMap.of(
			JbusConst.TOPIC_PREFIX_CMD, _collCmd, 
			JbusConst.TOPIC_PREFIX_DAT, _collDat, 
			JbusConst.TOPIC_PREFIX_STS, _collSts);
	
	
	private static void save(String topicType, String deviceSn, String message, byte[] origin, boolean parsed, Date time) {
		
		String colName =  _topicType2Coll.get(topicType);
		if (colName == null) {
	        LOG.error("DbProxy.save: wrong topicType:" + topicType);
			return;
		}
		
        MongoCollection<Document> coll = MongoUtil.getCollection(_dbName, colName);
        Document doc = new Document();
        doc.put("deviceSn", deviceSn);
        if (parsed) {
        	doc.put("content", Document.parse(message));
        }
        doc.put("origin", HexHelper.bytesToHexString(origin));
        doc.put("time", makeTimeDoc(time));
        
        doc.put("parsed", parsed?1:0);
        coll.insertOne(doc);
        
        LOG.info("DbProxy.save" + doc.toJson());
	}
	
	public static void saveOrigin(String topicType, String deviceSn, byte[] orgin, Date time) {
		
//		String content = HexHelper.bytesToHexString(message);
		
		save(topicType, deviceSn, null , orgin, false, time);
	}
	
	public static void saveParsed(String topicType, String deviceSn, String message, byte[] origin, Date time) {
		
			save(topicType, deviceSn, message, origin, true, time);
	}
	
	private static Document makeTimeDoc(Date time) {
		Document doc = new Document();
        String timeStr = DateHelper.toYmdhms(time);
        doc.put("year", timeStr.substring(0, 4));
        doc.put("month", timeStr.substring(0, 7));
        doc.put("day", timeStr.substring(0, 10));
        doc.put("hour", timeStr.substring(0, 13));
        doc.put("ten-minute", timeStr.substring(0, 15) + "0");
        doc.put("minute", timeStr.substring(0, 16));
        doc.put("second", timeStr);
        
        Calendar cl = Calendar.getInstance();
        cl.setTime(time);
        int weekOfYear = cl.get(Calendar.WEEK_OF_YEAR);
        if (cl.get(Calendar.MONTH) >=11 && weekOfYear <=1) {
        	weekOfYear += 52;
        }
        doc.put("week", timeStr.substring(0, 4)  + " W" + (weekOfYear<10?"0":"") + String.valueOf(weekOfYear));
        
        return  doc;
	}
	
	// 保存原始的往来指令和返回数据
	public static void saveMsgLog(String topicType, String deviceSn, byte[] origin, Date time) {
		
		if (!JbusConst.TOPIC_PREFIX_CMD.equals(topicType) && !JbusConst.TOPIC_PREFIX_DAT.equals(topicType)) {
			return;
		}
		
        MongoCollection<Document> coll = MongoUtil.getCollection(_dbName, _collMsglog);
        Document doc = new Document();
        doc.put("deviceSn", deviceSn);
        doc.put("type", topicType);
        doc.put("msg", HexHelper.bytesToHexString(origin));
        doc.put("time", time);
        
        coll.insertOne(doc);
        
        LOG.info("DbProxy.save" + doc.toJson());
		
	}
}

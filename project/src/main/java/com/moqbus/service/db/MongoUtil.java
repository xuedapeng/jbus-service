package com.moqbus.service.db;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.moqbus.service.common.conf.ZSystemConfig;

public class MongoUtil {

    static String _host = ZSystemConfig.getProperty("mdb_host");
    static int _port = Integer.valueOf(ZSystemConfig.getProperty("mdb_port"));
    static String _user = ZSystemConfig.getProperty("mdb_user");
    static String _pwd = ZSystemConfig.getProperty("mdb_pwd");
    static String _db = ZSystemConfig.getProperty("mdb_dbname");

   private static MongoClient mongoClient;

   static {
       System.out.println("===============MongoDBUtil初始化========================");

       ServerAddress address = new ServerAddress(_host, _port);
       MongoCredential credential = MongoCredential.createCredential(
            		   _user,
            		   _db,
                       _pwd.toCharArray()
               );
       Builder options = new MongoClientOptions.Builder();
       options.cursorFinalizerEnabled(true);
       options.connectionsPerHost(300);// 连接池设置为300个连接,默认为100
       options.connectTimeout(30000);// 连接超时，推荐>3000毫秒
       options.maxWaitTime(5000); //
       options.socketTimeout(0);// 套接字超时时间，0无限制
       options.threadsAllowedToBlockForConnectionMultiplier(5000);// 线程队列数，如果连接线程排满了队列就会抛出“Out of semaphores to get db”错误。
       options.writeConcern(WriteConcern.ACKNOWLEDGED);//

       mongoClient = new MongoClient(address, credential, options.build());
   }

   // ------------------------------------共用方法---------------------------------------------------
   /**
    * 获取DB实例 - 指定DB
    * 
    * @param dbName
    * @return
    */
   public static MongoDatabase getDB(String dbName) {
       if (dbName != null && !"".equals(dbName)) {
           MongoDatabase database = mongoClient.getDatabase(dbName);
           return database;
       }
       return null;
   }

   /**
    * 获取collection对象 - 指定Collection
    * 
    * @param collName
    * @return
    */
   public static MongoCollection<Document> getCollection(String dbName, String collName) {
       if (null == collName || "".equals(collName)) {
           return null;
       }
       if (null == dbName || "".equals(dbName)) {
           return null;
       }
       MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collName);
       return collection;
   }

   /**
    * 查询DB下的所有表名
    */
   public static List<String> getAllCollections(String dbName) {
       MongoIterable<String> colls = getDB(dbName).listCollectionNames();
       List<String> _list = new ArrayList<String>();
       for (String s : colls) {
           _list.add(s);
       }
       return _list;
   }

   /**
    * 获取所有数据库名称列表
    * 
    * @return
    */
   public static MongoIterable<String> getAllDBNames() {
       MongoIterable<String> s = mongoClient.listDatabaseNames();
       return s;
   }

   /**
    * 删除一个数据库
    */
   public static void dropDB(String dbName) {
       getDB(dbName).drop();
   }

   /**
    * 查找对象 - 根据主键_id
    * 
    * @param collection
    * @param id
    * @return
    */
   public static Document findById(MongoCollection<Document> coll, String id) {
       ObjectId _idobj = null;
       try {
           _idobj = new ObjectId(id);
       } catch (Exception e) {
           return null;
       }
       Document myDoc = coll.find(Filters.eq("_id", _idobj)).first();
       return myDoc;
   }

   /** 统计数 */
   public static int getCount(MongoCollection<Document> coll) {
       int count = (int) coll.countDocuments();
       return count;
   }

   /** 条件查询 */
   public static MongoCursor<Document> find(MongoCollection<Document> coll, Bson filter) {
       return coll.find(filter).iterator();
   }

   /** 分页查询 */
   public static MongoCursor<Document> findByPage(MongoCollection<Document> coll, Bson filter, int pageNo, int pageSize) {
       Bson orderBy = new BasicDBObject("_id", 1);
       return coll.find(filter).sort(orderBy).skip((pageNo - 1) * pageSize).limit(pageSize).iterator();
   }
   

   /**
    * 通过ID删除
    * 
    * @param coll
    * @param id
    * @return
    */
   public static int deleteById(MongoCollection<Document> coll, String id) {
       int count = 0;
       ObjectId _id = null;
       try {
           _id = new ObjectId(id);
       } catch (Exception e) {
           return 0;
       }
       Bson filter = Filters.eq("_id", _id);
       DeleteResult deleteResult = coll.deleteOne(filter);
       count = (int) deleteResult.getDeletedCount();
       return count;
   }

   /**
    * FIXME
    * 
    * @param coll
    * @param id
    * @param newdoc
    * @return
    */
   public static Document updateById(MongoCollection<Document> coll, String id, Document newdoc) {
       ObjectId _idobj = null;
       try {
           _idobj = new ObjectId(id);
       } catch (Exception e) {
           return null;
       }
       Bson filter = Filters.eq("_id", _idobj);
       // coll.replaceOne(filter, newdoc); // 完全替代
       coll.updateOne(filter, new Document("$set", newdoc));
       return newdoc;
   }

   public static void dropCollection(String dbName, String collName) {
       getDB(dbName).getCollection(collName).drop();
   }

   /**
    * 关闭Mongodb
    */
   public static void close() {
       if (mongoClient != null) {
           mongoClient.close();
           mongoClient = null;
       }
   }
}
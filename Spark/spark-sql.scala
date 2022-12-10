
//由range生成Dataset
val numDS = spark.range(5, 100, 5)  //5 10 15 20.........95  numDS :DataSet[Long]
// orderBy 转换操作；desc:function；show:Action 
numDS.orderBy(desc("id")).show(5)
// 统计信息： 返回count mean stddev min max信息
numDS.describe().show
// 显示schema信息 
numDS.printSchema
// 使用RDD执行同样的操作 
numDS.rdd.map(_.toInt).stats
// 检查分区数 
numDS.rdd.getNumPartitions //res7:Int = 2

////集合生成Dataset
case class Person(name: String, age: Int, height: Int)
// 注意 Seq 中元素的类型 
val seq1 = Seq(Person("Jack", 28, 184), Person("Tom", 10, 144), Person("Andy", 16, 165))
dataset[Person]
val ds1 = spark.createDataset(seq1)
// 显示schema信息 
ds1.printSchema
ds1.show
val seq2 = Seq(("Jack", 28, 184), ("Tom", 10, 144), ("Andy", 16, 165))
val ds2 = spark.createDataset(seq2)
ds2.show

////集合生成DataFrame
DataFrame = RDD[Row] + Schema

val lst = List(("Jack", 28, 184), ("Tom", 10, 144), ("Andy", 16, 165))
val df1 = spark.createDataFrame(lst).
  // 改单个字段名时简便 
  withColumnRenamed("_1", "name1").
  withColumnRenamed("_2", "age1").
  withColumnRenamed("_3", "height1")
df1.orderBy("age1").show(10)
// desc是函数，在IDEA中使用是需要导包 

import org.apache.spark.sql.functions._

df1.orderBy(desc("age1")).show(10)
// 修改整个DF的列名 
val df2 = spark.createDataFrame(lst).toDF("name", "age", "height")

////RDD 转成 DataFrame
DataFrame = RDD[Row] + Schema
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
val sc =spark.sparkContext
val arr = Array(("Jack", 28, 184), ("Tom", 10, 144), ("Andy",
  16, 165))
val rdd1 = sc.makeRDD(arr).map(f=>Row(f._1, f._2, f._3))
val schema = StructType( StructField("name", StringType,false) ::  //false是否允许为空 true 可以为空
                         StructField("age", IntegerType, false) ::
                         StructField("height", IntegerType, false) ::
                         Nil)
val schema1 = (new StructType).
  add("name", "string", false).
  add("age", "int", false).
  add("height", "int", false)
// RDD => DataFrame，要指明schema 
val rddToDF = spark.createDataFrame(rdd1, schema)
rddToDF.orderBy(desc("name")).show(false)


// IDEA中需要，spark-shell中不需要 
import spark.implicits._
val arr2 = Array(("Jack", 28, 150), ("Tom", 10, 144), ("Andy",16, 165))
val rddToDF = sc.makeRDD(arr2).toDF("name", "age", "height")
case class Person(name:String, age:Int, height:Int)
val arr2 = Array(("Jack", 28, 150), ("Tom", 10, 144), ("Andy",16, 165))
val rdd2 =spark.sparkContext.makeRDD(arr2).map(f=>Person(f._1, f._2,f._3))
val ds2 = rdd2.toDS() // 反射推断，spark 通过反射从case 
// class的定义得到类名
val df2 = rdd2.toDF() // 反射推断 
ds2.printSchema
df2.printSchema
ds2.orderBy(desc("name")).show(10)
df2.orderBy(desc("name")).show(10)

////RDD转Dataset
Dataset = RDD[case class] 
DataFrame = RDD[Row] + Schema
val ds3 = spark.createDataset(rdd2)
ds3.show(10)


////从文件创建DateFrame(以csv文件为例) 
val df1 = spark.read.csv("data/people1.csv") //hdfs://nameservicezzj/user/edw/data/people1.csv
df1.printSchema()
df1.show()

// 指定参数 
// spark 2.3.0 
val schema = "name string, age int, job string"
val df3 = spark.read.options(Map(("delimiter", ";"), ("header", "true")))
                    .schema(schema)
                    .csv("data/people2.csv")
df3.printSchema()
df3.show

// 自动类型推断 
val df4 = spark.read.option("delimiter", ";")
                    .option("header", "true")
                    .option("inferschema", "true")
                    .csv("data/people2.csv")
df4.printSchema()
df4.show

////json文件
val df = spark.read.json("examples/src/main/resources/people.json")
df.createOrReplaceTempView("people")

val sqlDF = spark.sql("SELECT * FROM people")
sqlDF.show()
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+
// $example off:run_sql$

// $example on:global_temp_view$
// Register the DataFrame as a global temporary view
df.createGlobalTempView("people")

// Global temporary view is tied to a system preserved database `global_temp`
spark.sql("SELECT * FROM global_temp.people").show()
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+

// Global temporary view is cross-session
spark.newSession().sql("SELECT * FROM global_temp.people").show()
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+
// $example off:global_temp_view$


////创建表 有管理表和无管理表
spark.sql("create database ds_spark")
spark.sql("show databases").show(100,false)  //false 左对其，true为右对其
spark.sql("use ds_spark")
spark.sql(
  s"""
     |create table ds_spark.people
     |(
     |name string,
     |age int
     |)
     |""".stripMargin)

spark.sql(
  s"""
     |create table people
     |(
     |name string,
     |age int,
     |job string
     |)
     |Using csv
     |options(
     |PATH '/user/ds_teacher/ds_spark/people.csv'
     |)
     |""".stripMargin)

////创建全局视图
spark.sql(
  """
    |create or replace global temp view people_view
    |as select * from ds_spark.people
    |""".stripMargin)
// 创建临时视图
spark.sql(
  """
    |create or replace  temp view people_view2
    |as select * from ds_spark.people
    |""".stripMargin)

//查询全局视图，需要加上 global_temp 前缀
spark.sql("select * from global_temp.people_view").show(10,false)
//查询临时视图
spark.sql("select * from people_view2").show(10,false)
//删除视图
spark.sql("drop view if exists people_view")
spark.sql("drop view if exists people_view2")

////查看元数据
spark.catalog.listDatabases()
spark.catalog.listTables()
spark.catalog.listColumns("ds_spark.people")
spark.catalog.listFunctions.show(10000, false)

////缓存SQL表
//缓存表
spark.sql("cache table ds_spark.people")
//清除缓存表
spark.sql("uncache table ds_spark.people")
//将表读取为DataFrame
val df1 = spark.sql("select * from ds_spark.people")
val df2 = spark.table("ds_spark.people")

////Action操作
val df1 = spark.read.option("header", "true").
                    .option("inferschema","true")
                    .csv("src/main/resources/emp.csv")
df1.count
// 缺省显示20行
df1.union(df1).show()
// 显示2行
df1.show(2)
// 不截断字符
df1.toJSON.show(false)
// 显示10行，不截断字符
df1.toJSON.show(10, false)
spark.catalog.listFunctions.show(10000, false)

// collect返回的是数组, Array[org.apache.spark.sql.Row]
val c1 = df1.collect()
// collectAsList返回的是List, List[org.apache.spark.sql.Row]
val c2 = df1.collectAsList()
// 返回 org.apache.spark.sql.Row
val h1 = df1.head()
val f1 = df1.first()
// 返回 Array[org.apache.spark.sql.Row]，长度为3
val h2 = df1.head(3)
val f2 = df1.take(3)
// 返回 List[org.apache.spark.sql.Row]，长度为2
val t2 = df1.takeAsList(2)

/ 结构属性 
df1.columns // 查看列名 
df1.dtypes // 查看列名和类型 
df1.explain() // 参看执行计划 
df1.col("name") // 获取某个列 
df1.printSchema // 常用 

////Transformation 操作

df1.map(row=>row.getAs[Int](0)).show
// randomSplit(与RDD类似，将DF、DS按给定参数分成多份) 
val df2 = df1.randomSplit(Array(0.5, 0.6, 0.7))
df2(0).count
df2(1).count
df2(2).count
// 取10行数据生成新的DataSet 
val df2 = df1.limit(10)
// distinct，去重 
val df2 = df1.union(df1)
df2.distinct.count
// dropDuplicates，按列值去重 
df2.dropDuplicates.show
df2.dropDuplicates("mgr", "deptno").show
df2.dropDuplicates("mgr").show
df2.dropDuplicates("deptno").show
// 返回全部列的统计（count、mean、stddev、min、max） 
ds1.describe().show
// 返回指定列的统计 
ds1.describe("sal").show
ds1.describe("sal", "comm").show


//Dataset 默认的存储级别是 MEMORY_AND_DISK
import org.apache.spark.storage.StorageLevel
spark.sparkContext.setCheckpointDir("data/checkpoint") 
df1.show()
df1.checkpoint()
df1.cache()
df1.persist(StorageLevel.MEMORY_ONLY)
df1.count()
df1.unpersist(true)
df1.createOrReplaceTempView("t1")
spark.catalog.cacheTable("t1")
spark.catalog.uncacheTable("t1")

// 列的多种表示方法。使用""、$""、'、col()、ds("") 
// 注意:不要混用；必要时使用spark.implicitis._；并非每个表示在所有的地方都有效
df1.select($"ename", $"hiredate", $"sal").show
df1.select("ename", "hiredate", "sal").show
df1.select('ename, 'hiredate, 'sal).show     ???spark 2.4不行
df1.select(col("ename"), col("hiredate"), col("sal")).show
df1.select(df1("ename"), df1("hiredate"), df1("sal")).show
// 下面的写法无效，其他列的表示法有效 
df1.select("ename", "hiredate", "sal"+100).show
df1.select("ename", "hiredate", "sal+100").show
// 这样写才符合语法 
df1.select($"ename", $"hiredate", $"sal"+100).show
df1.select('ename, 'hiredate, 'sal+100).show
// 可使用expr表达式(expr里面只能使用引号) 
df1.select(expr("comm+100"), expr("sal+100"),expr("ename")).show
//selectExpr里面字段没有双引号
df1.selectExpr("ename as name").show
df1.selectExpr("power(sal, 2)", "sal").show
df1.selectExpr("round(sal, -3) as newsal", "sal","ename").show
// drop、withColumn、 withColumnRenamed、casting 
// drop 删除一个或多个列，得到新的DF 
df1.drop("mgr")
df1.drop("empno", "mgr")
// withColumn，修改列值 
val df2 = df1.withColumn("sal", $"sal"+1000)
df2.show
// withColumnRenamed，更改列名 
df1.withColumnRenamed("sal", "newsal")
// 备注:drop、withColumn、withColumnRenamed返回的是DF 
// cast，类型转换 
df1.selectExpr("cast(empno as string)").printSchema
import org.apache.spark.sql.types._
df1.select($"empno".cast(StringType)).printSchema

// where操作
df1.filter("sal>1000").show
df1.filter("sal>1000 and job=='MANAGER'").show
// filter操作
df1.where("sal>1000").show
df1.where("sal>1000 and job=='MANAGER'").show

df1.groupBy("Job").sum("sal").show
df1.groupBy("Job").max("sal").show
df1.groupBy("Job").min("sal").show
df1.groupBy("Job").avg("sal").show
df1.groupBy("Job").count.show
// 类似having子句
df1.groupBy("Job").avg("sal").where("avg(sal) > 2000").show
df1.groupBy("Job").avg("sal").where($"avg(sal)" > 2000).show
// agg agg可以使我们同时获取多个聚合运算结果
df1.groupBy("Job").agg("sal"->"max", "sal"->"min", "sal"->"avg", "sal"->"sum", "sal"->"count").show
df1.groupBy("deptno").agg("sal"->"max", "sal"->"min", "sal"->"avg", "sal"->"sum", "sal"->"count").show
// 这种方式更好理解
df1.groupBy("Job").agg(max("sal"), min("sal"), avg("sal"),sum("sal"), count("sal")).show
// 给列取别名 withColumnRenamed
df1.groupBy("Job").agg(max("sal"), min("sal"), avg("sal"),sum("sal"), count("sal"))
                  .withColumnRenamed("min(sal)","min1").show
// 给列取别名，最简便 .as()
df1.groupBy("Job").agg(max("sal").as("max1"),
                       min("sal").as("min2"), avg("sal").as("avg3"),
                       sum("sal").as("sum4"), count("sal").as("count5")).show

// orderBy  从小到大
df1.orderBy("sal").show
df1.orderBy($"sal").show
df1.orderBy($"sal".asc).show
// 降序  从大到小
df1.orderBy(-$"sal").show   //-$
df1.orderBy('sal).show
df1.orderBy(col("sal")).show
df1.orderBy(df1("sal")).show
df1.orderBy($"sal".desc).show  //desc
df1.orderBy(-'sal).show
df1.orderBy(-'deptno, -'sal).show
// sort，以下语句等价  orderby 全局排序， sortby  在各自reduce中有序
df1.sort("sal").show
df1.sort($"sal").show
df1.sort($"sal".asc).show
df1.sort('sal).show
df1.sort(col("sal")).show
df1.sort(df1("sal")).show
df1.sort($"sal".desc).show
df1.sort(-'sal).show
df1.sort(-'deptno, -'sal).show

// 1、笛卡尔积 
df1.crossJoin(df1).count
// 2、等值连接（单字段）（连接字段empno，仅显示了一次） 
df1.join(df1, "empno").count
// 3、等值连接（多字段）（连接字段empno、ename，仅显示了一次） 
df1.join(df1, Seq("empno", "ename")).show
// 定义第一个数据集 
case class StudentAge(sno: Int, name: String, age: Int)
val lst = List(StudentAge(1,"Alice", 18), StudentAge(2,"Andy",19), 
               StudentAge(3,"Bob", 17), StudentAge(4,"Justin", 21),
               StudentAge(5,"Cindy", 20))
val ds1 = spark.createDataset(lst)
ds1.show()
// 定义第二个数据集 
case class StudentHeight(sname: String, height: Int)
val rdd = sc.makeRDD(List(StudentHeight("Alice", 160),
  StudentHeight("Andy", 159), StudentHeight("Bob", 170),
  StudentHeight("Cindy", 165), StudentHeight("Rose", 160)))
val ds2 = rdd.toDS
// 备注:不能使用双引号，而且这里是 === 关联字段名字相同会报错，可用下面的 ds1("sname")===ds2("sname")
ds1.join(ds2, $"name"===$"sname").show
ds1.join(ds2, 'name==='sname).show

ds1.join(ds2, ds1("name")===ds2("sname")).show
ds1.join(ds2, ds1("sname")===ds2("sname"), "inner").show
// 多种连接方式 
ds1.join(ds2, $"name"===$"sname").show
ds1.join(ds2, $"name"===$"sname", "inner").show
ds1.join(ds2, $"name"===$"sname", "left").show
ds1.join(ds2, $"name"===$"sname", "left_outer").show
ds1.join(ds2, $"name"===$"sname", "right").show
ds1.join(ds2, $"name"===$"sname", "right_outer").show
ds1.join(ds2, $"name"===$"sname", "outer").show
ds1.join(ds2, $"name"===$"sname", "full").show
ds1.join(ds2, $"name"===$"sname", "full_outer").show

// union、unionAll、intersect、except。集合的交、并、差 
val ds3 = ds1.select("name")
val ds4 = ds2.select("sname")
// union 求并集，不去重 
ds3.union(ds4).show
// unionAll、union 等价；unionAll过期方法，不建议使用 
ds3.unionAll(ds4).show
// intersect 求交 
//Returns a new Dataset containing rows only in both this Dataset and another Dataset.
// This is equivalent to INTERSECT in SQL.
ds3.intersect(ds4).show
// except 求差 
ds3.except(ds4).show

// NaN (Not a Number) 
math.sqrt(-1.0)  //NaN
math.sqrt(-1.0).isNaN() //true
df1.show
// 删除所有列的空值和NaN 
df1.na.drop.show
// 删除某列的空值和NaN 
df1.na.drop(Array("mgr")).show
// 对全部列填充；对指定单列填充；对指定多列填充 
df1.na.fill(1000).show
df1.na.fill(1000, Array("comm")).show
df1.na.fill(Map("mgr"->2000, "comm"->1000)).show
// 对指定的值进行替换 
df1.na.replace("comm" :: "deptno" :: Nil, Map(0 -> 100, 10 ->100)).show
// 查询空值列或非空值列。isNull、isNotNull为内置函数 
df1.filter("comm is null").show
df1.filter($"comm".isNull).show
df1.filter(col("comm").isNull).show
df1.filter("comm is not null").show
df1.filter(col("comm").isNotNull).show

////load和save操作

val df = spark.read.load("users.parquet")    //hdfs://nameservicezzj/user/edw/users.parquet
df.select("name", "age").write.save("namesAndAges.parquet")

val df = spark.read.format("json").load("people.json")
df.select("name", "age").write.format("parquet").save("namesAndAges.parquet")

Parquet读取和写入
val df1 = spark.read.format("parquet").load("data/users.parquet")
// Use Parquet; you can omit format("parquet") if you wish as it's the default
val df2 = spark.read.load("data/users.parquet")
spark.sql(
  """
    |CREATE OR REPLACE TEMPORARY VIEW users 
    |USING parquet 
    |OPTIONS (path "data/users.parquet") 
    |""".stripMargin
)
spark.sql("select * from users").show
df.write.format("parquet").mode("overwrite")
                          .option("compression", "snappy")
                          .save("data/parquet")

//自动分区推断 数字类型和字符串类型
spark.read.json("./data/game/raw/user_login/json").write.parquet("./data/eg/parquet/1/dt=20220401")
spark.read.json("./data/game/raw/user_login/json").write.parquet("./data/eg/parquet/1/dt=20220402")
spark.read.parquet("./data/eg/parquet/1/").printSchema()
/*
root
|-- channel_id: string (nullable = true)
|-- client_ip: string (nullable = true)
|-- event_time: long (nullable = true)
|-- online_time: long (nullable = true)
|-- op_type: string (nullable = true)
|-- plat_id: string (nullable = true)
|-- role_id: string (nullable = true)
|-- role_name: string (nullable = true)
|-- server_id: long (nullable = true)
|-- user_id: string (nullable = true)
|-- dt: integer (nullable = true)
*/

//合并元数据
SparkSession.builder().config("spark.sql.parquet.mergeSchema","true")
spark.read.option("mergeSchema","").parquet("true")
spark.sql("set spark.sql.parquet.mergeSchema=true")
示例:
spark.read.json("./data/game/raw/eg/parquet/a.json").write.parquet("./data/eg/parquet/2")
spark.read.json("./data/game/raw/eg/parquet/b.json").write.mode("append").parquet("./data/eg/parquet/2")
spark.read.option("mergeSchema","true").parquet("./data/eg/parquet/2").printSchema()


// orc 写入
spark.read.json("./data/game/raw/user_login/json").write.orc("./data/orc/raw/1/")
 // orc读取
spark.read.orc("./data/orc/raw/1/").show(10,false)

  // orc 是否也可以自动分区推断？
spark.read.orc("./data/orc/raw/1/").limit(10)
    .write.orc("./data/orc/raw/2/dt=20220401")
spark.read.orc("./data/orc/raw/1/").limit(10)
    .write.orc("./data/orc/raw/2/dt=20220402")
spark.read.orc("./data/orc/raw/2/").printSchema()
//  root
//    ...
//  |-- dt: integer (nullable = true)

// csv 写入
spark.read.json("./data/game/raw/user_login/json")
  .write.csv("./data/csv/raw/1/")
// csv 读取
spark.read.csv("./data/csv/raw/1/").show(10,false)
// 指定Schema 读取
val schema =
  """
    | channel_id string,
    | client_ip string,
    | event_time long,
    | online_time long,
    | op_type string,
    | plat_id string,
    | role_id string,
    | role_name string,
    | server_id long,
    | user_id string
    |""".stripMargin
spark.read.schema(schema).csv("./data/csv/raw/1/").show(10,false)

// json 读取
val  df = spark.read.json("./data/game/raw/user_login/json")
 df.show(10,false)
 // json 写入
 df.write.json("./data/json/raw/1/")
 // 指定schema读取
 val schema =
   """
     | channel_id string,
     | client_ip string,
     | event_time long,
     | online_time long,
     | op_type string,
     | plat_id string,
     | role_id string,
     | role_name string,
     | server_id long,
     | user_id string
     |""".stripMargin
 spark.read.schema(schema).json("./data/json/raw/1/").show(10,false)

5.1.2 Hive数据源读取和写入
// hive数据源读取
spark.read.table("ds_spark.people").show()
spark.sql("select * from ds_spark.people").show()
spark.table("ds_spark.people").show()
//hive 数据源写出
spark.read.json("user_log.json").write.saveAsTable("ds_spark.ods_user_login")
spark.read.table("ds_spark.ods_user_login").count()
spark.read.json("user_log.json").write.insertInto("ds_spark.ods_user_login")
spark.read.table("ds_spark.ods_user_login").count()

val df= spark.read.json("user_log.json")
// 会发现整个表的分区都被覆盖
df.write.partitionBy("dt_pt").saveAsTable("ds_spark.ods_user_login_di")
插入动态分区:insertInto 
spark.sql("set spark.sql.sources.partitionOverwriteMode = 'DYNAMIC' ")
df.write.mode("overwrite").insertInto("ds_spark.ods_user_login_di")
5.1.4 saveAstable 和 insertInto 企业中怎么用？
def saveAsHiveTable(
                     spark: SparkSession,
                     data: DataFrame,
                     dbName: String,
                     tableName: String,
                     tableSavePath: String,
                     ptCols: Array[String]
                   ): Unit = {
  val table = s"$dbName.$tableName"
  val tableIdentifier = TableIdentifier(tableName, Some(dbName))
  val catalog = spark.sessionState.catalog

  if (catalog.tableExists(tableIdentifier)) {
    // 如果表存在，则使用 insertInto 插入
    spark.conf.set("spark.sql.sources.partitionOverwriteMode", "DYNAMIC")

    val sortCols = Array.concat(
      data.columns.filterNot(ptCols.contains(_)),
      ptCols
    )
    val resultDF = data.select(sortCols.head, sortCols.tail: _*)
    resultDF.write.mode(SaveMode.Overwrite).insertInto(table)

  } else {
    // 如果表不存在，则使用
    val path = s"$tableSavePath/$tableName"

    data.write
      .mode(SaveMode.Overwrite)
      .options(Map("path" -> path))
      .partitionBy(ptCols:_*)
      .saveAsTable(table)
  }
}
5.1.5 Spark write parquet 和 save hive table 类型不一致
spark.sql("set spark.sql.parquet.writeLegacyFormat =true")

JDBC 的读取与写入
val mysqlUrl ="jdbc:mysql://host:port/db?useSSL=false"
val mysqlDriver = "com.mysql.jdbc.Driver"
val mysqlTableName = "ds_test"
val mysqlUser = "root"
val mysqlPassword = "root"

val jdbcDF = spark.read.format("jdbc")
  .option("url", mysqlUrl) //&useUnicode=true
  .option("driver", mysqlDriver)
  .option("dbtable", mysqlTableName)
  .option("user", mysqlUser)
  .option("password", mysqlPassword)
  .load()
jdbcDF.show()
jdbcDF.write
  .format("jdbc")
  .option("url", mysqlUrl)
  .option("user", mysqlUser)
  .option("password", mysqlPassword)
  .option("driver", mysqlDriver)
  .option("dbtable",mysqlTableName)
  .mode("append").save

5.2.3 Spark JDBC 其他案例
// 执行 SQL语句
val query = "select * from ds_test"
val jdbcDF2 = spark.read.format("jdbc")
  .option("query",query)
  .load()
// 写入mysql时不删除已有表结构（可能有index，主键）
jdbcDF.write
  .format("jdbc")
  .option("truncate","true")
  .mode("overwrite").save
// 读取时自定义Schema
val custerSchema= "id DECIMAL(38, 0), name STRING"
spark.read.format("jdbc")
  .option("customSchema",custerSchema)
  .load()
//覆盖写入时指定schema
val createTableSchema= "name CHAR(64), comments VARCHAR(1024))"
jdbcDF.write
  .format("jdbc")
  .option("createTableColumnTypes",createTableSchema)
  .mode("overwrite").save


内置函数案例实战☆☆☆
官网:https://spark.apache.org/docs/latest/api/sql/index.html

// 日期格式化
spark.sql("select  ('20220401','yyyyMMdd')").show()
//date_format(timestamp, fmt) - Converts timestamp to a value of string in the format specified by the date format fmt.
spark.sql("SELECT date_format('2016-04-08', 'y')")
// date_add(start_date, num_days) - Returns the date that is num_days after start_date.
spark.sql("SELECT date_add('2016-07-30', 1)")
// date_sub date_sub(start_date, num_days) - Returns the date that is num_days before start_date.
spark.sql("SELECT date_sub('2016-07-30', 1)")
// datediff(endDate, startDate) - Returns the number of days from startDate to endDate.
spark.sql("SELECT datediff('2009-07-31', '2009-07-30')")
// from_unixtime(unix_time, format) - Returns unix_time in the specified format.
spark.sql("SELECT from_unixtime(0, 'yyyy-MM-dd HH:mm:ss')")
// from_utc_timestamp(timestamp, timezone) - Given a timestamp like '2017-07-14 02:40:00.0', interprets it as a time in UTC, and renders that time as a timestamp in the given time zone. For example, 'GMT+1' would yield '2017-07-14 03:40:00.0'.
spark.sql("  SELECT from_utc_timestamp('2016-08-31', 'Asia/Seoul');")
// last_day(date) - Returns the last day of the month which the date belongs to.
spark.sql("SELECT last_day('2009-01-12')")

// explode(expr) - Separates the elements of array expr into multiple rows, 
//or the elements of map expr into multiple rows and columns.
spark.sql("SELECT explode(array(10, 20))")
// from_json(jsonStr, schema[, options]) - Returns a struct value with the given jsonStr and schema.
spark.sql("SELECT from_json('{\"a\":1, \"b\":0.8}', 'a INT, b DOUBLE')")
spark.sql("SELECT from_json('{\"time\":\"26/08/2015\"}', 'time Timestamp', map('timestampFormat', 'dd/MM/yyyy'))")
//get_json_object(json_txt, path) - Extracts a json object from path.
spark.sql("SELECT get_json_object('{\"a\":\"b\"}', '$.a')")
//json_tuple
spark.sql("SELECT json_tuple('{\"a\":1, \"b\":2}', 'a', 'b');")
//to_json(expr[, options]) - Returns a json string with a given struct value
spark.sql("select to_json(named_struct('a', 1, 'b', 2))")
spark.sql("select to_json(named_struct('time', to_timestamp('2015-08-26', 'yyyy-MM-dd')), map('timestampFormat', 'dd/MM/yyyy'))")
spark.sql("select to_json(array(named_struct('a', 1, 'b', 2))")
spark.sql("select to_json(map('a', named_struct('b', 1)))")
spark.sql("SELECT to_json(map(named_struct('a', 1),named_struct('b', 2)))")
spark.sql("SELECT to_json(map('a', 1))")
spark.sql("SELECT to_json(array((map('a', 1))))")

// 数组 array(expr, ...) - 返回给定值组成的数组。
spark.sql("select array(1,3,4)")
// array_contains(array, value) - 如果数组包含了 value，则返回 true。
spark.sql("select array_contains(array(1,3,4),1)")
// size size(expr) - Returns the size of an array or a map. Returns -1 if null.
spark.sql("SELECT size(array('b', 'd', 'c', 'a'))")
// sort_array(array[, ascendingOrder]) - Sorts the input array in ascending or descending order according to the natural ordering of the array elements.
spark.sql("SELECT sort_array(array('b', 'd', 'c', 'a'), true)")
// collect_list(expr) - 收集并返回非唯一元素列表。
spark.sql("select a,collect_list(b) from table group a ")
// collect_set(expr) - 收集并返回唯一元素列表。
spark.sql("select a,collect_set(b) from table group a ")
// map
//map(key0, value0, key1, value1, ...) - Creates a map with the given key/value pairs.
spark.sql("SELECT map(1.0, '2', 3.0, '4')")
// map_keys(map) - Returns an unordered array containing the keys of the map.
spark.sql("SELECT map_keys(map(1, 'a', 2, 'b'))")
// map_values(map) - Returns an unordered array containing the values of the map.
spark.sql("SELECT map_values(map(1, 'a', 2, 'b'))")
// struct
// struct(col1, col2, col3, ...) - Creates a struct with the given field values.
spark.sql("select struct(1 as a, 2 as b) s")
// coalesce(expr1, expr2, ...) - 返回第一个非空参数（如果存在）。 否则，返回 null。
spark.sql("SELECT coalesce(NULL, 1, NULL)")


开窗函数使用讲解
import org.apache.spark.sql.expressions.Window
val w1 = Window.partitionBy("cookieid").orderBy("createtime")
val w2 = Window.partitionBy("cookieid").orderBy("pv")
val w3 = w1.rowsBetween(Window.unboundedPreceding, Window.currentRow)
val w4 = w1.rowsBetween(-1, 1)

// 聚组函数【用分析函数的数据集】 
df.select($"cookieid", $"pv", sum("pv").over(w1).alias("pv1")).show
df.select($"cookieid", $"pv", sum("pv").over(w3).alias("pv1")).show
df.select($"cookieid", $"pv", sum("pv").over(w4).as("pv1")).show
df.select($"cookieid", $"pv", rank().over(w2).alias("rank")).show
df.select($"cookieid", $"pv", dense_rank().over(w2).alias("denserank")).show
df.select($"cookieid", $"pv", row_number().over(w2).alias("rownumber")).show

// lag、lead 
df.select($"cookieid", $"pv", lag("pv", 2).over(w2).alias("rownumber")).show
df.select($"cookieid", $"pv", lag("pv", -2).over(w2).alias("rownumber")).show

import spark.implicits._
spark.sparkContext.makeRDD(List(
  (1, "zs", 18, 1500, "A", true),
  (2, "ls", 20, 3000, "B", false),
  (3, "ww", 20, 2000, "B", false),
  (4, "zl", 30, 3500, "A", true),
  (3, "tq", 40, 2500, "A", false)
)).toDF("id", "name", "age", "salary", "dept_id", "sex")
  .createOrReplaceTempView("t_user")
//窗口函数语法
spark.sql(
  """
    |select id,name,age,salary,dept_id,sex, max(salary) over(partition by dept_id) as dept_max_salary 
    from t_user
    |""".stripMargin)

import spark.implicits._
spark.sparkContext
  .makeRDD(
    List(
      ("2018-01-01", 1, "www.baidu.com", "10:01"),
      ("2018-01-01", 2, "www.baidu.com", "10:01"),
      ("2018-01-01", 1, "www.sina.com", "10:01"),
      ("2018-01-01", 3, "www.baidu.com", "10:01"),
      ("2018-01-01", 3, "www.baidu.com", "10:01"),
      ("2018-01-01", 1, "www.sina.com", "10:01")
    )
  )
  .toDF("day", "user_id", "page_id", "time")
  .createOrReplaceTempView("t_page")

spark.sql("select * from (select user_id,page_id,num,
rank() over(partition by user_id order by num desc) as rank 
from (select user_id,page_id,count(page_id) as num 
from t_page group by user_id,page_id)) where rank < 10")


////udf
class UDF {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName(this.getClass.getCanonicalName).master("local[*]").getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    val data = List(("scala", "author1"), ("spark", "author2"),
      ("hadoop", "author3"), ("hive", "author4"), ("strom", "author5"), ("kafka", "author6"))
    val df = spark.createDataFrame(data).toDF("title", "author")
    df.createTempView("books")

    // 定义函数并注册 
    def len1(bookTitle: String): Int = bookTitle.length

    spark.udf.register("len1", len1 _)
    // UDF可以在select语句、where语句等多处使用 
    spark.sql("select title, author, len1(title) from books").show
    spark.sql("select title, author from books where len1(title)>5").show // UDF可以在DataFrame、Dataset的API中使用 
    import spark.implicits._
    df.filter("len1(title)>5").show

    // 不能通过编译 
    //      df.filter(len1($"title")>5).show // 
    //能通过编译，但不能执行 
    df.select("len1(title)").show
    // 不能通过编译 
    //      df.select(len1($"title")).show

    // 如果要在DSL语法中使用$符号包裹字符串表示一个Column，需要用udf方 法来接收函数。这种函数无需注册 
    import org.apache.spark.sql.functions._
    val len2 = udf((bookTitle: String) => bookTitle.length)
    val a: (String) => Int = (bookTitle: String) => bookTitle.length
    val len2 = udf(a)
    val len2 = udf(len1 _)
    //      df.filter(len2($"title") > 5).show 
    //      df.select($"title", $"author", len2($"title")).show 
    // 不使用UDF 
    df.map { case Row(title: String, author: String) => (title, author, title.length) }.show
    spark.stop()
  }



////udaf
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{Encoder, Encoders, SparkSession, TypedColumn}
import org.apache.spark.sql.expressions.Aggregator

case class Sales(id: Int, name1: String, sales: Double, discount: Double, name2: String, stime: String)

case class SalesBuffer(var sales2019: Double, var sales2020: Double)

class TypeSafeUDAF extends Aggregator[Sales, SalesBuffer, Double] {
  // 定义初值 
  override def zero: SalesBuffer = SalesBuffer(0, 0)

  // 分区内数据合并 
  override def reduce(buf: SalesBuffer, inputRow: Sales): SalesBuffer = {
    val sales: Double = inputRow.sales
    val year: String = inputRow.stime.take(4)
    year match {
      case "2019" => buf.sales2019 += sales
      case "2020" => buf.sales2020 += sales
      case _ => println("ERROR!")
    }
    buf
  }

  // 分区间数据合并 
  override def merge(part1: SalesBuffer, part2: SalesBuffer): SalesBuffer = {
    val sales1 = part1.sales2019 + part2.sales2019
    val sales2 = part1.sales2020 + part2.sales2020
    SalesBuffer(sales1, sales2)
  }

  // 最终结果的计算 
  override def finish(reduction: SalesBuffer): Double = {
    if (math.abs(reduction.sales2019) < 0.0000000001) 0 else (reduction.sales2020 - reduction.sales2019) / reduction.sales2019
  }

  // 定义buffer 和 输出结果的编码器 
  override def bufferEncoder: Encoder[SalesBuffer] = Encoders.product

  override def outputEncoder: Encoder[Double] = Encoders.scalaDouble
}

object TypeSafeUDAFTest {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.WARN)
    val spark = SparkSession.builder().appName(s"${this.getClass.getCanonicalName}").master("local[*]").getOrCreate()
    val sales = Seq(Sales(1, "Widget Co", 1000.00, 0.00, "AZ", "2019-01-02"), 
                    Sales(2, "Acme Widgets", 2000.00, 500.00, "CA", "2019-02-01"), 
                    Sales(3, "Widgetry", 1000.00, 200.00, "CA", "2020-01-11"), 
                    Sales(4, "Widgets R Us", 2000.00, 0.0, "CA", "2020-02-19"), 
                    Sales(5, "Ye Olde Widgete", 3000.00, 0.0, "MA", "2020-02-28"))
    import spark.implicits._
    val ds = spark.createDataset(sales)
    ds.show
    // name 会作为列名 
    val rate: TypedColumn[Sales, Double] = new TypeSafeUDAF().toColumn.name("rate")
    ds.select(rate).show
    spark.stop()
  }
}


Spark SQL 的创建

df.createTempView
df.createOrReplaceTempView
spark.sql("SQL") 



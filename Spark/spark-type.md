










12.在Spark2.0 版本之前,Spark SQL中创建DataFrame和执行SQL的入口是()

C、SQLContext

13.在DataFrame的操作中,用于实现对列名进行重命名的操作是()
A、select()






//










17. SparkContext可以从哪些位置读取数据
A.本地磁盘
C.hdfs
D.内存

19. 要读取people.json文件生成DataFrame,可以使用下列那些命令
A. spark.read.json("people.json")
C. spark.read.format("json").load("people.json")

20. 从RDD转换得到DataFrame包含两种典型的方法,分别是
A.利用反射机制推断RDD模式
B.使用编程方式定义RDD模式







9.简述Spark 执行一条SQL,主要经过了那些阶段(结合Catalyst描述)
7.Spark SQL 原理[4.Spark Catalyst优化器详解]

10.简述Spark AQE 和 Spark DPP 原理和使用场景
AQE:Spark的动态优化机制
3.0版本主要有以下几个内容:自动分区合并、自动join倾斜处理、动态调整join策略
自动分区合并:
其原理是针对小分区进行合并,从而减少task数量,进而导致输出文件数量减少。
用户需要设置合并后分区尺寸推荐值以及合并后分区数量最小值。
常用于输出小文件过多、task数量过多而数据量较少的场景。

自动join倾斜处理:
其原理是针对分区尺寸进行判断,当判断为倾斜分区时,就将分区拆分多个小分区,从而避免task粒度的数据倾斜
判断依据是两个值,分区尺寸大于这两个值,那么就为倾斜分区
①用户设定的最低阈值
②分区尺寸正序排序后的中位数分区尺寸*用户设定的比例系数
此功能常用于出现task粒度的数据倾斜时,开启以解决。

动态调整join策略:
其原理是,一开始join策略并非BHJ,随着查询的进行,在join之前,当表容量小于broadcast join 设定的阈值时,将join策略自动降级为broadcast join 
此功能常用于当大小表join且小表尺寸大于BHJ阈值的场景,开启此功能后,后续一旦小表数据经过过滤后尺寸小于阈值,即可自动修改BHJ,提高join性能。

DPP:动态分区裁剪
其原理是两表join时,将一个表的过滤条件套用在另一张表上,使得join的数据量减少,且不影响结果准确性。
其适用于两个分区表join且分区字段在关联条件中且其中一张表存在过滤条件的场景。

1.对于Spark 参数的优化,主要有哪些参数,它们对应调优的场景是什么？(5个以上)
①调整reduce端并行度
CPU利用率低说明其处理的每个task数据量过大,例如最大能处理4个task,但是两个task就把Executor内存占满,从而导致剩下两个CPU空转。
此时增大并行度,使得task数据量变小
针对CPU利用率较低的场景。

②开启堆外内存、设置cache缓存优先使用堆外内存、设置缓存压缩
可减少Executor端堆内内存溢出情况

③增加shuffle write溢写时缓冲大小、shuffle read缓冲容量大小
提高溢写效率、增加read阶段数据吞吐量,减少拉取数据次数,各场景均适用。

④增加任务失败后重试次数、重试时间间隔
增加重试次数与重试间隔后,能够使得任务稳定性增加,适用于网络经常波动导致任务失败次数过多时的调优

2.对于Spark SQL来说,大表关联小表的优化方式有哪些？(分多场景作答,小表可广播、小表太大不能广播)
当小表读到内存后,表容量小于广播变量阈值时,那么程序在join时,会自动选择broadcast hash join策略。
当大于阈值不多的时候,我们可以人为的评估一下,采用广播变量是否可行,表容量是否超过executor的储存内存,若没超过则可以选择强制广播
当大于阈值过多不能广播的时候,那么可以指定join策略为shuffle hash join
某些特定的需求,当小表超过阈值过多时,也可以人为的减少数据量来使得其满足broadcast的需求: 例如对大表进行维度补充。
维度表事实表重复字段很多时,可以选择将关联字段全部用concat连接起来,再用MD5进行加密,使得字段值从一个较长的字符串变成一个较短的字符串,此时对小表建立一个子查询,里面仅有关联字段和待补充进维度表的字段,使得数据量减少,从而触发broadcast join。
针对大小表优化,3.0版本可以开启AQE功能中的join策略自动调整,使得符合要求的自动被优化。

3.对于Spark SQL来说,大表关联大表的优化方式有哪些？
大表 join 大表
先看是不是有数据倾斜情况,针对两个表的关联字段进行抽样。
若不存在倾斜情况:
①可以分而治之
大表一般是有分区字段的,可以根据分区字段对内表进行拆分,然后将每一部分跟大表join,使得join策略为broadcast join 
最后再将每部分跟外表join的结果联合起来。形成最终结果。

若内/外表存在数据倾斜情况:
①分而治之+两阶段shuffle
抽样将大key定位出来
然后对原表筛选,变成俩部分:1A是仅有大key的表,2A是不存在大key的表
然后将2A跟另一张表B进行join 
再将1A跟表B进行join,join时需要加盐和减盐操作
最后再将结果union联合起来。

4.举例说明Spark 数据倾斜有哪些场景,对应的解决方案是什么？
即某个或多个task的数据量过大,导致执行速度过慢的情况。
单表聚合操作出现倾斜:
可开启AQE的join自动倾斜处理
或者提高在reduce端并行度
或者两阶段聚合:对大key加随机数前缀、再去除随机数前缀后最终聚合聚合

大小表join数据倾斜:
可以避免shuffle:采用BHJ策略
可以采用分而治之:将大key加随机数与另一张表关联,再将小key与另一张表关联,最后结果union

大表与大表join产生的数据倾斜:
分而治之,两阶段shuffle

5.Spark 的OOM会发生在Spark 那个组件上?对应的解决方案是？
一般发生在Driver端
当拉取数据到river端时,可能导致数据量太大从而使得OOM,例如RDD的collect算子
解决方案:
①增加Driver端内存
②开启堆外内存
③调整river端执行内存和储存内存的比例




val ipSchema = "ip_start string,ip_end string,long_ip_start long,long_ip_end long,country string,province string"
val ipDF = spark.read.schema(ipSchema).option("header", true).csv("/user/ds_teacher/raw/spark_test/ip_china.csv")

ipDF.createOrReplaceTempView("view_ip_china")

spark.sql(
  """
    |create table ds_stu5.ods_ip_china_xb
    |(
    |ip_start string,
    |ip_end string,
    |long_ip_start long,
    |long_ip_end long,
    |country string,
    |province string
    |)
    |stored as parquetfile
    |
    |""".stripMargin)

spark.sql(
  """
    |insert overwrite table ds_stu5.ods_ip_china_xb
    |select * from view_ip_china
    |
    |""".stripMargin)

///
val loginSchema = "logtime string,account_id long,ip string"
val loginDF = spark.read.schema(loginSchema).option("header", true).csv("/user/ds_teacher/raw/spark_test/login_data.csv")

loginDF.createOrReplaceTempView("view_login_data")

spark.sql(
  """
    |create external table ds_stu5.ods_login_data_xb
    |(
    |logtime string,
    |account_id long,
    |ip string
    |)
    |partitioned by (dt string)
    |stored as parquetfile
    |location '/user/hive/warehouse/ds_stu5.db/ods_login_data_xb'
    |""".stripMargin)

spark.conf.set("hive.exec.dynamic.partition.mode", "nonstrict")

spark.sql(
  """
    |insert overwrite table ds_stu5.ods_login_data_xb partition(dt)
    |select logtime,account_id, ip, date_format(logtime,'yyyy-MM-dd') dt from view_login_data
    |
    |""".stripMargin)

///
val ipRangeAndLocation = spark.sql(
  """
    |
    |select
    |long_ip_start,
    |long_ip_end,
    |country,
    |province
    |from
    |ds_stu5.ods_ip_china_xb
    |
    |""".stripMargin).collect()


def ipToLong(ip: String): Long = {
  // aaa.bbb.ccc.ddd
  val fourParts: Array[String] = ip.split("[.]")

  val aaa: Long = fourParts(0).toLong * 256 * 256 * 256
  val bbb: Long = fourParts(1).toLong * 256 * 256
  val ccc: Long = fourParts(2).toLong * 256
  val ddd: Long = fourParts(3).toLong
  val long_ip: Long = aaa + bbb + ccc + ddd
  long_ip
}

import scala.collection.mutable
val startEndCountryProvince = mutable.Buffer[(Long, Long, String, String)]()

for (row <- ipRangeAndLocation) {
  val str: String = row.toString()
  val subStr: String = str.substring(1, str.length - 1)
  val llss: Array[String] = subStr.split(",")
  startEndCountryProvince.append((llss(0).toLong, llss(1).toLong, llss(2), llss(3)))
}
val ipRangeArr: Array[(Long, Long, String, String)] = startEndCountryProvince.toArray

import org.apache.spark.broadcast.Broadcast
val bc: Broadcast[Array[(Long, Long, String, String)]] = spark.sparkContext.broadcast(ipRangeArr)


def longIpMatchCountry(long_ip: Long): String = {

  val arr: Array[(Long, Long, String, String)] = bc.value
  var low: Int = 0
  var high: Int = arr.length - 1
  while (low <= high) {
    val mid: Int = (low + high) / 2

    if ((long_ip >= arr(mid)._1) && (long_ip <= arr(mid)._2)) {

      return arr(mid)._3
    }

    if (long_ip < arr(mid)._1) {
      high = mid - 1
    } else {
      low = mid + 1
    }
  }
  "CannotMatchCountry"
}

def longIpMatchProvince(long_ip: Long): String = {

  val arr: Array[(Long, Long, String, String)] = bc.value
  var low: Int = 0
  var high: Int = arr.length - 1
  while (low <= high) {
    val mid: Int = (low + high) / 2

    if ((long_ip >= arr(mid)._1) && (long_ip <= arr(mid)._2)) {

      return arr(mid)._4
    }

    if (long_ip < arr(mid)._1) {
      high = mid - 1
    } else {
      low = mid + 1
    }
  }
  "CannotMatchProvince"
}

spark.udf.register("ipToLong", ipToLong _)
spark.udf.register("longIpMatchCountry", longIpMatchCountry _)
spark.udf.register("longIpMatchProvince", longIpMatchProvince _)

spark.sql(
  """
    |insert overwrite table ds_stu5.ods_login_data_xb partition(dt)
    | select
    |   logtime,
    |   account_id,
    |   ip,
    |   longIpMatchCountry(ipToLong(ip)) country,
    |   longIpMatchProvince(ipToLong(ip)) province,
    |   dt
    | from
    |  ds_stu5.ods_login_data_xb
    |""".stripMargin)

///
spark.sql(
  """
    |select
    |   dt,
    |   province,
    |   count(distinct account_id) cnt_login
    |from
    |   ds_stu5.ods_login_data_xb
    |group by dt,province
    |order by dt
    |
    |""".stripMargin).show(100,false)

///
spark.sql(
  """
    |with login_info as
    |(select
    |    province,
    |    case when rn == 1 then account_id else "" end as account_id_1,
    |    case when rn == 1 then login_time else "" end as login_time_1,
    |    case when rn == 2 then account_id else "" end as account_id_2,
    |    case when rn == 2 then login_time else "" end as login_time_2,
    |    case when rn == 3 then account_id else "" end as account_id_3,
    |    case when rn == 3 then login_time else "" end as login_time_3
    |from
    |
    |(
    |    select
    |       province,
    |       account_id,
    |       logtime as login_time,
    |       rn
    |       from
    |    (
    |       select
    |          province,
    |          account_id,
    |          logtime,
    |          row_number()over(partition by province order by logtime) rn
    |       from
    |          ds_stu5.ods_login_data_xb
    |    ) rank_table
    |    where rn <=3
    |) earliestTop3
    |)
    |select
    |   province,
    |   max(account_id_1) account_id_1,
    |   max(login_time_1) login_time_1,
    |   max(account_id_2) account_id_2,
    |   max(login_time_2) login_time_2,
    |   max(account_id_3) account_id_3,
    |   max(login_time_3) login_time_3
    |from
    |   login_info
    |   where province <> "CannotMatchProvince"
    |   group by province
    |
    |
    |""".stripMargin).show (100,false)







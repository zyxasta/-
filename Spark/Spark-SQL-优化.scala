
//1:编码的优化☆☆☆

1)、检索数据:你还在select * 嘛？
如果我们不需要把所有列都检索出来,还是先指定出所需的列名,因为写清列名可以减少数据表查询的网络传输量

2)、巧用分区过滤
   比如我们的表分区是 dt_pt,那么我们在跑数据的时候一定要记得选择对应的分区数据,避免全表扫描
select   *  from table where dt_pt = '20220401'

3)、分而治之
有时,我们做需求的时候,会出现拉取历史1年的数据,这个时候我们跑一年的数据肯定跑不动,那我们其实可以分而治之,先把每个月的数据跑出来,然后在聚合一年的数据.

4)、使用广播,让Spark SQL选择Broadcast Joins
    利用配置项强制广播
    spark.sql.autoBroadcastJoinThreshold 默认10mb

    利用 API 强制广播
    用 Join Hints 强制广播Join Hints 中的 Hints 表示“提示”,它指的是在开发过程中使用特殊的语法,
    明确告知 Spark SQL 在运行时采用哪种 Join 策略。一旦你启用了 Join Hints,
    不管你的数据表是不是满足广播阈值,Spark SQL 都会尽可能地尊重你的意愿和选择,使用 Broadcast Joins 去完成数据关联。
    val query: String = “select /*+ broadcast(t2) */ * from t1 inner join t2 on t1.key = t2.key”
    val queryResutls: DataFrame = spark.sql(query)
    
    table1.join(table2.hint(“broadcast”), Seq(“key”), “inner”)
    
    用 broadcast 函数强制广播
    import org.apache.spark.sql.functions.broadcast
    table1.join(broadcast(table2), Seq(“key”), “inner”)
    
5)、使用缓存 
with a as ( select id from t1 left join t3 using(id)),
b as (select * from t2  left join t1 using(id))
对于表多次被重复使用的情况,可以考虑cache table ,进行缓存复用


//2:参数的优化
1 基础参数调优
为任务分配更多的资源
bin/spark-submit \
--class com.xx.xx.xx\
--master yarn
--deploy-mode cluster
--num-executors 100\
--driver-memory 5g \
--executor-memory 5g \
--executor-cores 4 \
xxxxx.jar \
调节原则:尽量将任务分配的资源调节到可以使用的资源的最大限度

2 动态资源分配(DRA,dynamic resource allocation) 
在Spark On Yarn模式下使用: 
	num-executors指定app使用executors数量 
	executor-memory、executor-cores指定每个executor所使用的内存、cores 
动态申请executor:如果有新任务处于等待状态,并且等待时间超过Spark.dynamicAllocation.schedulerBacklogTimeout(默认 1s),
                 则会依次启动executor,每次启动1、2、4、8…个executor(如果有的话)。
启动的间隔由 
spark.dynamicAllocation.sustainedSchedulerBacklogTimeout 控制 (默认与schedulerBacklogTimeout相同) 
动态移除executor: 
executor空闲时间超过 spark.dynamicAllocation.executorIdleTimeout 设置的值(默认60s),该executor会被移除,除非有缓存数据

3 设置合理的shuffle并行度
spark.sql.shuffle.partitions 默认200
Spark中并不会根据数据量进行动态的设置
建议:建议设置初始分区的1.5-2倍之间
更改方式spark.sql(“set spark.sql.shuffle.partitions=100”)


//Spark SQL 原理剖析☆☆☆
见同目录下文档


1.开发调优，包括开发原则、开发经验、Spark 配置应用
2.通用的性能调优，主要是资源和Shuffle相关的优化，
  包括一些比较典型的场景（Shuffle调优、Broadcast优化），硬件资源的优化（CPU、磁盘、内存、网络）
3.Spark SQL的定向优化，包括我们常见的数据倾斜、数据清洗、数据关联的优化，
  以及Spark SQL本身有哪些优化可以让我们坐享其成（Catalyst、Tungsten、
         Adaptive Query Execution（简称AQE）、Dynamic Partition Pruning（DPP）。




尽量不要自定义 UDF 来实现业务逻辑，要尽可能地使用 Spark 内置的 SQL functions。


木桶理论


//1.通用性能调优☆☆☆
通用开发原则☆☆
	坐享其成：通过设置相关的配置项，或是调用相应的 API 去充分享用 Spark 自身带来的性能优势。
1.钨丝计划
2.AQE

	能省则省、能拖则拖
    	尽量把能节省数据扫描量和数据处理量的操作往前推；
    	尽力消灭掉 Shuffle，省去数据落盘与分发的开销；
    	如果不能干掉 Shuffle，尽可能地把涉及 Shuffle 的操作拖到最后去执行。

	摆脱单机思维



//2.Spark Conf☆☆☆ 

1:硬件资源类
2:Shuffle 类
3:Spark SQL 大类


////硬件资源类
    CPU Conf
    内存 Conf
    磁盘Conf
        spark.local.dir
////shuffle conf
        spark.shuffle.file.buffer
        spark.reducer.maxSizeInFlight
        spark.shuffle.sort.bypassMergeThreshold

////Spark SQL 配置项
AQE:spark.sql.adaptive.enabled 
  自动分区合并
  自动数据倾斜处理
  Join 策略调整


//3.Shuffle 调优☆☆☆
在数据关联场景中，广播变量就可以轻而易举地省去 Shuffle。


CPU视角调优☆☆
并行度
  spark.default.parallelism    RDD 的默认并行度
  spark.sql.shuffle.partitions Spark SQL 开发框架下，指定了 Shuffle Reduce 阶段默认的并行度。
执行内存大小
集群的并行计算能力

内存视角调优☆☆☆
	预估内存占用
	调整内存配置项


堆内内存划分为 Reserved Memory   300MB
             User Memory
                      我们先汇总应用中包含的自定义数据结构，并估算这些对象的总大小 #size，
                      然后用 #size 乘以 Executor 的线程池大小
             Storage Memory
                      我们先汇总应用中涉及的广播变量和分布式数据集缓存，分别估算这两类对象的总大小，
                      分别记为 #bc、#cache。另外，我们把集群中的 Executors 总数记作 #E。
                      这样，每个 Executor 中 Storage Memory 区域的内存消耗的公式就是：
                      #Storage = #bc + #cache / #E。
             Execution Memory 
                      执行内存的消耗与多个因素有关。
                      第一个因素是 Executor 线程池大小 #threads，
                      第二个因素是数据分片大小，而数据分片大小取决于数据集尺寸 #dataset 和并行度 #N。
                      因此，每个 Executor 中执行内存的消耗的计算公式为：
                      #Execution = #threads * #dataset / #N。
cache 
Storage Memory 内存空间受限，
因此 Cache 应该遵循最小公共子集原则，也就是说，开发者应该仅仅缓存后续操作必需的那些数据列。
Cache Manager 要求两个查询的 Analyzed Logical Plan 必须完全一致，
才能对 DataFrame 的缓存进行复用。

当及时清理用过的 Cache


磁盘视角调优☆☆
分别是溢出临时文件、
缓存分布式数据集
存储 Shuffle 中间文件

是失败重试
另一个是 ReuseExchange 机制

网络视角调优☆☆
数据读取阶段 RACK_LOCAL
           ANY
           NODE_LOCAL



Spark SQL性能优化☆☆☆
--统计信息
spark.sql("analyze table ds_spark.ods_user_login_di compute statistics").show(1000,false)
spark.sql("describe extended ds_spark.ods_user_login_di").show(1000,false)
--列级别统计信息

analyze table ds_spark.ods_user_login_di compute statistics for columns user_id
spark.sql("DESCRIBE EXTENDED ds_spark.ods_user_login_di user_id").show(10,false)

--spark3
val item_df = spark.read.parquet("./data/game/parquet/item_purchase")
item_df.explain("cost")    //explain(mode="cost") 


Spark 3.0 AQE☆☆☆
CBO:
  窄：仅支持注册到 Hive Metastore 的数据表
  慢:计信息的搜集效率比较低 需要调用 ANALYZE TABLE COMPUTE STATISTICS 
  静:静态优化 在运行时数据分布发生动态变化，CBO 先前制定的执行计划并不会跟着调整、适配。


aqe 动态优化机制
运行时，每当 Shuffle Map 阶段执行完毕，AQE 都会结合这个阶段的统计信息，
基于既定的规则动态地调整、修正尚未执行的逻辑计划和物理计划，来完成对原始查询语句的运行时优化。

AQE 赖以优化的统计信息 是 Shuffle Map 阶段输出的中间文件


AQE 既定的规则和策略主要有 4 个，分为 1 个逻辑优化规则和 3 个物理优化策略。


	Join 策略调整：如果某张表在过滤之后，尺寸小于广播变量阈值，
   这张表参与的数据关联就会从 Shuffle Sort Merge Join 降级（Demote）为执行效率更高的
    Broadcast Hash Join。
	自动分区合并：在 Shuffle 过后，Reduce Task 数据分布参差不齐，AQE 将自动合并过小的数据分区。
	自动倾斜处理：结合配置项，AQE 自动拆分 Reduce 阶段过大的数据分区，
             降低单个 Reduce Task 的工作负载



Spark 3.0 DPP	☆☆☆
星型数仓的数据关联场景中，可以充分利用过滤之后的维度表，大幅削减事实表的数据扫描量，
从整体上提升关联计算的执行性能。




静态分区裁剪
裁剪意味着优化器将避免读取不包含我们正在查找的数据的文件
动态分区裁剪
其实动态分区裁剪优化就是在 broadcast hash join 中大表进行 build relation 
的时候拿到维度表的广播结果（broadcast results），
然后在 build relation 的时候（Scan 前）进行动态过滤，从而达到避免扫描无用的数据效果。

动态分区裁剪的适用条件
	spark.sql.optimizer.dynamicPartitionPruning.enabled 参数必须设置为 true，
  不过这个值默认就是启用的；
	需要裁减的表必须是分区表，而且分区字段必须在 join 的 on 条件里面；
	Join 类型必须是 INNER, LEFT SEMI （左表是分区表）, LEFT OUTER （右表是分区表）, 
    or RIGHT OUTER （左表是分区表）。
	满足上面的条件也不一定会触发动态分区裁减，还必须满足 
     spark.sql.optimizer.dynamicPartitionPruning.useStats 
     和 spark.sql.optimizer.dynamicPartitionPruning.fallbackFilterRatio 
     两个参数综合评估出一个进行动态分区裁减是否有益的值，满足了才会进行动态分区裁减。
     评估函数实现请参见 
     org.apache.spark.sql.dynamicpruning.PartitionPruning#pruningHasBenefit



大表Join小表☆☆☆

大表与小表尺寸相差 3 倍以上
我们应该优先考虑 BHJ
如果小表的数据量超过广播阈值

Spark 只好退而求其次，选择 SMJ
把 SMJ 转化成 BHJ。


大表Join大表☆☆☆
内表是尺寸较小的那一方，外表是尺寸较大的一方
分而治之
  通过均匀拆分内表的方式 ，把一个复杂而又庞大的 Shuffle Join 
  转化为多个 Broadcast Joins，它的目的是，消除原有 Shuffle Join 
  中两张大表所引入的海量数据分发，大幅削减磁盘与网络开销的同时，从整体上提升作业端到端的执行性能。



数据倾斜
大表 Join 大表
	单表倾斜，内表倾斜
	单表倾斜，外表倾斜
	双表倾斜


以  Task 为粒度解决数据倾斜
学过 AQE 之后，要应对数据倾斜，想必你很快就会想到 AQE 的特性：自动倾斜处理。
  给定如下配置项参数，Spark SQL 在运行时可以将策略 OptimizeSkewedJoin 插入到物理计划中，自动完成 Join 过程中对于数据倾斜的处理。
	spark.sql.adaptive.skewJoin.skewedPartitionFactor，判定倾斜的膨胀系数。
	spark.sql.adaptive.skewJoin.skewedPartitionThresholdInBytes，判定倾斜的最低阈值。
	spark.sql.adaptive.advisoryPartitionSizeInBytes，以字节为单位定义拆分粒度。
以 Executor 为粒度解决数据倾斜
如果倾斜分区刚好落在集群中少数的 Executors 上，你该怎么办呢？答案是：“分而治之”和“两阶段 Shuffle”。
分而治之的含义就是，对于内外表中两组不同的数据，我们分别采用不同的方法做关联计算，然后通过 Union 操作，
  再把两个关联计算的结果集做合并，最终得到“大表 Join 大表”的计算结果。
对于 Join Keys 分布均匀的数据部分，我们可以沿用把 Shuffle Sort Merge Join 转化为 Shuffle Hash Join 的方法。
  对于 Join Keys 存在倾斜问题的数据部分，我们就需要借助“两阶段 Shuffle”的调优技巧，来平衡 Executors 之间的工作负载。
如何理解“两阶段 Shuffle”？
用一句话来概括，“两阶段 Shuffle”指的是，通过“加盐、Shuffle、关联、聚合”与“去盐化、Shuffle、聚合”这两个阶段的计算过程，
在不破坏原有关联关系的前提下，在集群范围内以 Executors 为粒度平衡计算负载 。


//spark 的四大组件
1、SparkStreaming:针对实时数据进行流式计算的组件
2、SparkSQL:用来操作结构化数据的组件
3、GraphX:Spark面向图计算提供的框架与算法库
4、MLlib:一个机器学习算法库。

//Spark的四大特点
一、速度快
由于Apache Spark支持内存计算,并且通过DAG(有向无环图)
执行引擎支持无环数据流,所以官方宣称其在内存中的运算速度要比Hadoop的MapReduce快100倍,在硬盘中要快10倍
二、易于使用
Spark的版本已经更新到了Spark3.3.1(截止日期2022.12.07),
支持了包括Java、Scala、Python、R和SQL语言在内的多种语言。为了兼容Spark2.x企业级应用场景,Spark仍然持续更新Spark2版本
三、通用性强
在Spark的基础上,Spark还提供了包括Spark SQL、Spark Streaming、MLib及GraphX在内的多个工具库,
我们可以在一个应用中无缝的使用这些工具库
四、运行方式
Spark支持多种运行方式,包括在Hadoop和Mesos上,也支持Standalone的独立运行模式,同时也可以运行在云Kubernets(Spark2.3开始支持)上
对于数据源而言,Spark支持从HDFS、HBase、Cassandra及Kafka等多种途径获取和数据


15.spark的特点包括
A. 快速
B. 通用
D. 兼容性

////Spark 架构原理
//Spark 运行架构
Spark 框架的核心是一个计算引擎,整体来说,它采用了标准 master-slave的结构。
Driver 表示 master,负责管理整个集群中的作业任务调度。图形中的Executor 则是 slave,负责实际执行任务。
Driver:Spark 驱动器节点，用于执行 Spark 任务中的 main 方法，负责实际代码的执行工作。
        	将用户程序转化为作业（job）
        	在 Executor 之间调度任务(task)
        	跟踪Executor 的执行情况
        	通过UI 展示查询运行情况
Executor:是集群中工作节点（Worker）中的一个 JVM 进程，
         负责在 Spark作业中运行具体任务（Task），任务彼此之间相互独立。
        	负责运行组成 Spark 应用的任务，并将结果返回给驱动器进程
        	它们通过自身的块管理器（Block Manager）为用户程序中要求缓存的 RDD 提供内存式存储。
            RDD 是直接缓存在 Executor 进程内的，因此任务可以在运行时充分利用缓存数据加速运算。


Master & Worker:
	Spark 集群的独立部署环境中，不需要依赖其他的资源调度框架，自身就实现了资源调度的功能，
    所以环境中还有其他两个核心组件：Master 和 Worker，
    这里的 Master 是一个进程，主要负责资源的调度和分配，并进行集群的监控等职责，类似于 Yarn 环境中的 RM, 
    而Worker 呢？也是进程，一个 Worker 运行在集群中的一台服务器上，
    由 Master 分配资源对数据进行并行的处理和计算，类似于 Yarn 环境中 NM

ApplicationMaster:
Hadoop 用户向 YARN 集群提交应用程序时,提交程序中应该包含ApplicationMaster，
用于向资源调度器申请执行任务的资源容器 Container，
运行用户自己的程序任务 job，监控整个任务的执行，跟踪整个任务的状态，处理任务失败等异常情况。
说的简单点就是，ResourceManager（资源）和Driver（计算）之间的解耦合靠的就是ApplicationMaster。





//////简述Spark的架构与作业提交流程
1.Spark 部署方式
主要是说明一下Spark 的架构,和Spark on yarn client 和 spark on yarn cluster的架构



//Spark支持3种集群部署模式
Standalone、Yarn、Mesos

7.Spark 的集群部署模式不包括
D. Local



////
窄转化/窄依赖(Narrow Dependency):指父RDD的每个分区只被子RDD的一个分区所使用,
例如map、filter等这些算子 一个RDD,对它的父RDD只有简单的一对一的关系,
RDD的每个partition仅仅依赖于父RDD 中的一个partition,
父RDD和子RDD的partition之间的对应关系,是一对一的。

宽转化/(Shuffle Dependency):父RDD的每个分区都可能被子RDD的多个分区使用,
例如groupByKey、 reduceByKey,sortBykey等算子,这些算子其实都会产生shuffle操作,
每一个父RDD的partition中的数据都可能会传输一部分到下一个RDD的每个partition中。
此时就会出现,父RDD和子RDD的partition之间,具有错综复杂的关系,这种情况就叫做两个RDD 之间是宽依赖,
同时,他们之间会发生shuffle操作。


5. 下面哪个操作是窄依赖
B. filter

6.下面哪个操作肯定是宽依赖
C. reduceByKey


////rdd特点
RDD:弹性分布式数据集 (Resilient Distributed DataSet)
分区:每一个 RDD 包含的数据被存储在系统的不同节点上。
逻辑上我们可以将 RDD 理解成一个大的数组,数组中的每个元素就代表一个分区 (Partition) 。
在物理存储中,每个分区指向一个存储在内存或者硬盘中的数据块 (Block) ,
其实这个数据块就是每个 task 计算出的数据块,它们可以分布在不同的节点上。
所以,RDD 只是抽象意义的数据集合,分区内部并不会存储具体的数据,
只会存储它在该 RDD 中的 index,通过该 RDD 的 ID 和分区的 index 可以唯一确定对应数据块的编号,
然后通过底层存储层的接口提取到数据进行处理。
在集群中,各个节点上的数据块会尽可能的存储在内存中,只有当内存没有空间时才会放入硬盘存储,
这样可以最大化的减少硬盘 IO 的开销。


不可变:不可变性是指每个 RDD 都是只读的,它所包含的分区信息是不可变的。
      由于已有的 RDD 是不可变的,所以我们只有对现有的 RDD 进行转化 (Transformation) 操作,才能得到新的 RDD 
      ,一步一步的计算出我们想要的结果。

      这样会带来这样的好处：我们在 RDD 的计算过程中,不需要立刻去存储计算出的数据本身,
      我们只要记录每个 RDD 是经过哪些转化操作得来的,即：依赖关系,这样一方面可以提高计算效率,
      一方面是错误恢复会更加容易。如果在计算过程中,第 N 步输出的 RDD 的节点发生故障,数据丢失,
      那么可以根据依赖关系从第 N-1 步去重新计算出该 RDD,
      这也是 RDD 叫做"弹性"分布式数据集的一个原因。

并行操作:因为 RDD 的分区特性,所以其天然支持并行处理的特性。
        即不同节点上的数据可以分别被处理,然后生成一个新的 RDD。

8. 下面哪个不是 RDD 的特点
C. 可修改


//RDD持久化 action算子 触发
////缓存
可以控制存储级别(内存、磁盘等)来进行缓存。 如果在应用程序中多次使用同一个RDD,
可以将该RDD缓存起来,该RDD只有在第一次计算的时候会根据血缘关系得到分区的数据,
在后续其他地方用到该RDD的时候,会直接从缓存处取而不用再根据血缘关系计算,这样就加速后期的重用。

// 数据缓存。  
wordToOneRdd.cache()   
// 可以更改存储级别  
mapRdd.persist(StorageLevel.MEMORY_AND_DISK_2) 

////checkpoint
虽然RDD的血缘关系天然地可以实现容错,当RDD的某个分区数据失败或丢失,可以通过血缘关系重建。
但是于长时间迭代型应用来说,随着迭代的进行,RDDs之间的血缘关系会越来越长,一旦在后续迭代过程中出错,
则需要通过非常长的血缘关系去重建,势必影响性能。
RDD支持checkpoint将数据保存到持久化的存储中,这样就可以切断之前的血缘关系,
因为checkpoint后的RDD不需要知道它的父RDDs了,它可以从checkpoint处拿到数据。

检查点其实就是通过将RDD 中间结果写入磁盘
wordToOneRdd.checkpoint()  

缓存和检查点区别
	Cache 缓存只是将数据保存起来,不切断血缘依赖。Checkpoint 检查点切断血缘依赖。
	Cache 缓存的数据通常存储在磁盘、内存等地方,可靠性低。Checkpoint 的数据通常存储在HDFS 等容错、高可用的文件系统,可靠性高。
	建议对checkpoint()的RDD 使用Cache 缓存,这样 checkpoint 的 job 只需从 Cache 缓存中读取数据即可,否则需要再从头计算一次RDD。
18.RDD有哪些缺陷?
A. 不支持细粒度的写和更新操作(如网络爬虫)
D. 不支持增量迭代计算

9. 下列哪个不是 RDD 的缓存方法
C. Memory()


10. Spark默认的存储级别
A. MEMORY_ONLY


7.Spark中的缓存机制cache与checkpoint机制,并指出两者的区别与联系
cache缓存和checkpoint都是Spark的缓存机制,将RDD的结果缓存到内存或者磁盘中,以供后面调用。
区别是:
cache缓存不切断血缘关系,而checkepoint切断血缘关系。
cache是缓存到内存中或者磁盘临时文件中,安全性低,checkpoint是持久化到高可用的分布式文件系统或者本地,安全性高。
cache缓存的是当前RDD已经计算出的结果,而checkpoint为了保证数据的准确性,会从头开始计算得到结果后再缓存,
也就是说cache时RDD链只计算一次,而checkpoint时RDD链需要计算两次。


14. DataFrame 和 RDD 最大的区别
B. 多了 schema

8.简述Spark 中 RDD、DataFrame、DataSet三者之间的区别与三者之间怎么相互转换
RDD<=>DF
定义schema信息后,rdd.DF(schema)
DF.toRDD

RDD<=>DS
首先定义一个样例类,参数中明确字段名称、字段类型
RDD首先结构转换为kv类型的数据
然后RDD.toDS(样例类名)
DS.toRDD

DF<=>DS
DF.toDS(样例类名)
DS.toDF








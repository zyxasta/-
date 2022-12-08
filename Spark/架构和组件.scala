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



//
SQLContext:spark SQL的入口点位 
SparkContext:为Spark的主要入口点
             SparkContext则是客户端的核心；如注释所说 SparkContext用于连接Spark集群、
             创建RDD、累加器（accumlator）、广播变量（broadcast variables）


//Spark on Yarn 的优势

	部署Application和服务更加方便：只需要yarn服务，Spark应用程序不要要自带服务只需要经由客户端提交后，
                                由yarn提供的分布式缓存机制分发到各个计算节点上。
	资源隔离：yarn只负责资源的管理和调度，完全由用户和自己决定在yarn集群上运行哪种服务和Applicatioin，
            所以在yarn上有可能同时运行多个同类的服务和Application。
            Yarn利用Cgroups实现资源的隔离，用户在开发新的服务或者Application时，
            不用担心资源隔离方面的问题。
	资源弹性管理：Yarn可以通过队列的方式，管理同时运行在yarn集群种的多个服务，
                可根据不同类型的应用程序压力情况，调整对应的资源使用量，实现资源弹性管理。





//Spark on yarn client 和 spark on yarn cluster


//Spark支持3种集群部署模式
Standalone、Yarn、Mesos






//核心概念
应用 Application:使用 Spark 的 API 构建的基于 Spark 的用户程序。
                它由一个驱动器程序和集群内的多个执行器组成。
                
SparkSession:SparkSession 对象提供与下层 Spark 功能交互的入口。
             它允许用户用Spark 的 API 编写 Spark 程序。
             在交互式 Spark shell 中，Spark 驱动器已经初始化了一个 SparkSession 对象，
             但在 Spark 应用中，你需要自行创建SparkSession 对象。

作业:由许多任务组成的并行计算，在调用 Spark 行动操作（如 save()、collect() 等）时生成。
执行阶段：每个作业会被分为更小的任务集合，即执行阶段。执行阶段之间会存在依赖关系。
任务：发送到 Spark 执行器上的具体工作的独立单元。





//题目
////Stage
4.Stage 的 Task 的数量由什么决定
A.Partition

////6.简述Spark Stage 的划分原理
Job->Stage->Task
开发完一个应用以后,把这个应用提交到Spark集群,这个应用叫Application。
这个应用里面开发了很多代码,这些代码里面凡是遇到一个action操作,就会产生一个job任务。
一个Application有一个或多个job任务。job任务被DAGScheduler划分为不同stage去执行,stage是一组Task任务。
Task分别计算每个分区partition上的数据,Task数量=分区partition数量。

Spark如何划分Stage:
会从执行action的最后一个RDD开始向前推,首先为最后一个RDD创建一个stage,
向前遇到某个RDD是宽依赖,再划分一个stage。如下图,从宽依赖处划分为2个stage。
原理的应用场景:
1.通过监控界面上每个stage及其内部task运行情况,找到对应的代码段做性能调优。
2.指定RDD的分区数参数,实际也调整了task的数量,在数据量较大时适当调整增加并行度。



////Spark的组件
A.DAGScheduler
C.TaskScheduler
D.SparkContext

////15.spark的特点包括
A. 快速
B. 通用
D. 兼容性

////3. Task 运行在下来哪里个选项中 Executor 上的工作单元
C. worker node

////16. Spark driver的功能是什么
A. 是作业的主进程
B. 负责了作业的调度
D. 负责作业的解析



////7.Spark 的集群部署模式不包括
D. Local



////简述Spark的架构与作业提交流程
1.Spark 部署方式
主要是说明一下Spark 的架构,和Spark on yarn client 和 spark on yarn cluster的架构

////12.在Spark2.0 版本之前,Spark SQL中创建DataFrame和执行SQL的入口是()
C、SQLContext



////17. SparkContext可以从哪些位置读取数据
A.本地磁盘
C.hdfs
D.内存



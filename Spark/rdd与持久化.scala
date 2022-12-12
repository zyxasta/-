

//rdd特点
RDD:弹性分布式数据集 (Resilient Distributed DataSet)
RDD 是一种抽象,是 Spark 对于分布式数据集的抽象,它用于囊括所有内存中和磁盘中的分布式数据实体。
	弹性:
 	存储的弹性:内存与磁盘的自动切换
 	容错的弹性:数据丢失可以自动恢复
 	计算的弹性:计算出错重试机制
 	分片的弹性:可根据需要重新分片
	分布式:数据存储在大数据集群不同节点上数据集:RDD 封装了计算逻辑,并不保存数据
	数据抽象:RDD 是一个抽象类,需要子类具体实现
	不可变:RDD 封装了计算逻辑,是不可以改变的,想要改变,只能产生新的RDD,在新的RDD 里面封装计算逻辑
	可分区、并行计算





特点:
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

      这样会带来这样的好处:我们在 RDD 的计算过程中,不需要立刻去存储计算出的数据本身,
      我们只要记录每个 RDD 是经过哪些转化操作得来的,即:依赖关系,这样一方面可以提高计算效率,
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
	建议对checkpoint()的RDD 使用Cache 缓存,这样 checkpoint 的 job 只需从 Cache 缓存中读取数据即可,
    否则需要再从头计算一次RDD。  见93行
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



//RDD创建
////1:从集合中创建RDD,Spark 主要提供了两个方法:parallelize 和 makeRDD
val sparkConf =  new SparkConf().setMaster("local[*]").setAppName("spark") 
val sparkContext = new SparkContext(sparkConf)  
val rdd1 = sparkContext.parallelize( List(1,2,3,4))  
val rdd2 = sparkContext.makeRDD( List(1,2,3,4))  

////2:从外部存储（文件）创建RDD
由外部存储系统的数据集创建RDD 包括:本地的文件系统,所有Hadoop 支持的数据集, 比如HDFS、HBase 等。
val sparkConf =  new SparkConf().setMaster("local[*]").setAppName("spark") 
val sparkContext = new SparkContext(sparkConf)  
val fileRDD: RDD[String] = sparkContext.textFile("input") 
fileRDD.collect().foreach(println)  
sparkContext.stop()

////3:从其他RDD创建
////4:直接创建RDD(new) :使用 new 的方式直接构造RDD,一般由Spark 框架自身使用










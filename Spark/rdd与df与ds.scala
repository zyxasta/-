

数据抽象

//rdd

//DataFrame:
	DataFrame可以看做分布式 Row 对象的集合,提供了由列组成的详细模式信息,使其可以得到优化。
              DataFrame 不仅有比RDD更多的算子,还可以进行执行计划的优化
	DataFrame更像传统数据库的二维表格,除了数据以外,还记录数据的结构信息,即schema
	DataFrame也支持嵌套数据类型(struct、 array和map)
	DataFrame API提供的是一套高层的关系操作,比函数式的RDD API要更加友好,门槛更低
	Dataframe的劣势在于在编译期缺少类型安全检查,导致运行时出错

////schema:
DataFrame中提供了详细的数据结构信息,从而使得SparkSQL可以清楚地知道该数据集中包含哪些列,
每列的名称和类型各是什么,DataFrame中的数据结构信息,即为schema。


//DataSet:DataSet会逐步取代 RDD 和 DataFrame 成为唯一的API接口。

1:与RDD相比,保存了更多的描述信息,概念上等同于关系型数据库中的二维表
2:与DataFrame相比,保存了类型信息,是强类型的,提供了编译时类型检查；
3:调用Dataset的方法先会生成逻辑计划,然后Spark的优化器进行优化,最终生成物理计划,然后提交到集群中运行。
4:DataSet包含了DataFrame的功能,在Spark2.0中两者得到了统一：  DataFrame表示 为DataSet[Row],即DataSet的子集。
                                                             Row是一个泛化的无类型 JVM object 

////三者的共性：
	RDD、DataFrame、Dataset都是 Spark 平台下的分布式弹性数据集,为处理海量数据提供便利 
	三者都有许多相同的概念,如分区、持久化、容错等；有许多共同的函数,如map、filter,sortBy等 
	三者都有惰性机制,只有在遇到 Action 算子时,才会开始真正的计算 
	对DataFrame和Dataset进行操作许多操作都需要这个包进行支持,import spark.implicits._ 

////三者的区别：
DataSet(DataFrame = RDD[Row] + Schema): 
	与RDD和Dataset不同,DataFrame每一行的类型固定为Row,只有通过解析才能获取各个字段的值 
	DataFrame与Dataset均支持 SparkSQL 的操作 
   Dataset(Dataset = RDD[case class].toDS)
	Dataset和DataFrame拥有完全相同的成员函数,区别只是每一行的数据类型不同
	DataFrame 定义为 Dataset[Row]。每一行的类型是Row,每一行究竟有哪些字段,各个字段又是什么类型都无从得知,
                      只能用前面提到的getAS方法或者模式匹配拿出特定字段
	Dataset每一行的类型都是一个case class,在自定义了case class之后可以很自由的获得每一行的信息

Spark SQL的数据类型
https://spark.apache.org/docs/latest/sql-ref-datatypes.html


在 Spark 2.0 之前： 
SQLContext 是创建 DataFrame 和执行 SQL 的入口 
HiveContext通过Hive sql语句操作Hive数据,兼Hhive操作,HiveContext继承自SQLContext
 
在 Spark 2.0 之后： 
将这些入口点统一到了SparkSession,SparkSession 封装了 SqlContext 及 HiveContext,
实现了 SQLContext 及 HiveContext 所有功能,通过SparkSession可以获取到SparkConetxt

//创建
不要刻意区分:DF、DS。DF是一种特殊的DS
ds.transformation => df 
//创建ds
1:val numDS = spark.range(5, 100, 5)

2:case class Person(name: String, age: Int, height: Int)
// 注意 Seq 中元素的类型 
val seq1 = Seq(Person("Jack", 28, 184), Person("Tom", 10, 144), Person("Andy", 16, 165))
val ds1 = spark.createDataset(seq1)
// 显示schema信息 
ds1.printSchema
ds1.show

//创建df
1:val lst = List(("Jack", 28, 184), ("Tom", 10, 144), ("Andy", 16, 165))
val df1 = spark.createDataFrame(lst).
  // 改单个字段名时简便 
  withColumnRenamed("_1", "name1").
  withColumnRenamed("_2", "age1").
  withColumnRenamed("_3", "height1")
df1.orderBy("age1").show(10)
// desc是函数,在IDEA中使用是需要导包 

import org.apache.spark.sql.functions._

df1.orderBy(desc("age1")).show(10)
// 修改整个DF的列名 
val df2 = spark.createDataFrame(lst).toDF("name", "age", "height")


//rdd转df
A.利用反射机制推断RDD模式
B.使用编程方式定义RDD模式
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
val sc =spark.sparkContext
val arr = Array(("Jack", 28, 184), ("Tom", 10, 144), ("Andy",
  16, 165))
val rdd1 = sc.makeRDD(arr).map(f=>Row(f._1, f._2, f._3))  ////rdd1
val schema = StructType( StructField("name", StringType,false) ::
  StructField("age", IntegerType, false) ::
  StructField("height", IntegerType, false) ::
  Nil)
val schema1 = (new StructType).add("name", "string", false).add("age", "int", false).add("height", "int", false)
// RDD => DataFrame,要指明schema 
val rddToDF = spark.createDataFrame(rdd1, schema)   //rdd  to   df
rddToDF.orderBy(desc("name")).show(false)

// IDEA中需要,spark-shell中不需要 
import spark.implicits._
val arr2 = Array(("Jack", 28, 150), ("Tom", 10, 144), ("Andy",
  16, 165))
val rddToDF = sc.makeRDD(arr2).toDF("name", "age", "height")
case class Person(name:String, age:Int, height:Int)
val arr2 = Array(("Jack", 28, 150), ("Tom", 10, 144), ("Andy",
  16, 165))
val rdd2: RDD[Person] =
  spark.sparkContext.makeRDD(arr2).map(f=>Person(f._1, f._2,
    f._3))
val ds2 = rdd2.toDS() // 反射推断,spark 通过反射从case 
val df2 = rdd2.toDF() // 反射推断 rdd to  df



//RDD转Dataset
Dataset = RDD[case class] 
DataFrame = RDD[Row] + Schema
val ds3 = spark.createDataset(rdd2)
val ds2 = rdd2.toDS() // 反射推断,spark 通过反射从case 

//从文件创建DateFrame
val df1 = spark.read.csv("data/people1.csv")
df1.printSchema()
df1.show()
val df2 = spark.read.csv("data/people2.csv")
df2.printSchema()
df2.show()
// 指定参数 
// spark 2.3.0 
val schema = "name string, age int, job string"
val df3 = spark.read
  .options(Map(("delimiter", ";"), ("header", "true")))
  .schema(schema)
  .csv("data/people2.csv")      //1
df3.printSchema()
df3.show
// 自动类型推断 
val df4 = spark.read
  .option("delimiter", ";")
  .option("header", "true")
  .option("inferschema", "true")
  .csv("data/people2.csv")   //2
df4.printSchema()
df4.show

//三者的转换
rdd <-> df
  rdd -> df :1 createDataFrame(rdd,schema)  2:rdd.toDF
  DF -> RDD: 直接转调用rdd df.rdd

rdd <-> ds
  rdd -> ds :指定case class 调用 toDS
  ds -> RDD: 直接转调用rdd  ds.rdd
df <-> ds
  df -> ds 指定case class 调用as[case class]
  df <- ds 直接转,调用toDF


////14. DataFrame 和 RDD 最大的区别
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




////19. 要读取people.json文件生成DataFrame,可以使用下列那些命令
A. spark.read.json("people.json")
C. spark.read.format("json").load("people.json")

////20. 从RDD转换得到DataFrame包含两种典型的方法,分别是
A.利用反射机制推断RDD模式
B.使用编程方式定义RDD模式











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




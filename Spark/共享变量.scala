


//共享变量☆☆
累加器

累加器用来把Executor 端变量信息聚合到Driver 端。
在Driver 程序中定义的变量，在Executor 端的每个Task 都会得到这个变量的一份新的副本，
每个 task 更新这些副本的值后， 传回Driver 端进行 merge。

1:系统累加器
val rdd = sc.makeRDD(List(1,2,3,4,5))  
// 声明累加器  
var sum = sc.longAccumulator("sum"); rdd.foreach(  
num => {  
// 使用累加器  
sum.add(num)  
}  
)  
// 获取累加器的值  
println("sum = " + sum.value) 

2:自定义累加器
// 自定义累加器  
// 1. 继承 AccumulatorV2，并设定泛型  
// 2. 重写累加器的抽象方法  
class WordCountAccumulator extends AccumulatorV2[String, mutable.Map[String, Long]]{  
  
var map : mutable.Map[String, Long] = mutable.Map()  
  
// 累加器是否为初始状态  
override def isZero: Boolean = { map.isEmpty  
}  
  
// 复制累加器  
override def copy(): AccumulatorV2[String, mutable.Map[String, Long]] = { new WordCountAccumulator  
}  
  
// 重置累加器  
override def reset(): Unit = { map.clear()  
}  
  
// 向累加器中增加数据 (In)  
override def add(word: String): Unit = {  
// 查询 map 中是否存在相同的单词  
// 如果有相同的单词，那么单词的数量加 1  
// 如果没有相同的单词，那么在 map 中增加这个单词  
map(word) = map.getOrElse(word, 0L) + 1L  
}  
// 合并累加器  
override def merge(other: AccumulatorV2[String, mutable.Map[String, Long]]): Unit = {  
  
val map1 = map  
val map2 = other.value  
  
// 两个 Map 的合并  
map = map1.foldLeft(map2)( ( innerMap, kv ) => {  
innerMap(kv._1) = innerMap.getOrElse(kv._1, 0L) + kv._2 innerMap  
}  
)  
}  
  
// 返回累加器的结果 （Out）  
override def value: mutable.Map[String, Long] = map 


广播变量

广播变量用来高效分发较大的对象。
向所有工作节点发送一个较大的只读值，以供一个或多个 Spark 操作使用。
比如，如果你的应用需要向所有节点发送一个较大的只读查询表， 广播变量用起来都很顺手。
在多个并行操作中使用同一个变量，但是 Spark 会为每个任务分别发送。

val rdd1 = sc.makeRDD(List( ("a",1), ("b", 2), ("c", 3), ("d", 4) ),4)  
val list = List( ("a",4), ("b", 5), ("c", 6), ("d", 7) )  
// 声明广播变量  
val broadcast: Broadcast[List[(String, Int)]] = sc.broadcast(list)  
  
val resultRDD: RDD[(String, (Int, Int))] = rdd1.map { case (key, num) => {  
var num2 = 0  
// 使用广播变量  
for ((k, v) <- broadcast.value) { if (k == key) {  
num2 = v  
}  
}  
(key, (num, num2))  
}  
}



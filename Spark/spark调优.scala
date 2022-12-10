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














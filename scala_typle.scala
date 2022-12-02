
//经典知识点使用
//函数式,避免指令式写法



//////////////////////////////////////////////////////////////////////////////////////////////

////闭包
闭包是一个函数,返回值依赖于声明在函数外部的一个或多个变量。
函数的参数称为绑定变量,而依赖的函数外的变量称为自由变量
创建的闭包能够看到闭包外对自由变量的修改,反之,闭包对捕获的变量的修改也能在闭包外看到

(x : Int) => x + more

//x绑定变量
more自由变量

////柯里化
柯里化指的是将原来接受两个参数的函数变成接受一个参数的函数的过程。
相当于做了连着做了2次传统的函数调用,第一次用第一个参数,第二次用第二个参数
相当于以第一个参数做函数,函数里面是以第二个参数做参数的匿名函数
柯里化解决了只支持一个参数列表的问题
//

def plainOldSum(x: Int,y :Int) = x + y
def plainOldSum(x: Int)(y :Int) = x + y
def plainOldSum(x: Int) = (y :Int) => x + y



////尾递归函数
在最后一步调用自己的函数
尾递归不需要支付任何额外的运行时开销,
不会在每次调用是构建新的栈,所有的调用在同一个栈
尾递归写法和非尾递归写法生成的java字节码是一样的.
//局限性
尾递归优化,仅适用与在某个方法或嵌套函数在最后一步操作中直接调用自己,
并没有经过函数值或其他中间环节的场合

def approximate(guess: Double): Double =
  if isGoodEnough(guess) then guess
  else approximate(improve(guess))


////惰性赋值
对修饰的val变量的初始化,延迟到第一次访问的时候.
首次求值后,其结果会保存起来,并在后续的使用中,都复用这个相同的val

lazy val abc = "cdcddc....dfvv....."

lazy 的优势和应用
1.lazy来定义惰性变量,实现延迟加载
2.对于特别耗费资源的特别有用,打开文件IO、网络IO等比如声明MySQL Conn、Spark RDD计算的惰性加载)
3.对函数式对象的完美补充.
在惰性的val的初始化既不产生副作用又不依赖副作用的时候有效,此时,初始化顺序不重要

trait LazyRationalTrait:

  val numerArg: Int 
  val denomArg: Int 

  lazy val numer = numerArg / g
  lazy val denom = denomArg / g

  override def toString = s"$numer/$denom"

  private lazy val g =
    require(denomArg != 0)
    gcd(numerArg, denomArg)

  private def gcd(a: Int, b: Int): Int = 
    if b == 0 then a else gcd(b, a % b)

val x = 2
//x: Int = 2

new LazyRationalTrait:
         val numerArg = 1 * x //2
         val denomArg = 2 * x  //4
//val res4:LazyRationalTrait = 1/2
//g numer denom


////高阶函数
使用其他函数作为参数,或者使用函数作为输出结果。
函数在Scala中是“头等公民”,它的使用方法和任何其他变量是一样的。

1.函数作为值被传递和赋值
Scala中可以将函数赋值给var、val、def定义的变量,代码实例如下：
object HigherOrderFunctionOps1 {
  def main(args: Array[String]): Unit = {
    sayHello("Old One")
    val sayTwo = sayHello _
    sayTwo("Old Two")
    var sayThree = sayHello _
    sayThree("Old Three")

    def sayFour = sayHello _

    sayFour("Old Four")
  }

  def sayHello(name: String): Unit = {
    println(s"${name} say Hello to you!")
  }
}
这里有个约定：将函数赋值给变量时,必须在函数后加上空格和下划线。

2.匿名函数
匿名函数顾名思义是没有函数名的函数,这种函数在使用的使用必须将函数赋值给其他变量或者函数。语法形式是：
(函数参数) => {代码块} 或 返回值类型
object HigherOrderFunctionOps2 {
  def main(args: Array[String]): Unit = {
    var printMag = (name: String) => println(s"${name}搞偷袭,不讲武德！")
    printMag("年轻人")
    var niceSub = () => println("好感减1!")
    niceSub()
    var numSub = (num: Int) => num - 1
    println(s"当前好感度：${numSub(5)}")
  }
}
3.高阶函数定义和参数类型推断
有了匿名函数和函数作为值传递的语法基础,就可以定义高阶函数了。下面是函数作为函数参数的使用实例：
object HigherOrderFunctionOps3 {
  def main(args: Array[String]): Unit = {
    funOps
  }

  def funOps: Unit = {
    def sayBye(name: String, fun: (String) => Unit): Unit = {
      fun(name)
    }

    sayBye("张三", (name: String) => method(name)) //调用有名函数
    sayBye("李四", (name: String) => {
      println(s"${name}真是个人才！") //调用匿名函数
    })
  }

  def method(str: String): Unit = {
    println(s"${str}是名字！")
  }
}

Scala提供了一个使用通配符_ 下划线替代参数的用法,或直接使用函数名的方法：
sayBye("王五", println(_))//类型推断
sayBye("赵四", println)//类型推断

////样例类 case class

case class,它其实就是一个普通的class。但是它又和普通的class略有区别,

1、apply()方法.初始化的时候可以不用new,当然你也可以加上,普通类一定需要加new;
嵌套定义时,更有效.
参数列表隐式的获得val前缀,会被当字段处理.
case class DsScala(name:String)
//defined class DsScala
val ds_scala = DsScala("DsScala")
//ds_scala: DsScala = DsScala(DsScala)
val ds_scala = new DsScala("DsScala")
//ds_scala: DsScala = DsScala(DsScala)
val op = BinOp("+", Num(1), v)

2、默认实现了equals 和hashCode,toString
val ds_scala2 = DsScala("DsScala")
//ds_scala2: DsScala = DsScala(DsScala)
ds_scala2 == ds_scala
//res2: Boolean = true
ds_scala.hashCode
//res3: Int = 1877692305
ds_scala
//res1: DsScala = DsScala(DsScala)


3、默认是可以序列化的,也就是实现了Serializable ;
import java.io._
//import java.io._
class A
//defined class A
val bos = new ByteArrayOutputStream 
//bos: java.io.ByteArrayOutputStream =
val oos = new ObjectOutputStream(bos)
//oos: java.io.ObjectOutputStream = java.io.ObjectOutputStream@58179860
oos.writeObject(ds_scala)
val a = new A
//a: A = A@c9a8e12
oos.writeObject(a)
java.io.NotSerializableException: A
at java.io.ObjectOutputStream.writeObject0(ObjectOutputStream.java:1184)
at java.io.ObjectOutputStream.writeObject(ObjectOutputStream.java:348)
... 51 elided


5、自动从scala.Product中继承一些函数  ???
6、case class构造函数的参数是public级别的,我们可以直接访问；
scala> ds_scala.name
res6: String = DsScala

7、支持模式匹配
其实感觉case class最重要的特性应该就是支持模式匹配。这也是我们定义case class的唯一理由,
来看下面的例子：
//将Person类定义为case class
case class Person(name: String, age: Int)

object ConstructorPattern {
  def main(args: Array[String]): Unit = {
    val p = new Person("nyz", 27)

    def constructorPattern(p: Person) = p match {
      //构造器模式必须将Person类定义为case class,否则需要自己定义伴生对象并实现unapply方法。
      case Person(name, age) => "name =" + name + ",age =" + age
      case Person(_,age) => "age =" + age
      case _ => "Other"
    }
    println(constructorPattern(p))
  }
}

// 序列模式用于匹配如数组Array、列表List、Range这样的线性结构集合,
//其实原理也是通过case class起作用的。
object SequencePattern {
  def main(args: Array[String]): Unit = {
    val list = List("spark", "Hive", "SparkSQL")
    val arr = Array("SparkR", "Spark Streaming", "Spark MLib")

    def sequencePattern(p: Any) = p match {
      //序列模式匹配,_*表示匹配剩余内容,first、second匹配数组p中的第一、二个元素
      case Array(first, second, _*) => first + "," + second
      //_匹配数组p的第一个元素,但不赋给任何变量
      case List(_, second, _*) => second
      case _ => "Other"
    }

    println(sequencePattern(list))
    println(sequencePattern(arr))
  }
}

////伴生对象
//1、class和object在同一个scala文件中
//2、class和object要同名
// 特征：
// 1、伴生类和伴生对象可互访私有成员
// 2、伴生对象 的apply方法,可简化 伴生类之对象 的创建
// 感性认识：
// class里面的全是 非静态的,object里面的全是 静态的

object Companion {
  
  def main(args: Array[String]): Unit = {
    val person = new Person("东莞", "MALE")
    // print(person.name)
    // print(person.age)
    println(Person.param)
    // Person.add()
    println(person.sum(11, 22))
    Person.printAge
    val p1: Person = Person.apply("HZ", "M")
    val p2: Person = Person("SG", "F") // 相当于 Person.apply()
    println(p1.address + "~" + p1.sex)
    println(p2.address + "~" + p2.sex)
    val p = Person("FS", "MALE")
    println(p.address + "~" + p.sex)
    Array(1, 2, 3, 4)
    List(1, 2, 3, 4)
  }
}

//伴生类
class Person(val address: String, val sex: String) {
  val name = "Jacky"
  private val age = 20

  // 访问伴生对象的私有方法
  def sum(x: Int, y: Int) = Person.add(x, y)
}

//伴生对象
object Person {
  val param = "100"

  private def add(x: Int, y: Int) = x + y

  // 通过 伴生对象,访问 伴生类 的私有成员 时,必须要有 伴生类的对象
  def printAge = println(s"age=${new Person("SZ", "男").age}")

  def apply(address: String, sex: String): Person = new Person(address, sex)
}


////模式匹配


模式匹配是Scala中非常有特色,非常强大的一种功能。
模式匹配,其实类似于Java中的swich case语法,即对一个值进行条件判断,然后针对不同的条件,进行不同的处理。
但是Scala的模式匹配的功能比Java的swich case语法的功能要强大地多,Java的swich case语法只能对值进行匹配。
但是Scala的模式匹配除了可以对值进行匹配之外,还可以对类型进行匹配、对Array和List的元素情况进行匹配、
对case class进行匹配、甚至对有值或没值(Option)进行匹配。
而且对于Spark来说,Scala的模式匹配功能也是极其重要的,在spark源码中大量地使用了模式匹配功能。
因此为了更好地编写Scala程序,并且更加通畅地看懂Spark的源码,学好模式匹配都是非常重要的。

1、模式匹配的基础语法(案例：成绩评价)
// Scala是没有Java中的switch case语法的,相对应的,Scala提供了更加强大的match case语法,即模式匹配,
来替代switch case,match case也被称为模式匹配
// Scala的match case与Java的switch case最大的不同点在于,Java的switch case仅能匹配变量的值,比1、2、3等；
而Scala的match case可以匹配各种情况,比如变量的类型、集合的元素、有值或无值
// match case的语法如下：变量 match { case 值 => 代码 }。如果值为下划线,则代表了不满足以上所有情况下的默认情况如何处理。
此外,match case中,只要一个case分支满足并处理了,就不会继续判断下一个case分支了。(与Java不同,java的switch case需要用break阻止)
// match case语法最基本的应用,就是对变量的值进行模式匹配
object test {
  def main(args: Array[String]): Unit = {
    def studentScore(score: String): Unit = {
      score match {
        case "A" => println("excellent")
        case "B" => println("good")
        case "C" => println("soso")
        case _ => println("you need work harder")
      }
    }

    studentScore("D")
  }
}
在模式匹配中使用if守卫
// Scala的模式匹配语法,有一个特点在于,可以在case后的条件判断中,不仅仅只是提供一个值,而是可以在值后面再加一个if守卫,进行双重过滤
// 案例：成绩评价(升级版)
object test {
  def main(args: Array[String]): Unit = {
    def studentScore(name: String, score: String): Unit = {
      score match {
        case "A" => println("excellent")
        case "B" => println("good")
        case "C" => println("soso")
        case _ if name == "leo" => print(name + ",you are good boy,come on!") //if守卫
        case _ => println("you need work harder")
      }
    }

    studentScore("leo", "D")
  }
}
在模式匹配中进行变量赋值
// Scala的模式匹配语法,有一个特点在于,可以将模式匹配的默认情况,下划线,替换为一个变量名,
此时模式匹配语法就会将要匹配的值赋值给这个变量,从而可以在后面的处理语句中使用要匹配的值
// 为什么有这种语法？？思考一下。因为只要使用用case匹配到的值,是不是我们就知道这个只啦！
！在这个case的处理语句中,是不是就直接可以使用写程序时就已知的值！
// 但是对于下划线_这种情况,所有不满足前面的case的值,都会进入_这种默认情况进行处理,
此时如果我们在处理语句中需要拿到具体的值进行处理呢？那就需要使用这种在模式匹配中进行变量赋值的语法！！
object test {
  def main(args: Array[String]): Unit = {
    def studentScore(name: String, score: String): Unit = {
      score match {
        case "A" => println("excellent")
        case "B" => println("good")
        case "C" => println("soso")
        case _ if name == "leo" => print(name + ",you are good boy,come on!")
        case _score => println("you need work harder,your score only " + _score) //变量赋值
      }
    }

    studentScore("le", "F")
  }
}

2、对类型进行模式匹配(案例：异常处理)
// Scala的模式匹配一个强大之处就在于,可以直接匹配类型,而不是值！！！这点是java的switch case绝对做不到的。
// 理论知识：对类型如何进行匹配？其他语法与匹配值其实是一样的,但是匹配类型的话,
就是要用“case 变量: 类型 => 代码”这种语法,而不是匹配值的“case 值 => 代码”这种语法。
// 案例：异常处理
object test {
  def main(args: Array[String]): Unit = {
    import java.io._
    def processException(e: Exception) {
      e match {
        case e1: IllegalArgumentException => println("you have illegal arguments! exception is: " + e1)
        case e2: FileNotFoundException => println("cannot find the file you need read or write!, exception is: " + e2)
        case e3: IOException => println("you got an error while you were doing IO operation! exception is: " + e3)
        case _: Exception => println("cannot know which exception you have!")
      }
    }

    processException(new IOException("not such file"))
  }
}
3、对Array和List的元素进行模式匹配(案例：对朋友打招呼)
// 对Array进行模式匹配,分别可以匹配带有指定元素的数组、带有指定个数元素的数组、以某元素打头的数组
// 对List进行模式匹配,与Array类似,但是需要使用List特有的::操作符
// 案例：对朋友打招呼
object test {
  def main(args: Array[String]): Unit = {
    def greeting(arr: Array[String]) {
      arr match {
        case Array("Leo") => println("Hi, Leo!") //匹配一个元素
        case Array(girl1, girl2, girl3) => println("Hi, girls, nice to meet you. " + girl1 + " and " + girl2 + " and " + girl3) 
        //匹配三个元素
        case Array("Leo", _*) => println("Hi, Leo, please introduce your friends to me.") 
        //匹配以Leo开头,三个元素会被上面匹配scala匹配机制是匹配到就停止
        case _ => println("hey, who are you?")
      }
    }

    greeting(Array("Leo", "lily", "poly", "jack"))
  }
}
//list
object test {
  def main(args: Array[String]): Unit = {
    def greeting(list: List[String]) {
      list match {
        case "Leo" :: Nil => println("Hi, Leo!")
        case girl1 :: girl2 :: girl3 :: Nil => println("Hi, girls, nice to meet you. " + girl1 + " and " + girl2 + " and " + girl3)
        case "Leo" :: tail => println("Hi, Leo, please introduce your friends to me.")
        case _ => println("hey, who are you?")
      }
    }

    greeting(List("Leo", "jack", "poly", "herry"))
  }
}

4、case class与模式匹配(案例：学校门禁)
// Scala中提供了一种特殊的类,用case class进行声明,中文也可以称作样例类。
case class其实有点类似于Java中的JavaBean的概念。即只定义field,并且由Scala编译时自动提供getter和setter方法,但是没有method。
// case class的主构造函数接收的参数通常不需要使用var或val修饰,
Scala自动就会使用val修饰(但是如果你自己使用var修饰,那么还是会按照var来)
// Scala自动为case class定义了伴生对象,也就是object,并且定义了apply()方法,
该方法接收主构造函数中相同的参数,并返回case class对象
// 案例：学校门禁
class Person

case class Teacher(name: String, subject: String) extends Person
case class Student(name: String, classroom: Int) extends Person
case class Worker(name: String, work: String) extends Person
case class Stranger() extends Person

object test {
  def main(args: Array[String]): Unit = {
    def entranceGuard(p: Person): Unit = {
      p match {
        case Student(name, classroom) => println(s"hello,$name,welcome to school,your classroom is $classroom")
        case Teacher(name, subject) => println(s"hello,$name,welcome to school,your teach $subject")
        case Worker(name, work) if work == "repairman" => println(s"hello,$name,you should leave school afternoon")
        case Worker(name, work) => println(s"hello,$name,you should leave school 2 hours later")
        case _ => println(s"stranger,you can not into school")
      }
    }

    entranceGuard(Worker("Jason", "cleaner"))
  }
}

5、Option与模式匹配(案例：成绩查询)
// Scala有一种特殊的类型,叫做Option。Option有两种值,一种是Some,表示有值,一种是None,表示没有值。
// Option通常会用于模式匹配中,用于判断某个变量是有值还是没有值,这比null来的更加简洁明了
// Option的用法必须掌握,因为Spark源码中大量地使用了Option,比如Some(a)、None这种语法,
因此必须看得懂Option模式匹配,才能够读懂spark源码。
object test {
  def main(args: Array[String]): Unit = {
    val grades = Map("Leo" -> "A", "Jack" -> "B", "Jen" -> "C")

    def getGrade(name: String) {
      val grade = grades.get(name)
      grade match {
        case Some(grade) => println("your grade is " + grade)
        case None => println("Sorry, your grade information is not in the system")
      }
    }

    getGrade("J") //Sorry, your grade information is not in the system
  }
}



////基本类型
数值类型 整数类型+Float Double
  整数类型 Byte Short Int Long Char
字符串 String
逻辑   Boolean



////scala语言

面向对象:
  Scala是一种纯粹的面向对象语言,每一个值都是对象。对象的数据类型以及行为由类和特征来描述,
  类抽象机制的扩展通过两种途径实现:一种是子类继承,另一种是混入机制,这两种途径都能够避免多重继承的问题。 
函数式:
  Scala也是一种函数式语言,其函数可以作为值来使用。
  Scala提供了轻量级的语法用于定义匿名函数,支持高阶函数,允许嵌套多层函数,并支持柯里化。
兼容的 ,java
  Scala可以与流行的Java Runtime Environment(JRE)进行良好的交互操作。
  Scala用scalac编译器把源文件编译成Java的class文件(即可以在JVM上运行的字节码)。
  我们可以从Scala中调用所有的Java类库,同样也可以从Java应用程序中调用Scala代码。
精简的:
  相比java,代码短
静态类型:
  Scala具备类型系统,通过编译时的类型检查来保证代码的安全性和一致性。
  类型系统支持的特性包括泛型类、注释、类型上下限约束、类别和抽象类型作为对象成员、复合类型、
  引用自己时显示指定类型、视图、多态方法等。
Scala是可扩展的:
  在实际开发中,某个特定领域的应用程序开发往往需要特定领域的语言扩展。
  Scala提供了许多独特的语言机制,它能够很容易地以库的方式无缝添加新的语言结构。



////函数式编程
函数式编程(functional programing)是编程范式之一。我们常见的范式还有面向过程、面向行为、面向对象等。
程序应该被分解成许多函数,且每个函数都只做明确定义的任务
1.纯函数
相同的输入会得到相同的输出,而且没有任何可观测的副作用。
2.闭包
当函数可以记住并访问所在的词法作用域时,就产生了闭包,即使函数是在当前词法作用域之外执行。包括缓存和模块化
3.高阶函数
参数是一个函数,会返回一个新函数
curry,compose,memoize都是高阶函数


////隐式转换 implicit
implicit关键字申明的带有单个参数的函数
implicit def func_name() = {}

在两种情况下会使用隐式转换：
①调用方法时,传递的参数类型与方法声明的参数类型不同时,编译器会在查找范围下查找隐式转换,把传递的参数转变为方法声明需要的类型。
②调用方法时,如果对象没有该方法,那么编译器会在查找范围内查找隐式转换,把调用方法的对象转变成有该方法的类型的对象。


在Spark Sql中,这种隐式转换大量的应用到了我们的DSL风格语法中,
并且在Spark2.0版本以后,DataSet里面如果进行转换RDD或者DF的时候,那么都需要导入必要的隐式转换操作。


当需要查找隐式对象、隐式方法、隐式类时,查找的范围是：
1.现在当前代码作用域下查找。
2.如果当前作用域下查找失败,会在隐式参数类型的作用域里查找。
类型的作用域是指与该类型相关联的全部伴生模块,一个隐式实体的类型T它的查找范围如下：
（1）如果T被定义为T with A with B with C,那么A,B,C都是T的部分,在T的隐式解析过程中,它们的伴生对象都会被搜索
（2）如果T是参数化类型,那么类型参数和与类型参数相关联的部分都算作T的部分,
    比如List[String]的隐式搜索会搜索List的伴生对象和String的伴生对象
（3） 如果T是一个单例类型p.T,即T是属于某个p对象内,那么这个p对象也会被搜索
（4） 如果T是个类型注入S#T,那么S和T都会被搜索
————————————————
版权声明：本文为CSDN博主「binbin_civil」的原创文章,遵循CC 4.0 BY-SA版权协议,转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/bingospunky/article/details/88854352






////协变,逆变,非变
首先定义的一个Trait Q[T]{}
协变 ： 如果 A 是B的子类,那么Q[A] 是 Q[B]的子类,Q这样定义Trait Q[+T]{}
逆变： 如果A是B的子类,那么Q[B]是Q[A]的子类,Q这样定义Trait Q[-T]{}
非变： 没有任何从属关系,Q这样定义Trait Q[T]{}


协变和逆变主要是用来解决参数化类型的泛化问题。Scala 的协变与逆变是非常有特色
的,完全解决了Java中泛型的一大缺憾；举例来说,Java中,如果有 A 是 B 的子类,但 Card[A]
却不是 Card[B] 的子类；而 Scala 中,只要灵活使用协变与逆变,就可以解决此类 Java 泛
型问题；
由于参数化类型的参数（参数类型）是可变的,当两个参数化类型的参数是继承关系（可
泛化）,那被参数化的类型是否也可以泛化呢？Java 中这种情况下是不可泛化的,然而 Scala
提供了三个选择,即协变(“+”)、逆变（“-”）和非变。
下面说一下三种情况的含义,首先假设有参数化特征 Queue,那它可以有如下三种定义。
(1) trait Queue[T] {}
这是非变情况。这种情况下,当类型 B 是类型 A 的子类型,则 Queue[B]与 Queue[A]没
有任何从属关系,这种情况是和 Java 一样的。
(2) trait Queue[+T] {}
这是协变情况。这种情况下,当类型 B 是类型 A 的子类型,则 Queue[B]也可以认为是
Queue[A]的子类型,即 Queue[B]可以泛化为 Queue[A]。也就是被参数化类型的泛化方向与
参数类型的方向是一致的,所以称为协变。
(3) trait Queue[-T] {}
这是逆变情况。这种情况下,当类型 B 是类型 A 的子类型,则 Queue[A]反过来可以认
为是 Queue[B]的子类型。也就是被参数化类型的泛化方向与参数类型的方向是相反的,所
以称为逆变。

 C[+T]：如果 A 是 B 的子类,那么 C[A]是 C[B]的子类。
 C[-T]：如果 A 是 B 的子类,那么 C[B]是 C[A]的子类。
 C[T]： 无论 A 和 B 是什么关系,C[A]和 C[B]没有从属关系
————————————————
版权声明：本文为CSDN博主「Deltao_Taic」的原创文章,遵循CC 4.0 BY-SA版权协议,转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/Deltao_Taic/article/details/106818091

////特质
Scala中类只能继承一个超类, 可以扩展任意数量的特质
特质可以要求实现它们的类具备特定的字段, 方法和超类
与Java接口不同, Scala特质可以提供方法和字段的实现
当将多个特质叠加使用的时候, 顺序很重要

2. 带有特质的类
2.1 当做接口使用的特质
Scala可以完全像Java接口一样工作, 你不需要将抽象方法声明为 abstract, 特质中未被实现的方法默认就是抽象方法;
类可以通过 extends 关键字继承特质, 如果需要的特质不止一个, 通过 with 关键字添加额外特质
重写特质的抽象方法时, 不需要 override 关键字
所有 Java 接口都可以当做 Scala 特质使用
trait Logger {
   def log(msg: String)  // 抽象方法
}

class ConsoleLogger extends Logger with Serializable {  // 使用extends
  def log(msg: String): Unit = {  // 不需要override关键字
    println("ConsoleLogger: " + msg)
  }
}

object LoggerTest extends App{
  val logger = new ConsoleLogger
  logger.log("hi")  //ConsoleLogger: hi
}

2.2 带有具体实现的特质
让特质混有具体行为有一个弊端. 当特质改变时, 所有混入该特质的类都必须重新编译.
trait Logger {
  def log(msg: String)  // 抽象方法
  def printAny(k: Any) { // 具体方法
    println("具体实现")
    }
}
2.3 继承类的特质
特质继承另一特质是一种常见的用法, 而特质继承类却不常见.
特质继承类, 这个类会自动成为所有混入该特质的超类
trait Logger extends Exception { }

class Mylogger extends Logger { } // Exception 自动成为 Mylogger 的超类
如果我们的类已经继承了另一个类怎么办?
没关系只要这个类是特质超类的子类就好了;
//IOException 是 Exception 的子类
class Mylogger extends IOException with Logger { } 
不过如果我们的类继承了一个和特质超类不相关的类, 那么这个类就没法混入这个特质了.

3. 带有特质的对象
在构造单个对象时, 你可以为它添加特质;

特质可以将对象原本没有的方法与字段加入对象中
如果特质和对象改写了同一超类的方法, 则排在右边的先被执行.
// Feline 猫科动物
abstract class Feline {
  def say()
}

trait Tiger extends Feline {
  // 在特质中重写抽象方法, 需要在方法前添加 abstract override 2个关键字
  abstract override def say() = println("嗷嗷嗷")
  def king() = println("I'm king of here")
}

class Cat extends Feline {
  override def say() = println("喵喵喵")
}

object Test extends App {
  val feline = new Cat with Tiger
  feline.say  // Cat 和 Tiger 都与 say 方法, 调用时从右往左调用, 是 Tiger 在叫
  feline.king // 可以看到即使没有 cat 中没有 king 方法, Tiger 特质也能将自己的方法混入 Cat 中
}

/*output
  嗷嗷嗷
  I'm king of here
*/ 
4. 特质的叠加

可以为类和对象添加多个相互调用的特质 时, 从最后一个开始调用. 这对于需要分阶段加工处理某个值的场景很有用.

下面展示一个char数组的例子, 展示混入的顺序很重要
定义一个抽象类CharBuffer, 提供两种方法
put 在数组中加入字符
get 从数组头部取出字符
abstract class CharBuffer {
  def get: Char
  def put(c: Char)
}

class Overlay extends CharBuffer{
  val buf = new ArrayBuffer[Char]
  
  override def get: Char = {
    if (buf.length != 0) buf(0) else '@'
  }
  override def put(c: Char): Unit = {
    buf.append(c)
  }
}
定义两种对输入字符进行操作的特质:

ToUpper 将输入字符变为大写
ToLower 将输入字符变为小写
因为上面两个特质改变了原始队列类的行为而并非定义了全新的队列类, 所以这2种特质是可堆叠的,你可以选择它们混入类中,获得所需改动的全新的类。

trait ToUpper extends CharBuffer {

// 特质中重写抽象方法  abstract override
 abstract override def put(c: Char) = super.put(c.toUpper)
  
  // abstract override def put(c: Char): Unit = put(c.toUpper)
  // java.lang.StackOverflowError, 由于put相当于 this.put, 在特质层级中一直调用自己, 死循环
}

trait ToLower extends CharBuffer {
  abstract override def put(c: Char) = super.put(c.toLower)
  }

特质中 super 的含义和类中 super 含义并不相同, 如果具有相同含义, 
这里super.put调用时超类的 put 方法, 它是一个抽象方法, 则会报错, 下面会详细介绍 super.put 的含义

测试
object TestOverlay extends App {
  val cb1 = new Overlay with ToLower with ToUpper
  val cb2 = new Overlay with ToUpper with ToLower

  cb1.put('A')
  println(cb1.get)

  cb2.put('a')
  println(cb2.get)

}

/*output
a
A
*/
上面代码的一些说明:

上面的特质继承了超类charBuffer, 意味着这两个特质只能混入继承了charBuffer的类中

上面每一个put方法都将修改过的消息传递给 super.put, 对于特质来说, super.put 调用的是特质层级的下一个特质(下面说), 
具体是哪一个根据特质添加的顺序来决定. 一般来说, 特质从最后一个开始被处理.

在特质中,由于继承的是抽象类,super调用时非法的。这里必须使用abstract override 这两个关键字,
在这里表示特质要求它们混入的对象(或者实现它们的类)具备 put 的具体实现, 这种定义仅在特质定义中使用。

混入的顺序很重要,越靠近右侧的特质越先起作用。当你调用带混入的类的方法时,最右侧特质的方法首先被调用。
如果那个方法调用了super,它调用其左侧特质的方法,以此类推。

如果要控制具体哪个特质的方法被调用, 则可以在方括号中给出名称: super[超类].put(...), 
这里给出的必须是直接超类型, 无法使用继承层级中更远的特质或者类; 
不过在本例中不行, 由于两个特质的超类是抽象类, 没有具体方法, 编译器报错


5. 特质的构造顺序

特质也可以有构造器,由字段的初始化和其他特质体中的语句构成。这些语句在任何混入该特质的对象在构造时都会被执行。
构造器的执行顺序：

调用超类的构造器；
特质构造器在超类构造器之后、类构造器之前执行；
特质由左到右被构造；
每个特质当中,父特质先被构造；
如果多个特质共有一个父特质,父特质不会被重复构造
所有特质被构造完毕,子类被构造。





//////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////

////定义变量
//推荐val,更符合函数式编程
val msg : String = "hello world"
val msg = "hello world"  //类型推断
var msg = "hello world"  //可重新赋值


////定义函数

def max(x:Int,y:Int):Int = if x>y then x else y
def max(x:Int,y:Int) = if x>y then x else y  //可以自动推断出返回结果类型

def greet():Unit  = println("hello world")
def greet()  = println("hello world")

////注释
//  kind 1

/* kind 2

*/
package examples
import java.io.{BufferedWriter, File, FileWriter}
import scala.io.Source
import scala.collection.mutable.ArrayBuffer

object eval extends App {

  val matrix = Array.ofDim[Double](3,25)
  val arr = Array.ofDim[Double](3,25)
  var vAnswers = ArrayBuffer[Double]()
  var veval = Array.ofDim[Double](3,25)
  var test2 = ArrayBuffer[Double]()
  var finalUtilityArray = ArrayBuffer[Double]()
  var tempOverhead = ArrayBuffer[Double]()
  var idle = ArrayBuffer[Double](3195, 0.15)
  var tempNonP = Array.ofDim[Double](3,2)

  for(i <- 0 to 9) {
    val fileName = "/home/chorus_eval/src/main/scala/examples/small/privateResult" + i + ".txt"
    val fileName2 = "/home/chorus_eval/src/main/scala/examples/nonPrivate/small/nonPrivateResult" + i + ".txt"

      var j = 0
      var m = 0

    val bufferedSource = Source.fromFile(fileName)
    for (line <- bufferedSource.getLines) {           //fill matrix with private values
      matrix(i)(j) = line.toDouble
      if(j < 24){
        j += 1
      }
    }
    bufferedSource.close
    val bufferedSource2 = Source.fromFile(fileName2)
    for (line <- bufferedSource2.getLines) {            //fill matrix with non-private values
      arr(i)(m) = line.toDouble
      if(m < 24){
        m += 1
      }
    }
    bufferedSource2.close
  }
  for(k <- 0 to 9){               //accuracy of private
    for(l <- 0 to 22){
      var temp = (matrix(k)(l) - arr(k)(l))
      var temp2 = (1 - (temp/arr(k)(l)).abs)
      veval(k)(l) = temp2
    }
    var s = 0
    for(r <- 23 to 24){                     // calc overhead of both private and non-private
      veval(k)(r) =   (matrix(k)(r)/idle(s))
      tempNonP(k)(s) =  (arr(k)(r)/idle(s))         //average cpu and memory of non-private divided by idle              //(tempMem2/idle(s))
      s = 1
    }
  }
  for(p <- 0 to 24){
    var sumTempUtility = 0.0
    var sumTempOverhead = 0.0        // average accuracy of each query after run 10 times
    for (q <- 0 to 9){
      sumTempUtility = (sumTempUtility + veval(q)(p))
      if(p < 2){
        sumTempOverhead = (sumTempOverhead + tempNonP(q)(p))
      }
    }
    if(p < 2){
      tempOverhead += (sumTempOverhead/10)
    }
    finalUtilityArray += (sumTempUtility/10)

  }
  val finalResArray = finalUtilityArray ++ tempOverhead


  var writeArray = ArrayBuffer[String]()
  finalResArray.foreach(res => writeArray += res.toString)

  writeFile("/home/chorus_eval/src/main/scala/examples/Results/small/Result.txt", writeArray)

  def writeFile(filename: String, lines: Seq[String]): Unit = {
    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))
    for (line <- lines) {
      bw.write(line)
      bw.newLine()
    }
    bw.close()
  }
}



package examples
import scala.io.Source
import java.sql.{Connection, DriverManager}
import java.io._
import scala.collection.mutable.ArrayBuffer
import java.lang.management._


object NonPrivacy extends App {
  var connection: Connection = _

    val MB = 1024 * 1024
    val runtime = Runtime.getRuntime
    val startUsedMem = (runtime.totalMemory - runtime.freeMemory) / MB

  for(k <- 0 to 9) {
    var vAnswers = ArrayBuffer[String]()
    var cpuAvg = 0.0
    val url = "jdbc:mysql://localhost:3306/DATABASE_NAME"
    val username = ""
    val password = ""
    var answer = ""

    try {

      connection = DriverManager.getConnection(url, username, password)
      val statement = connection.createStatement


      for (i <- 1 to 29) {
        val osBean = ManagementFactory.getOperatingSystemMXBean
        cpuAvg += osBean.getSystemLoadAverage

        val filename = "query-" + String.valueOf(i) + ".sql"
        var file = Source.fromFile("/home/2.18.0_rc2/dbgen/" + filename).getLines.mkString
        val toRemove = ";".toSet
        file = file.filterNot(toRemove)

        var colName = file.substring(file.indexOf("t") + 1, file.indexOf("f"))
        colName = colName.trim()
        if (i == 8 || i == 14 || i == 16 || i == 21 || i == 22 || i == 26) {//Chorus can not deal with certain join operations
                                                                            //and query 8 takes to much tame and memory likely
                                                                            //likely due to changes made to the query
        } else {
          val rs = statement.executeQuery(file)
          while (rs.next) {
            answer = rs.getString(colName)
            vAnswers += answer
          }
        }
      }
    } catch {
      case e: Exception => e.printStackTrace
    }
    connection.close

    val endUsedMem = (runtime.totalMemory - runtime.freeMemory) / MB
    vAnswers += (endUsedMem - startUsedMem).toString
    vAnswers += (cpuAvg / 29).toString

    writeFile("/home/chorus_eval/src/main/scala/examples/nonPrivate/nonPrivateResult" + k + ".txt", vAnswers)
    println(k)
    }
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
package examples


import chorus.schema.Schema
import chorus.util.ElasticSensitivity
import chorus.util.DB
import chorus.schema.Schema
import chorus.sql.QueryParser
import chorus.mechanisms.LaplaceMechClipping
import chorus.mechanisms.AverageMechClipping
import chorus.mechanisms.EpsilonCompositionAccountant
import chorus.rewriting.RewriterConfig

import java.io.{BufferedWriter, File, FileWriter}
import java.lang.management.ManagementFactory
import scala.io.Source
import java.sql.{Connection, DriverManager}
import scala.collection.mutable.ArrayBuffer


object test extends App {
  var connection:Connection = _
  val MB = 1024 * 1024
  val runtime = Runtime.getRuntime
  val startUsedMem = (runtime.totalMemory - runtime.freeMemory) / MB
    for (j <- 0 to 9) {
      var vAnswers = ArrayBuffer[String]()
      var cpuAvg = 0.0

      System.setProperty("schema.config.path", "src/test/resources/schema.yaml")
      System.setProperty("dp.elastic_sensitivity.check_bins_for_release", "false")
      System.setProperty("db.use_dummy_database", "false")

      val database = Schema.getDatabase("DATABASE_NAME")
      val config = new RewriterConfig(database)

      val url = "jdbc:mysql://localhost:3306/DATABASE_NAME"
      val username = ""
      val password = ""
      var answer = ""


      try {

        connection = DriverManager.getConnection(url, username, password)
        val statement = connection.createStatement
        val EPSILON = 0.01
        val denominator = math.pow(100000000, 2)
        val DELTA = 1 / denominator
        val accountant = new EpsilonCompositionAccountant()

        DB.connection = connection

        for (i <- 1 to 29) {

          val osBean = ManagementFactory.getOperatingSystemMXBean
          cpuAvg += osBean.getSystemLoadAverage

          val filename = "query-" + String.valueOf(i) + ".sql"
          var file = Source.fromFile("/home/2.18.0_rc2/dbgen/queries2/" + filename).getLines.mkString
          val toRemove = ";".toSet
          file = file.filterNot(toRemove)

          var colName = file.substring(file.indexOf("t") + 1, file.indexOf("f"))
          colName = colName.trim()

          if (i == 4 || i == 13 || i == 24) {

            val rs = statement.executeQuery(file)
            while (rs.next) {
              answer = rs.getString(colName)
            }
            val answerDouble = answer.toDouble
            val noisyResult = ElasticSensitivity.addNoise(file, database, answerDouble, EPSILON, DELTA)

            vAnswers += noisyResult.toString()
          } else if (i == 27 || i == 28 || i == 29) {

            val rs = statement.executeQuery(file)
            while (rs.next) {
              answer = rs.getString(colName)
            }
            val root2 = QueryParser.parseToRelTree(file, database)
            val r2 = new AverageMechClipping(EPSILON, 0, answer.toDouble, root2, config).execute(accountant)

            var resString = r2.mkString
            resString = resString.substring(resString.lastIndexOf('(')+1, resString.indexOf(')'))

            val resDouble = resString.toDouble
            val tempAbs = resDouble.abs
            resString = tempAbs.toString

            vAnswers += resString
          } else if (i == 8 || i == 14 || i == 16 || i == 21 || i == 22 || i == 26) { //Chorus can not deal with certain join operations
                                                                                      //and query 8 takes to much tame and memory likely
                                                                                      //likely due to changes made to the query
          } else {
            val rs = statement.executeQuery(file)
            while (rs.next) {
              answer = rs.getString(colName)
            }
            val root1 = QueryParser.parseToRelTree(file, database)
            val r1 = new LaplaceMechClipping(EPSILON, 0, answer.toDouble, root1, config).execute(accountant)

            var resString = r1.mkString
            resString = resString.substring(resString.lastIndexOf('(')+1, resString.indexOf(')'))

            val resDouble = resString.toDouble
            val tempAbs = resDouble.abs
            resString = tempAbs.toString

            vAnswers += resString
          }
        }

      } catch {
        case e: Exception => e.printStackTrace()
      }
      connection.close

      val endUsedMem = (runtime.totalMemory - runtime.freeMemory) / MB
      vAnswers += (endUsedMem - startUsedMem).toString()
      vAnswers += (cpuAvg / 29).toString()

      writeFile("/home/chorus_eval/src/main/scala/examples/private/privateResult" + j + ".txt", vAnswers)
      println(j)
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
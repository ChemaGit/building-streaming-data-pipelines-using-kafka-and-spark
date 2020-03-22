package finaldemostreamingdatapipelines

import java.io._
import java.text.SimpleDateFormat
import java.util.Date

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Simulator {
  //{"data": {"deviceId": "11c1310e-c0c2-461b-a4eb-f6bf8da2d23a","temperature": 3,"location": {"latitude": "52.14691120000001","longitude": "11.658838699999933"},"time": "1509793235"}}

  def main(args: Array[String]): Unit = {
    val logger: Logger = LoggerFactory.getLogger(Simulator.getClass.getName)

    val fich = "/home/cloudera/files/device.json"
    writeFile(fich,logger)
  }

  def writeFile(file: String, log: Logger): Unit ={
    while(true) {
      val (deviceId,temperature,latitude,longitude,time) = generateRandomValues()
      val date = new Date(time.toLong)
      val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(date)
      log.info(format)
      val data = s"""{"data": {"deviceId": "$deviceId","temperature": $temperature,"location": {"latitude": "$latitude","longitude": "$longitude"},"time": "$time"}}"""
      val f = new File(file)
      val fw = new FileWriter(f, true)
      try {
        fw.write(data)
        fw.write("\n")
      } catch {
        case ex: FileNotFoundException => println("Missing file exception: " + ex.getMessage)
        case ex: IOException => println("IO Exception: " + ex.getMessage)
      }finally {
        fw.close()
        Thread.sleep(1000)
      }
    }
  }

  def generateRandomValues(): (String,String,String,String,String) = {
    val r = scala.util.Random
    val abc =  (r.nextInt(3) + 97).toChar.toString
    val deviceId = s"11c1310e-c0c2-461b-a4eb-f6bf8da2d23$abc"
    val temperature = r.nextInt(40).toString
    val latitude = (r.nextDouble() * 100).toString
    val longitude = (r.nextDouble() * 100).toString
    val start = 1509793235L
    val time = (r.nextInt(Integer.MAX_VALUE) + start).toString

    (deviceId,temperature,latitude,longitude,time)
  }
}

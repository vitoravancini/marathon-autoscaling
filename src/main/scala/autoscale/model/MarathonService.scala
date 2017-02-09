package autoscale.model

import autoscale.model.MarathonService.TaskWithStats
import org.json4s.FieldSerializer._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization

import scalaj.http.{Http, HttpResponse}

/**
  * Created by vitorpaulonavancini on 03/02/17.
  *
  * Represents the marathon rest api
  */

trait MarathonServiceTrait {
  def fetchAppsForScale():Seq[MarathonApp]
  def getAppTasks(appId: String): Seq[TaskWithStats]
  def getTasksStats(task: Task): (TaskStats, TaskStats)
  def scaleApp(appId:String, scalePolicy:Int)
}

case class MarathonResponse(id:String, labels: Map[String, String])

object MarathonService extends MarathonServiceTrait {
  def scaleApp(appId:String, scalePolicy: Int):Unit = println("scaling!");

  val AUTO_SCALE_LABEL: String = "autoscale"
  val MARATHON_HOST = "***REMOVED***"

  def fetchAppsForScale():Seq[MarathonApp] = {
    implicit val formats = DefaultFormats
    implicit val marathonSource = this

    val response: HttpResponse[String] = Http(s"$MARATHON_HOST/v2/apps").asString
    val responseJson: JValue = parse(response.body)
    val appsJson: JValue = responseJson \ "apps"
    val marathonApps: Seq[MarathonResponse] = Serialization.read[Seq[MarathonResponse]](Serialization.write(appsJson))

   marathonApps.filter(_.labels.contains(AUTO_SCALE_LABEL)).map((marathonApp: MarathonResponse) => {
      MarathonApp(marathonApp.id, getAppTasks(marathonApp.id))
    })
  }

  def getTasksStats(task: Task): (TaskStats, TaskStats) = {
    implicit val marathonSource = this
    implicit val formats = DefaultFormats +
      TaskStats.renameTaskStats +
      Statistics.taskStatisticsRename

    val host = task.host
    val response: HttpResponse[String] = Http(s"http://$host:5051/monitor/statistics.json").header("Cache-Control", "no-cache").asString
    val allStats: Seq[TaskStats] = Serialization.read[Seq[TaskStats]](response.body)

    Thread.sleep(1000)
    val response2: HttpResponse[String] = Http(s"http://$host:5051/monitor/statistics.json").header("Cache-Control", "no-cache").asString
    val allStats2: Seq[TaskStats] = Serialization.read[Seq[TaskStats]](response2.body)

//   Returning two samples to be able to calculate cpu time usage
    (allStats.filter(_.executorId == task.id).head, allStats2.filter(_.executorId == task.id).head)
  }

  case class TaskWithStats(id:String, stats:(TaskStats,TaskStats))
  case class AppTasks(tasks:Seq[Task])
  def getAppTasks(appId: String): Seq[TaskWithStats] = {
    implicit val formats = DefaultFormats
    implicit val marathonSource = this

    val response: HttpResponse[String] = Http(s"${MarathonService.MARATHON_HOST}/v2/apps/$appId").asString
    val responseJson: JValue = parse(response.body)
    val appJson: JValue = responseJson \ "app"
    val appTasks: AppTasks = Serialization.read[AppTasks](Serialization.write(appJson))
    appTasks.tasks.map((task:Task) =>{
      TaskWithStats(task.id, getTasksStats(task))
    })
  }

  def scaleApp():Unit = ???

}

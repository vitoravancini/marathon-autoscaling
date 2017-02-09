package autoscale.model

import org.json4s.FieldSerializer
import org.json4s.FieldSerializer._

/**
  * Created by vitorpaulonavancini on 08/02/17.
  */

case class Task(id:String, host:String)

case class TaskStats(executorId:String, statistics: Statistics)
object TaskStats {
  val renameTaskStats = FieldSerializer[TaskStats](
    renameTo("executor_id", "executorId"),
    renameFrom("executor_id", "executorId")
  )
}

case class Statistics(cpusSystemTimeSecs: Float,
                      cpusLimit:Float,
                      cpusUserTimeSecs:Float,
                      memLimitBytes:Float,
                      memRssBytes:Float,
                      timestamp: Double)

object Statistics{
  val taskStatisticsRename = FieldSerializer[Statistics](
    renameTo("cpus_system_time_secs", "cpusSystemTimeSecs") orElse
      renameTo("cpus_limit", "cpusLimit") orElse
      renameTo("cpus_user_time_secs", "cpusUserTimeSecs") orElse
      renameTo("mem_limit_bytes", "memLimitBytes") orElse
      renameTo("mem_rss_bytes", "memRssBytes")
    ,

    renameFrom("cpus_system_time_secs", "cpusSystemTimeSecs") orElse
      renameFrom("cpus_limit", "cpusLimit") orElse
      renameFrom("cpus_user_time_secs", "cpusUserTimeSecs") orElse
      renameFrom("mem_limit_bytes", "memLimitBytes") orElse
      renameFrom("mem_rss_bytes", "memRssBytes")
  )
}

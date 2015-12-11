package dominus.scala.examples

//[26.7 Regular expressions]
object x_3_regex {
  def main(args: Array[String]) {

    //Note: Regex raw string
    //simply append a .r to a string to obtain a regular expression
    val Decimal =
      """(-)?(\d+)(\.\d*)?""".r

    //TODO: Searching for regular expressions


    //NOTE: Extracting with regular expressions
    val Decimal(sign, integerpart, decimalpart) = "-1.23"
    println(sign, integerpart, decimalpart)

    val auditLog = "2015-12-02 11:01:09,679 INFO org.apache.hadoop.yarn.server.resourcemanager.RMAuditLogger: USER=royan     OPERATION=AM Allocated Container        TARGET=SchedulerApp     RESULT=SUCCESS  APPID=application_1448965205762_0121    CONTAINERID=container_e105_1448965205762_0121_01_000002"
    //val auditRegex = """(.*)\sINFO.*USER=(\w*)\s*OPERATION=(.*\w)\s*TARGET.*""".r
    val auditRegex =
      """(.*)\sINFO.*USER=(\w*)\s*.*""".r
    val auditRegex(timestamp, user) = auditLog
    println(timestamp, user)
  }

}

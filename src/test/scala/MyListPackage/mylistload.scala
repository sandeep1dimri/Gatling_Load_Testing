import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.util.Random

class MyListLoad extends Simulation {

  val httpProtocol = http
    .baseUrl("http://127.0.0.1:5000")
    .inferHtmlResources()
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate")

  var myDynamicValue = Random.between(20, 30)

  val headers_with_api_key = Map(
    "API-Key" -> "FAse-rSueh9zEJuK8h-zW35grEddgqcZ",
    "Content-Type" -> "application/json")

  // Data feeders
  val customSeparatorFeeder = separatedValues("data/input_tasks.txt", '|').circular // use your own separator
  //val csvFeeder = csv("data/taskdata.csv").circular // use a comma separator

  val scn = scenario("MyList")
    // getkey
    .exec(
      http("Get Home Page")
        .get("/")
        //.headers(headers_0)
        .check(jsonPath("$.api_key").optional.saveAs("p-api_key"))

    )
    .pause(1)
    //.feed(csvFeeder)
    .feed(customSeparatorFeeder)
    // add a item
    .exec(
      http("Add a task")
        .post("/add_task")
        .headers(Map(
          "API-Key" -> "${p-api_key}","Content-Type" -> "application/json"))


        //.body(StringBody("""{ "task_title":"${task_title1}","is_done":"${is_done1}""""))//.asJson
        .body(StringBody("""{ "task_title":"${task_title}","is_done":${is_done}}""")).asJson
        .check(jsonPath("$.id").optional.saveAs("p_id"))
    ) // end of exe
    .pause(1)
    // select all
    .exec(
      http("get all tasks")
        .get("/get_all_tasks")
        .header(
          "API-Key" , "${p-api_key}")
    )
    .pause(1)
    // delete the item created
    .exec(http("delete the task")
      .delete("/tasks/delete/${p_id}")
      .header(
        "API-Key" , "${p-api_key}"))

  /*
  two options to run with ,
  1: run 10 users at same time,

  2: constantUsersPerSec(rate) during(duration): Injects users at a constant rate, defined in users per second, during a given duration. Users will be injected at regular intervals
   */
  setUp(scn.inject(atOnceUsers(5))).protocols(httpProtocol)
  //setUp(scn.inject(constantUsersPerSec(10) during(5 seconds))).protocols(httpProtocol)
}

/* run the below goal in maven
mvn gatling:test
 */
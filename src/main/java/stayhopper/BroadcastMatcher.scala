package stayhopper

import com.graphhopper.GraphHopper
import com.graphhopper.config.{CHProfile, Profile}
import com.graphhopper.matching.{MapMatching, MatchResult, Observation}
import com.graphhopper.routing.util.CarFlagEncoder
import com.graphhopper.util.PMap

import java.util
import scala.collection.JavaConverters._


object BroadcastMatcher {
  private var instance = null: MapMatching

  private def initialize(config: String) {
    if (instance != null) return
    this.synchronized {
      if (instance == null) { // initialize map matcher once per Executor (JVM process/cluster node)

        val hopper = new GraphHopper()
        hopper.setStoreOnFlush(false)
        hopper.setOSMFile(config)
        hopper.setGraphHopperLocation("hdfs://127.0.0.1:9000/hopper");
        hopper.setProfiles(new Profile("car").setVehicle("car").setWeighting("fastest").setTurnCosts(false))
        hopper.getCHPreparationHandler.setCHProfiles(new CHProfile("car"))

        val encoder = new CarFlagEncoder()

        hopper.importOrLoad

        val hints = new PMap
        hints.putObject("profile", "car")

        instance = new MapMatching(hopper, hints)
      }
    }
  }

  @SerialVersionUID(1L)
  class BroadcastMatcher(config: String) extends Serializable {

    def mmatch(samples: List[Observation]): MatchResult = {
      BroadcastMatcher.initialize(config)
      BroadcastMatcher.instance.`match`(new util.ArrayList[Observation](samples.asJava))
    }

  }

}

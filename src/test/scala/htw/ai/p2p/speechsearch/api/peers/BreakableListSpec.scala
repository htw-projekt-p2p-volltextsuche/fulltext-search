package htw.ai.p2p.speechsearch.api.peers

import cats.effect.IO
import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.api.peers.PeerClient._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class BreakableListSpec extends BaseShouldSpec {

  behavior of "A Breakable List"

  it should "return all failed mappings together with all remaining elements after break" in {
    val elements = List("success", "fail", "success", "break", "fail", "success")

    elements
      .traverseOrCancel[IO] {
        case "success" => IO(Success)
        case "fail"    => IO(Fail)
        case "break"   => IO(FatalFail)
      }
      .asserting {
        _ should have size 4
      }

  }

}

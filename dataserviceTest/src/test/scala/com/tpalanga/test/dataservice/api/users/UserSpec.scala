package com.tpalanga.test.dataservice.api.users

import akka.http.scaladsl.model.StatusCodes
import com.tpalanga.test.spec.RestSpec
import org.scalatest.{AsyncFlatSpec, Matchers}

class UserSpec extends AsyncFlatSpec with Matchers with RestSpec with DataserviceRestServiceClient {

  "Dataservice" should "return 404 if a user does not exist" in {
    userRetrieve("unknown").map { reply =>
      reply.status shouldBe StatusCodes.NotFound
    }
  }

}

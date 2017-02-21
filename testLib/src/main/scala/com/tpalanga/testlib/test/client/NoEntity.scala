package com.tpalanga.testlib.test.client

import spray.json.{JsNull, JsValue, RootJsonFormat}

object NoEntity {
  object DataFormats {
    implicit object NoEntityFormat extends RootJsonFormat[NoEntity] {
      override def read(json: JsValue) = NoEntity()
      override def write(entity: NoEntity) = JsNull
    }
  }
}

case class NoEntity()

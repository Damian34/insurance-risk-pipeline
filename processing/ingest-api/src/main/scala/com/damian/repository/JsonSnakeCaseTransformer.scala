package com.damian.repository

import io.circe.Json

class JsonSnakeCaseTransformer {

  def transformKeys(json: Json): Json =
    json.arrayOrObject(
      json,
      arr => Json.fromValues(arr.map(transformKeys)),
      obj => Json.fromFields(
        obj.toMap.map { case (k, v) =>
          toSnakeCase(k) -> transformKeys(v)
        }
      )
    )

  private def toSnakeCase(str: String): String =
    str
      .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
      .toLowerCase
    
}

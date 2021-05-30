package htw.ai.p2p.speechsearch.domain.mapreduce

object MapReduceOperations {
  def mapper[KeyIn, ValueIn, KeyMOut, ValueMOut](
      mapFun: (
          (KeyIn, ValueIn),
          Map[String, Any]
      ) => List[(KeyMOut, ValueMOut)],
      data: List[(KeyIn, ValueIn)],
      config: Map[String, Any]
  ): List[(KeyMOut, ValueMOut)] = data.flatMap(mapFun(_, config))

  def sorter[KeyMOut, ValueMOut](
      data: List[(KeyMOut, ValueMOut)]
  ): List[(KeyMOut, List[ValueMOut])] = data.groupMap(_._1)(_._2).toList

  def reducer[KeyMOut, ValueMOut, KeyROut, ValueROut](
      redFun: (
          (KeyMOut, List[ValueMOut]),
          Map[String, Any]
      ) => List[(KeyROut, ValueROut)],
      data: List[(KeyMOut, List[ValueMOut])],
      config: Map[String, Any]
  ): List[(KeyROut, ValueROut)] = {
    data.flatMap(redFun(_, config))
  }

  def mapReduce[KeyIn, ValueIn, KeyMOut, ValueMOut, KeyROut, ValueROut](
      mapFun: (
          (KeyIn, ValueIn),
          Map[String, Any]
      ) => List[(KeyMOut, ValueMOut)],
      redFun: (
          (KeyMOut, List[ValueMOut]),
          Map[String, Any]
      ) => List[(KeyROut, ValueROut)],
      data: List[(KeyIn, ValueIn)],
      config: Map[String, Any]
  ): List[(KeyROut, ValueROut)] =
    reducer(redFun, sorter(mapper(mapFun, data, config)), config)
}

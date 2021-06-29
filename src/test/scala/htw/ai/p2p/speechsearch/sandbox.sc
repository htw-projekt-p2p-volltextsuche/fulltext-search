

//def process(word: String)(regions: Regions): String = ???

//(process andThen process)("hi", Regions(Region()))

case class Regions(x: Int)

val r = Regions(1)

case class Step(x: String) {
  def process(word: String, regions: Regions): String = word + x + regions
}

val Step1 = Step("one")
val Step2 = Step("two")

val steps = List(Step1, Step2)

def x(w: String, regions: Regions): String =
  steps.foldLeft(w)((x, s) => s.process(x, regions))

def y(w: String) = w.toUpperCase

y compose { case (w: String, reg: Regions) => x(w, reg) }

y compose ((k: (String, Regions)) => x(k._1, k._2))

(x _).tupled andThen y

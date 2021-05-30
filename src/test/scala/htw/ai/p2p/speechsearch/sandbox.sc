case class Safe[+T](private val internal: T) {
  def get: T =
    synchronized {
      // smthg interesting..
      internal
    }
}

// external API
def mkSafe[T](value: T): Safe[T] = Safe(value)

def safeString: Safe[String] = mkSafe("Monads")
// extract
val string = safeString.get
// transform
val upperString = string.toUpperCase

package es.richweb

object Solution {
  val morseMap = Map(
    'a' -> ".-",
    'b' -> "-...",
    'c' -> "-.-.",
    'd' -> "-..",
    'e' -> ".",
    'f' -> "..-.",
    'g' -> "--.",
    'h' -> "....",
    'i' -> "..",
    'j' -> ".---",
    'k' -> "-.-",
    'l' -> ".-..",
    'm' -> "--",
    'n' -> "-.",
    'o' -> "---",
    'p' -> ".--.",
    'q' -> "--.-",
    'r' -> ".-.",
    's' -> "...",
    't' -> "-",
    'u' -> "..-",
    'v' -> "...-",
    'w' -> ".--",
    'x' -> "-..-",
    'y' -> "-.--",
    'z' -> "--.."
  )

  def uniqueMorseRepresentations(words: Array[String]): Int = {
    words.map(word => word.map(morseMap(_)).mkString).distinct.size

    //    words.map((word: String) => {
    //      word.map((v: Char) => morseMap(v)).mkString("")
    //    }).distinct.size
  }

  def main(args: Array[String]): Unit = {
    val t0 = System.currentTimeMillis()
    println(Solution.uniqueMorseRepresentations(Array("gin", "zen", "gig", "msg")))
    val t1 = System.currentTimeMillis()
    println(s"Completed in ${t1 - t0} ms")
  }
}

package main

import util.parsing.combinator.RegexParsers

object TestIndentHandler{
  def main(args: Array[String]) {
    
    println ("Hello World")
    println("========================")
    println(input)
    println("========================")
    println(IndentHandler.annotate(input))
    println("========================")
  }
  val input =
    """div.
      |    h1. This div
      |
      |    has some stuff
      |
      |    p.  inside with
      |        more stuff
      |
      |    p.
      |        inside several
      |        nested paragraphs
      |        a.  Some super nested text
      |            link.
      |                more nesting
      |        a.
      |            more nested text
      |        and
      |
      |    some unstructured text
      |
      |and is followed by more text
      |which sits outside
      |
      |in a few chunks
      |with another
      |ol.
      |    li. There is some stuff here
      |    li. There is more stuff here
      |    li. There stuff more is here""".stripMargin
}


object IndentHandler{
  
  def indent = ">"//64976
  def dedent = "<"//64977

  def annotate(input: String) = {

    val lines = "" :: input.lines.toList ::: List("")
    val tails = lines.tails.takeWhile(_ != Nil) 
    
    def getIndent(rest: List[String]): Int = rest match {
      case Nil => 0
      case x if x.head.trim() != "" => x.head.takeWhile(_ == ' ').length
      case _ => getIndent(rest.tail)
    }

    val linesOut = tails.foldLeft((0 :: Nil, Seq[String]())){ (x, tail) =>
      val (stack, text) = x
      val current :: next = tail
      
      val nextIndent = getIndent(next)
      
      val delta = if (nextIndent > stack.head) 1
                  else -stack.takeWhile(_ > nextIndent).length

      val newText = if (delta > 0) current.takeWhile(_ == ' ' ) + indent + current.dropWhile(_ == ' ' ) 
                    else current + dedent * -delta
      
      val newStack = if (delta > 0) nextIndent :: stack
                     else stack.drop(-delta)
      
      (newStack, text :+ newText)
    }
    
    linesOut._2.mkString("\n")
  }
}

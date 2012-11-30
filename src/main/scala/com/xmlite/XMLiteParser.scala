package main

import scala.util.parsing.combinator._
import scala.util.parsing.input._
import scala.xml._

object XMLiteDefaults{
   
  case class ArgSpec(
    open: String = "[",
    close: String = "]",
    prefix: String = "arg",
    separator: String = ",",
    kvSeparator: String = "="
  )

  val shortcuts = Seq(
    "*" -> <b/>,    "__" -> <u/>,
    "_" -> <i/>,    "--" -> <strike/>,
    "^" -> <sup/>,  "~" -> <sub/>,
    "`" -> <code/>
  )
}

object DefaultXMLiteParser extends XMLiteParser()

case class XMLiteParser(
  listSpec: (String, scala.xml.Elem) = ("-", <li/>),
  argSpec: XMLiteDefaults.ArgSpec = XMLiteDefaults.ArgSpec(),
  shortcutSpec: Seq[(String, scala.xml.Elem)] = XMLiteDefaults.shortcuts,
  entityStart: String = "&",
  openInline: String = "{",
  closeInline: String = "}",
  autoChunkers: (String, scala.xml.Elem, scala.xml.Elem) = ("..", <p/>, <ul/>),
  rootChunker: Option[(scala.xml.Elem, scala.xml.Elem)] = Some(<p/>, <ul/>),
  blockStub: String = ".",
  rawStub: String = ":",
  procInstrStub: String = "?",
  escapeChar: String = "\\",
  newLine: String = "\n",
  chainTagSep: String = ".",
  logging: Boolean = true) extends RegexParsers{

  override def skipWhitespace = false
  var indentLevel = 0;
  implicit def LogString(s: String) = LogParser[String](s)

  implicit class LogParser[A](p: => Parser[A]){
    def xlog(name: String) = Parser{ in =>
      val l = math.abs(util.Random.nextLong()) % 10000
      println("    ".*(indentLevel) + l + " trying "+ name +" at <"+ in.source.toString.drop(in.offset).take(20).replace('\n', '|') + ">")
      indentLevel += 1
      val r = p(in)
      indentLevel -= 1
      println("    ".*(indentLevel) + l + " " + name +" --> "+ r)
      r
    }
  }

  def parse(input: String): Node = {
    
    val nodes = this.parseAll(
      entirePage,
      IndentHandler.annotate(input)
    ).get

    Elem(null, "root", Null, TopScope, true, nodes:_*)
  }


  val entirePage = textOr(
    rootAutoList | outsideChunk | rootAutoChunks | insideChunk,
    tail=dedent
  )
  
  lazy val (indent, dedent) = (IndentHandler.indent, IndentHandler.dedent)


  def nameChar = {
    def nameStartChar =
      """:A-Z_a-z\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u02FF\u0370-\u037D""" +
      """\u037F-\u1FFF\u200C-\u200D\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF""" +
      """\uF900-\uFDCF\uFDF0-\uFFFD"""

    ("[" + nameStartChar + """\-\.0-9\u00B7\u0300-\u036F\u203F-\u2040]""").r
  }

  def entityChars = "[0-9a-zA-Z#]+".r

  def escapedGroup = escapedGroupRaw.map(x => if(x == " ") Seq() else Seq(Text(x)))

  def escapedGroupRaw = escapeChar ~> char

  def anything = insideChunk | outsideChunk

  def insideChunk = entity | procInline | inlineElem | shortcut(failure(""))
  def outsideChunk = procBlock | rawBlock | chunkBlock | blockElem | listItem

  def blockContent(content: Parser[Seq[Node]]) = (x: Parser[Any]) => textOr(content, tail = x | dedent)

  def rootAutoChunks = rootChunker.map(e => autoChunk(e._1)).getOrElse(failure("NoRootAutoChunk"))
  def rootAutoList = rootChunker.map(e => autoList(e._2)).getOrElse(failure("NoRootAutoChunk"))

  def autoChunk(chunkElem: xml.Elem): Parser[Seq[Node]] = {
    (blockLead ~ (not("\n" | "$".r) ~> textOr(shortcut("") | insideChunk, tail = guard(blankLine) | dedent)) ^^ {
      case (leadBlanks, pushBlanks, _, trailBlanks) ~ stuff => Seq(
        Text(leadBlanks),
        chunkElem.copy(child =
          Text(pushBlanks) +: stuff :+ Text(trailBlanks)
        )
      )
    })
  }
  
  def autoList(listElem: xml.Elem) = {
    def additionalItem = blankLine.*.map(_.flatten) ~ listItem ^^ {
      case x ~ y => x ++ y 
    }
    guard(blockLead) ~ rep1(listItem, additionalItem) ^^ {
      case (leadBlanks, pushBlanks, _, trailBlanks) ~ items => Seq(
        Text(leadBlanks),
        listElem.copy(child =
          items.flatten :+ Text(trailBlanks)
        )
      )
    } 
  }

  def blockElem: Parser[Seq[Node]] =
    block(
      namedHeadChain((blockStub: Parser[String]), explicit = false),
      blockContent(anything)
    )

  def rawBlock: Parser[Seq[Node]] = {
    def indentedArea: Parser[Seq[Node]] = indent ~> textOr(indentedArea, tail = dedent) <~ dedent
    block(
      namedHeadChain(rawStub),
      blockContent(textOr(indentedArea, tail = dedent))
    )
  }

  def chunkBlock: Parser[Seq[Node]] = {
    val (token, elem, listElem) = autoChunkers
    block(
      namedHeadChain(token),
      blockContent(autoList(listElem) | anything | autoChunk(elem))
    )
  }
  
  def namedHeadChain(token: Parser[String], explicit: Boolean=true) = {

    def singleHead(stub: Parser[String], explicit: Boolean) = {
      tagName ~ (args <~ (if(explicit) stub else success("")) | stub.^^^(List())) ^^ {
        case label ~ args =>
          args.reverse.foldLeft(Elem(null, label, Null, TopScope, true))(
            (e, k) => e % new UnprefixedAttribute(k._1, k._2, Null)
          )
      }
    }
    def ender = singleHead(token, explicit)
    
    (rep(singleHead(chainTagSep, explicit=false) <~ guard(ender))  ~ ender).map{
      case x ~ y => (z: Seq[Node], trailBlanks: String) => (x :+ y).foldRight(if(trailBlanks.length > 0)z :+ Text(trailBlanks) else z)(
        (a, b) => Seq(a.copy(child = b))
      )
    }
  }

  def procBlock: Parser[Seq[Node]] = {
    def indentedString(newline: Parser[Any] = failure("")): Parser[String] = 
      rep1(charsNot(indent | dedent | newline) | indent ~> indentedString(newline) <~ dedent).map(_.mkString)
    def possibleTagName = "?" ~> (tagName | "").map(x =>
      (c: String, b: String) => Seq(xml.ProcInstr(x, c + b))
    )
    block(
      possibleTagName, 
      indentedString _
    )
  }

  def procInline: Parser[Seq[Node]] = {
    (procInstrStub ~> tagName.? <~ openInline) ~ rep1(charsNot(closeInline | dedent | escapedGroupRaw) | escapedGroupRaw) <~ closeInline ^^ {
      case head ~ body => Seq(xml.ProcInstr(head.getOrElse(""), body.mkString))
    }
  }

  def blockLead = ("\\n *".r ^^ (x => (x, x, "", x)))
  
  def block[H](headMaker: Parser[(H, String) => Seq[Node]],
               content: Parser[Any] => Parser[H]) = {

    def single = blockLead ~ headMaker ~ " *".r ~ content("\n") ^^ {
      case (leadBlanks, _, _, _) ~ headM ~ blanks ~ children =>
        Text(leadBlanks) +: headM(children, "")
    }

    def multiple = blockLead ~ (indent ~> headMaker) ~ " *".r ~ content(failure("")) <~ dedent ^^ {
      case (leadBlanks, _, _, trailBlanks) ~ headM ~ blanks ~ children => 
        Text(leadBlanks) +: headM(children, trailBlanks) 
    }
    
    single | multiple
  }

  def listItem: Parser[Seq[Node]] = {
    val (token, elem) = listSpec
    val (chunkToken, chunkElem, listElem) = autoChunkers
    def listHeadMaker(marker: String) = marker.^^^((x: Seq[Node], trailBlanks: String) => Seq(elem.copy(child = x :+ Text(trailBlanks))))

    def plainListItem = block(
      listHeadMaker(token), 
      blockContent(anything)
    )
    def chunkingListItem = block(
      listHeadMaker(token + chunkToken), 
      blockContent(autoList(listElem) | anything | autoChunk(chunkElem))
    )
    
    chunkingListItem | plainListItem
  }

  def inlineElem: Parser[Seq[Node]] = {
    def openContent = textOr(inlineElem | shortcut() | entity,
      start = shortcut(""),
      tail = closeInline | dedent
    )
    def open: Parser[Seq[Node]] = namedHeadChain(openInline, explicit=true) ~  openContent <~ closeInline ^^ {
      case headGen ~ body => headGen(body, "")
    }
    def closed = namedHeadChain(failure(""), explicit=false).map(_(Seq(), ""))
    
    open | closed
  }
  
  def shortcut(start: Parser[String] = failure(""), end: Parser[String] = failure("")): Parser[Seq[Node]] = {

    def leftBound =  start | "\\ "^^^"" | " "| "\n" | ":" | "/" | "'" | "\"" | "<" | "(" | "[" | "{"
    def rightBound = end | "\\ " | dedent | " " | "\n" | "-" | "/" | "'" | "\"" | ">" | ")" | "]" | "}" | "." | "," | ":" | ";" | "!" | "?" | shortcutSpec.map(x => x._1: Parser[String]).reduce(_|_)

    def singleShortcut(token: String, tag: xml.Elem) = {
      def shortcutStart = token <~ guard(nonBlank) ^^^ tag
      def shortcutEnd = token <~ guard(rightBound)
      def bodyContent = textOr(
        anything,
        start = shortcut(""),
        end = shortcut(end = ""),
        tail = shortcutEnd | dedent,
        reps = 1
      )

      shortcutStart ~ bodyContent <~ shortcutEnd ^^ {
        case start ~ body => start.copy(child = body)
      }
    }
    
    leftBound ~ shortcutSpec.map((singleShortcut _).tupled(_)).reduce(_|_) ^^ {
      case left ~ rest => Seq(Text(left), rest)
    }
  }

  def entity = entityStart ~ openInline ~ entityChars.* ~ closeInline ^^ {
    case start ~ middle ~ end => Seq(EntityRef(middle.mkString))
  }
  
  def textOr(p: Parser[Seq[Node]],
             start: Parser[Seq[Node]] = failure(""),
             end: Parser[Seq[Node]] = failure(""),
             tail: Parser[Any] = failure("textOrNoTail"),
             reps: Int = 0) = {

    def textSection = charsNot(p | tail | escapedGroup).map(x => Seq(Text(x)))

    start.? ~ rep(not(tail) ~> (p | escapedGroup | textSection))  ~ end.? ^^ {
      case a ~ b ~ c => (a ++ b ++ c).flatten.toList
    } ^? {
      case list if list.length >= reps => list
    }
  }

  def charsNot(p: Parser[Any], filter: Parser[Any] = char): Parser[String] =
    rep1(not(p) ~> filter).map(_.mkString)

  def char = "\\s|\\S".r
  def blank = "\\s".r
  def nonBlank = "\\S".r

  def blankLine = ("\n *".r) <~ guard("$|\n".r) ^^ {x => Seq(Text(x))}
  def escapedArgText = charsNot(escapeChar | argSpec.close | argSpec.separator)

  def args = (argSpec.open ~> repsep(arg, argSpec.separator) <~ argSpec.close).map{
    _.zipWithIndex.map {
      case ((Some(k), v), index) => k -> v
      case ((None, v), index) => argSpec.prefix + index -> v
    }
  }

  def arg = ("\\s*".r ~> argName <~ "=").? ~ rep(escapedArgText | escapedGroupRaw) ^^ {
    case nameMaybe ~ value => nameMaybe -> value.foldLeft("")(_+_)
  }

  def argName = charsNot(" " | "," | argSpec.kvSeparator, nameChar)
  def tagName = charsNot(" " | newLine | escapeChar | argSpec.open | argSpec.close | openInline | closeInline | blockStub | rawStub, nameChar)
}
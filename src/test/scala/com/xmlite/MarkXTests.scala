package com.xmlite

import io.Source

import main.DefaultMarkXParser
import org.scalatest._


class Tests extends FreeSpec with ShouldMatchers with ParallelTestExecution {

  def load(path: String) = {
    println("Loading: " + path)
    Source.fromURL(getClass.getResource("/" + path))
      .getLines().reduce(_ + "\n" + _)
      .split("\n?================================================================\\d*\n?")
      .drop(1).grouped(2).map(x => (x(0), x(1))).toList
  }
  def parse(s: String) = {

    val parser = new xml.parsing.ConstructingParser(Source.fromString(s), true) {
      override def replacementText(entityName: String): io.Source = super.replacementText(entityName);
      nextch;
    }
    parser.element(xml.TopScope);
  }
  def test(data: (String, String)) = {
    val (input, output) = data
    val expectedOutput = parse("<root>\n" + output + "\n</root>")
    val parsedOutput = parse(DefaultMarkXParser.parse(input).toString())
    println("====================EXPECTED====================")
    println(expectedOutput)
    println("====================PARSED====================")
    println(parsedOutput)
    assert(parse(parsedOutput.toString) == parse(expectedOutput.toString))

  }
  
  "All Tests" - {
    "block tests" - {
      val data = load("blocks.txt")
      "autoblock of plain text" in test(data(0))
      "Single-line block with single autoblock" in test(data(1))
      "simple multiline block" in test(data(2))
      "long multiline block with a blank line" in test(data(3))
      "multiline block with text on first line" in test(data(4))
      "multiline block with text after" in test(data(5))
      "complex series of nested blocks" in test(data(6))
      "escaping of block delimiters with backslashes" in test(data(7))
      "escaping of backslashes" in test(data(8))
      "multi-tag block" in test(data(9))
    }
    "entity tests" - {
      val data = load("entities.txt")
      "basic entity works and html-style entity fails" in test(data(0))
      "multiple entities chained together" in test(data(1))
      "numeric entities" in test(data(2))
      "ensuring entities can't contain spaces" in test(data(3))
      "entities inside an inline tag" in test(data(4))
      "escaping entities" in test(data(5))
    }
    "inline tests" - {
      val data = load("inline.txt")
      "simple inline tags" in test(data(0))
      "inline tags spread over multiple lines" in test(data(1))
      "inline tags within a block tag" in test(data(2))
      "complex inline tags inside block tags" in test(data(3))
      "escaping of inline tags" in test(data(4))
      "more escaping of inline tags" in test(data(5))
      "inline tags in the middle of a word" in test(data(6))
      "inline tags inside another inline" in test(data(7))
    }

    "attribute tests" - {
      val data = load("attributes.txt")
      "simple attributes" in test(data(0))
      "attributes on a block tag" in test(data(1))
      "auto-numbered nameless attributes" in test(data(2))
      "attributes on inline tags" in test(data(3))
      "spreading attributes over multiple lines" in test(data(4))
      "escaping delimiters inside attributes" in test(data(5))
      "attributes on separate lines" in test(data(6))
    }

    "processing instruction tests" - { 
      val data = load("procinsts.txt")
      "singleline block procinsts" in test(data(0))
      "inline procinsts" in test(data(1))
      "both singleline block and inline procinsts" in test(data(2))
      "multiline block procinsts" in test(data(3))
      "multiply indented multiline block procinsts" in test(data(4))
      "escaping procinsts" in test(data(5))
    }

    "raw block tests" - { val data = load("rawblocks.txt")
      "simple multiline raw block" in test(data(0))
      "more complex raw block, ensuring nothing gets parsed" in test(data(1))
      "multi-tag raw blocks" in test(data(2))
    }

    "list tests" - { val data = load("lists.txt")
      "short list with one item each, autochunked" in test(data(0))
      "short list nested in a tag" in test(data(1))
      "lists nested within each other" in test(data(2))
      "numbered lists with nesting" in test(data(3))
      "lists with blank lines in between" in test(data(4))
      "escaping lists" in test(data(5))
    }

    "shortcut tests" - { val data = load("shortcuts.txt")
      "single bold word" in test(data(0))
      "entire range of shortcuts" in test(data(1))
      "shortcuts over multiple lines but not multiple blocks" in test(data(2))
      "reStructuredText shortcut behavior" in test(data(3))
      "shortcuts inside a long word" in test(data(4))
      "more shortcuts inside a long word" in test(data(5))
      "shortcuts immediately inside inline block" -{
        "left" in test(data(6))
        "right" in test(data(7))
        "tight" in test(data(8))
        "open" in test(data(9))
      }
      "shortcuts within shortcuts" in test(data(10))

    }

    "autochunk tests" - { val data = load("autochunk.txt")
      "several paragraphs being chunked" in test(data(0))
      "chunked paragraph with explicit multiline block paragraph" in test(data(1))
      "auto-chunking inside divs" in test(data(2))
      "nested autochunking divs" in test(data(3))
      "autochunking lists" in test(data(4))
      "escaping autochunks" in test(data(5))
      "bold starting autochunk" in test(data(6))
    }

    "sample tests" - { val data = load("samples.txt")
      "dropbox developer reference" in test(data(0))
      "wikipedia snippet" in test(data(1))
      "simple Ant build.xml file" in test(data(2))
      "microsoft excel spreadsheet xml" in test(data(3))

    }
  }
}



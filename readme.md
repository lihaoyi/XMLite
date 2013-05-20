Xmlite
======

Xmlite is a lightweight markup language that compiles to XML. It's goal is to offer a level of brevity/clarity similar to [Markdown](http://en.wikipedia.org/wiki/Markdown) and [Textile](http://redcloth.org/textile), but with extensible semantics and a more regular syntax. It turns Xmlite text that looks like this:


    h1. Cow

    img[src=http://upload.wikimedia.org/wikipedia/commons/f/fb/CH_cow_2.jpg]

    *Cattle* (colloquially cows) are the most common type
    of a[href=http://en.wikipedia.org/wiki/Domestication]{domesticated}
    large a[href=ungulates]{http://en.wikipedia.org/wiki/Ungulates}. They are a
    prominent modern member of the subfamily em{Bovinae}.

    div[class=box]..

       Cattle are raised as livestock for

       - meat (beef and veal)
       - as dairy animals
       - for milk and other dairy products
       - as draft animals (pulling carts, plows and the like)

    Other products include leather and dung for manure or fuel. In some countries,
    such as a[href=http://en.wikipedia.org/wiki/India]{India}, cattle are
    sacred.

into

    <h1>Cow</h1>

    <img src="http://upload.wikimedia.org/wikipedia/commons/f/fb/CH_cow_2.jpg"/>

    <p>
	<b>Cattle</b> (colloquially cows) are the most common type
    of <a href="http://en.wikipedia.org/wiki/Domestication">domesticated</a>
    large <a href="ungulates">http://en.wikipedia.org/wiki/Ungulates</a>. They are a
    prominent modern member of the subfamily <em>Bovinae</em>.
    </p>

    <div class="box">

        <p>
        Cattle are raised as livestock for
        </p>

        <ul>
        <li>meat (beef and veal)</li>
        <li>as dairy animals</li>
        <li>for milk and other dairy products</li>
        <li>as draft animals (pulling carts, plows and the like)</li>
        </ul>
    </div>

    <p>
    Other products include leather and dung for manure or fuel. In some countries,
    such as <a href="http://en.wikipedia.org/wiki/India">India</a>, cattle are
    sacred.
    </p>

Xmlite maps directly to XML, and can be thought of as the minimal viable XML syntax. All the commonly used functionality and features of XML are still present

Regular Syntax
==============
Unlike the vast majority of lightweight markup languages out there, Xmlite has an extremely regular syntax: apart from a (small) number of special-form shorthands (e.g. *bold*, _italic_, etc.) the vast majority of markup is done using a few forms:

Block Tags
----------

    h1. Single-line block
    <h1>Single-line block</h1>

    h1.
        Multi-line block
        has multiple lines
    <h1>
        Multi-line block
        has multiple lines
    </h1>

    div..
        auto-chunked block groups

        body into paragraphs automatically
    <div>
        <p>
        auto-chunked block groups
        </p>
        <p>
        body into paragraphs automatically
        </p>
    </div>

Block tags are a simple way of grouping markup into a hierarchical structure. They convert directly into XML, but are whitespace-delimited and far less verbose.

Block tags can also be given attributes:

    h1[id=#omg]
        block with attributes

    <h1 id="#omg">
        block with attributes
    </h1>

Which map straight onto XML attributes, but are less verbose.

Inline Tags
-----------

    this is a a{simple inline tag} in a line
    this is a <a>simple inline tag</a> in a line

    This is an a[href=#Domestication]{inline tag} with attributes
    This is an <a href='#Domestication'>inline tag</a> with attributes

Inline tags are a way of marking up sections inside a block of text, without using a block tag and having to open up a new indentation level. They can also have attributes.

Shortcuts
---------

    as *bold* or _italic_ or __underline__ or --strike through--
    or ^super script^ or ~sub script~ or `code` tags

    - This is a very
    - short list with
    - all items on one
    - line each

    as <b>bold</b> or <i>italic</i> or <u>underline</u> or <strike>strike through</strike>
    or <sup>super script</sup> or <sub>sub script</sub> or <code>code</code> tags

    <ul>
    <li>This is a very</li>
    <li>short list with</li>
    <li>all items on one</li>
    <li>line each</li>
    </ul>

Shortcuts are a convenient way of accessing the most commonly used tags in a marked up document.

-----------------------------------------

These are the primary mechanisms by which Xmlite marks up documents; unlike languages like [Markdown](http://en.wikipedia.org/wiki/Markdown) or [Textile](http://tinyurl.com/y4hjrs), none of the tags are special cased: all of them translate almost verbatim into equivalent XML, meaning you never need to drop into inline HTML or invent [special syntax](https://help.github.com/articles/github-flavored-markdown#task-lists) for your own use. The underlying markup is extensible, so you can just create a new tag `my_tag.` rather than coming up with ad-hoc modifications to the grammar. All the features of HTML which require special syntactic forms in other languages (links, images, divs, etc.) are simply tags in Xmlite.

Edge Cases
==========

Xmlite takes a special effort to cover almost all XML use cases, except with less ceremony and verbosity. Here are some of the things you probably won't need, but are provided in case you do.

Processing Instructions
-----------------------

    ?x Short tags

    both in block form and ?x{inline}

    <?x Short tags?>

    both in block form and <?x inline?>

    ?php Testing the use of a processing
        instruction that spills over
        several lines

    <?php Testing the use of a processing
        instruction that spills over
        several lines
    ?>

Processing instructions are present in many XML documents; `<?xml>`, `<?php?>`, and many others, we may not like the fact that we need them, but we need them all the same, so Xmlite provides a nice syntax to handle them. They have basically the same forms as tags (block or inline) but look like `?tag` or `?tag{}` rather than `tag.` or `tag{}`.


Raw Blocks
----------
    div:
        a block with a a{link} and bold{multiline
        tags i{zomg}}

        a block with a a{link} and bold{multiline
            tags i{zomg}}

            h1. With some title

            p.
                And some paragraphs
                with text

    <div>
        a block with a a{link} and bold{multiline
        tags i{zomg}}

        a block with a a{link} and bold{multiline
            tags i{zomg}}

            h1. With some title

            p.
                And some paragraphs
                with text
    </div>

Raw blocks, delimited by `div:` instead of `div.`, are blocks which are converted to XML verbatim and are not transformed by Xmlite.

Autochunk Blocks
----------------

    div.
        This is a div without
        auto blocking of text

        so the text just stays as
        one big blob inside the div

    div..
        This is a div with
        auto blocking of text

        so the text gets split into
        two separate paragraphs

    <div>
        This is a div without
        auto blocking of text

        so the text just stays as
        one big blob inside the div
    </div>

    <div>
        <p>
        This is a div with
        auto blocking of text
        </p>

        <p>
        so the text gets split into
        two separate paragraphs
        </p>
    </div>

Autochunking blocks are blocks which automatically look for chunks of text and group them into `<p>` tags. This is done by default for top-level text, but you can enable it for the contents of block tags by using `div..` instead of `div.`.

Escaping
--------

    Testing escaping of inline elements i am cow hear me a\{moo\}
    i wiki{weigh a\{twice\} as much\} as} you

    <p>
    Testing escaping of inline elements i am cow hear me a{moo}
    i <wiki>weigh a{twice} as much} as</wiki> you
    </p>

Basically any characters can be escaped with a backslash (`\`). This converts the character into its literal representation and prohibits it from taking part in any markup. This consistently works for escaping any syntactic construct imaginable: tags, lists, shortcuts, etc., preventing a common situation in other markup languages where certain forms of text are unrepresentable due to a lack of proper escaping.

Xmlite: The Future of Markup
============================

Xmlite does not do any clever pre/post-processing of input: no resolving of references, creation of footnotes, none of that. It is a simple mapper directly onto XML, providing a nice syntax to denote your marked up documents but imposing no semantics of its own.

Xmlite proves to be almost as lightweight and clutter-free as more specialized markup languages such as Markdown or Textile. Unlike those languages, Xmlite is extensible enough that you do not need to invent new special syntax every time you have new semantics to represent; like XML, defining custom tags is trivial. In addition, Xmlite is general enough to mark up arbitrary hierarchies of tags; any sort of tree-structured document can be easily and elegantly written in Xmlite, whereas other languages like Markdown or Textile are limited to basically flat documents.
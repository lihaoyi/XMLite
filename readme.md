Unmarked
========

Unmarked is a lightweight markup language that compiles to XML. It's goal is to offer a level of brevity/clarity similar to [Markdown](http://en.wikipedia.org/wiki/Markdown) and [Textile](http://redcloth.org/textile), but with extensible semantics and a more regular syntax. It turns Unmarked text that looks like this:


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

Unmarked maps directly to XML, and can be thought of as the minimal viable XML syntax. All the commonly used functionality and features of XML are still present


Why Unmarked
============

I designed Unmarked because I found that when I looked for a lightweight, extensible markup language, there was no such thing.

Lightweight
-----------

A lightweight language would contain minimal syntax; Markdown and Textile are good examples of lightweight languages. For example, it wouldn't have redundancies like XML's closing tag, which repeats the tag name already defined in the opening tag.

Extensible
----------

An extensible language would allow a user to easily define his own semantics with minimal difficulty. XML is extensible, for example, because users can define whatever tag they want and attribute meaning to them. Markdown is not extensible, as the only way to insert "custom" tags is via XML, at which point you're not working with Markdown anymore.

Markup
------

A markup language allows a user to freely and easily mix tags and text. After all, the original purpose of "markup" languages is to take a blob of text and mark certain words or sections with special formatting. XML is a markup language, but there are lightweight/extensible languages like JSON which are more suited to storing data than marking up text. Nobody writes their blog in JSON.

There are countless existing markup languages out there, but none of them fit the criterion of a lightweight extensible markup.
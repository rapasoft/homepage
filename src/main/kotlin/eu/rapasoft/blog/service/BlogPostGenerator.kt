package eu.rapasoft.blog.service

import eu.rapasoft.blog.model.BlogEntry
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import java.io.FileWriter
import java.nio.charset.Charset

class BlogPostGenerator {

  private companion object {
    val blogSourceIndexFile = File("./src/main/resources/index.html");
    val blogTargetIndexFile = File("./src/main/webapp/blog/index.html");
    val targetDir = File("./src/main/webapp/blog/posts/")
    val sourceDir = File("./src/main/resources/blog_posts/")
    val highlighterString = { title: String ->
      """
          <title>blog.rapasoft.eu | ${title}</title>
          <meta name="viewport" content="initial-scale=1, maximum-scale=1">
          <meta name="author" content="Pavol Rajzak" />
          <link rel="stylesheet" href="../css/styles.css">
          <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.7.0/styles/default.min.css">
          <script src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.7.0/highlight.min.js"></script>
          <script>hljs.initHighlightingOnLoad();</script>
        """
    }
    val disqusCode = { id: String ->
      """
            <div id="disqus_thread"></div>
            <script>
            var disqus_config = function () {
                this.page.url = 'http://blog.rapasoft.eu/${id}';  // Replace PAGE_URL with your page's canonical URL variable
                this.page.identifier = '${id}'; // Replace PAGE_IDENTIFIER with your page's unique identifier variable
            };
            (function() { // DON'T EDIT BELOW THIS LINE
                var d = document, s = d.createElement('script');
                s.src = '//blograpasofteu.disqus.com/embed.js';
                s.setAttribute('data-timestamp', +new Date());
                (d.head || d.body).appendChild(s);
            })();
            </script>
            <noscript>Please enable JavaScript to view the <a href="https://disqus.com/?ref_noscript">comments powered by Disqus.</a></noscript>
        """
    }
    val parser = Parser.builder().build();
  }

  fun generateBlogPosts() {
    clean(targetDir)

    val files = sourceDir
        .list()
        .filter { it.endsWith("md") }
        .map { readMarkdownFile(it) }
        .map { markdownToHtml(it) }
        .map { appendDisqusElement(it) }
        .map { appendSyntaxHighlighting(it) }
        .map { addMetaInformation(it) }

    files.forEach { writeToHtmlFile(it, targetDir) }

    val indexHtml = Jsoup.parse(
        blogSourceIndexFile.readLines(Charset.forName("UTF-8")).reduce { s1, s2 -> s1 + s2 }
    )

    sourceDir.listFiles()
        .filter { it.path.toString().endsWith("jpg") }
        .forEach { it.copyTo(File(targetDir.path + "/" + it.toPath().fileName), true) }

    files
        .sortedByDescending { pair -> pair.first.published }
        .forEach { pair -> indexHtml.getElementById("posts").append(post(pair)) }

    if (blogTargetIndexFile.exists()) {
      blogTargetIndexFile.delete();
    }

    val fw = FileWriter(blogTargetIndexFile)
    fw.write(indexHtml.outerHtml())
    fw.close()
  }

  private fun post(pair: Pair<BlogEntry, Element>): String {
    return """
                  <div class="post">
                    <img class="post-avatar" src="./posts/${pair.first.fileName}.jpg" alt="${pair.first.fileName}" />
                    <h3 class="post-title">
                      <a href="/blog/posts/${pair.first.fileName}.html">${pair.first.title}</a>
                    </h3>
                    <p class="post-description">
                      ${pair.first.perexifyContent()}
                    </p>
                    <div>
                      ${pair.first.categories
        .map { "<span class=\"post-category\">${it}</span>" }
        .reduce { s1, s2 -> s1 + s2 }}
                    </div>
                  </div>
                """
  }

  private fun addMetaInformation(it: Pair<BlogEntry, Element>): Pair<BlogEntry, Element> {
    it.second.getElementsByTag("body").first()
        .prepend("""
        <h1>${it.first.title}</h1>
        <p><span class=\"post-meta\">${it.first.published}</span></p>
        <p>${it.first.categories.map { "<span class=\"post-category\">${it}</span>" }.reduce { s1, s2 -> s1 + s2 }}</p>
        <hr />
      """);
    return Pair(
        it.first,
        it.second
    )
  }

  private fun appendSyntaxHighlighting(it: Pair<BlogEntry, Element>): Pair<BlogEntry, Element> {
    it.second.getElementsByTag("head").first().append(highlighterString(it.first.title))
    return Pair(
        it.first,
        it.second
    )
  }

  private fun readMarkdownFile(it: String) = BlogEntryParser.instance
      .parse(File(sourceDir.path + "/" + it).readLines(Charset.forName("UTF-8")), it)

  private fun writeToHtmlFile(it: Pair<BlogEntry, Element>, targetDir: File) {
    val fw = FileWriter(targetDir.path + "/" + it.first.fileName + ".html");
    fw.write(it.second.outerHtml())
    fw.close()
  }

  private fun appendDisqusElement(it: Pair<BlogEntry, Document>): Pair<BlogEntry, Element> {
    it.second.getElementsByTag("body").first().append(disqusCode(it.first.fileName))
    return Pair(
        it.first,
        it.second
    )
  }

  private fun markdownToHtml(it: BlogEntry): Pair<BlogEntry, Document> =
      Pair(
          it,
          Jsoup.parse(HtmlRenderer.builder().build().render(parser.parse(it.content)))
      )

  private fun clean(targetDir: File) {
    if (targetDir.exists() && targetDir.isDirectory) {
      targetDir.listFiles().forEach { it.delete() }
    }
  }


}
package eu.rapasoft.blog.controller;

import eu.rapasoft.blog.model.BlogEntry;
import eu.rapasoft.blog.service.FileLoader;
import eu.rapasoft.blog.util.MDFileConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static eu.rapasoft.htmldsl.components.HtmlTags.*;

/**
 * @author Pavol Rajzak
 */
@RestController
@RequestMapping(value = MDFileConstants.MD_FILE, method = RequestMethod.GET)
public class MDFileResource {

	public final static String DISQUS = "<div id=\"disqus_thread\"></div>\n" +
			"<script>\n" +
			"/**\n" +
			"* RECOMMENDED CONFIGURATION VARIABLES: EDIT AND UNCOMMENT THE SECTION BELOW TO INSERT DYNAMIC VALUES FROM YOUR PLATFORM OR CMS.\n" +
			"* LEARN WHY DEFINING THESE VARIABLES IS IMPORTANT: https://disqus.com/admin/universalcode/#configuration-variables\n" +
			"*/\n" +
			"/*\n" +
			"var disqus_config = function () {\n" +
			"this.page.url = PAGE_URL; // Replace PAGE_URL with your page's canonical URL variable\n" +
			"this.page.identifier = PAGE_IDENTIFIER; // Replace PAGE_IDENTIFIER with your page's unique identifier variable\n" +
			"};\n" +
			"*/\n" +
			"(function() { // DON'T EDIT BELOW THIS LINE\n" +
			"var d = document, s = d.createElement('script');\n" +
			"\n" +
			"s.src = '//blograpasofteu.disqus.com/embed.js';\n" +
			"\n" +
			"s.setAttribute('data-timestamp', +new Date());\n" +
			"(d.head || d.body).appendChild(s);\n" +
			"})();\n" +
			"</script>\n" +
			"<noscript>Please enable JavaScript to view the <a href=\"https://disqus.com/?ref_noscript\" rel=\"nofollow\">comments powered by Disqus.</a></noscript>";

	@Autowired
	private FileLoader fileLoader;

	@RequestMapping(value = "{id}", produces = MediaType.TEXT_HTML_VALUE)
	private ResponseEntity<String> get(@PathVariable String id) {
		BlogEntry blogEntry = fileLoader.loadMdFile(id + ".md");

		return ResponseEntity.ok(html(
				head(
						title(blogEntry.getTitle())
				),
				body(
						div("<a href=\"http://blog.rapasoft.eu\">Back to blog.rapasoft.eu</a>")
								.style("\tposition: relative;\n" +
										"\tmax-width: 1170px;\n" +
										"\tmargin: 0 auto;\n" +
										"\ttext-align: right;"),
						xmp("Date: " + blogEntry.getPublished() + "\n\n" + blogEntry.getContent()),
						div().id("disqus_thread"),
						script(disqusScript("http://blog.rapasoft.eu/" + id, id)),
						script().src("http://strapdownjs.com/v/0.2/strapdown.js"),
						script().id("dsq-count-scr").src("//blograpasofteu.disqus.com/count.js"),
						script("function adjustDisqus() {\n" +
								"\tdocument.getElementById('disqus_thread').style.marginLeft = 'auto';\n" +
								"\tdocument.getElementById('disqus_thread').style.marginRight = 'auto';\n" +
								"\tdocument.getElementById('disqus_thread').style.width = document.getElementById('content').getBoundingClientRect().width;\n" +
								"}\n" +
								"(function () {\n" +
								"\tadjustDisqus();\n" +
								"\twindow.onresize = adjustDisqus;\n" +
								"})();")
				)
		).getHtml());
	}

	private String disqusScript(String pageUrl, String pageId) {
		return "\n" +
				"var disqus_config = function () {\n" +
				"this.page.url = '" + pageUrl + "';\n" +
				"this.page.identifier = '" + pageId + "';\n" +
				"};\n" +
				"\n" +
				"(function() { // DON'T EDIT BELOW THIS LINE\n" +
				"var d = document, s = d.createElement('script');\n" +
				"\n" +
				"s.src = '//blograpasofteu.disqus.com/embed.js';\n" +
				"\n" +
				"s.setAttribute('data-timestamp', +new Date());\n" +
				"(d.head || d.body).appendChild(s);\n" +
				"})();\n";
	}

	@RequestMapping
	private ResponseEntity<List<BlogEntry>> list() {
		return ResponseEntity.ok(fileLoader.listMdFiles());
	}

}

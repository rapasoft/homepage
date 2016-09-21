package eu.rapasoft.blog.model;

import lombok.Getter;
import lombok.experimental.Builder;

import java.util.Date;
import java.util.List;

/**
 * @author Pavol Rajzak
 */
@Getter
@Builder
public final class BlogEntry {

	private String title;
	private String content;
	private List<String> categories;
	private String published;
	private String fileName;

	public static BlogEntry perexifyContent(BlogEntry blogEntry) {
		return BlogEntry.builder()
				.fileName(blogEntry.getFileName())
				.title(blogEntry.getTitle())
				.published(blogEntry.getPublished())
				.fileName(blogEntry.getFileName())
				.categories(blogEntry.getCategories())
				.content(blogEntry.content.substring(0, blogEntry.content.indexOf("\n"))
						.replaceAll("\\(.*\\)","")
						.replaceAll("\\]|\\[",""))
				.build();
	}
}

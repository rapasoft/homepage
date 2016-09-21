package eu.rapasoft.blog.service;

import eu.rapasoft.blog.model.BlogEntry;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static eu.rapasoft.blog.util.MDFileConstants.*;

/**
 * @author Pavol Rajzak
 */
@Component
public class BlogEntryParser {

	public BlogEntry parse(File match) {
		List<String> lines;
		try {
			lines = Files.readAllLines(match.toPath(), Charset.forName("ISO-8859-1"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return BlogEntry.builder()
				.fileName(match.getName().substring(0,match.getName().indexOf(".")))
				.title(getLine(lines, TITLE_LINE))
				.published(getLine(lines, PUBLISH_DATE_LINE))
				.categories(Arrays.asList(getLine(lines, CATEGORY_LINE).split(" ")))
				.content(fetchContent(lines, CONTENT_START_LINE, lines.size()))
				.build();
	}

	private String getLine(List<String> lines, int position) {
		return lines
				.get(position)
				.replaceAll("^[\\-|#]\\s*", "")
				.trim();
	}

	private String fetchContent(List<String> lines, int contentStartLine, int size) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = contentStartLine; i < size; i++) {
			stringBuilder.append(lines.get(i)).append("\n");
		}
		return stringBuilder.toString();
	}

}

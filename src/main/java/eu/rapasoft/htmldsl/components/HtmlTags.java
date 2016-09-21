package eu.rapasoft.htmldsl.components;

import eu.rapasoft.htmldsl.enums.Cardinality;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;

/**
 * @author Pavol Rajzak
 */
public class HtmlTags {

	public static final BinaryOperator<String> STRING_BINARY_OPERATOR = new BinaryOperator<String>() {
		public String apply(String s, String s2) {
			return s + s2;
		}
	};

	public static Content html(Content... children) {
		return genericBinaryTag("html", children);
	}

	public static Content head(Content... children) {
		return genericBinaryTag("head", children);
	}

	public static Content body(Content... children) {
		return genericBinaryTag("body", children);
	}

	public static Content title(final String title) {
		return genericBinaryTag("title", StringContent.withContent(title));
	}

	public static Content xmp(final String content) {
		return genericBinaryTag("xmp", StringContent.withContent(content));
	}

	public static DivTag div(final String... content) {
		Tag div = genericBinaryTag("div", StringContent.withContent(Arrays.asList(content).stream().reduce("", STRING_BINARY_OPERATOR)));
		return new DivTag(div);
	}

	public static ScriptTag script(String... content) {
		Tag script = genericBinaryTag("script", StringContent.withContent(Arrays.asList(content).stream().reduce("", STRING_BINARY_OPERATOR)));
		return new ScriptTag(script);
	}

	private static Tag genericBinaryTag(String name, Content... children) {
		return Tag.builder()
				.cardinality(Cardinality.BINARY)
				.children(toList(children))
				.attributes(new ArrayList<Attribute>())
				.name(name)
				.build();
	}

	private static List<Content> toList(Content[] children) {
		return Arrays.asList(children);
	}
}

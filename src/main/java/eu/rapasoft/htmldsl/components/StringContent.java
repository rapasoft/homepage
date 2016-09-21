package eu.rapasoft.htmldsl.components;

/**
 * @author Pavol Rajzak
 */
public class StringContent extends Content {

	private final String content;

	private StringContent(String content) {
		this.content = content;
	}

	@Override
	public String getHtml() {
		return content;
	}

	public static StringContent withContent(String content) {
		return new StringContent(content);
	}
}

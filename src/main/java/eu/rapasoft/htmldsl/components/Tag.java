package eu.rapasoft.htmldsl.components;

import eu.rapasoft.htmldsl.enums.Cardinality;
import lombok.Getter;
import lombok.experimental.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavol Rajzak
 */
@Getter
@Builder
public class Tag extends Content {

	protected String name;
	protected Cardinality cardinality;
	protected List<Content> children = new ArrayList<Content>();
	protected List<Attribute> attributes = new ArrayList<Attribute>();

	@Override
	public String getHtml() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<").append(name).append(" ");
		for (Attribute attribute : attributes) {
			stringBuilder.append(attribute.getName()).append("=\"").append(attribute.getValue()).append("\" ");
		}
		if (cardinality == Cardinality.UNARY) {
			stringBuilder.append("/");
		}
		stringBuilder.append(">");
		if (cardinality == Cardinality.BINARY) {
			for (Content content : children) {
				stringBuilder.append(content.getHtml());
			}
			stringBuilder.append("</").append(name).append(">");
		}

		return stringBuilder.toString();
	}

}

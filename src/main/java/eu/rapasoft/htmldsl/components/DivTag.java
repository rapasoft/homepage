package eu.rapasoft.htmldsl.components;

/**
 * @author Pavol Rajzak
 */
public class DivTag extends Content {

	private Tag tag;

	public DivTag(Tag div) {
		this.tag = div;
	}

	public Content id(String id) {
		this.tag.attributes.add(Attribute.builder().name("id").value(id).build());
		return this.tag;
	}

	public Content style(String style) {
		this.tag.attributes.add(Attribute.builder().name("style").value(style).build());
		return this.tag;
	}

	@Override
	public String getHtml() {
		return tag.getHtml();
	}
}

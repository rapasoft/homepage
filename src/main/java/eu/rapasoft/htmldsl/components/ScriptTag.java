package eu.rapasoft.htmldsl.components;

/**
 * @author Pavol Rajzak
 */
public class ScriptTag extends Content {

	private Tag tag;

	public ScriptTag(Tag tag) {
		this.tag = tag;
	}

	public ScriptTag src(String src) {
		tag.attributes.add(Attribute.builder().name("src").value(src).build());
		return this;
	}

	public ScriptTag id(String id) {
		tag.attributes.add(Attribute.builder().name("id").value(id).build());
		return this;
	}

	public String getHtml() {
		return tag.getHtml();
	}

}

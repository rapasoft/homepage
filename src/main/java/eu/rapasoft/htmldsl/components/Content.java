package eu.rapasoft.htmldsl.components;

import lombok.Data;

/**
 * @author Pavol Rajzak
 */
@Data
public abstract class Content {

	public abstract String getHtml();

}

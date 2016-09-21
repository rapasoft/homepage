package eu.rapasoft.htmldsl.components;

import lombok.Data;
import lombok.experimental.Builder;

/**
 * @author Pavol Rajzak
 */
@Data
@Builder
public class Attribute {

	private String name;
	private String value;

}

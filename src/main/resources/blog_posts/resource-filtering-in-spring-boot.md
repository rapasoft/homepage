# Resource filtering in Spring Boot not working
- Rapasoft
- 
- 2015/12/24
- Java Spring Boot
- publish

Title can be misleading, but it is the only one thing that annoyed user would search for when trying to figure out this problem.

The reason it is "not working" lies in Spring Boot's parent pom file. When you explore it you will stumble upon something like this:

			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-resources-plugin</artifactId>
				<version>2.6</version>
				<configuration>
					<delimiters>
						<delimiter>${resource.delimiter}</delimiter>
					</delimiters>
					<useDefaultDelimiters>false</useDefaultDelimiters>
				</configuration>
			</plugin>

So, okay, the default ${.*} pattern is replaced by something else, in this case:

			<resource.delimiter>@</resource.delimiter> <!-- delimiter that doesn't clash with Spring ${} placeholders -->
			
This is a nasty little fella, since it is not so obvious and it overrides the global config of maven-resource-plugin. I was wondering why my custom.properties file is not being filtered and this was the culprit. So just for future generation and my future self, define filterable properties like this (for example in properties file)

			property=@your.property@
    				
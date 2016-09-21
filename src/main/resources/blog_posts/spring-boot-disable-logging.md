# Disable excesive logging in Spring Boot JUnit Integration Tests
- Rapasoft
- 
- 2015/03/26
- Spring Boot
- publish

This was a major annoyance at first and couldn't find it anywhere, so when you want to write a SpringJUnit test for let's say Spring controller in Spring Boot similar to this:

			@RunWith(SpringJUnit4ClassRunner.class)
			@WebAppConfiguration
			@ContextConfiguration(classes = {Application.class})
			public class IntegrationTest {
			}
			
You might be surprised by the unnecessary amount of logging messages that cannot be tamed by your `application.properties` file. The culprit is logback library, so just put in `test/resources/logback-test.xml` this configuration and you should be good:			

			<?xml version="1.0" encoding="UTF-8"?>

			<configuration scan="true">
				<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
					<encoder>
						<charset>utf-8</charset>
						<Pattern>[%p] %c - %m%n</Pattern>
					</encoder>
				</appender>
				<logger name="ch.erni.community.ideasboard" level="DEBUG"/>
				<logger name="org.springframework" level="OFF"/>
				<logger name="org.apache.catalina.startup.DigesterFactory" level="OFF"/>

				<root level="WARN">
					<appender-ref ref="CONSOLE"/>
				</root>

			</configuration>
			
This is something that I will return to in the future, so I just wanted to have it here.
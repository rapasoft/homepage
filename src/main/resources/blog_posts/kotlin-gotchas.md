# Kotlin gotcha's 
- Rapasoft
-
- 2016/02/21
- Kotlin
- publish

You have plenty of JVM-based languages to choose from, like Scala, Groovy, Clojure, Ceylon,... and I could go on. There is one another, which just recently hit the major version 1.0.0 and it is Kotlin.

Kotlin is developed by JetBrains, which is responsible for many successful products like IntelliJ IDEA, TeamCity, YouTrack and so on. I don't want to give you the tutorial to Kotlin, since it is [well documented on their website](https://kotlinlang.org/docs/reference/) and you can even try it [within your browser](http://try.kotlinlang.org/). They are also claiming that version 1.0.0 is production ready and you can even [build Spring Boot application](https://spring.io/blog/2016/02/15/developing-spring-boot-applications-with-kotlin) using Kotlin. That is a nice claim and I sure wanted to try it out. There are few gotchas, however, and I would like to share my experience. 

### Color scheme

First of all, you will need an IDE that supports Kotlin well. I am an avid IntelliJ IDEA user, therefore this was my first choice. Version 15 has a nice support but you can use older versions with the Kotlin plugin as well. Here I must mention that in version 14.1.6 with Kotlin plugin and Darcula theme the colors are not distinguished very well (e.g. special words and annotations are in the same color). But this is just a minor issue which can be changed by custom color schema.

### Compiler version in Maven repository

Next thing was very odd compilation errors when I was trying to use the same code within several computers. One day the code compiled, the second day not. I was using Maven for dependency management and only thing that helped me was to clear the `org.jetbrains.*` directory in my `~/.m2/repository/` and let the Maven download the Kotlin compiler once again. Maybe version 1.0.0 was updated several times in the repository without changing the version number?

### Code completion in IDEA

Sometimes it is difficult to complete the code - especially if you are switching from Java - because you have to write the variable name before you specify the type. Sometime there are hints, if you name your variables as lowercase class names, e.g. `extractionService : ExtractionService` but I've encountered that it is not working well by default. Another thing is entering parameters in for example annotations. Pressing `Ctrl + Space` will give you list of everything but the things you need (e.g. the parameters that are needed for this annotation). Luckily `Ctrl + P` works for this purpose well, but it's a unnecessary keystroke. 
 
### Annotations

If you read the Spring Boot tutorial it was already mentioned there, but I will repeat it as well since it annoys me a lot. If you have an annotation that accepts varargs like `@ContextConfiguration(classes = TestApplication.class)` you have to explicitly define it as array, e.g. `@ContextConfiguration(classes = arrayOf(TestApplication::class))`. This becomes especially annoying if you need to declare several nested ones, like in Spring, where it is pretty common to have this:

	@ComponentScan(basePackages = arrayOf("eu.rapasoft.kotlin"), excludeFilters = arrayOf(
			Filter(Controller::class, type = FilterType.ANNOTATION),
			Filter(RestController::class, type = FilterType.ANNOTATION),
			Filter(pattern = arrayOf(".*SwaggerConfig"), type = FilterType.REGEX)))
			
I know this is a "tax" for Java interoperability but it would be nice to have this a bit nicer in the next versions.

### Data classes and Hibernate entities

Data class is an especially nice feature which lets you build simple data structures easily. If you are thinking about use case where you use it as Hibernate entities, than let's have a look:
 
	 @Entity
	 data class DailyMenuSourcePage(
			 @Id val restaurantName: String,
			 val url: String,
			 @Column(columnDefinition = "varchar(1000)") val soupsPath: String?,
			 @Column(columnDefinition = "varchar(1000)") val mainDishesPath: String?,
			 @Column(columnDefinition = "varchar(1000)") val otherPath: String?)
			 			 
Seems nice, you can easily define the entity without any getter/setters, they are automatically generated for you (something similar like `@Data` annotation in [Project Lombok](https://projectlombok.org/)). The main problem here is that Hibernate requires a no-args constructor, which isn't available here, since this definition only let's you build new instance entering all parameters. You could therefore either ditch data class, or hack it:

	@Entity
	data class DailyMenuSourcePage(
			@Id val restaurantName: String,
			val url: String,
			@Column(columnDefinition = "varchar(1000)") val soupsPath: String?,
			@Column(columnDefinition = "varchar(1000)") val mainDishesPath: String?,
			@Column(columnDefinition = "varchar(1000)") val otherPath: String?) {
			constructor() : this("", "", "", "", "")
	}
	
Here we provide the default values for no-args constructor. This is only possible if you know what you are doing and allow creation of such an objects. Sometimes it is not that easy to create default values, especially if you are using relations heavily.

### The "private static final LOG"

One thing that unfortunately doesn't work well with Kotlin is above-mentioned project Lombok. I was used to "inject" Slf4j loggers into class using `@Slf4j` annotation. Since Kotlin doesn't have notion of "static" this is understandable. Here, you have to use this construct to define Logger: 
 
     companion object {
         val LOG: org.slf4j.Logger = LoggerFactory.getLogger(MenuExtractorServiceImpl::class.java.name)
     }
     
Yes, using companion object. It is somewhat awkward, but this is how the language was designed,...

### It is not all that bad

There are other minor quirks, but in general it is a very pleasant experience to use Kotlin and I would definitely recommend at least trying it for your personal projects. I am experimenting with simple app that would extract daily menus from the restaurants near our work, you can view the [sources on my Github page](https://github.com/rapasoft/AtEleven). ;)
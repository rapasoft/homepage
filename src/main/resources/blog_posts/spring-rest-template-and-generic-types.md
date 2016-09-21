# Spring RestTemplate and generic types
- Rapasoft
- 
- 2015/12/25
- Spring RestTemplate 
- publish

Using Spring's [RestTemplate](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html) is sometimes more convenient than using JAX-RS implementations. Usually you have Jackson2 on classpath, so JSON to object deserialization is working out of box. But what about parameterized collections like `List<T>`?

For example you want to retrieve List of some DTOs from REST API using method `getForObject`:

			List<Person> persons = restTemplate.getForObject("http://rest.com/person", List.class);
			
You are expecting a list of persons, but instead you get a List of LinkedHashMaps that are representing the persons' properties. Well, Spring has a solution for that, but you cannot use convenience methods like `getForObject` in this case. The trick is to use more generic method `exchange` and define a `ParameterizedTypeReference` as a result type:

			ResponseEntity<List<Person>> response = exchange("http://rest.com/person", HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<List<Person>>() {});

`ParameterizedTypeReference` is an abstract class, but it does not require you to implement any of its methods. This is a bit weird construct, but it works. It will return a `ResponseEntity` which you can use to retrieve the object (body) if everything is OK.
 
 It's been like this in Spring for a while and I currently don't know about any other nicer solution, unfortunately. 
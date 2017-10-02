# Stateless Double Submit Cookie CSRF protection with Spring/jQuery (with CORS, of course)
- Rapasoft
-
- 2016/03/24
- Java Spring jQuery CORS CSRF REST


Common approach on writing modern web applications is to have different technology stack for backend and frontend and let them communicate via RESTful web services. This decoupling, however, may introduce a security issue where attacker uses man-in-middle attack to eavesdrop on communication and inject malicious code. In other words [cross-site request forgery](https://en.wikipedia.org/wiki/Cross-site_request_forgery). There are multiple ways how to implement protection against these kinds of attacks, like adding hidden input field with CSRF token to every form. But what if we don't generate forms on the server and are rather using stateless REST services?
 
## The solution
There are basically two ways. First is to only allow requests from trusted domains, e.g. [CORS](https://en.wikipedia.org/wiki/Cross-origin_resource_sharing). This can help if backend and frontend are running on different domains (ports, whatever). Second way is to protect the request in a way that its origin can be verified by backend. I strongly recommend reading [how the Double submit cookie mechanism works
](http://appsandsecurity.blogspot.sk/2012/01/stateless-csrf-protection.html), since this is what I am going to present in this post.

## The implementation

### Technology stack
* Backend: Java 7/8, Spring Boot 1.3.x
* Frontend: AJAX requests will be performed by jQuery's `$.ajax()` function

The rest is really arbitrary. 

### Backend
We need to setup two filters. First one will be handling CORS protection and second CSRF. 

    import org.springframework.http.HttpHeaders;
    import org.springframework.http.HttpMethod;
    import org.springframework.web.filter.OncePerRequestFilter;
    
    import javax.servlet.FilterChain;
    import javax.servlet.ServletException;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import java.io.IOException;
    
    public class CorsFilter extends OncePerRequestFilter {
    
        private static final String LOCALHOST_DEV = "http://localhost:8001";
    
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, LOCALHOST_DEV);
            response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    
            if (request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD) != null && HttpMethod.OPTIONS.name().equals(request.getMethod())) {
                // CORS "pre-flight" request
                response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE");
                response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, HttpHeaders.CONTENT_TYPE + ", " + StatelessCSRFFilter.X_CSRF_TOKEN_HEADER);
                response.addHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "1");
            }
    
            filterChain.doFilter(request, response);
        }
    }
    
To summarize - we are extending `OncePerRequestFilter` that will add `HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN` with value that will define allowed domain. It could be set to wildcard `*` but this won't work with `HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"`, since this is required later on when you perform requests from the client and you want attach cookie to request. Additionally, we are very restrictive about the `ACCESS_CONTROL_ALLOW_HEADERS` stating that only `content-type` and our `X-CSRF-TOKEN` header are accepted. Now, we can add CSRF filter for the requests:

    import lombok.extern.slf4j.Slf4j;
    import org.springframework.security.access.AccessDeniedException;
    import org.springframework.security.web.access.AccessDeniedHandler;
    import org.springframework.security.web.access.AccessDeniedHandlerImpl;
    import org.springframework.security.web.util.matcher.RequestMatcher;
    import org.springframework.web.filter.OncePerRequestFilter;
    
    import javax.servlet.FilterChain;
    import javax.servlet.ServletException;
    import javax.servlet.http.Cookie;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import java.io.IOException;
    import java.util.regex.Pattern;
    
    @Slf4j
    public class StatelessCSRFFilter extends OncePerRequestFilter {
    
        public static final String X_CSRF_TOKEN_HEADER = "X-CSRF-TOKEN";
        public static final String CSRF_TOKEN_COOKIE = "CSRF-TOKEN";
        private static final int EXPIRE = 0;
        private final RequestMatcher requireCsrfProtectionMatcher = new DefaultRequiresCsrfMatcher();
        private final AccessDeniedHandler accessDeniedHandler = new AccessDeniedHandlerImpl();
    
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
    
            if (requireCsrfProtectionMatcher.matches(request)) {
                final String csrfTokenValue = request.getHeader(X_CSRF_TOKEN_HEADER);
                final Cookie[] cookies = request.getCookies();
    
                String csrfCookieValue = null;
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals(CSRF_TOKEN_COOKIE)) {
                            csrfCookieValue = cookie.getValue();                          
                        }
                    }
                }
    
                if (csrfTokenValue == null || !csrfTokenValue.equals(csrfCookieValue)) {
                    accessDeniedHandler.handle(request, response, new AccessDeniedException("Missing or non-matching CSRF-token"));
                    log.warn("Missing/bad CSRF-TOKEN while CSRF is enabled for request {}", request.getRequestURI());
                    return;
                }
            }
    
			invalidate(response);
            filterChain.doFilter(request, response);
        }
		
		private void invalidate(HttpServletResponse response) {
			Cookie cookie = new Cookie(CSRF_TOKEN_COOKIE, "");
			cookie.setMaxAge(EXPIRE);
			response.add(cookie);
		}
    
        private static final class DefaultRequiresCsrfMatcher implements RequestMatcher {
            private final Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
    
            @Override
            public boolean matches(HttpServletRequest request) {
                return !allowedMethods.matcher(request.getMethod()).matches();
            }
        }
    }
    
Few things are noticeable here:

* We are only matching `POST`, `PUT` and `DELETE` requests, since there it makes sense
* The request header value is compared with request cookie - the cookie creation will be managed on the client side
* Immediately after check we are expiring the cookie by setting `maxAge` to `0` which means that cookie should be deleted after response is finished
* I have used implementation of the filter mentioned [in this fantastic blog post](http://blog.jdriven.com/2014/10/stateless-spring-security-part-1-stateless-csrf-protection/), which describes the same thing but using Angular
* `@Slf4j` is [a cool feature of project Lombok](https://projectlombok.org/api/lombok/extern/slf4j/Slf4j.html)


Spring Boot has it's own CSRF protection implemented and enabled by default. However, it is using slightly different technique (which is not suitable for our purposes), therefore [disable it](https://docs.spring.io/spring-security/site/docs/current/reference/html/csrf.html#csrf-configure) and include filters in the filter chain. There are many ways, this is the Spring Boot way:

    ...
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;  
    
    import javax.servlet.Filter;
    
    @Configuration
    public class FilterConfig {
    
        @Bean
        public Filter corsFilter() {
            return new CorsFilter();
        }
    
        @Bean
        public Filter csrfFilter() {
            return new StatelessCSRFFilter();
        }
         
    }

### Frontend

> All examples are written using ES6 syntax, so just replace `() => {}` with `function() {}` whenever needed.

Now the actual fun starts. Let's say you want to send `POST` request to your REST resource that would save your object. 

    var myDomainObject = {...} 
    $.ajax({
      url: someUrl,
      dataType: 'json',
      contentType: "application/json",
      type: "POST",
      data: myDomainObject,
      success: (data) => handleResponse(data),
      error: (xhr, status, err) => handleError(xhr, status, err)
    });
    
This, obviously, does not work resulting in error message similar to this:

    {
       "timestamp":1458804129690,
       "status":403,
       "error":"Forbidden",
       "message":"Missing or non-matching CSRF-token",
       "path":"/rest/resource"
    }

This is our CSRF protection working, checking and not finding the token.

To implement double submit cookie protection we need, well, send the cookie twice. Once as a request parameter and once as a request cookie.
We are going to create cookie on the client, which will be unique for each new request. The function I've used [can be found on this Gist](https://gist.github.com/jed/982883):

    const createCookie = (a) => {
      return a ? (a ^ Math.random() * 16 >> a / 4).toString(16) : ([1e16] + 1e16).replace(/[01]/g, createCookie)
    };
    
We will modify the `$.ajax` call like this:
    
    var myDomainObject = {...}
    const cookie = createCookie();
    document.cookie = CSRF_TOKEN + '=' + cookie;
    $.ajax({
      url: someUrl,
      dataType: 'json',
      contentType: "application/json",
      crossDomain: true,
      xhrFields: {
        withCredentials: true
      },
      headers: {'X-CSRF-TOKEN': cookie},
      type: "POST",
      data: myDomainObject,
      success: (data) => handleResponse(data),
      error: (xhr, status, err) => handleError(xhr, status, err)
    });
    
The main things to note here are:

* `crossDomain` - from the [official documentation](http://api.jquery.com/jquery.ajax/) it is clear, that cross domain request won't be working unless this is true:
*...if you wish to force a crossDomain request (such as JSONP) on the same domain, set the value of crossDomain to true. This allows, for example, server-side redirection to another domain.*
* `withCredentials` - once again, the [official documentation](https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest/withCredentials) is very clear about this:
*...this flag is also used to indicate when cookies are to be ignored in the response. The default is false. XMLHttpRequest from a different domain cannot set cookie values for their own domain unless withCredentials is set to true before making the request.*
However, what is not so obvious here is that `withCredentials` enforces that `ACCESS_CONTROL_ALLOW_ORIGIN` header in `CorsFilter` must be set to specific value and not `*` and `ACCESS_CONTROL_ALLOW_CREDENTIALS` header must be `true`.
* We are sending the `'X-CSRF-TOKEN'` header that we have defined in the `StatelessCSRFFilter` earlier and that is allowed in the `CorsFilter`.


## End notes

I wouldn't say that this is a definitive and ultimate protection, but it provides some of the best practices, like:

* Creating cookies on client to prevent state saving on server
* Using random unique cookie for each request
* Expiring the cookie once request was performed
* Using CSRF protection together with CORS


One last note is that I recommend using Chrome for testing this issue, since other browsers "hide" the real cause of failing requests into very vague messages (yes, I am talking to you Firefox!).

I've written down this article as a future reference and to help anyone struggling with this issue. Fill free to comment if you have any improvements or remarks!

## Recommended reading

* [OWASP CSRF Prevention Cheat Sheet](https://www.owasp.org/index.php/Cross-Site_Request_Forgery_%28CSRF%29_Prevention_Cheat_Sheet)
* [On stateless CSRF protection](http://appsandsecurity.blogspot.sk/2012/01/stateless-csrf-protection.html)
* [Blog that helped me a lot understanding implementation details](http://blog.jdriven.com/2014/10/stateless-spring-security-part-1-stateless-csrf-protection/)
* [Jeff Atwood has something to say about it too...](http://blog.codinghorror.com/preventing-csrf-and-xsrf-attacks/) 
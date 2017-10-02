# Passing parameters across views in JSF
- Rapasoft
- 
- 2014/12/23
- JSF
- publish

Even though not so popular and hip nowadays, JSF is still a very powerful framework for building web applications. You can do many things using simple code constructs. But sometimes it can be real brain-teaser since there are several ways to achieve one thing. One of those is a problem of navigating across views and passing parameters between them.

##### The problem

Imagine situation when you have one view that is represented by a xhtml facelet page and `@ViewScoped` JSF Managed Bean. This can be a product catalog with a several items organized in datatable. Clicking on one item would navigate you to another view, let's call it detailed view, which has also its own xhtml and `@ViewScoped` bean.

The navigation steps are simple: User opens the page with list of items, picks one and opens a detailed view. 

##### Approach

There are several ways how to approach this but I have found that using [**Flash scope**](https://javaserverfaces.java.net/nonav/docs/2.0/javadocs/javax/faces/context/Flash.html) suits me the best. This way you can store some parameters that will survive multiple HTTP requests until they are retrieved in the next view.

This is what the first view looks like:

    <h:body>
        <h:form>
            <h1><h:outputText value="#{viewScopedBean.someMessage}"/></h1>
    
            <h:commandButton value="Just navigate to the meaning of life" action="#{viewScopedBean.showProduct(42)}"/>
    
            <p:commandButton update="@form" value="Just refresh form" />
        </h:form>
    </h:body>
    
I have added a simple PrimeFaces refresh button that will update the form using ajax call just to see that the bean is not recreated each time. I will get to this later.

The backing bean is simple as well:

    @ManagedBean @ViewScoped
    public class ViewScopedBean implements Serializable {
    
        private Logger logger = Logger.getLogger(ViewScopedBean.class.getName());
    
        private String someMessage = "Wazzup";
    
        @ManagedProperty("#{jsfHelper}")
        private JsfHelper jsfHelper;
    
        @PostConstruct
        public void init() {
            logger.info("ViewScopedBean initiated " + jsfHelper.currentPhase());
        }
    
        public String showProduct(int productId) {
            logger.info("AnotherViewScopedBean init " + jsfHelper.currentPhase());
            jsfHelper.setFlash(JsfHelper.Constants.PRODUCT_ID, productId);
            return "meaning_of_life?faces-redirect=true";
        }
    
        public String getSomeMessage() {
            return someMessage;
        }
    
        public void setJsfHelper(JsfHelper jsfHelper) {
            this.jsfHelper = jsfHelper;
        }
    }

`JsfHelper` class is a `@ApplicationScoped` bean, which only serves to handle some simple JSF-related tasks and it is more for the demonstration purposes. There are methods for getting the current JSF Phase, and methods for working with flash scope. They are really simple:

	@ManagedBean @ApplicationScoped
	public class JsfHelper implements Serializable{

		public String currentPhase() {
			return "["+FacesContext.getCurrentInstance().getCurrentPhaseId().getName() + "]";
		}

		public void setFlash(String key, Object value) {
			FacesContext.getCurrentInstance().getExternalContext().getFlash().put(key, value);
		}

		public Object getFlash(String key) {
			return FacesContext.getCurrentInstance().getExternalContext().getFlash().get(key);
		}

		public static class Constants {
			public static final String PRODUCT_ID = "prodId";
		}

	}
    
So, before we navigate to the next view we set the value of `PRODUCT_ID` to the one that was passed as a method parameter. `PRODUCT_ID` is defined in a inner static class of constants for more clarity. Another thing to notice here is that flash scope will survive even when `faces-redirect` is set to `true`, which is basically the desired situation most of the time.

Here is the code for the second view. You can see that there is another button for refreshing the form via ajax.

    <h:body>
        <h:form>
            <h1>The meaning of life is <h:outputText id="meaningOfLife" value="#{anotherViewScopedBean.meaningOfLife}"/></h1>
    
            <p:commandButton update="@form" value="Really?" />
        </h:form>
    </h:body>
    
and backing bean:
    
    @ManagedBean @ViewScoped
    public class AnotherViewScopedBean implements Serializable {
    
        private Logger logger = Logger.getLogger(AnotherViewScopedBean.class.getName());
    
        private Integer meaningOfLife;
    
        @ManagedProperty("#{jsfHelper}")
        private JsfHelper jsfHelper;
    
        @PostConstruct
        public void init() {
            logger.info("AnotherViewScopedBean init " + jsfHelper.currentPhase());
            meaningOfLife = (Integer) jsfHelper.getFlash(JsfHelper.Constants.PRODUCT_ID);
        }
    
        public String getMeaningOfLife() {
            logger.info("AnotherViewScopedBean meaningOfLife " + jsfHelper.currentPhase());
            return "[" + meaningOfLife + "]";
        }
    
        public void setJsfHelper(JsfHelper jsfHelper) {
            this.jsfHelper = jsfHelper;
        }
    }

##### What's going on in the background

The output of our logging statements will make my case:

When the first page is loaded:

    15:21:35,983 INFO  [eu.rapasoft.viewscoped.ViewScopedBean] (default task-12) ViewScopedBean initiated [RENDER_RESPONSE]
    
When we navigate to the second view using button:

    15:21:37,925 INFO  [eu.rapasoft.viewscoped.ViewScopedBean] (default task-18) AnotherViewScopedBean init [INVOKE_APPLICATION]
    15:21:37,962 INFO  [eu.rapasoft.viewscoped.AnotherViewScopedBean] (default task-19) AnotherViewScopedBean init [RENDER_RESPONSE]
    
When we push the update button on a second page several times:

    15:21:39,966 INFO  [eu.rapasoft.viewscoped.AnotherViewScopedBean] (default task-19) AnotherViewScopedBean meaningOfLife [RENDER_RESPONSE]
    15:21:41,030 INFO  [eu.rapasoft.viewscoped.AnotherViewScopedBean] (default task-20) AnotherViewScopedBean meaningOfLife [RENDER_RESPONSE]
    15:21:41,532 INFO  [eu.rapasoft.viewscoped.AnotherViewScopedBean] (default task-21) AnotherViewScopedBean meaningOfLife [RENDER_RESPONSE]
    15:21:41,735 INFO  [eu.rapasoft.viewscoped.AnotherViewScopedBean] (default task-22) AnotherViewScopedBean meaningOfLife [RENDER_RESPONSE]
    15:21:41,939 INFO  [eu.rapasoft.viewscoped.AnotherViewScopedBean] (default task-23) AnotherViewScopedBean meaningOfLife [RENDER_RESPONSE]

So, no been creation, just direct method call.
	
##### After thoughts

Flash scope was introduced in JSF 2.0, together with `@ViewScoped` scope, but there is one glitch if you are using JSF version older than 2.2. Be sure to read [this perfect article](http://balusc.blogspot.sk/2010/06/benefits-and-pitfalls-of-viewscoped.html) by JSF expert [BalusC](http://stackoverflow.com/users/157882/balusc).
# Integration testing EJBs with OpenEJB
- Rapasoft
-
- 2015/01/08
- Java
- publish

When it comes to testing Integration tests are natural way to verify if "everything works together". Integration tests in Java EE environment can be a bit tricky, since for most desired services you'll need an EJB container. There are already several different solutions, among the most popular is [Arquillian](http://arquillian.org/). However, my personal experience with Arquillian is not so bright, so I looked for another solution - [OpenEJB](http://tomee.apache.org/), popularized by Apache TomEE.

I will demonstrate it on a simple Maven project. The main `pom.xml` should contain dependencies on a OpenEJB libraries:

    <dependencies>

        <dependency>
            <groupId>org.apache.openejb</groupId>
            <artifactId>javaee-api</artifactId>
            <version>6.0-6</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.apache.openejb</groupId>
            <artifactId>openejb-core</artifactId>
            <version>4.7.1</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.hibernate.javax.persistence</groupId>
            <artifactId>hibernate-jpa-2.0-api</artifactId>
            <version>1.0.0.Final</version>
        </dependency>

        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>4.2.0.Final</version>
        </dependency>

        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-entitymanager</artifactId>
            <version>4.2.0.Final</version>
        </dependency>

        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.10</version>
            <scope>test</scope>
        </dependency>

    </dependencies>

The `javaee-api` is provided by the application server (Apache TomEE), but there's also `openejb-core` needed for the Integration tests, so it is there with a `test` scope. I've switched the OpenJPA implementation to Hibernate, since I think it is more usable (and I have better experience with Hibernate than with OpenJPA when it comes to stability).

Next, there are two simple classes. One `Entity` class...

    @Entity
    public class Process {

        @Id
        private Integer id;

        private String name;

        public Process() {
        }

        public Process(String name, Integer id) {
            this.name = name;
            this.id = id;
        }
    }

...and one `Stateless` session bean that will simulate some business method that manipulates this entity.

    @Stateless
    public class SimpleService {

        @PersistenceContext(name = "test")
        private EntityManager entityManager;

        public void saveProcess(Process process) {
            entityManager.persist(process);
        }

    }

Be sure that you have a `persistence.xml` with correct settings. I've made some for the demonstration purposes:

    <?xml version="1.0" encoding="UTF-8"?>
    <persistence xmlns="http://java.sun.com/xml/ns/persistence" version="1.0">
        <persistence-unit name="default">
            <provider>org.hibernate.ejb.HibernatePersistence</provider>

            <jta-data-source>java:/DefaultDS</jta-data-source>

            <properties>

                <property name="hibernate.hbm2ddl.auto" value="create-drop"/>
                <property name="hibernate.dialect" value="org.hibernate.dialect.HSQLDialect" />

            </properties>

        </persistence-unit>
    </persistence>

The `DefaultDS` is provided by the application server and it uses `hsqldb` in-memory database. So, nothing fancy.

Now, it is time to create a test

    public class SimpleServiceIntegrationTest {

        @EJB
        private SimpleService simpleService;

        @Before
        public void init() throws NamingException {
            Context context = EJBContainer.createEJBContainer().getContext();
            context.bind("inject",this);
        }

        @Test
        public void testSaveProcess() throws Exception {
            Process process = new Process("Test", 1);

            simpleService.saveProcess(process);
        }

    }

This is the simplest way, how to inject your service into integration test. You can provide `Properties` parameter when creating EJB container, where you can override your default settings in `persistence.xml`, which is handy when working with test database. This is basicall described [in this article](http://tomee.apache.org/examples-trunk/testing-transactions/README.html) from the Apache TomEE examples.

It works well,... until you try to create another integration test. Imagine situation where there exists another one with the name `AnotherIntegrationTest` that has the same contents as the class above. When running all tests in Maven you will end up with something like this:

    INFO - EJBContainer already initialized.  Call ejbContainer.close() to allow reinitialization

    org.apache.openejb.Injector$NoInjectionMetaDataException: eu.rapasoft.blog.AnotherIntegrationTest :
    Annotate the class with @javax.annotation.ManagedBean so it can be discovered in the application scanning process

Which is clearly non-sense and it looks like a bug. You can follow the instructions and annotate the test class with `@ManagedBean`, which will work in Maven. But I had some problems running it inside IDE, so I don't think this is a solution. However, the workaround for this is (sigh) using JNDI lookup:

    public class SimpleServiceIntegrationTest {

        private SimpleService simpleService;

        @Before
        public void init() throws NamingException {
            Context context = EJBContainer.createEJBContainer().getContext();
            simpleService = (SimpleService) context.lookup("java:global/OpenEJBIntegrationTesting/SimpleService");
        }

        @Test
        public void testSaveProcess() throws Exception {
            Process process = new Process("Test", 1);

            simpleService.saveProcess(process);
        }

    }

When you lookup a service like this, the tests run without any problems. It looks nasty, but there exists a different aproach, which I'll tackle in my next post. Meanwhile, be sure to check Apache TomEE documentation and examples!

*EDIT:* I've got an email from Aliaxei Voitsik in which he points out that to overcome the problem mentioned above one just needs to explicitly close `EJBContainer` after each test, like this:

	public class SimpleServiceIntegrationTest {

		@EJB
		private SimpleService simpleService;

		private Context context;

		private EJBContainer ejbContainer;

		@Before
		public void init() throws NamingException {
			ejbContainer = EJBContainer.createEJBContainer();
			context = ejbContainer.getContext();
			context.bind("inject",this);
		}

		@Test
		public void testSaveProcess() throws Exception {
			Process process = new Process("Test", 2);

			simpleService.saveProcess(process);
		}

		@After
		public void destroy() {
			ejbContainer.close();
		}

	}
	
Many thanks for that!
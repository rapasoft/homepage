# Integration testing EJBs with OpenEJB part 2: Application composer
- Rapasoft
-
- 2015/01/15
- Java
- publish

Another way how to compose a testable module for OpenEJB embedded container is to use [application composer](http://tomee.apache.org/examples-trunk/application-composer/README.html).

Using `ApplicationComposer` is similar to [Arquillian](http://arquillian.org/) with [Shrinkwrap API](http://arquillian.org/guides/shrinkwrap_introduction/). It is a JUnit runner and you can basically create your own module that is necessary for test.

The tutorial is pretty self explanatory, you just annotate integration test with `@RunWith(ApplicationComposer.class)` and provide module details using `@Module` annotations.

    @RunWith(ApplicationComposer.class)
    public class MoviesTest extends TestCase {

        @EJB
        private Movies movies;

        @Resource
        private UserTransaction userTransaction;

        @PersistenceContext
        private EntityManager entityManager;

        @Module
        public PersistenceUnit persistence() {
            ...
        }

        @Module
        public EjbJar beans() {
            ...
        }

        @Configuration
        public Properties config() throws Exception {
            ...
        }

        @Test
        public void test() throws Exception {
            ...
        }
    }

With this simple API you can prepare your own Helper class that you can use to build your modules. This is basically as a future reference for me:

    public class ApplicationComposerHelper {

        public static final String MODEL_PACKAGE = "eu.rapasoft.blog.model";
        public static final String APPLICATION_NAME = "test-app";
        public static final String PERSISTENCE_UNIT = "test";

        public static EjbJar createEjbJar(List<Class> statelessBeans, List<Class> statefulBeans) {
            EjbJar ejbJar = new EjbJar(APPLICATION_NAME);
            for (Class statelessBeanClass : statelessBeans) {
                ejbJar.addEnterpriseBean(new StatelessBean(statelessBeanClass));
            }
            for (Class statefulBean : statefulBeans) {
                ejbJar.addEnterpriseBean(new StatefulBean(statefulBean));
            }

            return ejbJar;
        }

        public static PersistenceUnit createPersistenceUnit() {
            PersistenceUnit unit = new PersistenceUnit(PERSISTENCE_UNIT);
            unit.setJtaDataSource("test");
            unit.setNonJtaDataSource("testUnmanaged");
            List<Class> modelClasses = new ArrayList<>();
            try {
                modelClasses.addAll(getAllClasses(Package.getPackage(MODEL_PACKAGE).getName()));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            for (Class modelClass : modelClasses) {
                unit.getClazz().add(modelClass.getName());
            }
            unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
            unit.setProperty("openjpa.Log", "DefaultLevel=WARN, Runtime=INFO, Tool=INFO, SQL=TRACE");
            return unit;
        }

        public static Properties createConfig() {
            Properties p = new Properties();
            p.put("test", "new://Resource?type=DataSource");
            p.put("test.JdbcDriver", "org.hsqldb.jdbcDriver");
            p.put("test.JdbcUrl", "jdbc:hsqldb:mem:test");
            return p;
        }

        private static List<Class> getAllClasses(String pckgname) throws ClassNotFoundException {

            ArrayList<Class> classes = new ArrayList<>();
            File directory;
            try {
                directory = new File(Thread.currentThread().getContextClassLoader().getResource(pckgname.replace('.', '/')).getFile());
            } catch (NullPointerException x) {
                throw new ClassNotFoundException(pckgname + " does not appear to be a valid package");
            }
            if (directory.exists()) {
                String[] files = directory.list();
                for (String file : files) {
                    if (file.endsWith(".class")) {
                        classes.add(Class.forName(pckgname + '.' + file.substring(0, file.length() - 6)));
                    }
                }
            } else {
                throw new ClassNotFoundException(pckgname + " does not appear to be a valid package");
            }

            return classes;
        }


    }
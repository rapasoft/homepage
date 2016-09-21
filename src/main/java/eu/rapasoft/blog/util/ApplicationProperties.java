package eu.rapasoft.blog.util;

import lombok.SneakyThrows;

import java.net.URL;
import java.util.Properties;

/**
 * @author Pavol Rajzak
 */
public enum ApplicationProperties {

    INSTANCE;

    public URL getMDFilesPath() {
        return ApplicationProperties.class.getResource("/blog_posts");
    }

}

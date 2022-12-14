package local.pbaranowski.chat.commons;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

import static local.pbaranowski.chat.commons.Constants.DEFAULT_ENDPOINT;

public class ProxyFactory {
    private static final String INITIAL_CONTEXT_FACTORY = "org.wildfly.naming.client.WildFlyInitialContextFactory";
    private static String PROVIDER_URL = "http-remoting://" + DEFAULT_ENDPOINT;
    private static final String CLIENT_EJB_CONTEXT = "jboss.naming.client.ejb.context";
    private final InitialContext initialContext;

    public ProxyFactory(String endpoint) throws NamingException {
        this(prepareJndiProperties(endpoint));
    }

    public ProxyFactory(Properties properties) throws NamingException {
        initialContext = new InitialContext(properties);
    }

    public <T> T createProxy(String jndiName) throws NamingException {
        return (T) initialContext.lookup(jndiName);
    }

    private static Properties prepareJndiProperties(String endpoint) {
        var properties = new Properties();
        if (endpoint != null) {
            PROVIDER_URL = "http-remoting://" + endpoint;
        }
        properties.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        properties.put(Context.PROVIDER_URL, PROVIDER_URL);
        properties.put(Context.SECURITY_AUTHENTICATION, "simple");
        properties.put(Context.SECURITY_PRINCIPAL, "chat");
        properties.put(Context.SECURITY_CREDENTIALS, "chat");
        properties.put(CLIENT_EJB_CONTEXT, true);
        return properties;
    }
}

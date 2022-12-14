package local.pbaranowski.chat;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class ProxyFactory {
    private static final String INITIAL_CONTEXT_FACTORY = "org.wildfly.naming.client.WildFlyInitialContextFactory";
//    private static final String PROVIDER_URL = "jnp://localhost:8080";
    private static final String CLIENT_EJB_CONTEXT = "jboss.naming.client.ejb.context";
    private final InitialContext initialContext;

    public ProxyFactory() throws NamingException {
        this(prepareJndiProperties());
    }

    public ProxyFactory(Properties properties) throws NamingException {
        initialContext = new InitialContext(properties);
    }

    public <T> T createProxy(String jndiName) throws NamingException {
        return (T) initialContext.lookup(jndiName);
    }

    private static Properties prepareJndiProperties() {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        properties.put(Context.SECURITY_PRINCIPAL,"chat");
        properties.put(Context.SECURITY_CREDENTIALS,"chat");
        properties.put(CLIENT_EJB_CONTEXT, true);
        return properties;
    }
}

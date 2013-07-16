package proto.action.spring;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;
import org.springframework.web.method.HandlerMethodSelector;

public abstract class AbstractHandlerMethodMapping<T> implements ApplicationContextAware, InitializingBean {
    private static final Logger LOG = Logger.getLogger(AbstractHandlerMethodMapping.class);

    private final Map<T, HandlerMethod> handlerMethods = new LinkedHashMap<T, HandlerMethod>();
    private ApplicationContext applicationContext;
    private ConversionService conversionService;

    @Override
    public void afterPropertiesSet() throws Exception {
        initHandlerMethods();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    protected void initHandlerMethods() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Looking for request mappings in application context: " + getApplicationContext());
        }

        String[] beanNames = getApplicationContext().getBeanNamesForType(Object.class);
        for (String beanName : beanNames) {
            if (isHandler(getApplicationContext().getType(beanName))){
                detectHandlerMethods(beanName);
            }
        }
        handlerMethodsInitialized(getHandlerMethods());
    }

    protected abstract boolean isHandler(Class<?> beanType);

    /**
     * Look for handler methods in a handler.
     * @param handler the bean name of a handler or a handler instance
     */
    protected void detectHandlerMethods(final Object handler) {
        Class<?> handlerType = handler.getClass();

        final Class<?> userType = ClassUtils.getUserClass(handlerType);

        Set<Method> methods = HandlerMethodSelector.selectMethods(userType, new MethodFilter() {
            public boolean matches(Method method) {
                return getMappingForMethod(method, userType) != null;
            }
        });

        for (Method method : methods) {
            T mapping = getMappingForMethod(method, userType);
            registerHandlerMethod(handler, method, mapping);
        }
    }

    /**
     * Register a handler method and its unique mapping.
     * @param handler the bean name of the handler or the handler instance
     * @param method the method to register
     * @param mapping the mapping conditions associated with the handler method
     * @throws IllegalStateException if another method was already registered
     * under the same mapping
     */
    protected void registerHandlerMethod(Object handler, Method method, T mapping) {
        HandlerMethod handlerMethod = new HandlerMethod(handler, method, conversionService);

        HandlerMethod oldHandlerMethod = handlerMethods.get(mapping);
        if (oldHandlerMethod != null && !oldHandlerMethod.equals(handlerMethod)) {
            throw new IllegalStateException("Ambiguous mapping found. Cannot map '" + handlerMethod.getBean()
                    + "' bean method \n" + handlerMethod + "\nto " + mapping + ": There is already '"
                    + oldHandlerMethod.getBean() + "' bean method\n" + oldHandlerMethod + " mapped.");
        }

        this.handlerMethods.put(mapping, handlerMethod);
        if (LOG.isInfoEnabled()) {
            LOG.info("Mapped \"" + mapping + "\" onto " + handlerMethod);
        }
    }

    /**
     * Provide the mapping for a handler method. A method for which no
     * mapping can be provided is not a handler method.
     * @param method the method to provide a mapping for
     * @param handlerType the handler type, possibly a sub-type of the method's
     * declaring class
     * @return the mapping, or {@code null} if the method is not mapped
     */
    protected abstract T getMappingForMethod(Method method, Class<?> handlerType);

    /**
     * Invoked after all handler methods have been detected.
     * @param handlerMethods a read-only map with handler methods and mappings.
     */
    protected void handlerMethodsInitialized(Map<T, HandlerMethod> handlerMethods) {
    }

    /** Return a map with all handler methods and their mappings. */
    public Map<T, HandlerMethod> getHandlerMethods() {
        return Collections.unmodifiableMap(this.handlerMethods);
    }

    private ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}

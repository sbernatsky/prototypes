package proto.action.spring;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;
import proto.action.ActionHandler;
import proto.action.ActionHandlerRegistry;
import proto.action.annotation.ActionMethod;

public class SpringActionHandlerRegistry extends AbstractHandlerMethodMapping<ActionMapping> implements ActionHandlerRegistry {

    private final Map<String, ActionHandler> actionHandlers = new HashMap<String, ActionHandler>();

    @Override
    public ActionHandler lookup(String name) {
        return actionHandlers.get(name);
    }

    @Override
    public Set<String> getRegisteredNames() {
        return new HashSet<String>(actionHandlers.keySet());
    }

    public void setActionHandlers(Map<String, ActionHandler> handlers) {
        actionHandlers.putAll(handlers);
    }

    protected boolean isHandler(Class<?> beanType) {
        final Class<?> userType = ClassUtils.getUserClass(beanType);

        Set<Method> methods = HandlerMethodUtils.selectMethods(userType, new MethodFilter() {
            public boolean matches(Method method) {
                return getMappingForMethod(method, userType) != null;
            }
        });

        return !methods.isEmpty();
    }

    @Override
    protected ActionMapping getMappingForMethod(Method method, Class<?> handlerType) {
        ActionMethod annotation = AnnotationUtils.findAnnotation(method, ActionMethod.class);
        return annotation != null ? new ActionMapping(annotation.value()) : null;
    }

    @Override
    protected void handlerMethodsInitialized(Map<ActionMapping, HandlerMethod> handlerMethods) {
        for (Entry<ActionMapping, HandlerMethod> entry : handlerMethods.entrySet()) {
            actionHandlers.put(entry.getKey().getName(), new BridgedActionHandler(entry.getValue()));
        }
    }

}

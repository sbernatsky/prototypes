package proto.action.spring;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.annotation.AnnotationUtils;
import proto.action.ActionHandler;
import proto.action.ActionHandlerRegistry;
import proto.action.annotation.ActionClass;
import proto.action.annotation.ActionMethod;

public class SpringActionHandlerRegistry extends AbstractHandlerMethodMapping<ActionMapping> implements ActionHandlerRegistry {

    private final Map<String, ActionHandler> actionHandlers = new HashMap<String, ActionHandler>();

    @Override
    public ActionHandler lookup(String name) {
        return actionHandlers.get(name);
    }

    public void setActionHandlers(Map<String, ActionHandler> handlers) {
        actionHandlers.putAll(handlers);
    }

    protected boolean isHandler(Class<?> beanType) {
        return AnnotationUtils.findAnnotation(beanType, ActionClass.class) != null;
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

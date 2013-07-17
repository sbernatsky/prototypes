package proto.action.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.ReflectionUtils;
import proto.action.Action;
import proto.action.ActionResult;
import proto.action.annotation.ActionParameter;
import proto.action.annotation.ValueConstants;


class HandlerMethod {
    private static final TypeDescriptor returnTarget = TypeDescriptor.valueOf(ActionResult.class);
    private static final TypeDescriptor paramSource = TypeDescriptor.valueOf(String.class);
    private static final MethodParameter actionMethodParameter = new MethodParameter() {
        @Override public Action getValue(Action action) { return action; }
    };
    private static final Map<Class<?>, Class<?>> primitives;

    static {
        primitives = new HashMap<Class<?>, Class<?>>();
        primitives.put(boolean.class, Boolean.class);
        primitives.put(byte.class, Byte.class);
        primitives.put(short.class, Short.class);
        primitives.put(char.class, Character.class);
        primitives.put(int.class, Integer.class);
        primitives.put(long.class, Long.class);
        primitives.put(float.class, Float.class);
        primitives.put(double.class, Double.class);
        primitives.put(void.class, Void.class);
    }

    private static Class<?> convertToWrapper(Class<?> primitive) {
        Class<?> result = primitives.get(primitive);
        return result != null ? result : primitive;
    }

    private final Object bean;
    private final Method method;
    private final TypeDescriptor returnSource;
    private final MethodParameter[] parameters;
    private final ConversionService conversionService;

    public HandlerMethod(Object handler, Method method, ConversionService conversionService) {
        this.bean = handler;
        this.method = method;
        this.conversionService = conversionService;

        this.returnSource = TypeDescriptor.valueOf(convertToWrapper(method.getReturnType()));
        if (!conversionService.canConvert(returnSource, returnTarget)) {
            throw new RuntimeException(String.format("can not convert result %s -> %s for method %s",
                                       returnSource.getType(), returnTarget.getType(), method));
        }

        this.parameters = new MethodParameter[method.getParameterTypes().length];
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (Action.class.isAssignableFrom(parameterType)) {
                this.parameters[i] = actionMethodParameter;
            } else {
                TypeDescriptor paramTarget = TypeDescriptor.valueOf(parameterType);
                if (!conversionService.canConvert(paramSource, paramTarget)) {
                    throw new RuntimeException(String.format("can not convert parameter %s -> %s for method %s",
                                               paramSource.getType(), paramTarget.getType(), method));
                }
                ActionParameter desc = null;
                for (Annotation annotation : method.getParameterAnnotations()[i]) {
                    if (ActionParameter.class.isInstance(annotation)) {
                        desc = ActionParameter.class.cast(annotation);
                        break;
                    }
                }
                if (desc == null) {
                    throw new RuntimeException(String.format("missing @%s annotation on %s'th parameter", ActionParameter.class, i));
                }
                this.parameters[i] = new ConvertibleMethodParameter(paramTarget, desc);
            }
        }
    }

    public ActionResult invoke(Action action) {
        try {
            Object[] params = createInvocationParameters(action);
            Object methodResult = method.invoke(bean, params);
            ActionResult result = (ActionResult) conversionService.convert(methodResult, returnSource, returnTarget);
            return result != null ? result : ActionResult.SUCCESS;
        } catch (Exception e) {
            ReflectionUtils.handleReflectionException(e);
            return null;
        }
    }

    Object[] createInvocationParameters(Action action) {
        Object[] result = new Object[parameters.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = parameters[i].getValue(action);
        }
        return result;
    }

    public Object getBean() {
        return bean;
    }

    public static abstract interface MethodParameter {
        Object getValue(Action action);
    }

    public class ConvertibleMethodParameter implements MethodParameter {
        private final TypeDescriptor targetType;
        private final ActionParameter desc;

        public ConvertibleMethodParameter(TypeDescriptor targetType, ActionParameter desc) {
            this.targetType = targetType;
            this.desc = desc;
        }

        @Override
        public Object getValue(Action action) {
            String value = action.getParameterValue(desc.value());

            if (desc.required() && value == null) {
                throw new IllegalArgumentException("missing required parameter: "+ desc.value());
            }

            if (value == null && !ValueConstants.DEFAULT_NONE.equals(desc.defaultValue())) {
                value = desc.defaultValue();
            }

            return conversionService.convert(value, paramSource, targetType);
        }
    }

}


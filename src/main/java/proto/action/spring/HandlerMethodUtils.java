package proto.action.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;
import proto.action.Action;
import proto.action.ActionResult;
import proto.action.annotation.ActionParameter;
import proto.action.annotation.ValueConstants;
import proto.action.spring.HandlerMethod.MethodParameterConverter;
import proto.action.spring.HandlerMethod.ResultConverter;

/**
 * Defines the algorithm for searching handler methods exhaustively including interfaces and parent classes while also
 * dealing with parameterized methods as well as interface and class-based proxies.
 * 
 * @author Rossen Stoyanchev
 * @since 3.1
 */
abstract class HandlerMethodUtils {
    private static final TypeDescriptor RESULT_TARGET = TypeDescriptor.valueOf(ActionResult.class);
    private static final ResultConverter<Void> VOID_RESULT_CONVERTER = new ResultConverter<Void>() {
        @Override public ActionResult convert(Void result) { return ActionResult.SUCCESS; }
    };
    private static final ResultConverter<ActionResult> ACTION_RESULT_CONVERTER = new ResultConverter<ActionResult>() {
        @Override public ActionResult convert(ActionResult result) { return result; }
    };
    private static final TypeDescriptor PARAMETER_SOURCE = TypeDescriptor.valueOf(String.class);
    private static final MethodParameterConverter actionMethodParameter = new MethodParameterConverter() {
        @Override public Action getValue(Action action) { return action; }
    };
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new AnnotatedParameterNameDiscoverer(
            new LocalVariableTableParameterNameDiscoverer());

    /**
     * Selects handler methods for the given handler type. Callers of this method define handler methods of interest
     * through the {@link MethodFilter} parameter.
     * 
     * @param handlerType
     *            the handler type to search handler methods on
     * @param handlerMethodFilter
     *            a {@link MethodFilter} to help recognize handler methods of interest
     * @return the selected methods, or an empty set
     */
    public static Set<Method> selectMethods(final Class<?> handlerType, final MethodFilter handlerMethodFilter) {
        final Set<Method> handlerMethods = new LinkedHashSet<Method>();
        Set<Class<?>> handlerTypes = new LinkedHashSet<Class<?>>();
        Class<?> specificHandlerType = null;
        if (!Proxy.isProxyClass(handlerType)) {
            handlerTypes.add(handlerType);
            specificHandlerType = handlerType;
        }
        handlerTypes.addAll(Arrays.asList(handlerType.getInterfaces()));
        for (Class<?> currentHandlerType : handlerTypes) {
            final Class<?> targetClass = (specificHandlerType != null ? specificHandlerType : currentHandlerType);
            ReflectionUtils.doWithMethods(currentHandlerType, new ReflectionUtils.MethodCallback() {
                @Override
                public void doWith(Method method) {
                    Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
                    Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
                    if (handlerMethodFilter.matches(specificMethod)
                            && (bridgedMethod == specificMethod || !handlerMethodFilter.matches(bridgedMethod))) {
                        handlerMethods.add(specificMethod);
                    }
                }
            }, ReflectionUtils.USER_DECLARED_METHODS);
        }
        return handlerMethods;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Object> ResultConverter<T> createResultConverter(Method method, ConversionService conversionService) {
        ResultConverter<T> resultConverter;
        Class<?> returnType = method.getReturnType();
        if (Void.class.isAssignableFrom(returnType)
                || Void.TYPE.isAssignableFrom(returnType)) {
            resultConverter = (ResultConverter<T>) VOID_RESULT_CONVERTER;
        } else if (ActionResult.class.isAssignableFrom(returnType)) {
            resultConverter = (ResultConverter<T>) ACTION_RESULT_CONVERTER;
        } else {
            TypeDescriptor returnSource = TypeDescriptor.valueOf(method.getReturnType());
            if (!conversionService.canConvert(returnSource, RESULT_TARGET)) {
                throw new RuntimeException(String.format("can not convert result %s -> %s for method %s",
                                           returnSource.getType(), RESULT_TARGET.getType(), method));
            }
            resultConverter = new ConvertibleResultConverter<T>(returnSource, conversionService);
        }
        return resultConverter;
    }

    public static MethodParameterConverter[] createMethodParameterConverters(Method method, ConversionService conversionService) {
        MethodParameterConverter[] parameters = new MethodParameterConverter[method.getParameterTypes().length];
        Class<?>[] parameterTypes = method.getParameterTypes();
        String[] parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
        ActionParameter[] parameterAnnotations = getParameterAnnotations(method, ActionParameter.class);
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (Action.class.isAssignableFrom(parameterType)) {
                parameters[i] = actionMethodParameter;
            } else {
                TypeDescriptor paramTarget = TypeDescriptor.valueOf(parameterType);
                if (!conversionService.canConvert(PARAMETER_SOURCE, paramTarget)) {
                    throw new RuntimeException(String.format("can not convert parameter %s -> %s for method %s",
                                                             PARAMETER_SOURCE.getType(), paramTarget.getType(), method));
                }
                if (parameterNames[i] == null || parameterNames[i].length() == 0) {
                    throw new RuntimeException(String.format("can not determine %s'th parameter name for method %s", i, method));
                }
                ActionParameter desc = parameterAnnotations[i];
                if (desc == null) {
                    throw new RuntimeException(String.format("missing @%s annotation on %s'th parameter for method %s",
                                                             ActionParameter.class, i, method));
                }
                parameters[i] = new ConvertibleMethodParameter(paramTarget, desc, parameterNames[i], conversionService);
            }
        }
        return parameters;
    }

    private static <T extends Annotation> T[] getParameterAnnotations(Method method, Class<T> annotationClass) {
        Annotation[][] annotations = method.getParameterAnnotations();
        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(annotationClass, annotations.length);
        for (int i = 0; i < annotations.length; i++) {
            for (int j = 0; j < annotations[i].length; j++) {
                Annotation annotation = annotations[i][j];
                if (annotationClass.isInstance(annotation)) {
                    result[i] = annotationClass.cast(annotation);
                    break;
                }
            }
        }
        return result;
    }

    public static final class AnnotatedParameterNameDiscoverer implements ParameterNameDiscoverer {
        private final ParameterNameDiscoverer parent;

        public AnnotatedParameterNameDiscoverer(ParameterNameDiscoverer parent) {
            this.parent = parent;
        }

        @Override
        public String[] getParameterNames(Constructor<?> ctor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String[] getParameterNames(Method method) {
            String[] result = getNamesFromAnnotation(method);
            String[] parents = parent.getParameterNames(method);
            if (parents == null) {
                return result;
            }

            for (int i = 0; i < result.length; i++) {
                if (result[i] == null || ValueConstants.DEFAULT_NONE.equals(result[i])) {
                    result[i] = parents[i];
                }
            }
            return result;
        }

        private String[] getNamesFromAnnotation(Method method) {
            ActionParameter[] annotations = getParameterAnnotations(method, ActionParameter.class);
            String[] result = new String[annotations.length];
            for (int i = 0; i < annotations.length; i++) {
                if(annotations[i] != null) {
                    String value = annotations[i].value();
                    result[i] = ValueConstants.DEFAULT_NONE.equals(value) ? null : value;
                    break;
                }
            }
            return result;
        }

    }

    public static class ConvertibleResultConverter<T> implements ResultConverter<T> {
        private final TypeDescriptor sourceType;
        private final ConversionService conversionService;

        public ConvertibleResultConverter(TypeDescriptor sourceType, ConversionService conversionService) {
            this.sourceType = sourceType;
            this.conversionService = conversionService;
        }

        @Override
        public ActionResult convert(T result) {
            return (ActionResult) conversionService.convert(result, sourceType, RESULT_TARGET);
        }
    }

    public static class ConvertibleMethodParameter implements MethodParameterConverter {
        private final ConversionService conversionService;
        private final TypeDescriptor targetType;
        private final String name;
        private final ActionParameter desc;

        public ConvertibleMethodParameter(TypeDescriptor targetType, ActionParameter desc, String name, ConversionService conversionService) {
            this.targetType = targetType;
            this.desc = desc;
            this.name = name;
            this.conversionService = conversionService;
        }

        @Override
        public Object getValue(Action action) {
            String value = action.getParameterValue(name);

            if (desc.required() && value == null) {
                throw new IllegalArgumentException("missing required parameter: "+ name);
            }

            if (value == null && !ValueConstants.DEFAULT_NONE.equals(desc.defaultValue())) {
                value = desc.defaultValue();
            }

            return conversionService.convert(value, PARAMETER_SOURCE, targetType);
        }
    }

}

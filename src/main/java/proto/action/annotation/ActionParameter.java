package proto.action.annotation;


public @interface ActionParameter {
    /**
     * The name of the request parameter to bind to.
     */
    String value() default "";

    /**
     * Whether the parameter is required.
     * <p>Default is <code>true</code>, leading to an exception thrown in case
     * of the parameter missing in the request. Switch this to <code>false</code>
     * if you prefer a <code>null</value> in case of the parameter missing.
     * <p>Alternatively, provide a {@link #defaultValue() defaultValue},
     * which implicitly sets this flag to <code>false</code>.
     */
    boolean required() default true;

    /**
     * The default value to use as a fallback. Supplying a default value implicitly
     * sets {@link #required()} to false.
     */
    String defaultValue() default "\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n";
}

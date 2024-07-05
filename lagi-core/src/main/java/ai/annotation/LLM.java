package ai.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LLM {
    String company() default "";
    String[] modelNames() default {};
}

package ai.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Text2Video {
    String company() default "";
    String[] modelNames() default {};
}

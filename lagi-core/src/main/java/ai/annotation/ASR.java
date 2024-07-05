package ai.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ASR  {
    String company() default "";
    String[] modelNames() default {};
}

package ai.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Img2Text {
    String company() default "";
    String[] modelNames() default {};
}

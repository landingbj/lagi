package ai.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
* @program: annotate for url routing
*
* @description: get request routing
*
* @author: linzhen
*
* @create: 2023-06-29 09:00
**/
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface  Get {
	String value() default "";
}

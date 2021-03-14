package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated parameter is an index which is 1-based in Lua and 0-based in Java. The index will be
 * automatically shifted when calling the method.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface IndexArgument
{
}

/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package invtweaks.api.container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker for containers that have a chest-like persistent storage component. Enables the Inventory Tweaks sorting
 * buttons for this container.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ChestContainer
{
	// Set to true if the Inventory Tweaks sorting buttons should be shown for this container.
	boolean showButtons() default true;

	// Size of a chest row
	int rowSize() default 9;

	// Uses 'large chest' mode for sorting buttons
	// (Renders buttons vertically down the right side of the GUI)
	boolean isLargeChest() default false;

	// Annotation for method to get size of a chest row if it is not a fixed size for this container class
	// Signature int func()
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface RowSizeCallback
	{
	}

	// Annotation for method to get size of a chest row if it is not a fixed size for this container class
	// Signature int func()
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface IsLargeCallback
	{
	}
}

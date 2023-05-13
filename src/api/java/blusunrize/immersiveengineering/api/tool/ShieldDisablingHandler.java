/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ShieldDisablingHandler
{
	private static final Map<Class<? extends LivingEntity>, Consumer<? extends LivingEntity>> disablingFunctions = new HashMap<>();

	public static <E extends LivingEntity> void registerDisablingFunction(Class<E> clazz, Consumer<E> function) {
		disablingFunctions.put(clazz, function);
	}

	@SuppressWarnings("unchecked")
	public static  <E extends LivingEntity> void attemptDisabling(E target) {
		disablingFunctions.forEach((aClass, consumer) -> {
			if(aClass.isAssignableFrom(target.getClass()))
				((Consumer<E>)consumer).accept(target);
		});
	}
}

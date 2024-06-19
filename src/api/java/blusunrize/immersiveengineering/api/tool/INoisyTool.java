/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

public interface INoisyTool
{
	public Holder<SoundEvent> getIdleSound();

	public Holder<SoundEvent> getBusySound();

	public Holder<SoundEvent> getAttackSound();

	public Holder<SoundEvent> getHarvestSound();

	public boolean ableToMakeNoise(ItemStack stack);

	public static boolean isAbleNoisyTool(ItemStack stack)
	{
		return stack.getItem() instanceof INoisyTool noisyTool && noisyTool.ableToMakeNoise(stack);
	}
}

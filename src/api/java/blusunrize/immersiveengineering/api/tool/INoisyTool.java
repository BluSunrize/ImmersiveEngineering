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
	public static final float TEST_VOLUME_ADJUSTMENT = 1.0f; //temporary measure, remove after settling on a volume for the PR and re-adjusting the sounds themselves

	public Holder<SoundEvent> getIdleSound(ItemStack stack);

	public Holder<SoundEvent> getBusySound(ItemStack stack);

	public Holder<SoundEvent> getFadingSound(ItemStack stack);

	public Holder<SoundEvent> getAttackSound(ItemStack stack);

	public Holder<SoundEvent> getHarvestSound(ItemStack stack);

	public boolean ableToMakeNoise(ItemStack stack);

	public static boolean isAbleNoisyTool(ItemStack stack)
	{
		return stack.getItem() instanceof INoisyTool noisyTool && noisyTool.ableToMakeNoise(stack);
	}
}

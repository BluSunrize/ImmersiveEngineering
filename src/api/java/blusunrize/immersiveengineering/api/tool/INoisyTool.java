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

	/**
	 * Due to lacking information on sound duration, the duration is hard coded. Any Fading sounds need to be <b>more</b> than <b>1.0s</b> in duration.
	 * The sound cuts off after <b>1.0s</b>, but a little bit of excess duration (~0.01s) is required for the noisy tool sound stage machine to work correctly
	 *
	 * @param stack
	 * @return fading sound
	 */
	public Holder<SoundEvent> getFadingSound(ItemStack stack);

	/**
	 * Due to lacking information on sound duration, the duration is hard coded. Any Attack sounds need to be <b>more</b> than <b>0.35s</b> in duration.
	 * The sound cuts off after <b>0.35s</b>, but a little bit of excess duration (~0.01s) is required for the noisy tool sound stage machine to work correctly
	 *
	 * @param stack
	 * @return attack sound
	 */
	public Holder<SoundEvent> getAttackSound(ItemStack stack);

	public Holder<SoundEvent> getHarvestSound(ItemStack stack);

	public boolean ableToMakeNoise(ItemStack stack);

	public static boolean isAbleNoisyTool(ItemStack stack)
	{
		return stack.getItem() instanceof INoisyTool noisyTool&&noisyTool.ableToMakeNoise(stack);
	}
}

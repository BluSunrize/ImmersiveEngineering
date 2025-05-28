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
	static final float TEST_VOLUME_ADJUSTMENT = 1.0f; //TODO: temporary measure, remove after settling on a volume for the PR and re-adjusting the sounds themselves

	Holder<SoundEvent> getIdleSound(ItemStack stack);

	Holder<SoundEvent> getBusySound(ItemStack stack);

	/**
	 * Due to lacking information on sound duration, the duration is hard coded. Any Fading sounds need to be <b>more</b> than <b>1.0s</b> in duration.
	 * The sound cuts off after <b>1.0s</b>, but a little bit of excess duration (>~0.01s) is required for the noisy tool sound stage machine to work correctly
	 *
	 * @param stack
	 * @return fading sound
	 */
	Holder<SoundEvent> getFadingSound(ItemStack stack);

	/**
	 * Due to lacking information on sound duration, the duration is hard coded. Any Attack sounds need to be <b>more</b> than <b>0.3s</b> in duration.
	 * The sound cuts off after <b>0.3s</b>, but a little bit of excess duration (>~0.01s) is required for the noisy tool sound stage machine to work correctly
	 * Having a too small excess duration leads to notable gaps in the audio when transitioning, which is why the default attack sounds have ~0.06s extra.
	 * Cause they used to be 0.35s and then it caused issues.. Take heed ^^
	 *
	 * @param stack
	 * @return attack sound
	 */
	Holder<SoundEvent> getAttackSound(ItemStack stack);

	Holder<SoundEvent> getHarvestSound(ItemStack stack);

	boolean ableToMakeNoise(ItemStack stack);

	static boolean isAbleNoisyTool(ItemStack stack)
	{
		return stack.getItem() instanceof INoisyTool noisyTool&&noisyTool.ableToMakeNoise(stack);
	}

	/**
	 * When an ItemStack gets modified server side (i.e. takes damage, changes tags (i.e. uses fuel), etc.), it creates a new ItemStack on the client side.
	 * There is no unreasonably involved way to check if the new ItemStack is actually just the old ItemStack, but modified.
	 * So this for these cases, this default implementation checks the next best thing: Item equality and sound equality.
	 *
	 * It is encouraged to override this with a simpler check.
	 *
	 * This check also assumes, that it has already been checked and confirmed, that the stacks are not identical
	 *
	 * @param mainStack
	 * @param otherStack
	 * @return true if stacks are considered the same stack. By default: if stacks  produce the same sounds.
	 */
	default boolean noisySameStack(ItemStack mainStack, ItemStack otherStack)
	{
		return mainStack.getItem() instanceof INoisyTool noisyTool&&noisyTool.equals(otherStack.getItem())
				&&noisyTool.getIdleSound(mainStack).equals(noisyTool.getIdleSound(otherStack))
				&&noisyTool.getBusySound(mainStack).equals(noisyTool.getBusySound(otherStack))
				&&noisyTool.getFadingSound(mainStack).equals(noisyTool.getFadingSound(otherStack))
				&&noisyTool.getAttackSound(mainStack).equals(noisyTool.getAttackSound(otherStack))
				&&noisyTool.getHarvestSound(mainStack).equals(noisyTool.getHarvestSound(otherStack));
	}
}

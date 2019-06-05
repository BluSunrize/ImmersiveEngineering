/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import net.minecraft.client.audio.ITickableSound;

public class IEMuffledTickableSound extends IEMuffledSound implements ITickableSound
{
	final ITickableSound originalSoundTickable;

	public IEMuffledTickableSound(ITickableSound originalSound, float volumeMod)
	{
		super(originalSound, volumeMod);
		originalSoundTickable = originalSound;
	}

	@Override
	public boolean isDonePlaying()
	{
		return originalSoundTickable.isDonePlaying();
	}

	@Override
	public void tick()
	{
		originalSoundTickable.tick();
	}
}
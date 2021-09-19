/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import net.minecraft.client.resources.sounds.TickableSoundInstance;

public class IEMuffledTickableSound extends IEMuffledSound implements TickableSoundInstance
{
	final TickableSoundInstance originalSoundTickable;

	public IEMuffledTickableSound(TickableSoundInstance originalSound, float volumeMod)
	{
		super(originalSound, volumeMod);
		originalSoundTickable = originalSound;
	}

	@Override
	public boolean isStopped()
	{
		return originalSoundTickable.isStopped();
	}

	@Override
	public void tick()
	{
		originalSoundTickable.tick();
	}
}
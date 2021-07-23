/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

import javax.annotation.Nullable;

public class IEMuffledSound implements SoundInstance
{
	SoundInstance originalSound;
	float volumeMod;

	public IEMuffledSound(SoundInstance originalSound, float volumeMod)
	{
		this.originalSound = originalSound;
		this.volumeMod = volumeMod;
	}

	@Override
	public ResourceLocation getLocation()
	{
		return originalSound.getLocation();
	}

	@Nullable
	@Override
	public WeighedSoundEvents resolve(SoundManager handler)
	{
		return originalSound.resolve(handler);
	}

	@Override
	public Sound getSound()
	{
		return originalSound.getSound();
	}

	@Override
	public SoundSource getSource()
	{
		return originalSound.getSource();
	}

	@Override
	public boolean isLooping()
	{
		return originalSound.isLooping();
	}

	@Override
	public boolean isRelative()
	{
		return originalSound.isRelative();
	}

	@Override
	public int getDelay()
	{
		return originalSound.getDelay();
	}

	@Override
	public float getVolume()
	{
		return originalSound.getVolume()*volumeMod;
	}

	@Override
	public float getPitch()
	{
		return originalSound.getPitch();
	}

	@Override
	public double getX()
	{
		return originalSound.getX();
	}

	@Override
	public double getY()
	{
		return originalSound.getY();
	}

	@Override
	public double getZ()
	{
		return originalSound.getZ();
	}

	@Override
	public Attenuation getAttenuation()
	{
		return originalSound.getAttenuation();
	}
}
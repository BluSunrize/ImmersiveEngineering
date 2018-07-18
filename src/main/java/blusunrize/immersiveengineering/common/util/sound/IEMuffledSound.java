/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

import javax.annotation.Nullable;

public class IEMuffledSound implements ISound
{
	ISound originalSound;
	float volumeMod;

	public IEMuffledSound(ISound originalSound, float volumeMod)
	{
		this.originalSound = originalSound;
		this.volumeMod = volumeMod;
	}

	@Override
	public ResourceLocation getSoundLocation()
	{
		return originalSound.getSoundLocation();
	}

	@Nullable
	@Override
	public SoundEventAccessor createAccessor(SoundHandler handler)
	{
		return originalSound.createAccessor(handler);
	}

	@Override
	public Sound getSound()
	{
		return originalSound.getSound();
	}

	@Override
	public SoundCategory getCategory()
	{
		return originalSound.getCategory();
	}

	@Override
	public boolean canRepeat()
	{
		return originalSound.canRepeat();
	}

	@Override
	public int getRepeatDelay()
	{
		return originalSound.getRepeatDelay();
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
	public float getXPosF()
	{
		return originalSound.getXPosF();
	}

	@Override
	public float getYPosF()
	{
		return originalSound.getYPosF();
	}

	@Override
	public float getZPosF()
	{
		return originalSound.getZPosF();
	}

	@Override
	public AttenuationType getAttenuationType()
	{
		return originalSound.getAttenuationType();
	}
}
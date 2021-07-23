/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import blusunrize.immersiveengineering.common.entities.SkylineHookEntity;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SkyhookSound implements TickableSoundInstance
{
	private final SkylineHookEntity hook;
	private final ResourceLocation soundLoc;
	private Sound sound;
	private float speed = .01F;


	public SkyhookSound(SkylineHookEntity hook, ResourceLocation soundLoc)
	{
		this.hook = hook;
		this.soundLoc = soundLoc;
	}

	@Override
	public boolean isStopped()
	{
		return !hook.isAlive();
	}

	@Nonnull
	@Override
	public ResourceLocation getLocation()
	{
		return soundLoc;
	}

	@Nullable
	@Override
	public WeighedSoundEvents resolve(@Nonnull SoundManager handler)
	{
		WeighedSoundEvents soundEvent = handler.getSoundEvent(this.soundLoc);
		if(soundEvent==null)
			this.sound = SoundManager.EMPTY_SOUND;
		else
			this.sound = soundEvent.getSound();
		return soundEvent;
	}

	@Nonnull
	@Override
	public Sound getSound()
	{
		return sound;
	}

	@Nonnull
	@Override
	public SoundSource getSource()
	{
		return SoundSource.NEUTRAL;
	}

	@Override
	public boolean isLooping()
	{
		return true;
	}

	@Override
	public int getDelay()
	{
		return 0;
	}

	@Override
	public float getVolume()
	{
		return Math.min(speed, .75F);
	}

	@Override
	public float getPitch()
	{
		return Math.min(.5F*speed, .75F);
	}

	@Override
	public double getX()
	{
		return (float)hook.getX();
	}

	@Override
	public double getY()
	{
		return (float)hook.getY();
	}

	@Override
	public double getZ()
	{
		return (float)hook.getZ();
	}

	@Nonnull
	@Override
	public Attenuation getAttenuation()
	{
		return Attenuation.LINEAR;
	}

	@Override
	public void tick()
	{
		speed = (float)hook.getSpeed();
		if(speed < .01)
			speed = .01F;
	}

	@Override
	public boolean isRelative()
	{
		return false;
	}
}

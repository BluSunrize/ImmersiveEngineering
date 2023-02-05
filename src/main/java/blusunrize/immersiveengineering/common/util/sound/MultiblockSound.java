/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BooleanSupplier;

public class MultiblockSound extends AbstractTickableSoundInstance
{
	private static final float ACTIVE_VOLUME = 0.5f;
	private final BooleanSupplier active;
	private final BooleanSupplier valid;

	public MultiblockSound(
			BooleanSupplier active, BooleanSupplier valid, Vec3 pos, SoundEvent sound, boolean loop
	)
	{
		super(sound, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
		this.active = active;
		this.valid = valid;
		this.x = pos.x;
		this.y = pos.y;
		this.z = pos.z;
		this.looping = loop;
		this.volume = 0;
	}

	public static BooleanSupplier startSound(
			BooleanSupplier active, BooleanSupplier valid, Vec3 pos, RegistryObject<SoundEvent> sound
	)
	{
		return startSound(active, valid, pos, sound, true);
	}

	public static BooleanSupplier startSound(
			BooleanSupplier active, BooleanSupplier valid, Vec3 pos, RegistryObject<SoundEvent> sound, boolean loop
	)
	{
		final MultiblockSound instance = new MultiblockSound(active, valid, pos, sound.get(), loop);
		final SoundManager soundManager = Minecraft.getInstance().getSoundManager();
		soundManager.play(instance);
		if(loop)
			return () -> !instance.isStopped();
		else
			return () -> soundManager.isActive(instance);
	}

	@Override
	public boolean canStartSilent()
	{
		return true;
	}

	@Override
	public void tick()
	{
		if(!valid.getAsBoolean())
			this.stop();
		else if(this.active.getAsBoolean())
			this.volume = ACTIVE_VOLUME;
		else
			this.volume = 0;
	}
}

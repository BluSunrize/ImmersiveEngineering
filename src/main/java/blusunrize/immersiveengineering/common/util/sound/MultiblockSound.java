/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.mixin.accessors.client.GuiSubtitleOverlayAccess;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BooleanSupplier;

public class MultiblockSound extends AbstractTickableSoundInstance
{
	private final BooleanSupplier active;
	private final BooleanSupplier valid;
	private final float maxVolume;
	private long subtitleMillis;

	public MultiblockSound(
			BooleanSupplier active, BooleanSupplier valid, Vec3 pos, SoundEvent sound, boolean loop, float maxVolume
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
		this.maxVolume = maxVolume;
		this.subtitleMillis = Util.getMillis();
	}

	public static BooleanSupplier startSound(
			BooleanSupplier active, BooleanSupplier valid, Vec3 pos, RegistryObject<SoundEvent> sound, float maxVolume
	)
	{
		return startSound(active, valid, pos, sound, true, maxVolume);
	}

	public static BooleanSupplier startSound(
			BooleanSupplier active, BooleanSupplier valid, Vec3 pos, RegistryObject<SoundEvent> sound, boolean loop, float maxVolume
	)
	{
		final MultiblockSound instance = new MultiblockSound(active, valid, pos, sound.get(), loop, maxVolume);
		final SoundManager soundManager = Minecraft.getInstance().getSoundManager();
		soundManager.play(instance);
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
		{
			long currentMillis = Util.getMillis();
			// refresh subtitle every second
			if(currentMillis-this.subtitleMillis > 1000)
			{
				final SoundManager soundManager = Minecraft.getInstance().getSoundManager();
				WeighedSoundEvents weighedsoundevents = this.resolve(soundManager);
				if(weighedsoundevents!=null)
					((GuiSubtitleOverlayAccess)ClientUtils.mc().gui).getSubtitleOverlay().onPlaySound(this, weighedsoundevents);
				this.subtitleMillis = currentMillis;
			}
			this.volume = maxVolume;
		}
		else
			this.volume = 0;
	}
}

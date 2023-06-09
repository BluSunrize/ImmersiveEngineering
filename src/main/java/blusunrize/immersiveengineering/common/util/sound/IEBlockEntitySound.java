/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundBE;
import blusunrize.immersiveengineering.common.items.EarmuffsItem;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class IEBlockEntitySound implements TickableSoundInstance
{
	protected Sound sound;
	private final ResourceLocation resource;
	private final float volume;
	private final float pitch;

	private final BlockPos tilePos;
	private float volumeAjustment = 1;
	public boolean donePlaying = false;

	public IEBlockEntitySound(SoundEvent event, float volume, float pitch, BlockPos pos)
	{
		this.resource = event.getLocation();
		this.volume = volume;
		this.pitch = pitch;
		this.tilePos = pos;
	}

	@Override
	public Attenuation getAttenuation()
	{
		return Attenuation.LINEAR;
	}

	@Override
	public ResourceLocation getLocation()
	{
		return resource;
	}

	@Nullable
	@Override
	public WeighedSoundEvents resolve(SoundManager handler)
	{
		WeighedSoundEvents soundEvent = handler.getSoundEvent(this.resource);
		if(soundEvent==null)
			this.sound = SoundManager.EMPTY_SOUND;
		else
			this.sound = soundEvent.getSound(ApiUtils.RANDOM_SOURCE);
		return soundEvent;
	}

	@Override
	public Sound getSound()
	{
		return sound;
	}

	@Override
	public SoundSource getSource()
	{
		return SoundSource.BLOCKS;
	}

	@Override
	public float getVolume()
	{
		return volume*volumeAjustment;
	}

	@Override
	public float getPitch()
	{
		return pitch;
	}

	@Override
	public double getX()
	{
		return tilePos.getX();
	}

	@Override
	public double getY()
	{
		return tilePos.getY();
	}

	@Override
	public double getZ()
	{
		return tilePos.getZ();
	}

	@Override
	public boolean isLooping()
	{
		return true;
	}

	@Override
	public boolean isRelative()
	{
		return false;
	}

	@Override
	public int getDelay()
	{
		return 0;
	}

	public void evaluateVolume()
	{
		volumeAjustment = 1f;
		if(ClientUtils.mc().player!=null)
		{
			ItemStack earmuffs = EarmuffsItem.EARMUFF_GETTERS.getFrom(ClientUtils.mc().player);
			if(!earmuffs.isEmpty())
				volumeAjustment = EarmuffsItem.getVolumeMod(earmuffs);
		}

		BlockEntity tile = ClientUtils.mc().player.level().getBlockEntity(tilePos);
		if(!(tile instanceof ISoundBE soundBE))
			donePlaying = true;
		else
		{
			donePlaying = !soundBE.shouldPlaySound(resource.toString());
			if(!donePlaying)
			{
				float radiusSq = soundBE.getSoundRadiusSq();
				if(ClientUtils.mc().player!=null)
				{
					double distSq = ClientUtils.mc().player.distanceToSqr(Vec3.atCenterOf(tilePos));
					if(distSq>radiusSq)
						donePlaying = true;
					else
						volumeAjustment *= (radiusSq-distSq)/radiusSq;
				}
			}
		}
	}


	@Override
	public void tick()
	{
		if(ClientUtils.mc().player!=null&&ClientUtils.mc().player.level().getGameTime()%40==0)
			evaluateVolume();
	}

	@Override
	public boolean isStopped()
	{
		return donePlaying;
	}
}
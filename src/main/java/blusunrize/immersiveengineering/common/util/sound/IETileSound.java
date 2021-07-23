/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.items.EarmuffsItem;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.compat.CuriosCompatModule;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;

public class IETileSound implements TickableSoundInstance
{
	protected Sound sound;
	private WeighedSoundEvents soundEvent;
	private SoundSource category;
	public Attenuation attenuation;
	public final ResourceLocation resource;
	public float volume;
	public float pitch;

	public int tileX;
	public int tileY;
	public int tileZ;
	public boolean canRepeat;
	public int repeatDelay;
	public float volumeAjustment = 1;


	public IETileSound(SoundEvent event, float volume, float pitch, boolean repeat, int repeatDelay, int x, int y, int z, Attenuation attenuation, SoundSource category)
	{
		this(event.getLocation(), volume, pitch, repeat, repeatDelay, x, y, z, attenuation, category);
	}

	public IETileSound(ResourceLocation sound, float volume, float pitch, boolean repeat, int repeatDelay, int x, int y, int z, Attenuation attenuation, SoundSource category)
	{
		this.attenuation = attenuation;
		this.resource = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.tileX = x;
		this.tileY = y;
		this.tileZ = z;
		this.canRepeat = repeat;
		this.repeatDelay = repeatDelay;
		origPos = new float[]{(float)x, (float)y, (float)z};
		this.category = category;
	}

	public IETileSound(SoundEvent event, float volume, float pitch, boolean repeat, int repeatDelay, BlockPos pos, Attenuation attenuation, SoundSource category)
	{
		this(event.getLocation(), volume, pitch, repeat, repeatDelay, pos.getX(), pos.getY(), pos.getZ(), attenuation, category);
	}

	public IETileSound(ResourceLocation sound, float volume, float pitch, boolean repeat, int repeatDelay, BlockPos pos, Attenuation attenuation, SoundSource category)
	{
		this(sound, volume, pitch, repeat, repeatDelay, pos.getX(), pos.getY(), pos.getZ(), attenuation, category);
	}

	public float[] origPos;

	@Override
	public Attenuation getAttenuation()
	{
		return attenuation;
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
		this.soundEvent = handler.getSoundEvent(this.resource);
		if(this.soundEvent==null)
			this.sound = SoundManager.EMPTY_SOUND;
		else
			this.sound = this.soundEvent.getSound();
		return this.soundEvent;
	}

	@Override
	public Sound getSound()
	{
		return sound;
	}

	@Override
	public SoundSource getSource()
	{
		return category;
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
		return tileX;
	}

	@Override
	public double getY()
	{
		return tileY;
	}

	@Override
	public double getZ()
	{
		return tileZ;
	}

	@Override
	public boolean isLooping()
	{
		return canRepeat;
	}

	@Override
	public boolean isRelative()
	{
		return false;
	}

	@Override
	public int getDelay()
	{
		return repeatDelay;
	}

	public void evaluateVolume()
	{
		volumeAjustment = 1f;
		if(ClientUtils.mc().player!=null&&!ClientUtils.mc().player.getItemBySlot(EquipmentSlot.HEAD).isEmpty())
		{
			ItemStack head = ClientUtils.mc().player.getItemBySlot(EquipmentSlot.HEAD);
			ItemStack earmuffs = ItemStack.EMPTY;
			if(!head.isEmpty()&&(head.getItem()==Misc.earmuffs.get()||ItemNBTHelper.hasKey(head, Lib.NBT_Earmuffs)))
				earmuffs = head.getItem()==Misc.earmuffs.get()?head: ItemNBTHelper.getItemStack(head, Lib.NBT_Earmuffs);
			else if(ModList.get().isLoaded("curios"))
				earmuffs = CuriosCompatModule.getEarmuffs(ClientUtils.mc().player);

			if(!earmuffs.isEmpty())
				volumeAjustment = EarmuffsItem.getVolumeMod(earmuffs);
		}
		//TODO uncomment when XU updates and maybe look for a better solution (API)
		//if(volumeAjustment > .1f)
		//	for(int dx = (int)Math.floor(tileX-8) >> 4; dx <= (int)Math.floor(tileX+8) >> 4; dx++)
		//		for(int dz = (int)Math.floor(tileZ-8) >> 4; dz <= (int)Math.floor(tileZ+8) >> 4; dz++)
		//		{
		//			Iterator it = ClientUtils.mc().player.world.getChunk(dx, dz).getTileEntityMap().values().iterator();
		//			while(it.hasNext())
		//			{
		//				TileEntity tile = (TileEntity)it.next();
		//				if(tile!=null&&tile.getClass().getName().endsWith("TileEntitySoundMuffler"))
		//					if(tile.getBlockMetadata()!=1)
		//					{
		//						double d = tile.getDistanceSq(tileX, tileY, tileZ);
		//						if(d <= 64&&d > 0)
		//							volumeAjustment = .1f;
		//					}
		//			}
		//		}

		BlockEntity tile = ClientUtils.mc().player.level.getBlockEntity(new BlockPos(tileX, tileY, tileZ));
		if(!(tile instanceof ISoundTile))
			donePlaying = true;
		else
		{
			donePlaying = !((ISoundTile)tile).shouldPlaySound(resource.toString());
			if(!donePlaying)
			{
				float radiusSq = ((ISoundTile)tile).getSoundRadiusSq();
				if(ClientUtils.mc().player!=null)
				{
					double distSq = ClientUtils.mc().player.distanceToSqr(tileX, tileY, tileZ);
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
		if(ClientUtils.mc().player!=null&&ClientUtils.mc().player.level.getGameTime()%40==0)
			evaluateVolume();
	}

	public boolean donePlaying = false;

	@Override
	public boolean isStopped()
	{
		return donePlaying;
	}
}
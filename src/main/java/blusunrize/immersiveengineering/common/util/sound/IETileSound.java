/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.items.ItemEarmuffs;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Iterator;

public class IETileSound implements ITickableSound
{
	protected Sound sound;
	private SoundEventAccessor soundEvent;
	private SoundCategory category;
	public AttenuationType attenuation;
	public final ResourceLocation resource;
	public float volume;
	public float pitch;

	public int tileX;
	public int tileY;
	public int tileZ;
	public boolean canRepeat;
	public int repeatDelay;
	public float volumeAjustment = 1;


	public IETileSound(SoundEvent event, float volume, float pitch, boolean repeat, int repeatDelay, int x, int y, int z, AttenuationType attenuation, SoundCategory category)
	{
		this(event.getSoundName(), volume, pitch, repeat, repeatDelay, x, y, z, attenuation, category);
	}

	public IETileSound(ResourceLocation sound, float volume, float pitch, boolean repeat, int repeatDelay, int x, int y, int z, AttenuationType attenuation, SoundCategory category)
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

	public IETileSound(SoundEvent event, float volume, float pitch, boolean repeat, int repeatDelay, BlockPos pos, AttenuationType attenuation, SoundCategory category)
	{
		this(event.getSoundName(), volume, pitch, repeat, repeatDelay, pos.getX(), pos.getY(), pos.getZ(), attenuation, category);
	}

	public IETileSound(ResourceLocation sound, float volume, float pitch, boolean repeat, int repeatDelay, BlockPos pos, AttenuationType attenuation, SoundCategory category)
	{
		this(sound, volume, pitch, repeat, repeatDelay, pos.getX(), pos.getY(), pos.getZ(), attenuation, category);
	}

	public float[] origPos;

	@Override
	public AttenuationType getAttenuationType()
	{
		return attenuation;
	}

	@Override
	public ResourceLocation getSoundLocation()
	{
		return resource;
	}

	@Nullable
	@Override
	public SoundEventAccessor createAccessor(SoundHandler handler)
	{
		this.soundEvent = handler.getAccessor(this.resource);
		if(this.soundEvent==null)
			this.sound = SoundHandler.MISSING_SOUND;
		else
			this.sound = this.soundEvent.cloneEntry();
		return this.soundEvent;
	}

	@Override
	public Sound getSound()
	{
		return sound;
	}

	@Override
	public SoundCategory getCategory()
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
	public float getXPosF()
	{
		return tileX;
	}

	@Override
	public float getYPosF()
	{
		return tileY;
	}

	@Override
	public float getZPosF()
	{
		return tileZ;
	}

	@Override
	public boolean canRepeat()
	{
		return canRepeat;
	}

	@Override
	public int getRepeatDelay()
	{
		return repeatDelay;
	}

	//	public void setPos(float x, float y, float z)
	//	{
	//		this.tileX=x;
	//		this.tileY=y;
	//		this.tileZ=z;
	//	}

	public void evaluateVolume()
	{
		volumeAjustment = 1f;
		if(ClientUtils.mc().player!=null&&!ClientUtils.mc().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty())
		{
			ItemStack stack = ClientUtils.mc().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
			if(ItemNBTHelper.hasKey(stack, "IE:Earmuffs"))
				stack = ItemNBTHelper.getItemStack(stack, "IE:Earmuffs");
			if(!stack.isEmpty()&&IEContent.itemEarmuffs.equals(stack.getItem()))
				volumeAjustment = ItemEarmuffs.getVolumeMod(stack);
		}
		if(volumeAjustment > .1f)
			for(int dx = (int)Math.floor(tileX-8) >> 4; dx <= (int)Math.floor(tileX+8) >> 4; dx++)
				for(int dz = (int)Math.floor(tileZ-8) >> 4; dz <= (int)Math.floor(tileZ+8) >> 4; dz++)
				{
					Iterator it = ClientUtils.mc().player.world.getChunk(dx, dz).getTileEntityMap().values().iterator();
					while(it.hasNext())
					{
						TileEntity tile = (TileEntity)it.next();
						if(tile!=null&&tile.getClass().getName().endsWith("TileEntitySoundMuffler"))
							if(tile.getBlockMetadata()!=1)
							{
								double d = tile.getDistanceSq(tileX, tileY, tileZ);
								if(d <= 64&&d > 0)
									volumeAjustment = .1f;
							}
					}
				}

		TileEntity tile = ClientUtils.mc().player.world.getTileEntity(new BlockPos(tileX, tileY, tileZ));
		if(!(tile instanceof ISoundTile))
			donePlaying = true;
		else
			donePlaying = !((ISoundTile)tile).shoudlPlaySound(resource.toString());
	}


	@Override
	public void update()
	{
		if(ClientUtils.mc().player!=null&&ClientUtils.mc().player.world.getTotalWorldTime()%40==0)
			evaluateVolume();
	}

	public boolean donePlaying = false;

	@Override
	public boolean isDonePlaying()
	{
		return donePlaying;
	}
}
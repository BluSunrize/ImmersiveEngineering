package blusunrize.immersiveengineering.common.util.sound;

import java.util.Iterator;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.items.ItemEarmuffs;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;

public class IETileSound implements ITickableSound
{
	public AttenuationType attenuation;
	public final ResourceLocation resource;
	public float volume;
	public float pitch;

	public int tileX;
	public int tileY;
	public int tileZ;
	public boolean canRepeat;
	public int repeatDelay;
	public float volumeAjustment=1;

	public IETileSound(ResourceLocation sound, float volume, float pitch, boolean repeat, int repeatDelay, int x, int y, int z, AttenuationType attenuation)
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
		origPos = new float[]{(float)x,(float)y,(float)z};
	}
	public IETileSound(ResourceLocation sound, float volume, float pitch, boolean repeat, int repeatDelay, BlockPos pos, AttenuationType attenuation)
	{
		this(sound, volume, pitch, repeat, repeatDelay, pos.getX(),pos.getY(),pos.getZ(), attenuation);
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
		volumeAjustment=1f;
		if(ClientUtils.mc().thePlayer!=null && ClientUtils.mc().thePlayer.getCurrentArmor(3)!=null)
		{
			ItemStack stack = ClientUtils.mc().thePlayer.getCurrentArmor(3);
			if(ItemNBTHelper.hasKey(stack,"IE:Earmuffs"))
				stack = ItemNBTHelper.getItemStack(stack, "IE:Earmuffs");
			if(stack!=null && IEContent.itemEarmuffs.equals(stack.getItem()))
				volumeAjustment = ItemEarmuffs.getVolumeMod(stack);
		}
		if(volumeAjustment>.1f)
			for(int dx = (int)Math.floor(tileX-8)>>4; dx<=(int)Math.floor(tileX+8)>>4; dx++)
				for(int dz = (int)Math.floor(tileZ-8)>>4; dz<=(int)Math.floor(tileZ+8)>>4; dz++)
				{
					Iterator it = ClientUtils.mc().thePlayer.worldObj.getChunkFromChunkCoords(dx, dz).getTileEntityMap().values().iterator();
					while (it.hasNext())
					{
						TileEntity tile = (TileEntity)it.next();
						if(tile!=null && tile.getClass().getName().endsWith("TileEntitySoundMuffler"))
							if(tile.getBlockMetadata()!=1)
							{
								double d = tile.getDistanceSq(tileX, tileY, tileZ);
								if(d<=64 && d>0)
									volumeAjustment=.1f;
							}
					}
				}

		TileEntity tile = ClientUtils.mc().thePlayer.worldObj.getTileEntity(new BlockPos(tileX,tileY,tileZ));
		if(!(tile instanceof ISoundTile))
			donePlaying = true;
		else
			donePlaying = !((ISoundTile)tile).shoudlPlaySound(resource.toString());
	}


	@Override
	public void update()
	{
		if(ClientUtils.mc().thePlayer!=null && ClientUtils.mc().thePlayer.worldObj.getTotalWorldTime()%40==0)
			evaluateVolume();
	}

	public boolean donePlaying=false;
	@Override
	public boolean isDonePlaying()
	{
		return donePlaying;
	}
}
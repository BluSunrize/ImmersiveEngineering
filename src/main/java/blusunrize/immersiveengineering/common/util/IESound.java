package blusunrize.immersiveengineering.common.util;

import java.util.Iterator;

import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class IESound implements ITickableSound
{
	public AttenuationType attenuation;
	public final ResourceLocation resource;
	public float volume;
	public float pitch;
	public float x;
	public float y;
	public float z;
	public boolean canRepeat;
	public int repeatDelay;
	public float volumeAjustment=1;

	public IESound(ResourceLocation sound, float volume, float pitch, boolean repeat, int repeatDelay, double x, double y, double z, AttenuationType attenuation)
	{
		this.attenuation = attenuation;
		this.resource = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
		this.canRepeat = repeat;
		this.repeatDelay = repeatDelay;
		origPos = new float[]{(float)x,(float)y,(float)z};
	}

	public float[] origPos;

	@Override
	public AttenuationType getAttenuationType()
	{
		return attenuation;
	}
	@Override
	public ResourceLocation getPositionedSoundLocation()
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
		return x;
	}
	@Override
	public float getYPosF()
	{
		return y;
	}
	@Override
	public float getZPosF()
	{
		return z;
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

	public void setPos(float x, float y, float z)
	{
		this.x=x;
		this.y=y;
		this.z=z;
	}

	public void evaluateVolume()
	{
		volumeAjustment=1f;
		for(int dx = (int)Math.floor(x-8)>>4; dx<=(int)Math.floor(x+8)>>4; dx++)
			for(int dz = (int)Math.floor(z-8)>>4; dz<=(int)Math.floor(z+8)>>4; dz++)
			{
				Iterator it = ClientUtils.mc().thePlayer.worldObj.getChunkFromChunkCoords(dx, dz).chunkTileEntityMap.values().iterator();
				while (it.hasNext())
				{
					TileEntity tile = (TileEntity)it.next();
					if(tile!=null && tile.getClass().getName().endsWith("TileEntitySoundMuffler"))
						if(tile.getBlockMetadata()!=1)
						{
							double d = (tile.xCoord+.5-x)*(tile.xCoord+.5-x) + (tile.yCoord+.5-y)*(tile.yCoord+.5-y) + (tile.zCoord+.5-z)*(tile.zCoord+.5-z);
							if(d<=64 && d>0)
								volumeAjustment=.1f;
						}
				}
			}
	}


	@Override
	public void update()
	{
		if(ClientUtils.mc().thePlayer.worldObj.getTotalWorldTime()%40==0)
			evaluateVolume();
	}

	public boolean donePlaying=false;
	@Override
	public boolean isDonePlaying()
	{
		return donePlaying;
	}
}
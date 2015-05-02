package blusunrize.immersiveengineering.common.util;

import net.minecraft.client.audio.ITickableSound;
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
		return volume;
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


	@Override
	public void update()
	{
	}

	public boolean donePlaying=false;
	@Override
	public boolean isDonePlaying()
	{
		return donePlaying;
	}
}
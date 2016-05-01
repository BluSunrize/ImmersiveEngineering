package blusunrize.immersiveengineering.common.util.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.util.ResourceLocation;

public class IEMuffledTickableSound implements ITickableSound
{
	ITickableSound originalSound;
	float volumeMod;

	public IEMuffledTickableSound(ITickableSound originalSound, float volumeMod)
	{
		this.originalSound = originalSound;
		this.volumeMod = volumeMod;
	}

	@Override
	public ResourceLocation getSoundLocation()
	{
		return originalSound.getSoundLocation();
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
	@Override
	public void update()
	{
		originalSound.update();
	}
	@Override
	public boolean isDonePlaying()
	{
		return originalSound.isDonePlaying();
	}
}
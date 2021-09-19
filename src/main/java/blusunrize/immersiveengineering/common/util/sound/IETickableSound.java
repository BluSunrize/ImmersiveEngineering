/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class IETickableSound extends AbstractTickableSoundInstance
{
	protected final Supplier<Boolean> tickFunction;
	protected final Consumer<IETickableSound> onDoneFunction;

	public IETickableSound(SoundEvent event, SoundSource category, float volume, float pitch, Supplier<Boolean> tickFunction, Consumer<IETickableSound> onDoneFunction)
	{
		super(event, category);
		this.volume = volume;
		this.pitch = pitch;
		this.tickFunction = tickFunction;
		this.onDoneFunction = onDoneFunction;
		this.looping = true;
	}

	@Override
	public void tick()
	{
		if(!this.isStopped())
			if(this.tickFunction.get())
			{
				this.stop();
				this.onDoneFunction.accept(this);
			}
	}
}
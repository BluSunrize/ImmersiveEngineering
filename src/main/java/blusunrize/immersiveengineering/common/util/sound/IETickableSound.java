/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class IETickableSound extends TickableSound
{
	protected final Supplier<Boolean> tickFunction;
	protected final Consumer<IETickableSound> onDoneFunction;

	public IETickableSound(SoundEvent event, SoundCategory category, float volume, float pitch, Supplier<Boolean> tickFunction, Consumer<IETickableSound> onDoneFunction)
	{
		super(event, category);
		this.volume = volume;
		this.pitch = pitch;
		this.tickFunction = tickFunction;
		this.onDoneFunction = onDoneFunction;
		this.repeat = true;
	}

	@Override
	public void tick()
	{
		if(!this.isDonePlaying())
			if(this.tickFunction.get())
			{
				this.func_239509_o_();
				this.onDoneFunction.accept(this);
			}
	}
}
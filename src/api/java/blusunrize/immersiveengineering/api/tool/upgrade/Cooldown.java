/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool.upgrade;

import io.netty.buffer.ByteBuf;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualCodecs;

public record Cooldown(int remainingCooldown)
{
	public static final DualCodec<ByteBuf, Cooldown> CODECS = DualCodecs.INT
			.map(Cooldown::new, Cooldown::remainingCooldown);
	public static final Cooldown IDLE = new Cooldown(0);

	public boolean isOnCooldown()
	{
		return remainingCooldown > 0;
	}

	public Cooldown tick()
	{
		return remainingCooldown > 0?new Cooldown(remainingCooldown-1): this;
	}
}

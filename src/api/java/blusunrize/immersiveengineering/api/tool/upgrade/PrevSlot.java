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

import java.util.Optional;

public record PrevSlot(Optional<Integer> prevSlot)
{
	public static final DualCodec<ByteBuf, PrevSlot> CODECS = DualCodecs.INT
			.optionalFieldOf("prevSlot")
			.codec()
			.map(PrevSlot::new, PrevSlot::prevSlot);
	public static final PrevSlot NONE = new PrevSlot(Optional.empty());

	public PrevSlot(int prevSlot)
	{
		this(Optional.of(prevSlot));
	}
}

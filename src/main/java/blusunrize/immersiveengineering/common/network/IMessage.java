/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent.Context;

public interface IMessage
{
	void toBytes(FriendlyByteBuf buf);

	void process(Context context);
}

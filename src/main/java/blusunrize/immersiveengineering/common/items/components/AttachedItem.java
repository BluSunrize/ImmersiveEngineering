/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items.components;

import blusunrize.immersiveengineering.api.utils.codec.DualCodec;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public record AttachedItem(ItemStack attached)
{
	public static final DualCodec<RegistryFriendlyByteBuf, AttachedItem> CODECS = DualCodecs.ITEM_STACK
			.map(AttachedItem::new, AttachedItem::attached);

	public AttachedItem
	{
		attached = attached.copy();
	}
}

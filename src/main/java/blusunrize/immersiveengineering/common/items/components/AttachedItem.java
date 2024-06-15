/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items.components;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record AttachedItem(ItemStack attached)
{
	public static final Codec<AttachedItem> CODEC = ItemStack.CODEC
			.xmap(AttachedItem::new, AttachedItem::attached);
	public static final StreamCodec<RegistryFriendlyByteBuf, AttachedItem> STREAM_CODEC = ItemStack.STREAM_CODEC
			.map(AttachedItem::new, AttachedItem::attached);

	public AttachedItem
	{
		attached = attached.copy();
	}
}

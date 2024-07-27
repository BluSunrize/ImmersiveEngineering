/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public class MessageCrateName implements IMessage
{
	public static final ResourceLocation ID = IEApi.ieLoc("crate_name");

	private final BlockPos pos;
	private final String name;

	public MessageCrateName(BlockEntity tile, String name)
	{
		this.pos = tile.getBlockPos();
		this.name = name;
	}

	public MessageCrateName(FriendlyByteBuf buf)
	{
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		this.name = buf.readUtf();
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeInt(pos.getX()).writeInt(pos.getY()).writeInt(pos.getZ());
		buf.writeUtf(name);
	}

	@Override
	public void process(PlayPayloadContext context)
	{
		if(context.flow().getReceptionSide()==LogicalSide.SERVER)
			context.workHandler().execute(() -> {
				Level world = context.player().orElseThrow().level();
				if(world.isAreaLoaded(pos, 1))
				{
					BlockEntity tile = world.getBlockEntity(pos);
					if(tile instanceof WoodenCrateBlockEntity crate)
						crate.setCustomName(Component.literal(name));
				}
			});
	}

	@Override
	@NotNull
	public ResourceLocation id()
	{
		return ID;
	}
}
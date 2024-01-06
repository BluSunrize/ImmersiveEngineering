/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public class MessageBlockEntitySync implements IMessage
{
	public static final ResourceLocation ID = IEApi.ieLoc("be_sync");

	private final BlockPos pos;
	private final CompoundTag nbt;

	//TODO get rid of NBT in packets
	public MessageBlockEntitySync(IEBaseBlockEntity tile, CompoundTag nbt)
	{
		this.pos = tile.getBlockPos();
		this.nbt = nbt;
	}

	public MessageBlockEntitySync(FriendlyByteBuf buf)
	{
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		this.nbt = buf.readNbt();
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeInt(pos.getX()).writeInt(pos.getY()).writeInt(pos.getZ());
		buf.writeNbt(this.nbt);
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
					if(tile instanceof IEBaseBlockEntity)
						((IEBaseBlockEntity)tile).receiveMessageFromClient(nbt);
				}
			});
		else
			context.workHandler().execute(() -> {
				Level world = ImmersiveEngineering.proxy.getClientWorld();
				if(world!=null) // This can happen if the task is scheduled right before leaving the world
				{
					BlockEntity tile = world.getBlockEntity(pos);
					if(tile instanceof IEBaseBlockEntity)
						((IEBaseBlockEntity)tile).receiveMessageFromServer(nbt);
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
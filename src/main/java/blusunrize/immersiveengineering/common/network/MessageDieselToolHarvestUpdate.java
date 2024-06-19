/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.util.sound.DieselToolSoundHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static net.minecraft.network.protocol.PacketFlow.CLIENTBOUND;

public class MessageDieselToolHarvestUpdate implements IMessage
{
	public static final ResourceLocation ID = IEApi.ieLoc("diesel_tool_harvesting_update");
	private final int holderID;
	private final BlockPos targetPos;
	private final LeftClickBlock.Action action;

	public MessageDieselToolHarvestUpdate(LivingEntity holder, LeftClickBlock.Action action, BlockPos targetPos)
	{
		this.holderID = holder.getId();
		this.targetPos = targetPos;
		this.action = action;
	}

	public MessageDieselToolHarvestUpdate(FriendlyByteBuf buf)
	{
		this.holderID = buf.readInt();
		this.targetPos = buf.readBlockPos();
		this.action = LeftClickBlock.Action.class.getEnumConstants()[buf.readByte()];
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeInt(holderID);
		buf.writeBlockPos(targetPos);
		buf.writeByte(action.ordinal()); // saves 4 bytes, has 4 bit overhead, should be fine
	}

	@Override
	public void process(PlayPayloadContext context)
	{
		assert context.flow().equals(CLIENTBOUND); //todo: remove me?

		context.workHandler().execute(() -> {
			Level world = ImmersiveEngineering.proxy.getClientWorld();
			if(world!=null) // This can happen if the task is scheduled right before leaving the world
			{
				Entity entity = world.getEntity(holderID);
				if(entity instanceof LivingEntity holder)
					DieselToolSoundHandler.handleHarvestAction(holder, action, targetPos);
			}
		});
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}

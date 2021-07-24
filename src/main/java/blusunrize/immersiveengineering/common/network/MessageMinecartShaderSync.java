/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import java.util.Objects;
import java.util.function.Supplier;

public class MessageMinecartShaderSync implements IMessage
{
	private final int entityID;
	private final ItemStack shader;

	public MessageMinecartShaderSync(Entity entity, Object o)
	{
		this.entityID = entity.getId();
		if(o instanceof ShaderWrapper)
			shader = ((ShaderWrapper)o).getShaderItem();
		else
			shader = ItemStack.EMPTY;
	}

	public MessageMinecartShaderSync(FriendlyByteBuf buf)
	{
		this.entityID = buf.readInt();
		this.shader = buf.readItem();
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeInt(this.entityID);
		buf.writeItem(this.shader);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Context ctx = context.get();
		if(ctx.getDirection().getReceptionSide()==LogicalSide.SERVER)
		{
			ServerLevel world = Objects.requireNonNull(ctx.getSender()).getLevel();
			ctx.enqueueWork(() -> {
				Entity entity = world.getEntity(entityID);
				if(entity==null)
					return;
				LazyOptional<ShaderWrapper> cap = entity.getCapability(CapabilityShader.SHADER_CAPABILITY);
				cap.ifPresent(handler ->
						ImmersiveEngineering.packetHandler.send(PacketDistributor.DIMENSION.with(world::dimension),
								new MessageMinecartShaderSync(entity, handler))
				);
			});
		}
		else
			ctx.enqueueWork(() -> {
				Level world = ImmersiveEngineering.proxy.getClientWorld();
				if (world!=null) // This can happen if the task is scheduled right before leaving the world
				{
					Entity entity = world.getEntity(entityID);
					//TODO
					//if(entity instanceof AbstractMinecart)
					//	ShaderMinecartRenderer.shadedCarts.put(entityID, shader);
				}
			});
	}
}
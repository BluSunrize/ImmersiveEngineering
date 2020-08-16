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
import blusunrize.immersiveengineering.client.render.entity.ShaderMinecartRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Objects;
import java.util.function.Supplier;

public class MessageMinecartShaderSync implements IMessage
{
	private int entityID;
	private boolean request = false;
	private ItemStack shader;

	public MessageMinecartShaderSync(Entity entity, Object o)
	{
		this.entityID = entity.getEntityId();
		if(o instanceof ShaderWrapper)
			shader = ((ShaderWrapper)o).getShaderItem();
		else
			request = true;
	}

	public MessageMinecartShaderSync(PacketBuffer buf)
	{
		this.entityID = buf.readInt();
		this.request = buf.readBoolean();
		if(!request)
			this.shader = buf.readItemStack();
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(this.entityID);
		buf.writeBoolean(this.request);
		if(!request)
			buf.writeItemStack(this.shader);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Context ctx = context.get();
		if(ctx.getDirection().getReceptionSide()==LogicalSide.SERVER)
		{
			ServerWorld world = Objects.requireNonNull(ctx.getSender()).getServerWorld();
			ctx.enqueueWork(() -> {
				Entity entity = world.getEntityByID(entityID);
				if(entity==null)
					return;
				LazyOptional<ShaderWrapper> cap = entity.getCapability(CapabilityShader.SHADER_CAPABILITY);
				cap.ifPresent(handler ->
						ImmersiveEngineering.packetHandler.send(PacketDistributor.DIMENSION.with(world::getDimensionKey),
								new MessageMinecartShaderSync(entity, handler))
				);
			});
		}
		else
			ctx.enqueueWork(() -> {
				World world = ImmersiveEngineering.proxy.getClientWorld();
				if (world!=null) // This can happen if the task is scheduled right before leaving the world
				{
					Entity entity = world.getEntityByID(entityID);
					if(entity instanceof AbstractMinecartEntity)
						ShaderMinecartRenderer.shadedCarts.put(entityID, shader);
				}
			});
	}
}
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
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.client.render.entity.ShaderMinecartRenderer;
import blusunrize.immersiveengineering.common.register.IEDataAttachments;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class MessageMinecartShaderSync implements IMessage
{
	public static final ResourceLocation ID = IEApi.ieLoc("minecart_shader_sync");
	private final int entityID;
	private final ItemStack shader;

	public MessageMinecartShaderSync(Entity entity, ShaderWrapper wrapper)
	{
		this.entityID = entity.getId();
		this.shader = wrapper.getShaderItem();
	}

	public MessageMinecartShaderSync(Entity entity)
	{
		this.entityID = entity.getId();
		this.shader = ItemStack.EMPTY;
	}

	public MessageMinecartShaderSync(FriendlyByteBuf buf)
	{
		this.entityID = buf.readInt();
		this.shader = buf.readItem();
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeInt(this.entityID);
		buf.writeItem(this.shader);
	}

	@Override
	public void process(PlayPayloadContext context)
	{
		if(context.flow().getReceptionSide()==LogicalSide.SERVER)
		{
			Level world = context.player().orElseThrow().level();
			context.workHandler().execute(() -> {
				Entity entity = world.getEntity(entityID);
				if(!(entity instanceof AbstractMinecart))
					return;
				ShaderWrapper cap = entity.getData(IEDataAttachments.MINECART_SHADER);
				if(cap!=null)
					PacketDistributor.DIMENSION.with(world.dimension())
							.send(new MessageMinecartShaderSync(entity, cap));
			});
		}
		else
			context.workHandler().execute(() -> {
				Level world = ImmersiveEngineering.proxy.getClientWorld();
				if (world!=null) // This can happen if the task is scheduled right before leaving the world
				{
					Entity entity = world.getEntity(entityID);
					if(entity instanceof AbstractMinecart)
						ShaderMinecartRenderer.shadedCarts.put(entityID, shader);
				}
			});
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.shader;

import blusunrize.immersiveengineering.api.IEApi;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;
import java.util.Objects;

import static blusunrize.immersiveengineering.api.IEApiDataComponents.ATTACHED_SHADER;

/**
 * @author BluSunrize - 09.11.2016
 */
public class CapabilityShader
{
	// TODO probably rework
	public static final ItemCapability<ShaderWrapper, Void> ITEM = ItemCapability.createVoid(
			IEApi.ieLoc("shader_item"), ShaderWrapper.class
	);
	public static final BlockCapability<ShaderWrapper, Void> BLOCK = BlockCapability.createVoid(
			IEApi.ieLoc("shader_block"), ShaderWrapper.class
	);

	public static boolean shouldReequipDueToShader(ItemStack oldStack, ItemStack newStack)
	{
		ShaderWrapper wrapperOld = oldStack.getCapability(CapabilityShader.ITEM);
		ShaderWrapper wrapperNew = newStack.getCapability(CapabilityShader.ITEM);
		if(wrapperOld==null&&wrapperNew!=null)
			return true;
		else if(wrapperOld!=null&&wrapperNew==null)
			return true;
		else if(wrapperOld!=null)
			return !Objects.equals(wrapperOld.getShader(), wrapperNew.getShader());
		else
			return false;
	}

	public interface ShaderWrapper
	{
		ResourceLocation getShaderType();

		void setShader(@Nullable ResourceLocation shader);

		@Nullable
		ResourceLocation getShader();

		default ShaderCase getCase()
		{
			var shaderStack = getShader();
			if(shaderStack!=null)
				return ShaderRegistry.getShader(shaderStack, getShaderType());
			else
				return null;
		}
	}

	public static class ShaderWrapper_Item implements ShaderWrapper
	{
		private final ItemStack container;
		private final ResourceLocation shaderType;

		public ShaderWrapper_Item(ResourceLocation type, ItemStack container)
		{
			this.shaderType = type;
			this.container = container;
		}

		@Override
		public ResourceLocation getShaderType()
		{
			return shaderType;
		}

		@Override
		public void setShader(@Nullable ResourceLocation shader)
		{
			if(shader!=null)
				container.set(ATTACHED_SHADER, shader);
			else
				container.remove(ATTACHED_SHADER);
		}

		@Override
		@Nullable
		public ResourceLocation getShader()
		{
			return container.get(ATTACHED_SHADER);
		}
	}

	public static class ShaderWrapper_Direct implements ShaderWrapper
	{
		public static final IAttachmentSerializer<CompoundTag, ShaderWrapper_Direct> SERIALIZER = new WrapperSerializer();

		@Nullable
		private ResourceLocation shader = null;
		private final ResourceLocation type;

		public ShaderWrapper_Direct(ResourceLocation type)
		{
			this.type = type;
		}

		public ResourceLocation getShaderType()
		{
			return type;
		}

		@Override
		public void setShader(@Nullable ResourceLocation shader)
		{
			this.shader = shader;
		}

		@Override
		@Nullable
		public ResourceLocation getShader()
		{
			return this.shader;
		}
	}

	public static final class WrapperSerializer implements IAttachmentSerializer<CompoundTag, ShaderWrapper_Direct>
	{
		@Override
		public CompoundTag write(ShaderWrapper_Direct attachment, Provider provider)
		{
			CompoundTag nbt = new CompoundTag();
			var shader = attachment.getShader();
			if(shader!=null)
				nbt.putString("IE:Shader", shader.toString());
			else
				nbt.putString("IE:NoShader", "");
			nbt.putString("IE:ShaderType", attachment.getShaderType().toString());
			return nbt;
		}

		@Override
		public ShaderWrapper_Direct read(IAttachmentHolder holder, CompoundTag tag, Provider provider)
		{
			ShaderWrapper_Direct wrapper = new ShaderWrapper_Direct(ResourceLocation.parse(tag.getString("IE:ShaderType")));
			if(!tag.contains("IE:NoShader"))
				wrapper.setShader(ResourceLocation.parse(tag.getString("IE:Shader")));
			return wrapper;
		}
	}

	public static ModelProperty<ShaderCase> MODEL_PROPERTY = new ModelProperty<>();
}
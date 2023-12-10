/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.shader;

import blusunrize.immersiveengineering.api.IEApi;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;

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
	public static final EntityCapability<ShaderWrapper, Void> ENTITY = EntityCapability.createVoid(
			IEApi.ieLoc("shader_entity"), ShaderWrapper.class
	);

	public abstract static class ShaderWrapper
	{
		protected ResourceLocation shaderType;

		public ShaderWrapper(ResourceLocation type)
		{
			this.shaderType = type;
		}

		public void setShaderType(ResourceLocation shaderType)
		{
			this.shaderType = shaderType;
		}

		public ResourceLocation getShaderType()
		{
			return shaderType;
		}

		public abstract void setShaderItem(@Nonnull ItemStack shader);

		@Nonnull
		public abstract ItemStack getShaderItem();

		public ShaderCase getCase()
		{
			ItemStack shaderStack = getShaderItem();
			if(shaderStack.getItem() instanceof IShaderItem shaderItem)
				return shaderItem.getShaderCase(shaderStack, getShaderType());
			else
				return null;
		}
	}

	public static class ShaderWrapper_Item extends ShaderWrapper
	{
		public static final String SHADER_NBT_KEY = "IE:Shader";
		protected final ItemStack container;

		public ShaderWrapper_Item(ResourceLocation type, ItemStack container)
		{
			super(type);
			this.container = container;
		}

		@Override
		public void setShaderItem(ItemStack shader)
		{
			if(!container.hasTag())
				container.setTag(new CompoundTag());
			if(!shader.isEmpty())
			{
				CompoundTag shaderTag = shader.save(new CompoundTag());
				container.getOrCreateTag().put(SHADER_NBT_KEY, shaderTag);
			}
			else
				container.getOrCreateTag().remove(SHADER_NBT_KEY);
		}

		@Override
		@Nonnull
		public ItemStack getShaderItem()
		{
			if(!container.hasTag())
				return ItemStack.EMPTY;
			CompoundTag tagCompound = container.getOrCreateTag();
			if(!tagCompound.contains(SHADER_NBT_KEY, Tag.TAG_COMPOUND))
				return ItemStack.EMPTY;
			return ItemStack.of(tagCompound.getCompound(SHADER_NBT_KEY));
		}
	}

	public static class ShaderWrapper_Direct extends ShaderWrapper
	{
		@Nonnull
		protected ItemStack shader = ItemStack.EMPTY;

		public ShaderWrapper_Direct(ResourceLocation type)
		{
			super(type);
		}

		@Override
		public void setShaderItem(@Nonnull ItemStack shader)
		{
			this.shader = shader;
		}

		@Override
		@Nonnull
		public ItemStack getShaderItem()
		{
			return this.shader;
		}
	}

	public static ModelProperty<ShaderCase> MODEL_PROPERTY = new ModelProperty<>();
}
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
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

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

	public interface ShaderWrapper
	{
		ResourceLocation getShaderType();

		void setShaderItem(@Nonnull ItemStack shader);

		@Nonnull
		ItemStack getShaderItem();

		default ShaderCase getCase()
		{
			ItemStack shaderStack = getShaderItem();
			if(shaderStack.getItem() instanceof IShaderItem shaderItem)
				return shaderItem.getShaderCase(shaderStack, getShaderType());
			else
				return null;
		}
	}

	public static class ShaderWrapper_Item implements ShaderWrapper
	{
		public static final String SHADER_NBT_KEY = "IE:Shader";
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
		public void setShaderItem(@NotNull ItemStack shader)
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

	public static class ShaderWrapper_Direct implements ShaderWrapper
	{
		public static final IAttachmentSerializer<CompoundTag, ShaderWrapper_Direct> SERIALIZER = new WrapperSerializer();

		@Nonnull
		private ItemStack shader = ItemStack.EMPTY;
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

	public static final class WrapperSerializer implements IAttachmentSerializer<CompoundTag, ShaderWrapper_Direct>
	{
		@Override
		public CompoundTag write(ShaderWrapper_Direct attachment)
		{
			CompoundTag nbt = new CompoundTag();
			ItemStack shader = attachment.getShaderItem();
			if(!shader.isEmpty())
				shader.save(nbt);
			else
				nbt.putString("IE:NoShader", "");
			nbt.putString("IE:ShaderType", attachment.getShaderType().toString());
			return nbt;
		}

		@Override
		public ShaderWrapper_Direct read(CompoundTag tag)
		{
			ShaderWrapper_Direct wrapper = new ShaderWrapper_Direct(new ResourceLocation(tag.getString("IE:ShaderType")));
			if(!tag.contains("IE:NoShader"))
				wrapper.setShaderItem(ItemStack.of(tag));
			return wrapper;
		}
	}

	public static ModelProperty<ShaderCase> MODEL_PROPERTY = new ModelProperty<>();
}
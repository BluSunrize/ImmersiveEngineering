/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.shader;

import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author BluSunrize - 09.11.2016
 */
public class CapabilityShader
{
	@CapabilityInject(ShaderWrapper.class)
	public static Capability<ShaderWrapper> SHADER_CAPABILITY = null;

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
			if(!tagCompound.contains(SHADER_NBT_KEY, NBT.TAG_COMPOUND))
				return ItemStack.EMPTY;
			return ItemStack.of(tagCompound.getCompound(SHADER_NBT_KEY));
		}
	}

	public static class ShaderWrapper_Direct extends ShaderWrapper implements ICapabilityProvider, INBTSerializable<CompoundTag>
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

		private LazyOptional<ShaderWrapper> opt = CapabilityUtils.constantOptional(this);

		@Override
		@Nonnull
		public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing)
		{
			if(capability==SHADER_CAPABILITY)
				return opt.cast();
			return LazyOptional.empty();
		}

		@Override
		public CompoundTag serializeNBT()
		{
			CompoundTag nbt = new CompoundTag();
			ItemStack shader = getShaderItem();
			if(!shader.isEmpty())
				shader.save(nbt);
			else
				nbt.putString("IE:NoShader", "");
			nbt.putString("IE:ShaderType", getShaderType().toString());
			return nbt;
		}

		@Override
		public void deserializeNBT(CompoundTag nbt)
		{
			setShaderType(new ResourceLocation(nbt.getString("IE:ShaderType")));
			if(!nbt.contains("IE:NoShader"))
				setShaderItem(ItemStack.of(nbt));
		}
	}

	public static ModelProperty<ShaderCase> MODEL_PROPERTY = new ModelProperty<>();
}
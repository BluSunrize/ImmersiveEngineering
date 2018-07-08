/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.shader;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

/**
 * @author BluSunrize - 09.11.2016
 */
public class CapabilityShader
{
	@CapabilityInject(ShaderWrapper.class)
	public static Capability<ShaderWrapper> SHADER_CAPABILITY = null;

	public abstract static class ShaderWrapper
	{
		protected String shaderType;

		public ShaderWrapper(String type)
		{
			this.shaderType = type;
		}

		public void setShaderType(String shaderType)
		{
			this.shaderType = shaderType;
		}

		public String getShaderType()
		{
			return shaderType;
		}

		public abstract void setShaderItem(@Nonnull ItemStack shader);

		@Nonnull
		public abstract ItemStack getShaderItem();
	}

	public static class ShaderWrapper_Item extends ShaderWrapper
	{
		public static final String SHADER_NBT_KEY = "IE:Shader";
		protected final ItemStack container;

		public ShaderWrapper_Item(String type, ItemStack container)
		{
			super(type);
			this.container = container;
		}

		@Override
		public void setShaderItem(ItemStack shader)
		{
			if(!container.hasTagCompound())
				container.setTagCompound(new NBTTagCompound());
			if(!shader.isEmpty())
			{
				NBTTagCompound shaderTag = shader.writeToNBT(new NBTTagCompound());
				container.getTagCompound().setTag(SHADER_NBT_KEY, shaderTag);
			}
			else
				container.getTagCompound().removeTag(SHADER_NBT_KEY);
		}

		@Override
		@Nullable
		public ItemStack getShaderItem()
		{
			NBTTagCompound tagCompound = container.getTagCompound();
			if(tagCompound==null||!tagCompound.hasKey(SHADER_NBT_KEY))
				return ItemStack.EMPTY;
			return new ItemStack(tagCompound.getCompoundTag(SHADER_NBT_KEY));
		}
	}

	public static class ShaderWrapper_Direct extends ShaderWrapper implements ICapabilityProvider, INBTSerializable<NBTTagCompound>
	{
		@Nonnull
		protected ItemStack shader = ItemStack.EMPTY;

		public ShaderWrapper_Direct(String type)
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

		@Override
		public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
		{
			return capability==SHADER_CAPABILITY;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
		{
			if(capability==SHADER_CAPABILITY)
				return (T)this;
			return null;
		}

		@Override
		public NBTTagCompound serializeNBT()
		{
			NBTTagCompound nbt = new NBTTagCompound();
			ItemStack shader = getShaderItem();
			if(!shader.isEmpty())
				shader.writeToNBT(nbt);
			else
				nbt.setString("IE:NoShader", "");
			nbt.setString("IE:ShaderType", getShaderType());
			return nbt;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt)
		{
			NBTTagCompound tags = nbt;
			setShaderType(tags.getString("IE:ShaderType"));
			if(!tags.hasKey("IE:NoShader"))
				setShaderItem(new ItemStack(tags));
		}
	}

	public static void register()
	{
		CapabilityManager.INSTANCE.register(ShaderWrapper.class, new Capability.IStorage<ShaderWrapper>()
		{
			@Override
			public NBTBase writeNBT(Capability<ShaderWrapper> capability, ShaderWrapper instance, EnumFacing side)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				ItemStack shader = instance.getShaderItem();
				if(!shader.isEmpty())
					shader.writeToNBT(nbt);
				else
					nbt.setString("IE:NoShader", "");
				nbt.setString("IE:ShaderType", instance.getShaderType());
				return nbt;
			}

			@Override
			public void readNBT(Capability<ShaderWrapper> capability, ShaderWrapper instance, EnumFacing side, NBTBase nbt)
			{
				NBTTagCompound tags = (NBTTagCompound)nbt;
				instance.setShaderType(tags.getString("IE:ShaderType"));
				if(!tags.hasKey("IE:NoShader"))
					instance.setShaderItem(new ItemStack(tags));
			}
		}, new Callable<ShaderWrapper>()
		{
			@Override
			public ShaderWrapper call()
			{
				return new ShaderWrapper_Direct("");
			}
		});
	}

	public static IUnlistedProperty<ShaderWrapper> BLOCKSTATE_PROPERTY = new IUnlistedProperty<ShaderWrapper>()
	{
		@Override
		public String getName()
		{
			return "shaderwrapper";
		}

		@Override
		public boolean isValid(ShaderWrapper value)
		{
			return true;
		}

		@Override
		public Class<ShaderWrapper> getType()
		{
			return ShaderWrapper.class;
		}

		@Override
		public String valueToString(ShaderWrapper value)
		{
			return value.toString();
		}
	};
}
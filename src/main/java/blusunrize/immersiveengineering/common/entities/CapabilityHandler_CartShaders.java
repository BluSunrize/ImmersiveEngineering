package blusunrize.immersiveengineering.common.entities;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class CapabilityHandler_CartShaders implements ICapabilityProvider, INBTSerializable<NBTTagCompound>
{
	private EntityMinecart cart;
	private ItemStack shader;

	public CapabilityHandler_CartShaders()
	{
		this(null);
	}
	public CapabilityHandler_CartShaders(EntityMinecart cart)
	{
		this.setCart(cart);
	}

	public EntityMinecart getCart()
	{
		return cart;
	}
	public void setCart(EntityMinecart cart)
	{
		this.cart = cart;
	}
	public ItemStack getShader()
	{
		return shader;
	}
	public void setShader(ItemStack stack)
	{
		this.shader = stack;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		return capability == SHADER_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		if(capability == SHADER_CAPABILITY)
			return (T)this;
		return null;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		if(shader != null)
			nbt.setTag("shader", shader.writeToNBT(new NBTTagCompound()));
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		shader = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("shader"));
	}

	//TODO Make Shaders entirely capability based at soem point??

	@CapabilityInject(CapabilityHandler_CartShaders.class)
	public static Capability<CapabilityHandler_CartShaders> SHADER_CAPABILITY;

	public static void init()
	{
		CapabilityManager.INSTANCE.register(CapabilityHandler_CartShaders.class, new Capability.IStorage<CapabilityHandler_CartShaders>()
		{
			@Override
			public NBTBase writeNBT(Capability<CapabilityHandler_CartShaders> capability, CapabilityHandler_CartShaders instance, EnumFacing side)
			{
				return instance.serializeNBT();
			}

			@Override
			public void readNBT(Capability<CapabilityHandler_CartShaders> capability, CapabilityHandler_CartShaders instance, EnumFacing side, NBTBase base)
			{
				instance.deserializeNBT((NBTTagCompound)base);
			}
		}, new Callable<CapabilityHandler_CartShaders>()
		{
			@Override
			public CapabilityHandler_CartShaders call() throws Exception
			{
				return new CapabilityHandler_CartShaders();
			}
		});
	}
}
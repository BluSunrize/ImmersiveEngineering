/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ShaderInventory implements Container
{
	private ShaderWrapper wrapper;
	private AbstractContainerMenu container;
	@Nonnull
	public ItemStack shader;
	private ResourceLocation name;

	public ShaderInventory(AbstractContainerMenu par1Container, ShaderWrapper wrapper)
	{
		this.container = par1Container;
		this.wrapper = wrapper;
		this.shader = ShaderRegistry.makeShaderStack(wrapper.getShader());
		this.name = wrapper.getShaderType();
	}


	@Override
	public int getContainerSize()
	{
		return 1;
	}

	@Override
	public boolean isEmpty()
	{
		return this.shader.isEmpty();
	}

	@Override
	public ItemStack getItem(int i)
	{
		return this.shader;
	}

	@Override
	public ItemStack removeItemNoUpdate(int i)
	{
		if(!this.shader.isEmpty())
		{
			ItemStack itemstack = this.shader.copy();
			this.shader = ItemStack.EMPTY;
			return itemstack;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int i, int j)
	{
		if(!this.shader.isEmpty())
		{
			ItemStack itemstack;
			if(shader.getCount() <= j)
			{
				itemstack = this.shader.copy();
				this.shader = ItemStack.EMPTY;
				this.setChanged();
				this.container.slotsChanged(this);
				return itemstack;
			}
			itemstack = this.shader.split(j);

			if(shader.getCount()==0)
				this.shader = ItemStack.EMPTY;
			this.container.slotsChanged(this);
			return itemstack;
		}
		return ItemStack.EMPTY;
	}


	@Override
	public void setItem(int i, ItemStack stack)
	{
		this.shader = stack;
		if(!stack.isEmpty()&&stack.getCount() > this.getMaxStackSize())
			stack.setCount(this.getMaxStackSize());
		this.container.slotsChanged(this);
	}

	@Override
	public int getMaxStackSize()
	{
		return 64;
	}

	@Override
	public void setChanged()
	{
		if(wrapper!=null)
			if(shader.getItem() instanceof IShaderItem shaderItem)
				wrapper.setShader(shaderItem.getShaderName());
			else
				wrapper.setShader(null);
	}

	@Override
	public boolean stillValid(Player entityplayer)
	{
		return true;
	}

	@Override
	public void startOpen(Player player)
	{
	}

	@Override
	public void stopOpen(Player player)
	{
	}

	@Override
	public boolean canPlaceItem(int i, ItemStack itemstack)
	{
		return true;
	}

	@Override
	public void clearContent()
	{
	}
}
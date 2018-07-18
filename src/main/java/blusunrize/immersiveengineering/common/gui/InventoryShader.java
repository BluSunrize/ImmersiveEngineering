/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;

public class InventoryShader implements IInventory
{
	private ShaderWrapper wrapper;
	private Container container;
	@Nonnull
	public ItemStack shader;
	private String name;

	public InventoryShader(Container par1Container, ShaderWrapper wrapper)
	{
		this.container = par1Container;
		this.wrapper = wrapper;
		this.shader = wrapper.getShaderItem();
		this.name = wrapper.getShaderType();
	}


	@Override
	public int getSizeInventory()
	{
		return 1;
	}

	@Override
	public boolean isEmpty()
	{
		return this.shader.isEmpty();
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return this.shader;
	}

	@Override
	public ItemStack removeStackFromSlot(int i)
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
	public ItemStack decrStackSize(int i, int j)
	{
		if(!this.shader.isEmpty())
		{
			ItemStack itemstack;
			if(shader.getCount() <= j)
			{
				itemstack = this.shader.copy();
				this.shader = ItemStack.EMPTY;
				this.markDirty();
				this.container.onCraftMatrixChanged(this);
				return itemstack;
			}
			itemstack = this.shader.splitStack(j);

			if(shader.getCount()==0)
				this.shader = ItemStack.EMPTY;
			this.container.onCraftMatrixChanged(this);
			return itemstack;
		}
		return ItemStack.EMPTY;
	}


	@Override
	public void setInventorySlotContents(int i, ItemStack stack)
	{
		this.shader = stack;
		if(!stack.isEmpty()&&stack.getCount() > this.getInventoryStackLimit())
			stack.setCount(this.getInventoryStackLimit());
		this.container.onCraftMatrixChanged(this);
	}

	@Override
	public String getName()
	{
		return "container."+name;
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return this.hasCustomName()?new TextComponentString(this.getName()): new TextComponentTranslation(this.getName());
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public void markDirty()
	{
		if(wrapper!=null)
			wrapper.setShaderItem(shader);
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player)
	{
	}

	@Override
	public void closeInventory(EntityPlayer player)
	{
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}


	@Override
	public int getField(int id)
	{
		return 0;
	}

	@Override
	public void setField(int id, int value)
	{
	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
	}
}
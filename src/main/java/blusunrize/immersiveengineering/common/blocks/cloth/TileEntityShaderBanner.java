/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Direct;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class TileEntityShaderBanner extends TileEntityIEBase implements IAdvancedCollisionBounds, ITileDrop
{
	public boolean wall = false;
	public byte orientation = 0;
	public ShaderWrapper_Direct shader = new ShaderWrapper_Direct("immersiveengineering:banner");


	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		this.wall = nbt.getBoolean("wall");
		this.orientation = nbt.getByte("orientation");
		if(nbt.hasKey("shader"))
		{
			shader = new ShaderWrapper_Direct("immersiveengineering:banner");
			shader.deserializeNBT(nbt.getCompoundTag("shader"));
		}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setBoolean("wall", this.wall);
		nbt.setByte("orientation", this.orientation);
		nbt.setTag("shader", shader.serializeNBT());
	}

	@Override
	public float[] getBlockBounds()
	{
		if(this.wall)
			switch(this.orientation)
			{
				default:
				case 2:
					return new float[]{0, 0, .875f, 1, .78125f, 1};
				case 3:
					return new float[]{0, 0, 0, 1, .78125f, .125f};
				case 4:
					return new float[]{.875f, 0, 0, 1, .78125f, 1};
				case 5:
					return new float[]{0, 0, 0, .125f, .78125f, 1};
			}
		return new float[]{.25f, 0, .25f, .75f, 1, .75f};
	}

	private static final List<AxisAlignedBB> COLLISION = Collections.singletonList(Block.NULL_AABB);

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		return COLLISION;
	}


	@Override
	public NonNullList<ItemStack> getTileDrops(@Nullable EntityPlayer player, IBlockState state)
	{
		NonNullList<ItemStack> list = NonNullList.from(ItemStack.EMPTY,  new ItemStack(Items.BANNER, 1, 15), this.shader.getShaderItem());
		return list;
	}

	@Override
	public void readOnPlacement(@Nullable EntityLivingBase placer, ItemStack stack)
	{
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return super.receiveClientEvent(id, arg);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		if(capability==CapabilityShader.SHADER_CAPABILITY)
			return true;
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		if(capability==CapabilityShader.SHADER_CAPABILITY)
			return (T)shader;
		return super.getCapability(capability, facing);
	}
}
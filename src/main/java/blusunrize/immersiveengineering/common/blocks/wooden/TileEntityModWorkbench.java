/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasObjProperty;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.items.ItemEngineersBlueprint;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class TileEntityModWorkbench extends TileEntityIEBase implements IIEInventory, IDirectionalTile, IHasDummyBlocks, IGuiTile, IHasObjProperty
{
	NonNullList<ItemStack> inventory = NonNullList.withSize(7, ItemStack.EMPTY);
	EnumFacing facing = EnumFacing.NORTH;
	public boolean dummy = false;
	public int dummyOffset = 0;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.byIndex(nbt.getInteger("facing"));
		dummy = nbt.getBoolean("dummy");
		dummyOffset = nbt.getInteger("dummyOffset");
		//		if(!descPacket)
		//		{
		//read inv
		inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 7);
		//		}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
		nbt.setBoolean("dummy", dummy);
		nbt.setInteger("dummyOffset", dummyOffset);
		//		if(!descPacket)
		//		{
		nbt.setTag("inventory", Utils.writeInventory(inventory));
		//		}
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			renderAABB = new AxisAlignedBB(getPos().getX()-1, getPos().getY(), getPos().getZ()-1, getPos().getX()+2, getPos().getY()+2, getPos().getZ()+2);
		return renderAABB;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return this.inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return slot==0?1: 64;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return false;
	}

	@Override
	public void receiveMessageFromClient(NBTTagCompound message)
	{
		if(!inventory.get(0).isEmpty()&&inventory.get(0).getItem() instanceof IConfigurableTool)
			for(String key : message.getKeySet())
			{
				if(key.startsWith("b_"))
					((IConfigurableTool)inventory.get(0).getItem()).applyConfigOption(inventory.get(0), key.substring(2), message.getBoolean(key));
				else if(key.startsWith("f_"))
					((IConfigurableTool)inventory.get(0).getItem()).applyConfigOption(inventory.get(0), key.substring(2), message.getFloat(key));
			}
	}

	@Override
	public boolean isDummy()
	{
		return this.dummy;
	}

	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		EnumFacing dummyDir = facing.getAxis()==Axis.X?(hitZ < .5?EnumFacing.NORTH: EnumFacing.SOUTH): (hitX < .5?EnumFacing.WEST: EnumFacing.EAST);
		boolean mirror = false;
		BlockPos dummyPos = pos.offset(dummyDir);
		if(!world.getBlockState(dummyPos).getBlock().isReplaceable(world, dummyPos))
		{
			dummyDir = dummyDir.getOpposite();
			dummyPos = pos.offset(dummyDir);
		}
		mirror = dummyDir!=facing.rotateY();
		dummyOffset = mirror?-1: 1;
		if(mirror)
			this.dummy = true;
		world.setBlockState(dummyPos, state);
		TileEntityModWorkbench tileEntityDummy = ((TileEntityModWorkbench)world.getTileEntity(dummyPos));
		tileEntityDummy.facing = this.facing;
		tileEntityDummy.dummy = !mirror;
		tileEntityDummy.dummyOffset = mirror?-1: 1;
	}

	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		EnumFacing dummyDir = dummy?facing.rotateYCCW(): facing.rotateY();
		world.setBlockToAir(pos.offset(dummyDir));
	}

	@Override
	public boolean canOpenGui()
	{
		return true;
	}

	@Override
	public int getGuiID()
	{
		return Lib.GUIID_Workbench;
	}

	@Override
	public TileEntity getGuiMaster()
	{
		if(!dummy)
			return this;
		EnumFacing dummyDir = dummy?facing.rotateYCCW(): facing.rotateY();
		TileEntity tileEntityModWorkbench = world.getTileEntity(pos.offset(dummyDir));
		if(tileEntityModWorkbench instanceof TileEntityModWorkbench)
			return tileEntityModWorkbench;
		return null;
	}

	static ArrayList<String> normalDisplayList = Lists.newArrayList("cube0");
	static ArrayList<String> blueprintDisplayList = Lists.newArrayList("cube0", "blueprint");

	@Override
	public ArrayList<String> compileDisplayList()
	{
		if(this.inventory.get(0).getItem() instanceof ItemEngineersBlueprint)
			return blueprintDisplayList;
		return normalDisplayList;
	}
}
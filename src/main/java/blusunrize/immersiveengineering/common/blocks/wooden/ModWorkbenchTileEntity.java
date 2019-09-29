/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasObjProperty;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

public class ModWorkbenchTileEntity extends IEBaseTileEntity implements IIEInventory, IDirectionalTile, IHasDummyBlocks,
		IInteractionObjectIE, IHasObjProperty
{
	public static TileEntityType<ModWorkbenchTileEntity> TYPE;
	NonNullList<ItemStack> inventory = NonNullList.withSize(7, ItemStack.EMPTY);
	Direction facing = Direction.NORTH;
	public boolean dummy = false;

	public ModWorkbenchTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		facing = Direction.byIndex(nbt.getInt("facing"));
		dummy = nbt.getBoolean("dummy");
		inventory = Utils.readInventory(nbt.getList("inventory", 10), 7);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putInt("facing", facing.ordinal());
		nbt.putBoolean("dummy", dummy);
		nbt.put("inventory", Utils.writeInventory(inventory));
	}

	@OnlyIn(Dist.CLIENT)
	private AxisAlignedBB renderAABB;

	@OnlyIn(Dist.CLIENT)
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
	public Direction getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(Direction facing)
	{
		this.facing = facing;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
		if(!inventory.get(0).isEmpty()&&inventory.get(0).getItem() instanceof IConfigurableTool)
			for(String key : message.keySet())
			{
				//TODO remove suffix whatever sends these
				INBT tag = message.get(key);
				if(tag instanceof ByteNBT)
					((IConfigurableTool)inventory.get(0).getItem()).applyConfigOption(inventory.get(0), key,
							((ByteNBT)tag).getByte()!=0);
				else if(tag instanceof FloatNBT)
					((IConfigurableTool)inventory.get(0).getItem()).applyConfigOption(inventory.get(0), key,
							((FloatNBT)tag).getFloat());
			}
	}

	@Override
	public boolean isDummy()
	{
		return this.dummy;
	}

	@Override
	public void placeDummies(BlockItemUseContext ctx, BlockState state)
	{
		Direction dummyDir;
		if(facing.getAxis()==Axis.X)
			dummyDir = ctx.getHitVec().z < .5?Direction.NORTH: Direction.SOUTH;
		else
			dummyDir = ctx.getHitVec().x < .5?Direction.WEST: Direction.EAST;
		boolean mirror;
		BlockPos dummyPos = pos.offset(dummyDir);
		if(!world.getBlockState(dummyPos).isReplaceable(BlockItemUseContext.func_221536_a(ctx, dummyPos, dummyDir)))
		{
			dummyDir = dummyDir.getOpposite();
			dummyPos = pos.offset(dummyDir);
		}
		mirror = dummyDir!=facing.rotateY();
		if(mirror)
			this.dummy = true;
		world.setBlockState(dummyPos, state);
		ModWorkbenchTileEntity tileEntityDummy = ((ModWorkbenchTileEntity)world.getTileEntity(dummyPos));
		tileEntityDummy.facing = this.facing;
		tileEntityDummy.dummy = !mirror;
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		Direction dummyDir = dummy?facing.rotateYCCW(): facing.rotateY();
		world.removeBlock(pos.offset(dummyDir), false);
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
	{
		return true;
	}

	@Override
	public IInteractionObjectIE getGuiMaster()
	{
		if(!dummy)
			return this;
		Direction dummyDir = facing.rotateYCCW();
		TileEntity tileEntityModWorkbench = world.getTileEntity(pos.offset(dummyDir));
		if(tileEntityModWorkbench instanceof ModWorkbenchTileEntity)
			return (ModWorkbenchTileEntity)tileEntityModWorkbench;
		return null;
	}

	private static ArrayList<String> normalDisplayList = Lists.newArrayList("cube0");
	private static ArrayList<String> blueprintDisplayList = Lists.newArrayList("cube0", "blueprint");

	@Override
	public ArrayList<String> compileDisplayList()
	{
		if(this.inventory.get(0).getItem() instanceof EngineersBlueprintItem)
			return blueprintDisplayList;
		return normalDisplayList;
	}
}
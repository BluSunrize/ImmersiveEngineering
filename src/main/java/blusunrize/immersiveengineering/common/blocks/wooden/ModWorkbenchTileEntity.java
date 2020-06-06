/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class ModWorkbenchTileEntity extends IEBaseTileEntity implements IIEInventory, IStateBasedDirectional,
		IHasDummyBlocks, IInteractionObjectIE, IHasObjProperty
{
	public static TileEntityType<ModWorkbenchTileEntity> TYPE;
	NonNullList<ItemStack> inventory = NonNullList.withSize(7, ItemStack.EMPTY);

	public ModWorkbenchTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		inventory = Utils.readInventory(nbt.getList("inventory", 10), 7);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
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
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity)
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
		return getState().get(IEProperties.MULTIBLOCKSLAVE);
	}

	@Nullable
	@Override
	public IGeneralMultiblock master()
	{
		if(!isDummy())
			return this;
		// Used to provide tile-dependant drops after breaking
		if(tempMasterTE!=null)
			return tempMasterTE;
		Direction dummyDir = isDummy()?getFacing().rotateYCCW(): getFacing().rotateY();
		BlockPos masterPos = getPos().offset(dummyDir);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		return this.getClass().isInstance(te)?(IGeneralMultiblock)te: null;
	}

	private void setDummy(boolean dummy)
	{
		setState(getState().with(IEProperties.MULTIBLOCKSLAVE, dummy));
	}

	@Override
	public void placeDummies(BlockItemUseContext ctx, BlockState state)
	{
		final Direction facing = getFacing();
		Direction dummyDir;
		if(facing.getAxis()==Axis.X)
			dummyDir = ctx.getHitVec().z < .5?Direction.NORTH: Direction.SOUTH;
		else
			dummyDir = ctx.getHitVec().x < .5?Direction.WEST: Direction.EAST;
		BlockPos dummyPos = pos.offset(dummyDir);
		if(!world.getBlockState(dummyPos).isReplaceable(BlockItemUseContext.func_221536_a(ctx, dummyPos, dummyDir)))
		{
			dummyDir = dummyDir.getOpposite();
			dummyPos = pos.offset(dummyDir);
		}
		boolean mirror = dummyDir!=facing.rotateY();
		if(mirror)
			setDummy(true);
		world.setBlockState(dummyPos, state);
		ModWorkbenchTileEntity tileEntityDummy = ((ModWorkbenchTileEntity)world.getTileEntity(dummyPos));
		tileEntityDummy.setDummy(!mirror);
		tileEntityDummy.setFacing(facing);
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		tempMasterTE = master();
		Direction dummyDir = isDummy()?getFacing().rotateYCCW(): getFacing().rotateY();
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
		if(!isDummy())
			return this;
		Direction dummyDir = getFacing().rotateYCCW();
		TileEntity tileEntityModWorkbench = world.getTileEntity(pos.offset(dummyDir));
		if(tileEntityModWorkbench instanceof ModWorkbenchTileEntity)
			return (ModWorkbenchTileEntity)tileEntityModWorkbench;
		return null;
	}

	private static VisibilityList normalDisplayList = VisibilityList.show("cube0");
	private static VisibilityList blueprintDisplayList = VisibilityList.show("cube0", "blueprint");

	@Override
	public VisibilityList compileDisplayList(BlockState state)
	{
		if(this.inventory.get(0).getItem() instanceof EngineersBlueprintItem)
			return blueprintDisplayList;
		return normalDisplayList;
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}
}
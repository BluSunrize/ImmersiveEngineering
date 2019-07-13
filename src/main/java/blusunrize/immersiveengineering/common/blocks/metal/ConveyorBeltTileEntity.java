/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorCovered;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorExtractCovered;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorVertical;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorVerticalCovered;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ConveyorBeltTileEntity extends IEBaseTileEntity implements IDirectionalTile, IAdvancedCollisionBounds,
		IAdvancedSelectionBounds, IHammerInteraction, IPlayerInteraction, IConveyorTile, IPropertyPassthrough,
		ITickable, IGeneralMultiblock, IFaceShape
{
	public Direction facing = Direction.NORTH;
	private final IConveyorBelt conveyorBeltSubtype;

	public ConveyorBeltTileEntity(ResourceLocation typeName)
	{
		super(ConveyorHandler.getTEType(typeName));
		conveyorBeltSubtype = ConveyorHandler.getConveyor(typeName, this);
	}

	@Override
	@Nullable
	public IConveyorBelt getConveyorSubtype()
	{
		return conveyorBeltSubtype;
	}

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(this.conveyorBeltSubtype!=null)
			this.conveyorBeltSubtype.onEntityCollision(this, entity, facing);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		facing = Direction.byIndex(nbt.getInt("facing"));
		if(nbt.hasKey("conveyorBeltSubtypeNBT"))
			conveyorBeltSubtype.readConveyorNBT(nbt.getCompound("conveyorBeltSubtypeNBT"));

		if(descPacket&&world!=null)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putInt("facing", facing.ordinal());
		if(conveyorBeltSubtype!=null)
			nbt.put("conveyorBeltSubtypeNBT", conveyorBeltSubtype.writeConveyorNBT());
	}

	@Override
	public Direction getFacing()
	{
		return this.facing;
	}

	@Override
	public void setFacing(Direction facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 5;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity)
	{
		return !entity.isSneaking();
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	@Override
	public void afterRotation(Direction oldDir, Direction newDir)
	{
		if(this.conveyorBeltSubtype!=null)
			this.conveyorBeltSubtype.afterRotation(oldDir, newDir);
	}

	@Override
	public boolean isLogicDummy()
	{
		return this.conveyorBeltSubtype!=null&&!this.conveyorBeltSubtype.isTicking(this);
	}

	@Override
	public void tick()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(this.conveyorBeltSubtype!=null)
			this.conveyorBeltSubtype.onUpdate(this, getFacing());
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking()&&conveyorBeltSubtype!=null&&conveyorBeltSubtype.changeConveyorDirection())
		{
//			if(transportUp)
//			{
//				transportUp = false;
//				transportDown = true;
//			}
//			else if(transportDown)
//				transportDown = false;
//			else
//				transportUp = true;

			this.markDirty();
			this.markContainingBlockForUpdate(null);
			world.addBlockEvent(getPos(), this.getBlockState().getBlock(), 0, 0);
			return true;
		}
		return false;
	}

	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(conveyorBeltSubtype!=null)
		{
			boolean update;
			if(conveyorBeltSubtype.canBeDyed()&&Utils.isDye(heldItem))
			{
				DyeColor dye = Utils.getDye(heldItem);
				update = dye!=null&&conveyorBeltSubtype.setDyeColour(dye);
			}
			else
				update = conveyorBeltSubtype.playerInteraction(this, player, hand, heldItem, hitX, hitY, hitZ, side);
			if(update)
			{
				this.markDirty();
				this.markContainingBlockForUpdate(null);
				world.addBlockEvent(getPos(), this.getBlockState().getBlock(), 0, 0);
				return true;
			}
		}
		return false;
	}

	@Override
	public float[] getBlockBounds()
	{
//		if(conveyorBeltSubtype != null)
//		{
//			AxisAlignedBB aabb = conveyorBeltSubtype.getSelectionBox(this, facing);
//			return new float[]{(float) aabb.minX, (float) aabb.minY, (float) aabb.minZ, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ};
//		}
		return new float[]{0, 0, 0, 1, .125f, 1};
	}

	private static final AxisAlignedBB COLISIONBB =
			new AxisAlignedBB(0, 0, 0, 1, .125F, 1);

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		if(conveyorBeltSubtype!=null)
		{
			List<AxisAlignedBB> boxes = new ArrayList<>();
			for(AxisAlignedBB aabb : conveyorBeltSubtype.getColisionBoxes(this, facing))
				boxes.add(aabb.offset(getPos()));
			return boxes;
		}
		return Lists.newArrayList(COLISIONBB.offset(getPos()));
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		if(conveyorBeltSubtype!=null)
		{
			List<AxisAlignedBB> boxes = new ArrayList<>();
			for(AxisAlignedBB aabb : conveyorBeltSubtype.getSelectionBoxes(this, facing))
				boxes.add(aabb.offset(getPos()));
			return boxes;
		}
		return Lists.newArrayList(COLISIONBB.offset(getPos()));
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, PlayerEntity player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}

	private LazyOptional<IItemHandler> insertionCap = registerCap(() -> new ConveyorInventoryHandler(this));

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if(cap==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return insertionCap.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public BlockFaceShape getFaceShape(Direction side)
	{
		IConveyorBelt subtype = this.getConveyorSubtype();
		if(subtype==null)
			return BlockFaceShape.UNDEFINED;
		if(side==Direction.DOWN&&subtype.getConveyorDirection()==ConveyorDirection.HORIZONTAL)
			return BlockFaceShape.SOLID;
		if(subtype instanceof ConveyorVertical)
		{
			if(side==this.facing)
				return BlockFaceShape.SOLID;
			else if(side.getAxis()==Axis.Y)
				return BlockFaceShape.UNDEFINED;
		}
		if(subtype instanceof ConveyorCovered||subtype instanceof ConveyorVerticalCovered||subtype instanceof ConveyorExtractCovered)
			if(side.getAxis()!=facing.getAxis())
				return BlockFaceShape.SOLID;
		return BlockFaceShape.UNDEFINED;
	}

	public static class ConveyorInventoryHandler implements IItemHandlerModifiable
	{
		ConveyorBeltTileEntity conveyor;

		public ConveyorInventoryHandler(ConveyorBeltTileEntity conveyor)
		{
			this.conveyor = conveyor;
		}

		@Override
		public int getSlots()
		{
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			if(!simulate)
			{
				ItemEntity entity = new ItemEntity(conveyor.getWorld(), conveyor.getPos().getX()+.5, conveyor.getPos().getY()+.1875, conveyor.getPos().getZ()+.5, stack.copy());
				entity.motionX = 0;
				entity.motionY = 0;
				entity.motionZ = 0;
				conveyor.getWorld().spawnEntity(entity);
				if(conveyor.conveyorBeltSubtype!=null)
					conveyor.conveyorBeltSubtype.onItemDeployed(conveyor, entity, conveyor.facing);
			}
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack)
		{
			return true;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack)
		{
		}
	}
}
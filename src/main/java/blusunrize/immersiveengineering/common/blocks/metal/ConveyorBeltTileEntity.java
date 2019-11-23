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
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
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
		ITickableTileEntity, IGeneralMultiblock
{
	private final IConveyorBelt conveyorBeltSubtype;

	public ConveyorBeltTileEntity(ResourceLocation typeName)
	{
		super(Preconditions.checkNotNull(ConveyorHandler.getTEType(typeName), "Not TE type for "+typeName));
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
			this.conveyorBeltSubtype.onEntityCollision(entity);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		if(nbt.contains("conveyorBeltSubtypeNBT", NBT.TAG_COMPOUND))
			conveyorBeltSubtype.readConveyorNBT(nbt.getCompound("conveyorBeltSubtypeNBT"));

		if(descPacket&&world!=null)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		if(conveyorBeltSubtype!=null)
			nbt.put("conveyorBeltSubtypeNBT", conveyorBeltSubtype.writeConveyorNBT());
	}

	@Override
	public Direction getFacing()
	{
		BlockState state = getWorldNonnull().getBlockState(pos);
		return state.get(ConveyorBlock.FACING);
	}

	@Override
	public void setFacing(Direction facing)
	{
		BlockState oldState = getWorldNonnull().getBlockState(pos);
		BlockState newState = oldState.with(ConveyorBlock.FACING, facing);
		getWorldNonnull().setBlockState(pos, newState);
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL_QUADRANT;
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
	public boolean isDummy()
	{
		return this.conveyorBeltSubtype!=null&&!this.conveyorBeltSubtype.isTicking();
	}

	@Override
	public void tick()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(this.conveyorBeltSubtype!=null)
			this.conveyorBeltSubtype.onUpdate();
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, Vec3d hitVec)
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
				update = conveyorBeltSubtype.playerInteraction(player, hand, heldItem, hitX, hitY, hitZ, side);
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
		return new float[]{0, 0, 0, 1, .125f, 1};
	}

	private static final AxisAlignedBB COLISIONBB =
			new AxisAlignedBB(0, 0, 0, 1, .125F, 1);

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		if(conveyorBeltSubtype!=null)
			return new ArrayList<>(conveyorBeltSubtype.getColisionBoxes());
		return Lists.newArrayList(COLISIONBB);
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		if(conveyorBeltSubtype!=null)
			return new ArrayList<>(conveyorBeltSubtype.getSelectionBoxes());
		return Lists.newArrayList(COLISIONBB);
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
				ItemEntity entity = new ItemEntity(conveyor.getWorldNonnull(), conveyor.getPos().getX()+.5, conveyor.getPos().getY()+.1875, conveyor.getPos().getZ()+.5, stack.copy());
				entity.setMotion(Vec3d.ZERO);
				conveyor.getWorldNonnull().addEntity(entity);
				if(conveyor.conveyorBeltSubtype!=null)
					conveyor.conveyorBeltSubtype.onItemDeployed(entity);
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
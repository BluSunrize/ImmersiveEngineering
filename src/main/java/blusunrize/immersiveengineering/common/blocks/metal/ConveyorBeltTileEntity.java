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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConveyorBeltTileEntity extends IEBaseTileEntity implements IStateBasedDirectional, ICollisionBounds,
		ISelectionBounds, IHammerInteraction, IPlayerInteraction, IConveyorTile, IPropertyPassthrough,
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
	public EnumProperty<Direction> getFacingProperty()
	{
		return ConveyorBlock.FACING;
	}

	@Override
	public Direction getFacing()
	{
		return IStateBasedDirectional.super.getFacing();
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
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity)
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

	@Nullable
	@Override
	public IGeneralMultiblock master()
	{
		return this;
	}

	@Override
	public void tick()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(this.conveyorBeltSubtype!=null)
			this.conveyorBeltSubtype.onUpdate();
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, Hand hand, Vec3d hitVec)
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

			if(!world.isRemote)
			{
				this.markDirty();
				this.markContainingBlockForUpdate(null);
				world.addBlockEvent(getPos(), this.getBlockState().getBlock(), 0, 0);
			}
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

	private static final VoxelShape COLISIONBB =
			VoxelShapes.create(0, 0, 0, 1, .125F, 1);

	@Override
	public VoxelShape getCollisionShape(ISelectionContext ctx)
	{
		if(conveyorBeltSubtype!=null)
			return conveyorBeltSubtype.getCollisionShape();
		return COLISIONBB;
	}

	@Override
	public VoxelShape getSelectionShape(@Nullable ISelectionContext ctx)
	{
		if(conveyorBeltSubtype!=null)
			return conveyorBeltSubtype.getSelectionShape();
		return COLISIONBB;
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

	// Make public
	@Override
	public boolean isRSPowered()
	{
		return super.isRSPowered();
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
			if(conveyor.getConveyorSubtype().isBlocked())
				return stack;
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
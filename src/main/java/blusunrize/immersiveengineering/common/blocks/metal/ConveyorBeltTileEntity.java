/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.MOD)
public class ConveyorBeltTileEntity extends IEBaseTileEntity implements IStateBasedDirectional, ICollisionBounds,
		ISelectionBounds, IHammerInteraction, IPlayerInteraction, IConveyorTile, IPropertyPassthrough,
		IETickableBlockEntity
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
	public void onEntityCollision(Level world, Entity entity)
	{
		if(this.conveyorBeltSubtype!=null)
			this.conveyorBeltSubtype.onEntityCollision(entity);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		if(nbt.contains("conveyorBeltSubtypeNBT", NBT.TAG_COMPOUND))
			conveyorBeltSubtype.readConveyorNBT(nbt.getCompound("conveyorBeltSubtypeNBT"));

		if(descPacket&&level!=null)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		if(conveyorBeltSubtype!=null)
			nbt.put("conveyorBeltSubtypeNBT", conveyorBeltSubtype.writeConveyorNBT());
	}

	@Override
	public Property<Direction> getFacingProperty()
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
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return !entity.isShiftKeyDown();
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
	public void tickCommon()
	{
		ApiUtils.checkForNeedlessTicking(this, te -> te.conveyorBeltSubtype!=null&&!te.conveyorBeltSubtype.isTicking());
		if(this.conveyorBeltSubtype!=null)
			this.conveyorBeltSubtype.onUpdate();
	}

	@Override
	public boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(player.isShiftKeyDown()&&conveyorBeltSubtype!=null&&conveyorBeltSubtype.changeConveyorDirection())
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

			if(!level.isClientSide)
			{
				this.setChanged();
				this.markContainingBlockForUpdate(null);
				level.blockEvent(getBlockPos(), this.getBlockState().getBlock(), 0, 0);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
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
				this.setChanged();
				this.markContainingBlockForUpdate(null);
				level.blockEvent(getBlockPos(), this.getBlockState().getBlock(), 0, 0);
				return true;
			}
		}
		return false;
	}

	private static final VoxelShape COLISIONBB =
			Shapes.box(0, 0, 0, 1, .125F, 1);

	@Override
	public VoxelShape getCollisionShape(CollisionContext ctx)
	{
		if(conveyorBeltSubtype!=null)
			return conveyorBeltSubtype.getCollisionShape();
		return COLISIONBB;
	}

	@Override
	public VoxelShape getSelectionShape(@Nullable CollisionContext ctx)
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

	@SubscribeEvent
	public static void registerConveyorTEsAndBlocks(RegistryEvent.NewRegistry ev)
	{
		for(ResourceLocation rl : ConveyorHandler.classRegistry.keySet())
		{
			RegistryObject<BlockEntityType<?>> type = IETileTypes.REGISTER.register(
					ConveyorHandler.getRegistryNameFor(rl).getPath(), () -> new BlockEntityType<>(
							() -> new ConveyorBeltTileEntity(rl), ImmutableSet.of(ConveyorHandler.getBlock(rl)), null
					));
			ConveyorHandler.tileEntities.put(rl, type);
		}
		MetalDevices.initConveyors();
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
				ItemEntity entity = new ItemEntity(conveyor.getWorldNonnull(), conveyor.getBlockPos().getX()+.5, conveyor.getBlockPos().getY()+.1875, conveyor.getBlockPos().getZ()+.5, stack.copy());
				entity.setDeltaMovement(Vec3.ZERO);
				conveyor.getWorldNonnull().addFreshEntity(entity);
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
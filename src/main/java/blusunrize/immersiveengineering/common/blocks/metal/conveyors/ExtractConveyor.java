/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.client.render.conveyor.ExtractConveyorRender;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.OptionalDouble;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

/**
 * @author BluSunrize - 19.05.2017
 */
public class ExtractConveyor extends ConveyorBase<ExtractConveyor>
{
	public static final ResourceLocation NAME = new ResourceLocation(MODID, "extract");
	public static final IConveyorType<ExtractConveyor> TYPE = new BasicConveyorType<>(
			NAME, true, true, ExtractConveyor::new, () -> new ExtractConveyorRender(texture_on, texture_off)
	);
	private int transferCooldown = -1;
	private int transferTickrate = 8;
	private double extension = -1;
	private Rotation relativeExtractDir = Rotation.CLOCKWISE_180;

	public ExtractConveyor(BlockEntity tile)
	{
		super(tile);
	}

	@Override
	public IConveyorType<ExtractConveyor> getType()
	{
		return TYPE;
	}

	@Override
	public boolean changeConveyorDirection()
	{
		return false;
	}

	@Override
	public boolean setConveyorDirection(ConveyorDirection dir)
	{
		return false;
	}

	private boolean extensionRecursionLock = false;
	private static final VoxelShape ALLOWED_MISSING_SHAPE = Shapes.joinUnoptimized(
			Shapes.box(1/16., 1/16., 1/16., 15/16., 15/16., 15/16.),
			Shapes.block(),
			BooleanOp.NOT_SAME
	);

	/**
	 * @return empty if the correct value can not be computed at this time. In this case, assume 0, but do not cache.
	 * Otherwise an optional of the correct extension length.
	 */
	private OptionalDouble getExtensionIntoBlock(BlockEntity tile)
	{
		if(tile==null||!tile.hasLevel()||extensionRecursionLock)
			return OptionalDouble.empty();
		extensionRecursionLock = true;
		double extend = 0;

		Level world = tile.getLevel();
		BlockPos neighbour = tile.getBlockPos().relative(this.getExtractDirection());
		if(!world.isEmptyBlock(neighbour))
		{
			BlockState connected = world.getBlockState(neighbour);
			BlockEntity connectedTile = world.getBlockEntity(neighbour);
			if(connectedTile!=null&&connectedTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.getExtractDirection().getOpposite()).isPresent())
			{
				VoxelShape connectedShape = connected.getShape(world, neighbour);
				VoxelShape projected = connectedShape.getFaceShape(this.getExtractDirection().getOpposite());
				if(Shapes.joinIsNotEmpty(projected, ALLOWED_MISSING_SHAPE, BooleanOp.OR))
				{
					AABB aabb = connectedShape.bounds();
					extend = switch(getExtractDirection())
							{
								case NORTH -> 1-aabb.maxZ;
								case SOUTH -> aabb.minZ;
								case WEST -> 1-aabb.maxX;
								case EAST -> aabb.minX;
								default -> throw new IllegalStateException("Unexpected value: "+getExtractDirection());
							};
					if(extend > .25)
						extend = 0.25;
					double round = extend%.0625;
					if(round < extend)
						extend = round+.0625;
				}
			}
		}
		extensionRecursionLock = false;
		return OptionalDouble.of(extend);
	}

	@Override
	public boolean isActive()
	{
		return true;
	}

	@Override
	public void onUpdate()
	{
		if(!getBlockEntity().getLevel().isClientSide)
		{
			if(this.transferCooldown > 0)
			{
				this.transferCooldown--;
			}
			if(!isPowered()&&this.transferCooldown <= 0)
			{
				Level world = getBlockEntity().getLevel();
				BlockPos neighbour = getBlockEntity().getBlockPos().relative(this.getExtractDirection());
				if(!world.isEmptyBlock(neighbour))
				{
					LazyOptional<IItemHandler> cap = CapabilityUtils.findItemHandlerAtPos(world, neighbour, this.getExtractDirection().getOpposite(), true);
					cap.ifPresent(itemHandler ->
					{
						for(int i = 0; i < itemHandler.getSlots(); i++)
						{
							ItemStack extractItem = itemHandler.extractItem(i, 1, true);
							if(!extractItem.isEmpty())
							{
								extractItem = itemHandler.extractItem(i, 1, false);
								ItemEntity entity = new ItemEntity(world,
										getBlockEntity().getBlockPos().getX()+.5,
										getBlockEntity().getBlockPos().getY()+.1875,
										getBlockEntity().getBlockPos().getZ()+.5, extractItem);
								entity.setDeltaMovement(Vec3.ZERO);
								world.addFreshEntity(entity);
								this.onItemDeployed(entity);
								this.transferCooldown = this.transferTickrate;
								return;
							}
						}
					});
				}
			}
		}
	}

	@Override
	public boolean playerInteraction(Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, Direction side)
	{
		if(super.playerInteraction(player, hand, heldItem, hitX, hitY, hitZ, side))
			return true;
		if(Utils.isHammer(heldItem)&&player.isShiftKeyDown())
		{
			do
			{
				relativeExtractDir = Rotation.values()[(relativeExtractDir.ordinal()+1)%Rotation.values().length];
			} while(relativeExtractDir==Rotation.NONE);
			return true;
		}
		if(Utils.isScrewdriver(heldItem))
		{
			if(this.transferTickrate==4)
				this.transferTickrate = 8;
			else if(this.transferTickrate==8)
				this.transferTickrate = 16;
			else if(this.transferTickrate==16)
				this.transferTickrate = 20;
			else if(this.transferTickrate==20)
				this.transferTickrate = 4;
			player.displayClientMessage(new TranslatableComponent(Lib.CHAT_INFO+"tickrate", this.transferTickrate), true);
			return true;
		}
		return false;
	}

	public double getCurrentExtension()
	{
		double extension;
		if(this.extension >= 0)
			extension = this.extension;
		else
		{
			OptionalDouble optValue = getExtensionIntoBlock(getBlockEntity());
			if(optValue.isPresent())
				extension = this.extension = optValue.getAsDouble();
			else
				extension = 0;
		}
		return extension;
	}

	@Override
	public VoxelShape getSelectionShape()
	{
		VoxelShape ret = conveyorBounds;
		double extension = getCurrentExtension();
		VoxelShape extensionShape = switch(getExtractDirection())
				{
					case NORTH -> Shapes.box(.0625, .125, -extension, .9375, .75, .375-extension);
					case SOUTH -> Shapes.box(.0625, .125, .625+extension, .9375, .75, 1+extension);
					case WEST -> Shapes.box(-extension, .125, .0625, .375-extension, .75, .9375);
					case EAST -> Shapes.box(.625+extension, .125, .0625, 1+extension, .75, .9375);
					default -> null;
				};
		if(extensionShape!=null)
			ret = Shapes.join(ret, extensionShape, BooleanOp.OR);
		return ret;
	}

	@Override
	public CompoundTag writeConveyorNBT()
	{
		CompoundTag nbt = super.writeConveyorNBT();
		nbt.putInt("transferCooldown", transferCooldown);
		nbt.putInt("transferTickrate", transferTickrate);
		nbt.putInt("relativeExtractDir", relativeExtractDir.ordinal());
		return nbt;
	}

	@Override
	public void readConveyorNBT(CompoundTag nbt)
	{
		super.readConveyorNBT(nbt);
		transferCooldown = nbt.getInt("transferCooldown");
		transferTickrate = nbt.getInt("transferTickrate");
		relativeExtractDir = Rotation.values()[nbt.getInt("relativeExtractDir")];
		if(relativeExtractDir==Rotation.NONE)
			relativeExtractDir = Rotation.CLOCKWISE_180;
	}

	public Direction getExtractDirection()
	{
		return relativeExtractDir.rotate(getFacing());
	}
}

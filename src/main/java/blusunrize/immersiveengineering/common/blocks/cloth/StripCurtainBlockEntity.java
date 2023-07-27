/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author BluSunrize - 01.10.2016
 */
public class StripCurtainBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE, IRedstoneOutput,
		IScrewdriverInteraction, IAdvancedDirectionalBE, IStateBasedDirectional, IColouredBE, IBlockEntityDrop,
		IBlockBounds
{
	public int colour = 0xffffff;
	private int redstoneSignal = 0;
	private boolean strongSignal = false;

	public StripCurtainBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.STRIP_CURTAIN.get(), pos, state);
	}

	@Override
	public void tickServer()
	{
		if(level.getGameTime()%4==((getBlockPos().getX()^getBlockPos().getZ())&3))
		{
			List<Entity> entities = level.getEntitiesOfClass(Entity.class, getEntityCollectionBox());
			if(!isCeilingAttached()&&!entities.isEmpty()&&redstoneSignal==0)
			{
				redstoneSignal = 15;
				sendRSUpdates();
			}
			if(entities.isEmpty()&&redstoneSignal!=0)
			{
				redstoneSignal = 0;
				sendRSUpdates();
			}
		}
	}

	@Override
	public void onEntityCollision(Level world, Entity entity)
	{
		if(isCeilingAttached()&&entity.isAlive()&&redstoneSignal==0&&entity.getBoundingBox().intersects(getEntityCollectionBox()))
		{
			redstoneSignal = 15;
			sendRSUpdates();
		}
	}

	private void sendRSUpdates()
	{
		setChanged();
		level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
		level.updateNeighborsAt(getBlockPos().relative(getStrongSignalSide()), getBlockState().getBlock());
	}

	private Direction getStrongSignalSide()
	{
		if(isCeilingAttached())
			return Direction.UP;
		else
			return getFacing();
	}

	private AABB getEntityCollectionBox()
	{
		AABB aabb = bounds[isCeilingAttached()?(getFacing().getAxis()==Axis.Z?4: 5): ((getFacing().ordinal()-2)%4)];
		return new AABB(aabb.minX, aabb.minY-.8125, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ).move(getBlockPos());
	}

	@Override
	public int getStrongRSOutput(@Nonnull Direction side)
	{
		if(!strongSignal||side!=getStrongSignalSide().getOpposite())
			return 0;
		return getWeakRSOutput(side);
	}

	@Override
	public int getWeakRSOutput(@Nonnull Direction side)
	{
		if(side==Direction.DOWN)
			return 0;
		return redstoneSignal;
	}

	@Override
	public boolean canConnectRedstone(@Nonnull Direction side)
	{
		return false;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		colour = nbt.getInt("colour");
		this.strongSignal = nbt.getBoolean("strongSignal");
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.putInt("colour", colour);
		nbt.putBoolean("strongSignal", this.strongSignal);
	}

	private static final AABB[] bounds = {
			new AABB(0, 0, 0, 1, .1875f, .0625f),
			new AABB(0, 0, .9375f, 1, .1875f, 1),
			new AABB(0, 0, 0, .0625f, .1875f, 1),
			new AABB(.9375f, 0, 0, 1, .1875f, 1),
			new AABB(0, .8125f, .46875f, 1, 1, .53125f),
			new AABB(.46875f, .8125f, 0, .53125f, 1, 1)
	};
	private static final VoxelShape[] shapes = Arrays.stream(bounds)
			.map(Shapes::create)
			.toArray(VoxelShape[]::new);

	@Nonnull
	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return shapes[isCeilingAttached()?(getFacing().getAxis()==Axis.Z?4: 5): ((getFacing().ordinal()-2)%4)];
	}

	@Nonnull
	@Override
	public Property<Direction> getFacingProperty()
	{
		return StripCurtainBlock.FACING;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public void onDirectionalPlacement(Direction side, float hitX, float hitY, float hitZ, LivingEntity placer)
	{
		setCeilingAttached(side==Direction.DOWN);
	}

	@Override
	public int getRenderColour(int tintIndex)
	{
		if(tintIndex==1)
			return colour;
		return 0xffffff;
	}

	@Override
	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		if(colour!=0xffffff)
			ItemNBTHelper.putInt(stack, "colour", colour);
		drop.accept(stack);
	}

	@Override
	public void onBEPlaced(BlockPlaceContext ctx)
	{
		final ItemStack stack = ctx.getItemInHand();
		if(ItemNBTHelper.hasKey(stack, "colour"))
			this.colour = ItemNBTHelper.getInt(stack, "colour");
	}

	@Override
	public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(!level.isClientSide)
		{
			strongSignal = !strongSignal;
			player.displayClientMessage(
					Component.translatable(Lib.CHAT_INFO+"rsControl.strongSignal."+strongSignal), true
			);
			sendRSUpdates();
		}
		return InteractionResult.SUCCESS;
	}

	public boolean isCeilingAttached()
	{
		return getBlockState().getValue(StripCurtainBlock.CEILING_ATTACHED);
	}

	public void setCeilingAttached(boolean ceilingAttached)
	{
		BlockState newState = getBlockState().setValue(StripCurtainBlock.CEILING_ATTACHED, ceilingAttached);
		getLevelNonnull().setBlockAndUpdate(worldPosition, newState);
	}
}

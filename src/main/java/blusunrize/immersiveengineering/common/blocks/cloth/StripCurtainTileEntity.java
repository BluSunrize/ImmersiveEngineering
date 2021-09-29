/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.List;

/**
 * @author BluSunrize - 01.10.2016
 */
public class StripCurtainTileEntity extends IEBaseTileEntity implements TickableBlockEntity, IRedstoneOutput, IScrewdriverInteraction,
		ICollisionBounds, IAdvancedDirectionalTile, IStateBasedDirectional, IColouredTile, ITileDrop, ISelectionBounds
{
	public int colour = 0xffffff;
	private int redstoneSignal = 0;
	private boolean strongSignal = false;

	public StripCurtainTileEntity()
	{
		super(IETileTypes.STRIP_CURTAIN.get());
	}

	@Override
	public void tick()
	{
		if(!level.isClientSide&&level.getGameTime()%4==((getBlockPos().getX()^getBlockPos().getZ())&3))
		{
			AABB aabb = bounds[isCeilingAttached()?(getFacing().getAxis()==Axis.Z?4: 5): ((getFacing().ordinal()-2)%4)];
			aabb = new AABB(aabb.minX, aabb.minY-.8125, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ).move(getBlockPos());
			List<Entity> entities = level.getEntitiesOfClass(Entity.class, aabb);
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
		if(isCeilingAttached()&&entity.isAlive()&&redstoneSignal==0)
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

	@Nonnull
	@Override
	public VoxelShape getSelectionShape(@Nullable CollisionContext ctx)
	{
		AABB aabb = bounds[isCeilingAttached()?(getFacing().getAxis()==Axis.Z?4: 5): ((getFacing().ordinal()-2)%4)];
		return Shapes.box(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
	}

	private static final CachedVoxelShapes<Pair<Boolean, Direction>> SHAPES = new CachedVoxelShapes<>(StripCurtainTileEntity::getShape);

	@Nonnull
	@Override
	public VoxelShape getCollisionShape(CollisionContext ctx)
	{
		return SHAPES.get(Pair.of(isCeilingAttached(), getFacing()));
	}

	private static List<AABB> getShape(Pair<Boolean, Direction> key)
	{
		int index = key.getLeft()?(key.getRight().getAxis()==Axis.Z?4: 5): ((key.getRight().ordinal()-2)%4);
		return Lists.newArrayList(bounds[index]);
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
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	@Override
	public void onDirectionalPlacement(Direction side, float hitX, float hitY, float hitZ, LivingEntity placer)
	{
		if(side==Direction.DOWN)
			setCeilingAttached(true);
		else
			setCeilingAttached(false);
	}

	@Override
	public int getRenderColour(int tintIndex)
	{
		if(tintIndex==1)
			return colour;
		return 0xffffff;
	}

	@Override
	public List<ItemStack> getTileDrops(LootContext context)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		if(colour!=0xffffff)
			ItemNBTHelper.putInt(stack, "colour", colour);
		return ImmutableList.of(stack);
	}

	@Override
	public void readOnPlacement(LivingEntity placer, ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "colour"))
			this.colour = ItemNBTHelper.getInt(stack, "colour");
	}

	@Override
	public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(!level.isClientSide)
		{
			strongSignal = !strongSignal;
			ChatUtils.sendServerNoSpamMessages(player, new TranslatableComponent(Lib.CHAT_INFO+"rsControl.strongSignal."+strongSignal));
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
		getWorldNonnull().setBlockAndUpdate(worldPosition, newState);
	}
}

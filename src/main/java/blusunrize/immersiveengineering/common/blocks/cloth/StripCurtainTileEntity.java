/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.shapes.CachedVoxelShapes;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author BluSunrize - 01.10.2016
 */
public class StripCurtainTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IRedstoneOutput, IScrewdriverInteraction,
		ICollisionBounds, IAdvancedDirectionalTile, IStateBasedDirectional, IColouredTile, ITileDrop, ISelectionBounds
{
	public static TileEntityType<StripCurtainTileEntity> TYPE;

	public int colour = 0xffffff;
	private int redstoneSignal = 0;
	private boolean strongSignal = false;

	public StripCurtainTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void tick()
	{
		if(!world.isRemote&&world.getGameTime()%4==((getPos().getX()^getPos().getZ())&3))
		{
			AxisAlignedBB aabb = bounds[isCeilingAttached()?(getFacing().getAxis()==Axis.Z?4: 5): ((getFacing().ordinal()-2)%4)];
			aabb = new AxisAlignedBB(aabb.minX, aabb.minY-.8125, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ).offset(getPos());
			List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, aabb);
			if(!isCeilingAttached()&&!entities.isEmpty()&&redstoneSignal==0)
			{
				redstoneSignal = 15;
				markDirty();
				world.notifyNeighborsOfStateChange(getPos(), getBlockState().getBlock());
				world.notifyNeighborsOfStateChange(getPos().offset(getFacing()), getBlockState().getBlock());
			}
			if(entities.isEmpty()&&redstoneSignal!=0)
			{
				redstoneSignal = 0;
				world.notifyNeighborsOfStateChange(getPos(), getBlockState().getBlock());
				world.notifyNeighborsOfStateChange(getPos().offset(getFacing()), getBlockState().getBlock());
			}
		}
	}

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(isCeilingAttached()&&entity.isAlive()&&redstoneSignal==0)
		{
			AxisAlignedBB aabb = bounds[isCeilingAttached()?(getFacing().getAxis()==Axis.Z?4: 5): ((getFacing().ordinal()-2)%4)];
			aabb = new AxisAlignedBB(aabb.minX, aabb.minY-.8125, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ).offset(getPos());
			if(entity.getBoundingBox().intersects(aabb))
			{
				redstoneSignal = 15;
				markDirty();
				world.notifyNeighborsOfStateChange(getPos(), getBlockState().getBlock());
				world.notifyNeighborsOfStateChange(getPos().offset(Direction.UP), getBlockState().getBlock());
			}
		}
	}

	@Override
	public int getStrongRSOutput(@Nonnull Direction side)
	{
		if(!strongSignal)
			return 0;
		return getWeakRSOutput(getFacing());
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
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		colour = nbt.getInt("colour");
		this.strongSignal = nbt.getBoolean("strongSignal");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putInt("colour", colour);
		nbt.putBoolean("strongSignal", this.strongSignal);
	}

	private static final AxisAlignedBB[] bounds = {
			new AxisAlignedBB(0, 0, 0, 1, .1875f, .0625f),
			new AxisAlignedBB(0, 0, .9375f, 1, .1875f, 1),
			new AxisAlignedBB(0, 0, 0, .0625f, .1875f, 1),
			new AxisAlignedBB(.9375f, 0, 0, 1, .1875f, 1),
			new AxisAlignedBB(0, .8125f, .46875f, 1, 1, .53125f),
			new AxisAlignedBB(.46875f, .8125f, 0, .53125f, 1, 1)
	};

	@Nonnull
	@Override
	public VoxelShape getSelectionShape(@Nullable ISelectionContext ctx)
	{
		AxisAlignedBB aabb = bounds[isCeilingAttached()?(getFacing().getAxis()==Axis.Z?4: 5): ((getFacing().ordinal()-2)%4)];
		return VoxelShapes.create(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
	}

	private static final CachedVoxelShapes<Pair<Boolean, Direction>> SHAPES = new CachedVoxelShapes<>(StripCurtainTileEntity::getShape);

	@Nonnull
	@Override
	public VoxelShape getCollisionShape(ISelectionContext ctx)
	{
		return SHAPES.get(Pair.of(isCeilingAttached(), getFacing()));
	}

	private static List<AxisAlignedBB> getShape(Pair<Boolean, Direction> key)
	{
		int index = key.getLeft()?(key.getRight().getAxis()==Axis.Z?4: 5): ((key.getRight().ordinal()-2)%4);
		return Lists.newArrayList(bounds[index]);
	}

	@Nonnull
	@Override
	public EnumProperty<Direction> getFacingProperty()
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
	public boolean screwdriverUseSide(Direction side, PlayerEntity player, Hand hand, Vec3d hitVec)
	{
		if(!world.isRemote)
		{
			strongSignal = !strongSignal;
			ChatUtils.sendServerNoSpamMessages(player, new TranslationTextComponent(Lib.CHAT_INFO+"rsControl.strongSignal."+strongSignal));
		}
		return true;
	}

	public boolean isCeilingAttached()
	{
		return getBlockState().get(StripCurtainBlock.CEILING_ATTACHED);
	}

	public void setCeilingAttached(boolean ceilingAttached)
	{
		BlockState newState = getBlockState().with(StripCurtainBlock.CEILING_ATTACHED, ceilingAttached);
		getWorldNonnull().setBlockState(pos, newState);
	}
}

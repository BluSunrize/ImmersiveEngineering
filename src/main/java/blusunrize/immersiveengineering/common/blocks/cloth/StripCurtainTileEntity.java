/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;

/**
 * @author BluSunrize - 01.10.2016
 */
public class StripCurtainTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IRedstoneOutput, IHammerInteraction,
		IAdvancedCollisionBounds, IAdvancedDirectionalTile, IDualState, IColouredTile, ITileDrop
{
	public Direction facing = Direction.NORTH;
	public boolean ceilingAttached = false;
	public int colour = 0xffffff;
	private int redstoneSignal = 0;
	private boolean strongSignal = false;

	@Override
	public void tick()
	{
		if(!world.isRemote&&world.getGameTime()%4==((getPos().getX()^getPos().getZ())&3))
		{
			AxisAlignedBB aabb = bounds[ceilingAttached?(facing.getAxis()==Axis.Z?4: 5): ((facing.ordinal()-2)%4)];
			aabb = new AxisAlignedBB(aabb.minX, aabb.minY-.8125, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ).offset(getPos());
			List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, aabb);
			if(!ceilingAttached&&!entities.isEmpty()&&redstoneSignal==0)
			{
				redstoneSignal = 15;
				markDirty();
				world.notifyNeighborsOfStateChange(getPos(), getBlockState().getBlock());
				world.notifyNeighborsOfStateChange(getPos().offset(facing), getBlockState().getBlock());
			}
			if(entities.isEmpty()&&redstoneSignal!=0)
			{
				redstoneSignal = 0;
				world.notifyNeighborsOfStateChange(getPos(), getBlockState().getBlock());
				world.notifyNeighborsOfStateChange(getPos().offset(facing), getBlockState().getBlock());
			}
		}
	}

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(ceilingAttached&&entity.isAlive()&&redstoneSignal==0)
		{
			AxisAlignedBB aabb = bounds[ceilingAttached?(facing.getAxis()==Axis.Z?4: 5): ((facing.ordinal()-2)%4)];
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
	public int getStrongRSOutput(BlockState state, Direction side)
	{
		if(!strongSignal)
			return 0;
		return getWeakRSOutput(state, facing);
	}

	@Override
	public int getWeakRSOutput(BlockState state, Direction side)
	{
		if(side==Direction.DOWN)
			return 0;
		return redstoneSignal;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, Direction side)
	{
		return false;
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		facing = Direction.byIndex(nbt.getInt("facing"));
		ceilingAttached = nbt.getBoolean("ceilingAttached");
		colour = nbt.getInt("colour");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		if(facing!=null)
			nbt.putInt("facing", facing.ordinal());
		nbt.putBoolean("ceilingAttached", ceilingAttached);
		nbt.putInt("colour", colour);
	}

	AxisAlignedBB[] bounds = {
			new AxisAlignedBB(0, 0, 0, 1, .1875f, .0625f),
			new AxisAlignedBB(0, 0, .9375f, 1, .1875f, 1),
			new AxisAlignedBB(0, 0, 0, .0625f, .1875f, 1),
			new AxisAlignedBB(.9375f, 0, 0, 1, .1875f, 1),
			new AxisAlignedBB(0, .8125f, .46875f, 1, 1, .53125f),
			new AxisAlignedBB(.46875f, .8125f, 0, .53125f, 1, 1)
	};

	@Override
	public float[] getBlockBounds()
	{
		AxisAlignedBB aabb = bounds[ceilingAttached?(facing.getAxis()==Axis.Z?4: 5): ((facing.ordinal()-2)%4)];
		return new float[]{(float)aabb.minX, (float)aabb.minY, (float)aabb.minZ, (float)aabb.maxX, (float)aabb.maxY, (float)aabb.maxZ};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		return Lists.newArrayList(bounds[ceilingAttached?(facing.getAxis()==Axis.Z?4: 5): ((facing.ordinal()-2)%4)]);
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
	public int getFacingLimitation()
	{
		return 2;
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
	public void onDirectionalPlacement(Direction side, float hitX, float hitY, float hitZ, LivingEntity placer)
	{
		if(side==Direction.DOWN)
			ceilingAttached = true;
	}

	@Override
	public boolean getIsSecondState()
	{
		return ceilingAttached;
	}

	@Override
	public PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf)
	{
		return IEProperties.BOOLEANS[0];
	}

	@Override
	public int getRenderColour(int tintIndex)
	{
		if(tintIndex==1)
			return colour;
		return 0xffffff;
	}

	@Override
	public ItemStack getTileDrop(PlayerEntity player, BlockState state)
	{
		ItemStack stack = new ItemStack(state.getBlock(), 1);
		if(colour!=0xffffff)
			ItemNBTHelper.putInt(stack, "colour", colour);
		return stack;
	}

	@Override
	public void readOnPlacement(LivingEntity placer, ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "colour"))
			this.colour = ItemNBTHelper.getInt(stack, "colour");
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, float hitX, float hitY, float hitZ)
	{
		strongSignal = !strongSignal;
		ChatUtils.sendServerNoSpamMessages(player, new TranslationTextComponent(Lib.CHAT_INFO+"rsControl.strongSignal."+strongSignal));
		return true;
	}
}

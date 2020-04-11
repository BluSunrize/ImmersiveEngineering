/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.vecmath.Matrix4f;
import java.util.*;

public class ChuteTileEntity extends IEBaseTileEntity implements IStateBasedDirectional, IAdvancedHasObjProperty,
		IHammerInteraction, IAdvancedSelectionBounds, IAdvancedCollisionBounds
{
	private static final String NBT_POS = "immersiveengineering:chutePos";
	private static final String NBT_TIME = "immersiveengineering:chuteTime";
	private static final String NBT_GLITCH = "immersiveengineering:chuteGlitched";
	public static TileEntityType<ChuteTileEntity> TYPE;

	private boolean diagonal = false;

	public ChuteTileEntity()
	{
		super(TYPE);
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
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
		return !entity.isSneaking();
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		boolean contact = false;
		Direction facing = getFacing();
		if(!diagonal)
			contact = entity.posY-getPos().getY() <= .125;
		else if(facing==Direction.NORTH)
			contact = entity.posZ-getPos().getZ() <= .125;
		else if(facing==Direction.SOUTH)
			contact = entity.posZ-getPos().getZ() >= .875;
		else if(facing==Direction.WEST)
			contact = entity.posX-getPos().getX() <= .125;
		else if(facing==Direction.EAST)
			contact = entity.posX-getPos().getX() >= .875;

		if(this.diagonal&&entity.posY-getPos().getY() <= .625)
		{
			BlockPos target = getPos().offset(facing);
			TileEntity targetTile = world.getTileEntity(target);
			boolean opposedChute = targetTile instanceof ChuteTileEntity&&((ChuteTileEntity)targetTile).diagonal;
			// We're not going to redirect into another diagonal chute, to avoid infinite bouncing
			if(opposedChute)
				return;

			long time = world.getGameTime();
			long nbt_pos = getPos().toLong();

			boolean prevent = entity.getPersistentData().contains(NBT_POS)
					&&nbt_pos==entity.getPersistentData().getLong(NBT_POS)
					&&time-entity.getPersistentData().getLong(NBT_TIME) < 20;
			// glitch timer resets after 60 seconds
			boolean glitched = entity.getPersistentData().contains(NBT_GLITCH)&&time-entity.getPersistentData().getLong(NBT_GLITCH) < 1200;

			if(entity.getWidth() > 0.75||entity.getHeight() > 0.75)
			{
				// We're not going to redirect into a colliding block
				if(world.getBlockState(target).causesSuffocation(world, target))
					return;
				if(!glitched)
				{
					Vec3d oldPos = entity.getPositionVec();
					double py = entity.getHeight() > 1?getPos().getY()+.125: entity.posY;
					entity.setPosition(target.getX()+.5, py, target.getZ()+.5);
					if(entity.isEntityInsideOpaqueBlock())
					{
						entity.setPosition(oldPos.x, oldPos.y, oldPos.z);
						entity.getPersistentData().putLong(NBT_GLITCH, time);
					}
				}
			}
			else
			{
				double mY = entity.getMotion().y;
				if(mY==0)
					mY = 0.015;
				entity.setMotion(facing.getXOffset(), mY, facing.getZOffset());
			}

			if(!contact&&!prevent&&!glitched)
			{
				world.playSound(null, entity.posX, entity.posY, entity.posZ, IESounds.chute, SoundCategory.BLOCKS, .6f+(.4f*world.rand.nextFloat()), .5f+(.5f*world.rand.nextFloat()));
				entity.getPersistentData().putLong(NBT_POS, nbt_pos);
				entity.getPersistentData().putLong(NBT_TIME, time);
			}
		}

		if(entity instanceof ItemEntity)
		{
			ItemEntity itemEntity = (ItemEntity)entity;
			itemEntity.setPickupDelay(10);
			if(!contact)
			{
				if(itemEntity.age > itemEntity.lifespan-60*20)
					itemEntity.setAgeToCreativeDespawnTime();
			}
			else
			{
				TileEntity inventoryTile;
				BlockPos invPos = diagonal?getPos().offset(facing): getPos().down();
				inventoryTile = world.getTileEntity(invPos);
				if(!world.isRemote&&inventoryTile!=null&&!(inventoryTile instanceof IConveyorTile))
				{
					LazyOptional<IItemHandler> cap = ApiUtils.findItemHandlerAtPos(world, invPos, getFacing().getOpposite(), true);
					cap.ifPresent(itemHandler -> {
						ItemStack stack = itemEntity.getItem();
						ItemStack temp = ItemHandlerHelper.insertItem(itemHandler, stack.copy(), true);
						if(temp.isEmpty()||temp.getCount() < stack.getCount())
						{
							temp = ItemHandlerHelper.insertItem(itemHandler, stack, false);
							if(temp.isEmpty())
								itemEntity.remove();
							else if(temp.getCount() < stack.getCount())
								itemEntity.setItem(temp);
						}
					});
				}
			}
		}
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		diagonal = nbt.getBoolean("diagonal");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putBoolean("diagonal", diagonal);
	}

	@Override
	public float[] getBlockBounds()
	{
		return null;
	}

	static final Map<Direction, AxisAlignedBB> AABB_SIDES = new EnumMap<>(Direction.class);

	static
	{
		AABB_SIDES.put(Direction.NORTH, new AxisAlignedBB(0, 0, 0, 1, 1, .0625));
		AABB_SIDES.put(Direction.SOUTH, new AxisAlignedBB(0, 0, .9375, 1, 1, 1));
		AABB_SIDES.put(Direction.WEST, new AxisAlignedBB(0, 0, 0, .0625, 1, 1));
		AABB_SIDES.put(Direction.EAST, new AxisAlignedBB(.9375, 0, 0, 1, 1, 1));
	}

	static final AxisAlignedBB AABB_DOWN = new AxisAlignedBB(0, 0, 0, 1, .125f, 1);

	@Override
	public List<AxisAlignedBB> getAdvancedCollisionBounds()
	{
		ArrayList<AxisAlignedBB> list = new ArrayList<>();
		for(Direction dir : Direction.BY_HORIZONTAL_INDEX)
			if(!isInwardConveyor(dir)&&(!diagonal||dir!=getFacing()))
				list.add(AABB_SIDES.get(dir));
		if(diagonal)
			list.add(AABB_DOWN);
		return list;
	}

	static final List<AxisAlignedBB> selectionBoxes = Collections.singletonList(new AxisAlignedBB(0, 0, 0, 1, 1, 1));

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		return selectionBoxes;
	}

	@Override
	public IEObjState getIEObjState(BlockState state)
	{
		String key = getRenderCacheKey();
		return getStateFromKey(key);
	}

	public static HashMap<String, IEObjState> cachedOBJStates = new HashMap<>();

	private String getRenderCacheKey()
	{
		if(diagonal)
			return "diagonal:"+getFacing().name();
		String s = "base";
		for(Direction dir : Direction.BY_HORIZONTAL_INDEX)
			if(!isInwardConveyor(dir))
				s += ":"+dir.name().toLowerCase();
		return s;
	}

	public static IEObjState getStateFromKey(String key)
	{
		if(!cachedOBJStates.containsKey(key))
		{
			String[] split = key.split(":");
			if("diagonal".equals(split[0]))
			{
				Direction dir = Direction.valueOf(split[1]).getOpposite();
				Matrix4f matrix = new Matrix4(dir).toMatrix4f();
				cachedOBJStates.put(key, new IEObjState(VisibilityList.show("diagonal"), new TRSRTransformation(matrix)));
			}
			else
				cachedOBJStates.put(key, new IEObjState(VisibilityList.show(split)));
		}
		return cachedOBJStates.get(key);
	}

	protected boolean isInwardConveyor(Direction f)
	{
		TileEntity te = world.getTileEntity(getPos().offset(f));
		if(te instanceof IConveyorTile)
		{
			IConveyorBelt sub = ((IConveyorTile)te).getConveyorSubtype();
			if(sub!=null)
				for(Direction f2 : sub.sigTransportDirections())
					if(f==f2.getOpposite())
						return true;
		}
		else if(te instanceof ChuteTileEntity)
			return ((ChuteTileEntity)te).diagonal&&((ChuteTileEntity)te).getFacing()==f.getOpposite();

		te = world.getTileEntity(getPos().add(0, -1, 0).offset(f));
		if(te instanceof IConveyorTile)
		{
			IConveyorBelt sub = ((IConveyorTile)te).getConveyorSubtype();
			if(sub!=null)
			{
				int b = 0;
				for(Direction f2 : sub.sigTransportDirections())
				{
					if(f==f2.getOpposite())
						b++;
					else if(Direction.UP==f2)
						b++;
					if(b==2)
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, Vec3d hitVec)
	{
		if(player.isSneaking())
		{
			if(!world.isRemote)
			{
				this.diagonal = !this.diagonal;
				this.markDirty();
				this.markContainingBlockForUpdate(null);
				world.addBlockEvent(getPos(), getBlockState().getBlock(), 0, 0);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

}
/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.BasicConveyor;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.mixin.accessors.ItemEntityAccess;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ChuteTileEntity extends IEBaseTileEntity implements IStateBasedDirectional, IAdvancedHasObjProperty,
		IHammerInteraction, ISelectionBounds, ICollisionBounds
{
	private static final String NBT_POS = "immersiveengineering:chutePos";
	private static final String NBT_TIME = "immersiveengineering:chuteTime";
	private static final String NBT_GLITCH = "immersiveengineering:chuteGlitched";
	private boolean diagonal = false;

	public ChuteTileEntity()
	{
		super(IETileTypes.CHUTE.get());
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
	public boolean canHammerRotate(Direction side, Vector3d hit, LivingEntity entity)
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
			contact = entity.getPosY()-getPos().getY() <= .125;
		else if(facing==Direction.NORTH)
			contact = entity.getPosZ()-getPos().getZ() <= .125;
		else if(facing==Direction.SOUTH)
			contact = entity.getPosZ()-getPos().getZ() >= .875;
		else if(facing==Direction.WEST)
			contact = entity.getPosX()-getPos().getX() <= .125;
		else if(facing==Direction.EAST)
			contact = entity.getPosX()-getPos().getX() >= .875;

		if(this.diagonal&&entity.getPosY()-getPos().getY() <= .625)
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
				if(world.getBlockState(target).isSuffocating(world, target))
					return;
				if(!glitched)
				{
					Vector3d oldPos = entity.getPositionVec();
					double py = entity.getHeight() > 1?getPos().getY()+.125: entity.getPosY();
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
				world.playSound(null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), IESounds.chute, SoundCategory.BLOCKS, .6f+(.4f*world.rand.nextFloat()), .5f+(.5f*world.rand.nextFloat()));
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
				ItemEntityAccess access = (ItemEntityAccess)itemEntity;
				if(access.getAgeNonsided() > itemEntity.lifespan-60*20)
					access.setAge(itemEntity.lifespan-60*20);
			}
			else
			{
				TileEntity inventoryTile;
				BlockPos invPos = diagonal?getPos().offset(facing): getPos().down();
				inventoryTile = world.getTileEntity(invPos);
				if(!world.isRemote&&isValidTargetInventory(inventoryTile))
				{
					LazyOptional<IItemHandler> cap = CapabilityUtils.findItemHandlerAtPos(world, invPos, getFacing().getOpposite(), true);
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

	private boolean isValidTargetInventory(@Nullable TileEntity inventoryTile)
	{
		if(inventoryTile!=null)
		{
			if(!(inventoryTile instanceof IConveyorTile))
				return true;
			else
			{
				IConveyorTile conveyorTile = ((IConveyorTile)inventoryTile);
				if(conveyorTile.getConveyorSubtype() instanceof BasicConveyor) //TODO: change this to IConveyorTile.isCovered() and maybe inline this whole method
					return ((BasicConveyor)conveyorTile.getConveyorSubtype()).isCovered();
			}
		}
		return false;
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

	static final Map<Direction, AxisAlignedBB> AABB_SIDES = new EnumMap<>(Direction.class);

	static
	{
		AABB_SIDES.put(Direction.NORTH, new AxisAlignedBB(0, 0, 0, 1, 1, .0625));
		AABB_SIDES.put(Direction.SOUTH, new AxisAlignedBB(0, 0, .9375, 1, 1, 1));
		AABB_SIDES.put(Direction.WEST, new AxisAlignedBB(0, 0, 0, .0625, 1, 1));
		AABB_SIDES.put(Direction.EAST, new AxisAlignedBB(.9375, 0, 0, 1, 1, 1));
	}

	static final AxisAlignedBB AABB_DOWN = new AxisAlignedBB(0, 0, 0, 1, .125f, 1);
	private static final CachedVoxelShapes<BoundingBoxKey> SHAPES = new CachedVoxelShapes<>(BoundingBoxKey::getBoxes);

	@Override
	public VoxelShape getCollisionShape(ISelectionContext ctx)
	{
		return SHAPES.get(new BoundingBoxKey(this));
	}

	private static class BoundingBoxKey
	{
		private final boolean diagonal;
		private final Set<Direction> sidesToAdd;

		private BoundingBoxKey(ChuteTileEntity te)
		{
			this.diagonal = te.diagonal;
			this.sidesToAdd = EnumSet.noneOf(Direction.class);
			for(Direction dir : DirectionUtils.BY_HORIZONTAL_INDEX)
				if(!te.isInwardConveyor(dir)&&(!diagonal||dir!=te.getFacing()))
					this.sidesToAdd.add(dir);
		}

		private List<AxisAlignedBB> getBoxes()
		{
			ArrayList<AxisAlignedBB> list = new ArrayList<>();
			// SIDES
			for(Direction d : sidesToAdd)
				list.add(AABB_SIDES.get(d));
			//CORNERS
			for(Direction sideA : DirectionUtils.BY_HORIZONTAL_INDEX)
			{
				Direction sideB = sideA.rotateY();
				if(!sidesToAdd.contains(sideA)&&!sidesToAdd.contains(sideB))
				{
					AxisAlignedBB boxA = AABB_SIDES.get(sideA);
					AxisAlignedBB boxB = AABB_SIDES.get(sideB);
					list.add(boxA.intersect(boxB));
				}
			}
			if(diagonal)
				list.add(AABB_DOWN);
			return list;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			BoundingBoxKey that = (BoundingBoxKey)o;
			return diagonal==that.diagonal&&
					sidesToAdd.equals(that.sidesToAdd);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(diagonal, sidesToAdd);
		}
	}

	static final VoxelShape selectionShape = VoxelShapes.create(0, 0, 0, 1, 1, 1);

	@Override
	public VoxelShape getSelectionShape(@Nullable ISelectionContext ctx)
	{
		return selectionShape;
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
		for(Direction dir : DirectionUtils.BY_HORIZONTAL_INDEX)
			if(!isInwardConveyor(dir))
				s += ":"+dir.name().toLowerCase(Locale.US);
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
				cachedOBJStates.put(key, new IEObjState(VisibilityList.show("diagonal"), new TransformationMatrix(matrix)));
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
	public boolean hammerUseSide(Direction side, PlayerEntity player, Hand hand, Vector3d hitVec)
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

	private LazyOptional<IItemHandler> insertionCap = registerCap(() -> new ChuteTileEntity.ChuteInventoryHandler(this));

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if(cap==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY&&side==Direction.UP)
			return insertionCap.cast();
		return super.getCapability(cap, side);
	}

	public static class ChuteInventoryHandler implements IItemHandler
	{
		ChuteTileEntity chute;

		public ChuteInventoryHandler(ChuteTileEntity chute)
		{
			this.chute = chute;
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
				ItemEntity entity = new ItemEntity(chute.getWorldNonnull(), chute.getPos().getX()+.5, chute.getPos().getY()+.5, chute.getPos().getZ()+.5, stack.copy());
				entity.setMotion(Vector3d.ZERO);
				chute.getWorldNonnull().addEntity(entity);
				entity.setPickupDelay(10);
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
	}
}
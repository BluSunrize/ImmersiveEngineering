/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.IConveyorBlockEntity;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorBelt;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ICollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.mixin.accessors.ItemEntityAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ChuteBlockEntity extends IEBaseBlockEntity implements IStateBasedDirectional,
		IHammerInteraction, ISelectionBounds, ICollisionBounds
{
	private static final String NBT_POS = "immersiveengineering:chutePos";
	private static final String NBT_TIME = "immersiveengineering:chuteTime";
	private static final String NBT_GLITCH = "immersiveengineering:chuteGlitched";
	private static final String NBT_COUNT = "immersiveengineering:chuteCount";
	private boolean diagonal = false;

	public ChuteBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.CHUTE.get(), pos, state);
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return !entity.isShiftKeyDown();
	}

	@Override
	public void onEntityCollision(Level world, Entity entity)
	{
		boolean contact = false;
		Direction facing = getFacing();
		if(!diagonal)
			contact = entity.getY()-getBlockPos().getY() <= .125;
		else if(facing==Direction.NORTH)
			contact = entity.getZ()-getBlockPos().getZ() <= .125;
		else if(facing==Direction.SOUTH)
			contact = entity.getZ()-getBlockPos().getZ() >= .875;
		else if(facing==Direction.WEST)
			contact = entity.getX()-getBlockPos().getX() <= .125;
		else if(facing==Direction.EAST)
			contact = entity.getX()-getBlockPos().getX() >= .875;

		if(this.diagonal&&entity.getY()-getBlockPos().getY() <= .625)
		{
			BlockPos target = getBlockPos().relative(facing);
			BlockEntity targetTile = world.getBlockEntity(target);
			boolean opposedChute = targetTile instanceof ChuteBlockEntity&&((ChuteBlockEntity)targetTile).diagonal;
			// We're not going to redirect into another diagonal chute, to avoid infinite bouncing
			if(opposedChute)
				return;

			long time = world.getGameTime();
			long nbt_pos = getBlockPos().asLong();
			long timeSince = time-entity.getPersistentData().getLong(NBT_TIME);

			boolean prevent = entity.getPersistentData().contains(NBT_POS)
					&&nbt_pos==entity.getPersistentData().getLong(NBT_POS)
					&&timeSince < 20;
			// glitch timer resets after 60 seconds
			boolean glitched = entity.getPersistentData().contains(NBT_GLITCH)&&time-entity.getPersistentData().getLong(NBT_GLITCH) < 1200;

			if(entity.getBbWidth() > 0.75||entity.getBbHeight() > 0.75)
			{
				// We're not going to redirect into a colliding block
				if(world.getBlockState(target).isSuffocating(world, target))
					return;
				if(!glitched)
				{
					Vec3 oldPos = entity.position();
					double py = entity.getBbHeight() > 1?getBlockPos().getY()+.125: entity.getY();
					entity.setPos(target.getX()+.5, py, target.getZ()+.5);
					if(entity.isInWall())
					{
						entity.setPos(oldPos.x, oldPos.y, oldPos.z);
						entity.getPersistentData().putLong(NBT_GLITCH, time);
					}
				}
			}
			else
			{
				double mY = entity.getDeltaMovement().y;
				if(mY==0)
					mY = 0.015;
				entity.setDeltaMovement(facing.getStepX(), mY, facing.getStepZ());
			}

			if(!contact&&!prevent&&!glitched)
			{
				world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), IESounds.chute.value(), SoundSource.BLOCKS, .6f+(.4f*world.random.nextFloat()), .5f+(.5f*world.random.nextFloat()));
				CompoundTag entityData = entity.getPersistentData();
				entityData.putLong(NBT_POS, nbt_pos);
				entityData.putLong(NBT_TIME, time);
				if(entity instanceof Player player&&!world.isClientSide())
				{
					int bonkCount = entityData.getInt(NBT_COUNT)+1;
					if(timeSince > 200)
						bonkCount = 1;
					entity.getPersistentData().putInt(NBT_COUNT, bonkCount);
					if(bonkCount >= 6)
						Utils.unlockIEAdvancement(player, "main/chute_bonk");
				}
			}
		}

		if(entity instanceof ItemEntity itemEntity)
		{
			itemEntity.setPickUpDelay(10);
			if(!contact)
			{
				if(itemEntity.getAge() > itemEntity.lifespan-60*20)
					((ItemEntityAccess)itemEntity).setAge(itemEntity.lifespan-60*20);
			}
			else
			{
				BlockEntity inventoryTile;
				BlockPos invPos = diagonal?getBlockPos().relative(facing): getBlockPos().below();
				inventoryTile = world.getBlockEntity(invPos);
				Direction insertFrom = diagonal?facing.getOpposite(): Direction.UP;
				if(!world.isClientSide&&isValidTargetInventory(inventoryTile))
					ItemUtils.tryInsertEntity(world, invPos, insertFrom, itemEntity);
			}
		}
	}

	private boolean isValidTargetInventory(@Nullable BlockEntity inventoryTile)
	{
		if(inventoryTile!=null)
		{
			if(inventoryTile instanceof IConveyorBlockEntity<?> conveyorTile)
				return isCovered(conveyorTile.getConveyorInstance());
			else
				return true;
		}
		return false;
	}

	private static <T extends IConveyorBelt> boolean isCovered(IConveyorBelt belt)
	{
		return IConveyorBelt.isCovered(belt, Blocks.AIR);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		diagonal = nbt.getBoolean("diagonal");
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		nbt.putBoolean("diagonal", diagonal);
	}

	static final Map<Direction, AABB> AABB_SIDES = new EnumMap<>(Direction.class);

	static
	{
		AABB_SIDES.put(Direction.NORTH, new AABB(0, 0, 0, 1, 1, .0625));
		AABB_SIDES.put(Direction.SOUTH, new AABB(0, 0, .9375, 1, 1, 1));
		AABB_SIDES.put(Direction.WEST, new AABB(0, 0, 0, .0625, 1, 1));
		AABB_SIDES.put(Direction.EAST, new AABB(.9375, 0, 0, 1, 1, 1));
	}

	static final AABB AABB_DOWN = new AABB(0, 0, 0, 1, .125f, 1);
	private static final CachedVoxelShapes<BoundingBoxKey> SHAPES = new CachedVoxelShapes<>(BoundingBoxKey::getBoxes);

	@Override
	public VoxelShape getCollisionShape(CollisionContext ctx)
	{
		return SHAPES.get(new BoundingBoxKey(this));
	}

	private static class BoundingBoxKey
	{
		private final boolean diagonal;
		private final Set<Direction> sidesToAdd;

		private BoundingBoxKey(ChuteBlockEntity te)
		{
			this.diagonal = te.diagonal;
			this.sidesToAdd = EnumSet.noneOf(Direction.class);
			for(Direction dir : DirectionUtils.BY_HORIZONTAL_INDEX)
				if(diagonal && dir!=te.getFacing())
					this.sidesToAdd.add(dir);
				else if(!diagonal && !te.isInwardConveyor(dir))
					this.sidesToAdd.add(dir);
		}

		private List<AABB> getBoxes()
		{
			ArrayList<AABB> list = new ArrayList<>();
			// SIDES
			for(Direction d : sidesToAdd)
				list.add(AABB_SIDES.get(d));
			//CORNERS
			for(Direction sideA : DirectionUtils.BY_HORIZONTAL_INDEX)
			{
				Direction sideB = sideA.getClockWise();
				if(!sidesToAdd.contains(sideA)&&!sidesToAdd.contains(sideB))
				{
					AABB boxA = AABB_SIDES.get(sideA);
					AABB boxB = AABB_SIDES.get(sideB);
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

	static final VoxelShape selectionShape = Shapes.block();

	@Override
	public VoxelShape getSelectionShape(@Nullable CollisionContext ctx)
	{
		return selectionShape;
	}

	public boolean isInwardConveyor(Direction f)
	{
		BlockEntity te = level.getBlockEntity(getBlockPos().relative(f));
		if(te instanceof IConveyorBlockEntity<?> conveyorBE)
		{
			IConveyorBelt sub = conveyorBE.getConveyorInstance();
			if(sub!=null)
			{
				boolean isInputting = false;
				for(Direction f2 : sub.sigTransportDirections())
					if(f==f2.getOpposite())
						isInputting = true;
					else if(f2==Direction.UP)
					{
						isInputting = false;
						break;
					}
				if(isInputting)
					return true;
			}
		}
		else if(te instanceof ChuteBlockEntity chute)
			return chute.diagonal&&chute.getFacing()==f.getOpposite();

		te = level.getBlockEntity(getBlockPos().offset(0, -1, 0).relative(f));
		if(te instanceof IConveyorBlockEntity<?> conveyorBE)
		{
			IConveyorBelt sub = conveyorBE.getConveyorInstance();
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
	public boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(player.isShiftKeyDown())
		{
			if(!level.isClientSide)
			{
				this.diagonal = !this.diagonal;
				this.setChanged();
				this.markContainingBlockForUpdate(null);
				level.blockEvent(getBlockPos(), getBlockState().getBlock(), 0, 0);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	public boolean isDiagonal()
	{
		return diagonal;
	}

	private final IItemHandler insertionCap = new ChuteBlockEntity.ChuteInventoryHandler(this);

	public static void registerCapabilities(BECapabilityRegistrar<ChuteBlockEntity> registrar)
	{
		registrar.registerOnContext(ItemHandler.BLOCK, be -> be.insertionCap, Direction.UP);
	}

	public static class ChuteInventoryHandler implements IItemHandler
	{
		ChuteBlockEntity chute;

		public ChuteInventoryHandler(ChuteBlockEntity chute)
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
				ItemEntity entity = new ItemEntity(chute.getLevelNonnull(), chute.getBlockPos().getX()+.5, chute.getBlockPos().getY()+.5, chute.getBlockPos().getZ()+.5, stack.copy());
				entity.setDeltaMovement(Vec3.ZERO);
				chute.getLevelNonnull().addFreshEntity(entity);
				entity.setPickUpDelay(10);
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
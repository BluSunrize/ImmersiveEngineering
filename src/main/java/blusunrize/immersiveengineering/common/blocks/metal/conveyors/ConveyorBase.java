/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorBelt;
import blusunrize.immersiveengineering.api.utils.EntityCollisionTracker;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalBE;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBeltBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDecoration;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author BluSunrize - 20.08.2016
 */
public abstract class ConveyorBase implements IConveyorBelt
{
	public static ResourceLocation texture_on = IEApi.ieLoc("block/conveyor/conveyor");
	public static ResourceLocation texture_off = IEApi.ieLoc("block/conveyor/off");

	private Block cover = Blocks.AIR;

	ConveyorDirection direction = ConveyorDirection.HORIZONTAL;
	@Nullable
	DyeColor dyeColour = null;
	private final BlockEntity tile;
	protected final EntityCollisionTracker collisionTracker = new EntityCollisionTracker(10);

	public ConveyorBase(BlockEntity tile)
	{
		this.tile = tile;
	}

	@Override
	public BlockEntity getBlockEntity()
	{
		return tile;
	}

	@Override
	public ConveyorDirection getConveyorDirection()
	{
		return direction;
	}

	@Override
	public boolean changeConveyorDirection()
	{
		if(!tile.getLevel().isClientSide)
			direction = direction==ConveyorDirection.HORIZONTAL?ConveyorDirection.UP: direction==ConveyorDirection.UP?ConveyorDirection.DOWN: ConveyorDirection.HORIZONTAL;
		return true;
	}

	@Override
	public boolean setConveyorDirection(ConveyorDirection dir)
	{
		direction = dir;
		return true;
	}

	@Override
	public boolean isActive()
	{
		return true;
	}

	@Override
	public void onEntityCollision(@Nonnull Entity entity)
	{
		collisionTracker.onEntityCollided(entity);
		IConveyorBelt.super.onEntityCollision(entity);
		if(isCovered()&&entity instanceof ItemEntity)
			((ItemEntity)entity).setPickUpDelay(10);
	}

	@Override
	public boolean isBlocked()
	{
		return collisionTracker.getCollidedInRange(getBlockEntity().getLevel().getGameTime()) > 2;
	}

	@Override
	public void onItemDeployed(ItemEntity entity)
	{
		IConveyorBelt.super.onItemDeployed(entity);
		if(isCovered())
			entity.setPickUpDelay(10);
	}

	@Override
	public boolean playerInteraction(Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, Direction side)
	{
		return handleCoverInteraction(player, hand, heldItem);
	}

	/* ============ NBT ============ */

	@Override
	public CompoundTag writeConveyorNBT()
	{
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("direction", direction.ordinal());
		if(dyeColour!=null)
			nbt.putInt("dyeColour", dyeColour.getId());
		if(cover!=Blocks.AIR)
			nbt.putString("cover", BuiltInRegistries.BLOCK.getKey(cover).toString());
		return nbt;
	}

	@Override
	public void readConveyorNBT(CompoundTag nbt)
	{
		direction = ConveyorDirection.values()[nbt.getInt("direction")];
		if(nbt.contains("dyeColour", Tag.TAG_INT))
			dyeColour = DyeColor.byId(nbt.getInt("dyeColour"));
		else
			dyeColour = null;
		if(nbt.contains("cover", Tag.TAG_STRING))
			cover = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(nbt.getString("cover")));
	}

	/* ============ RENDERING ============ */

	@Override
	public boolean setDyeColour(DyeColor colour)
	{
		if(colour==this.dyeColour)
			return false;
		this.dyeColour = colour;
		return true;
	}

	@Override
	public DyeColor getDyeColour()
	{
		return this.dyeColour;
	}

	/* ============ AABB ============ */

	private static final AABB topBox = new AABB(0, .75, 0, 1, 1, 1);

	private static final CachedVoxelShapes<ShapeKey> SHAPES = new CachedVoxelShapes<>(ConveyorBase::getBoxes);

	@Override
	public VoxelShape getCollisionShape()
	{
		VoxelShape baseShape = IConveyorBelt.super.getCollisionShape();
		if(isCovered())
			return SHAPES.get(new ShapeKey(this, true, baseShape));
		return baseShape;
	}

	@Override
	public VoxelShape getSelectionShape()
	{
		if(isCovered())
			return SHAPES.get(new ShapeKey(this, false, null));
		return IConveyorBelt.super.getSelectionShape();
	}

	protected final boolean isCovered()
	{
		return IConveyorBelt.isCovered(this, Blocks.AIR);
	}

	private static List<AABB> getBoxes(ShapeKey key)
	{
		List<AABB> ret = new ArrayList<>();
		if(key.superShape!=null)
			ret.addAll(key.superShape.toAabbs());
		if(key.direction==ConveyorDirection.HORIZONTAL)
		{
			if(!key.collision)
				return ImmutableList.of(FULL_BLOCK.bounds());
			else
				ret.add(topBox);
		}
		else
		{
			boolean up = key.direction==ConveyorDirection.UP;
			boolean collision = key.collision;
			Direction facing = key.facing;
			ret.add(new AABB(
					(facing==Direction.WEST&&!up)||(facing==Direction.EAST&&up)?.5: 0,
					collision?1.75: .5,
					(facing==Direction.NORTH&&!up)||(facing==Direction.SOUTH&&up)?.5: 0,
					(facing==Direction.WEST&&up)||(facing==Direction.EAST&&!up)?.5: 1,
					2,
					(facing==Direction.NORTH&&up)||(facing==Direction.SOUTH&&!up)?.5: 1
			));
			ret.add(new AABB(
					(facing==Direction.WEST&&up)||(facing==Direction.EAST&&!up)?.5: 0,
					collision?1.25: 0,
					(facing==Direction.NORTH&&up)||(facing==Direction.SOUTH&&!up)?.5: 0,
					(facing==Direction.WEST&&!up)||(facing==Direction.EAST&&up)?.5: 1,
					1.5,
					(facing==Direction.NORTH&&!up)||(facing==Direction.SOUTH&&up)?.5: 1
			));
		}
		return ret;
	}


	/* ============ COVER UTILITY METHODS ============ */

	public static ArrayList<Predicate<Block>> validCoveyorCovers = new ArrayList<>();

	static
	{
		validCoveyorCovers.add(b -> b.defaultBlockState().is(IETags.scaffoldingAlu));
		validCoveyorCovers.add(b -> b.defaultBlockState().is(IETags.scaffoldingSteel));
		validCoveyorCovers.add(input -> input==WoodenDecoration.TREATED_SCAFFOLDING.get());
		validCoveyorCovers.add(b -> b.defaultBlockState().is(Tags.Blocks.GLASS_BLOCKS));
	}

	public void dropCover(Player player)
	{
		if(tile!=null&&!tile.getLevel().isClientSide&&cover!=Blocks.AIR&&tile.getLevel().getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS))
		{
			ItemEntity entityitem = player.drop(new ItemStack(cover), false);
			if(entityitem!=null)
				entityitem.setNoPickUpDelay();
		}
	}

	protected boolean handleCoverInteraction(Player player, InteractionHand hand, ItemStack heldItem)
	{
		if(heldItem.isEmpty()&&player.isShiftKeyDown()&&cover!=Blocks.AIR)
		{
			dropCover(player);
			this.cover = Blocks.AIR;
			return true;
		}
		else if(!heldItem.isEmpty()&&!player.isShiftKeyDown())
		{
			Block heldBlock = Block.byItem(heldItem.getItem());
			if(heldBlock!=Blocks.AIR)
				for(Predicate<Block> func : validCoveyorCovers)
					if(func.test(heldBlock))
					{
						if(heldBlock!=cover)
						{
							dropCover(player);
							this.cover = heldBlock;
							if(!player.hasInfiniteMaterials())
								heldItem.shrink(1);
							if(heldItem.getCount() <= 0)
								player.setItemInHand(hand, heldItem);
							return true;
						}
					}
		}
		return false;
	}

	protected final boolean isPowered()
	{
		BlockEntity te = getBlockEntity();
		if(te instanceof ConveyorBeltBlockEntity<?> conveBE)
			return conveBE.isRSPowered();
		else
			return te.getLevel().getBestNeighborSignal(te.getBlockPos()) > 0;
	}

	@Override
	public Direction getFacing()
	{
		BlockEntity te = getBlockEntity();
		if(te instanceof IDirectionalBE)
			return ((IDirectionalBE)te).getFacing();
		return Direction.NORTH;
	}

	@Override
	public Block getCover()
	{
		return cover;
	}

	@Override
	public void setCover(Block cover)
	{
		this.cover = cover;
	}

	private static class ShapeKey
	{
		private final ConveyorDirection direction;
		private final boolean collision;
		private final Direction facing;
		@Nullable
		private final VoxelShape superShape;

		public ShapeKey(ConveyorBase conveyor, boolean collision, @Nullable VoxelShape superShape)
		{
			this.direction = conveyor.getConveyorDirection();
			this.collision = collision;
			this.facing = conveyor.getFacing();
			this.superShape = superShape;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			ShapeKey shapeKey = (ShapeKey)o;
			return collision==shapeKey.collision&&
					direction==shapeKey.direction&&
					facing==shapeKey.facing&&
					Objects.equals(superShape, shapeKey.superShape);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(direction, collision, facing, superShape);
		}
	}

}
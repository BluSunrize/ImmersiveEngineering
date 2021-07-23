/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.IPostBlock;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IModelDataBlock;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PostBlock extends IEBaseBlock implements IModelDataBlock, IPostBlock, IModelOffsetProvider
{
	public static final IntegerProperty POST_SLAVE = IntegerProperty.create("post_slave", 0, 3);
	public static final EnumProperty<HorizontalOffset> HORIZONTAL_OFFSET = EnumProperty.create(
			"horizontal_offset", HorizontalOffset.class
	);

	public PostBlock(Properties blockProps)
	{
		super(blockProps);
		setMobility(PushReaction.BLOCK);
		lightOpacity = 0;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(POST_SLAVE, HORIZONTAL_OFFSET, BlockStateProperties.WATERLOGGED);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder)
	{
		return ImmutableList.of();
	}

	@Override
	public void onRemove(@Nonnull BlockState state, Level world, @Nonnull BlockPos pos, BlockState newState, boolean moving)
	{
		if(state.getBlock()!=newState.getBlock())
		{
			int dummyState = state.getValue(POST_SLAVE);
			HorizontalOffset offset = state.getValue(HORIZONTAL_OFFSET);
			if(dummyState > 0&&offset==HorizontalOffset.NONE)
				world.setBlockAndUpdate(pos.below(dummyState), Blocks.AIR.defaultBlockState());
			else if(dummyState==0)
			{
				popResource(world, pos, new ItemStack(this));
				final int highestBlock = 3;
				BlockPos armStart = pos.above(highestBlock);
				for(Direction d : DirectionUtils.BY_HORIZONTAL_INDEX)
				{
					BlockPos armPos = armStart.relative(d);
					BlockState armState = world.getBlockState(armPos);
					if(armState.getBlock()==this&&armState.getValue(HORIZONTAL_OFFSET).getOffset().equals(d.getNormal()))
						world.setBlockAndUpdate(armPos, Blocks.AIR.defaultBlockState());
				}
				for(int i = 0; i <= highestBlock; ++i)
					world.setBlockAndUpdate(pos.above(i), Blocks.AIR.defaultBlockState());
			}
		}
		super.onRemove(state, world, pos, newState, moving);
	}

	@Override
	public boolean canIEBlockBePlaced(@Nonnull BlockState newState, BlockPlaceContext context)
	{
		BlockPos startingPos = context.getClickedPos();
		Level world = context.getLevel();
		for(int hh = 1; hh <= 3; hh++)
		{
			BlockPos pos = startingPos.above(hh);
			BlockPlaceContext dummyContext = BlockPlaceContext.at(context, pos, context.getClickedFace());
			BlockState oldState = world.getBlockState(pos);
			if(Level.isOutsideBuildHeight(pos)||!oldState.getBlock().canBeReplaced(oldState, dummyContext))
				return false;
		}
		return true;
	}

	@Override
	public void onIEBlockPlacedBy(BlockPlaceContext context, BlockState state)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		for(int i = 1; i <= 3; i++)
		{
			BlockPos dummyPos = pos.above(i);
			world.setBlockAndUpdate(dummyPos, IEBaseBlock.applyLocationalWaterlogging(
					state.setValue(POST_SLAVE, i), world, dummyPos
			));
			world.blockEvent(pos.offset(0, i, 0), this, 255, 0);
		}
	}

	@Override
	public boolean isLadder(BlockState state, LevelReader world, BlockPos pos, LivingEntity entity)
	{
		return true;
	}

	private boolean hasArmFor(BlockPos center, Direction side, BlockGetter world)
	{
		final int highest = 3;
		BlockState centerState = world.getBlockState(center);
		if(centerState.getBlock()!=this||centerState.getValue(POST_SLAVE)!=highest||centerState.getValue(HORIZONTAL_OFFSET)!=HorizontalOffset.NONE)
			return false;
		BlockState armState = world.getBlockState(center.relative(side));
		return armState.getBlock()==this&&armState.getValue(POST_SLAVE)==highest
				&&armState.getValue(HORIZONTAL_OFFSET).getOffset().equals(side.getNormal());
	}

	@Override
	public InteractionResult hammerUseSide(Direction side, Player player, InteractionHand hand, Level world, BlockPos pos, BlockHitResult hit)
	{
		BlockState state = world.getBlockState(pos);
		int dummy = state.getValue(POST_SLAVE);
		HorizontalOffset offset = state.getValue(HORIZONTAL_OFFSET);
		boolean changed = false;
		if(dummy==3&&offset==HorizontalOffset.NONE&&side.getAxis()!=Axis.Y)
		{
			BlockPos offsetPos = pos.relative(side);
			BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(player, hand, hit));
			//No Arms if space is blocked
			if(!world.getBlockState(offsetPos).canBeReplaced(context))
				return InteractionResult.FAIL;
			//No Arms if perpendicular arms exist
			for(Direction forbidden : ImmutableList.of(side.getClockWise(), side.getCounterClockWise()))
				if(hasArmFor(pos, forbidden, world))
					return InteractionResult.FAIL;

			BlockState arm_state = this.getStateForPlacement(context).setValue(POST_SLAVE, 3)
					.setValue(HORIZONTAL_OFFSET, HorizontalOffset.get(side));
			world.setBlockAndUpdate(offsetPos, arm_state);
			changed = true;
		}
		else if(dummy==3&&offset!=HorizontalOffset.NONE)
		{
			world.removeBlock(pos, false);
			changed = true;
		}
		if(changed)
		{
			BlockPos masterPos = pos.below(dummy).subtract(offset.getOffset());
			BlockState masterState = world.getBlockState(masterPos);
			world.sendBlockUpdated(masterPos, masterState, masterState, 3);
			return InteractionResult.SUCCESS;
		}
		return super.hammerUseSide(side, player, hand, world, pos, hit);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return Shapes.joinUnoptimized(getMainShape(state, worldIn, pos),
				getConnectionShapes(state, worldIn, pos), BooleanOp.OR);
	}

	private VoxelShape getMainShape(BlockState state, BlockGetter world, BlockPos pos)
	{
		int dummy = state.getValue(POST_SLAVE);
		if(dummy==0)
			return Shapes.box(.25f, 0, .25f, .75f, 1, .75f);
		if(dummy <= 2)
			return Shapes.box(.3125f, .0f, .3125f, .6875f, 1.0f, .6875f);
		if(dummy==3)
		{
			float down = hasConnection(state, Direction.DOWN, world, pos)?0: .4375f;
			float up = down > 0?1: .5625f;
			switch(state.getValue(HORIZONTAL_OFFSET))
			{
				case NONE:
					return Shapes.box(.3125f, 0, .3125f, .6875f, 1, .6875f);
				case NORTH:
					return Shapes.box(.3125f, down, .3125f, .6875f, up, 1);
				case SOUTH:
					return Shapes.box(.3125f, down, 0, .6875f, up, .6875f);
				case EAST:
					return Shapes.box(0, down, .3125f, .6875f, up, .6875f);
				case WEST:
					return Shapes.box(.3125f, down, .3125f, 1, up, .6875f);
			}
		}
		return Shapes.block();
	}

	private VoxelShape getConnectionShapes(BlockState state, BlockGetter world, BlockPos pos)
	{
		HorizontalOffset offset = state.getValue(HORIZONTAL_OFFSET);
		if(offset==HorizontalOffset.NONE)
		{
			int dummy = state.getValue(POST_SLAVE);
			if(dummy==0)
				return Shapes.empty();
			final double baseWidth = dummy==3?6./16: 4./16;
			VoxelShape ret = Shapes.empty();
			for(Direction neighbor : DirectionUtils.BY_HORIZONTAL_INDEX)
			{
				if(hasConnection(state, neighbor, world, pos))
				{
					final double minY;
					final double maxY;
					if(dummy < 3)
					{
						minY = 4D/16;
						maxY = 13D/16;
					}
					else
					{
						BlockPos neighborPos = pos.relative(neighbor);
						BlockState neighborState = world.getBlockState(neighborPos);
						if(hasConnection(neighborState, Direction.DOWN, world, neighborPos))
						{
							minY = 0;
							maxY = 9D/16;
						}
						else
						{
							minY = 7D/16;
							maxY = 1;
						}
					}
					double[] horizontalBounds = {(1-baseWidth)/2, (1+baseWidth)/2, (1-baseWidth)/2, (1+baseWidth)/2};
					int minIndex;
					if(neighbor.getAxis()==Axis.X)
					{
						minIndex = 0;
					}
					else
					{
						minIndex = 2;
					}
					int maxIndex = minIndex+1;
					if(neighbor.getAxisDirection()==AxisDirection.POSITIVE)
					{
						horizontalBounds[minIndex] = horizontalBounds[maxIndex];
						horizontalBounds[maxIndex] = 1;
					}
					else
					{
						horizontalBounds[maxIndex] = horizontalBounds[minIndex];
						horizontalBounds[minIndex] = 0;
					}
					VoxelShape sideShape = Shapes.box(horizontalBounds[0], minY, horizontalBounds[2],
							horizontalBounds[1], maxY, horizontalBounds[3]);
					ret = Shapes.joinUnoptimized(ret, sideShape, BooleanOp.OR);
				}
			}
			return ret;
		}
		else
		{
			//TODO
			return Shapes.empty();
		}
	}

	ThreadLocal<Boolean> recursionLock = new ThreadLocal<>();

	public boolean hasConnection(BlockState stateHere, Direction dir, BlockGetter world, BlockPos pos)
	{
		if(recursionLock.get()!=null&&recursionLock.get())
			return true;
		recursionLock.set(true);
		BlockPos neighborPos = pos.relative(dir);
		int dummy = stateHere.getValue(POST_SLAVE);
		boolean ret = false;
		if(dummy > 0&&dummy < 3)
		{
			BlockState neighborState = world.getBlockState(neighborPos);
			VoxelShape shape = neighborState.getShape(world, neighborPos);
			if(!shape.isEmpty())
			{
				AABB aabb = shape.bounds();
				final double distance = dir==Direction.NORTH?aabb.maxZ-1: dir==Direction.SOUTH?aabb.minZ:
						dir==Direction.WEST?aabb.maxX-1: aabb.minX;
				final double eps = 1e-7;
				boolean connect = distance < eps&&distance > -eps;
				if(connect)
				{
					if(dir.getAxis()==Axis.Z)
						ret = aabb.minX > eps&&aabb.maxX < 1-eps;
					else
						ret = aabb.minZ > eps&&aabb.maxZ < 1-eps;
				}
			}
		}
		else if(dummy==3)
		{
			HorizontalOffset offset = stateHere.getValue(HORIZONTAL_OFFSET);
			if(offset==HorizontalOffset.NONE)
				ret = hasArmFor(pos, dir, world);
			else
			{
				if(world.getBlockState(neighborPos).isAir(world, neighborPos)||dir.getAxis()!=Axis.Y)
					ret = false;
				else
				{
					BlockState neighborState = world.getBlockState(neighborPos);
					if(neighborState.getMaterial().isReplaceable())
						ret = false;
					else
					{
						VoxelShape shape = neighborState.getShape(world, neighborPos);
						ret = shapeReachesBlockFace(shape, dir.getOpposite());
					}
				}
			}
		}
		recursionLock.set(false);
		return ret;
	}

	private boolean shapeReachesBlockFace(VoxelShape shape, Direction face)
	{
		//TODO is 1 and 0 correct? Or is that 1 pixel/0 pixels?
		if(face.getAxisDirection()==AxisDirection.POSITIVE)
			return shape.max(face.getAxis())==1;
		else
			return shape.min(face.getAxis())==0;
	}

	@Nonnull
	@Override
	public IModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData)
	{
		ArrayList<String> visible = new ArrayList<>();
		visible.add("base");
		final int height = state.getValue(POST_SLAVE);
		BlockPos centerPos = pos.subtract(state.getValue(HORIZONTAL_OFFSET).getOffset());
		BlockState centerState = world.getBlockState(centerPos);
		if(centerState.getBlock()==this)
		{
			// With model splitting it's enough to check the model state for the current layer, since none of the arm
			// models extend into other layers
			for(Direction f : DirectionUtils.BY_HORIZONTAL_INDEX)
				if(hasConnection(centerState, f, world, centerPos))
				{
					String name = f.getOpposite().getSerializedName();
					if(height==3)//Arms
					{
						BlockPos armPos = centerPos.relative(f);
						boolean down = hasConnection(world.getBlockState(armPos), Direction.DOWN, world, armPos);
						if(down)
							visible.add("arm_"+name+"_down");
						else
							visible.add("arm_"+name+"_up");
					}
					else//Simple Connectors
						visible.add("con_"+(height-1)+"_"+name);
				}
		}
		IEObjState modelState = new IEObjState(VisibilityList.show(visible));
		return new SinglePropertyModelData<>(modelState, Model.IE_OBJ_STATE);
	}

	@Override
	public boolean canConnectTransformer(BlockGetter world, BlockPos pos)
	{
		int offset = world.getBlockState(pos).getValue(POST_SLAVE);
		return offset > 0;
	}

	@Nonnull
	@Override
	public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size)
	{
		HorizontalOffset d = state.getValue(HORIZONTAL_OFFSET);
		return new BlockPos(0, state.getValue(POST_SLAVE), 0).offset(d.getOffset());
	}

	@Override
	public int getLightBlock(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos)
	{
		return 0;
	}

	enum HorizontalOffset implements StringRepresentable
	{
		NONE,
		NORTH,
		SOUTH,
		EAST,
		WEST;

		public static HorizontalOffset get(Direction side)
		{
			switch(side)
			{
				case NORTH:
					return NORTH;
				case SOUTH:
					return SOUTH;
				case WEST:
					return WEST;
				case EAST:
					return EAST;
				default:
					throw new IllegalArgumentException("No horizontal offset for "+side.name());
			}
		}

		Vec3i getOffset()
		{
			switch(this)
			{
				case NORTH:
					return Direction.NORTH.getNormal();
				case SOUTH:
					return Direction.SOUTH.getNormal();
				case EAST:
					return Direction.EAST.getNormal();
				case WEST:
					return Direction.WEST.getNormal();
				case NONE:
					return BlockPos.ZERO;
				default:
					throw new IllegalStateException();
			}
		}

		@Override
		public String getSerializedName()
		{
			return name().toLowerCase(Locale.ENGLISH);
		}
	}
}

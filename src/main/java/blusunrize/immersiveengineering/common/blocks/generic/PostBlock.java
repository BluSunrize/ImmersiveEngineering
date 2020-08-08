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
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IModelDataBlock;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PostBlock extends IEBaseBlock implements IModelDataBlock, IPostBlock
{
	public static final IntegerProperty POST_SLAVE = IntegerProperty.create("post_slave", 0, 3);
	public static final EnumProperty<HorizontalOffset> HORIZONTAL_OFFSET = EnumProperty.create("horizontal_offset",
			HorizontalOffset.class);

	public PostBlock(String name, Properties blockProps)
	{
		super(name, blockProps, BlockItemIE::new, POST_SLAVE, HORIZONTAL_OFFSET, BlockStateProperties.WATERLOGGED);
		setNotNormalBlock();
		setMobility(PushReaction.BLOCK);
		lightOpacity = 0;
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder)
	{
		return ImmutableList.of();
	}

	@Override
	public void onReplaced(@Nonnull BlockState state, World world, @Nonnull BlockPos pos, BlockState newState, boolean moving)
	{
		if(state.getBlock()!=newState.getBlock())
		{
			int dummyState = state.get(POST_SLAVE);
			HorizontalOffset offset = state.get(HORIZONTAL_OFFSET);
			if(dummyState > 0&&offset==HorizontalOffset.NONE)
				world.setBlockState(pos.down(dummyState), Blocks.AIR.getDefaultState());
			else if(dummyState==0)
			{
				spawnAsEntity(world, pos, new ItemStack(this));
				final int highestBlock = 3;
				BlockPos armStart = pos.up(highestBlock);
				for(Direction d : Direction.BY_HORIZONTAL_INDEX)
				{
					BlockPos armPos = armStart.offset(d);
					BlockState armState = world.getBlockState(armPos);
					if(armState.getBlock()==this&&armState.get(HORIZONTAL_OFFSET).getOffset().equals(d.getDirectionVec()))
						world.setBlockState(armPos, Blocks.AIR.getDefaultState());
				}
				for(int i = 0; i <= highestBlock; ++i)
					world.setBlockState(pos.up(i), Blocks.AIR.getDefaultState());
			}
			super.onReplaced(state, world, pos, newState, moving);
		}
	}

	@Override
	public boolean canIEBlockBePlaced(@Nonnull BlockState newState, BlockItemUseContext context)
	{
		BlockPos startingPos = context.getPos();
		World world = context.getWorld();
		for(int hh = 1; hh <= 3; hh++)
		{
			BlockPos pos = startingPos.up(hh);
			BlockItemUseContext dummyContext = BlockItemUseContext.func_221536_a(context, pos, context.getFace());
			BlockState oldState = world.getBlockState(pos);
			if(World.isOutsideBuildHeight(pos)||!oldState.getBlock().isReplaceable(oldState, dummyContext))
				return false;
		}
		return true;
	}

	@Override
	public void onIEBlockPlacedBy(BlockItemUseContext context, BlockState state)
	{
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		for(int i = 1; i <= 3; i++)
		{
			world.setBlockState(pos.add(0, i, 0), state.with(POST_SLAVE, i));
			world.addBlockEvent(pos.add(0, i, 0), this,
					255, 0);
		}
	}

	@Override
	public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity)
	{
		return true;
	}

	private boolean hasArmFor(BlockPos center, Direction side, IBlockReader world)
	{
		final int highest = 3;
		BlockState centerState = world.getBlockState(center);
		if(centerState.getBlock()!=this||centerState.get(POST_SLAVE)!=highest||centerState.get(HORIZONTAL_OFFSET)!=HorizontalOffset.NONE)
			return false;
		BlockState armState = world.getBlockState(center.offset(side));
		return armState.getBlock()==this&&armState.get(POST_SLAVE)==highest
				&&armState.get(HORIZONTAL_OFFSET).getOffset().equals(side.getDirectionVec());
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, Hand hand, World world, BlockPos pos, BlockRayTraceResult hit)
	{
		BlockState state = world.getBlockState(pos);
		int dummy = state.get(POST_SLAVE);
		HorizontalOffset offset = state.get(HORIZONTAL_OFFSET);
		boolean changed = false;
		if(dummy==3&&offset==HorizontalOffset.NONE&&side.getAxis()!=Axis.Y)
		{
			BlockPos offsetPos = pos.offset(side);
			BlockItemUseContext context = new BlockItemUseContext(new ItemUseContext(player, hand, hit));
			//No Arms if space is blocked
			if(!world.getBlockState(offsetPos).isReplaceable(context))
				return false;
			//No Arms if perpendicular arms exist
			for(Direction forbidden : ImmutableList.of(side.rotateY(), side.rotateYCCW()))
				if(hasArmFor(pos, forbidden, world))
					return false;

			BlockState arm_state = this.getStateForPlacement(context).with(POST_SLAVE, 3)
					.with(HORIZONTAL_OFFSET, HorizontalOffset.get(side));
			world.setBlockState(offsetPos, arm_state);
			changed = true;
		}
		else if(dummy==3&&offset!=HorizontalOffset.NONE)
		{
			world.removeBlock(pos, false);
			changed = true;
		}
		if(changed)
		{
			BlockPos masterPos = pos.down(dummy).subtract(offset.getOffset());
			BlockState masterState = world.getBlockState(masterPos);
			world.notifyBlockUpdate(masterPos, masterState, masterState, 3);
		}
		return super.hammerUseSide(side, player, hand, world, pos, hit);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return VoxelShapes.combine(getMainShape(state, worldIn, pos),
				getConnectionShapes(state, worldIn, pos), IBooleanFunction.OR);
	}

	private VoxelShape getMainShape(BlockState state, IBlockReader world, BlockPos pos)
	{
		int dummy = state.get(POST_SLAVE);
		if(dummy==0)
			return VoxelShapes.create(.25f, 0, .25f, .75f, 1, .75f);
		if(dummy <= 2)
			return VoxelShapes.create(.375f, 0, .375f, .625f, 1, .625f);
		if(dummy==3)
		{
			float down = hasConnection(state, Direction.DOWN, world, pos)?0: .4375f;
			float up = down > 0?1: .5625f;
			switch(state.get(HORIZONTAL_OFFSET))
			{
				case NONE:
					return VoxelShapes.create(.3125f, 0, .3125f, .6875f, 1, .6875f);
				case NORTH:
					return VoxelShapes.create(.3125f, down, .3125f, .6875f, up, 1);
				case SOUTH:
					return VoxelShapes.create(.3125f, down, 0, .6875f, up, .6875f);
				case EAST:
					return VoxelShapes.create(0, down, .3125f, .6875f, up, .6875f);
				case WEST:
					return VoxelShapes.create(.3125f, down, .3125f, 1, up, .6875f);
			}
		}
		return VoxelShapes.fullCube();
	}

	private VoxelShape getConnectionShapes(BlockState state, IBlockReader world, BlockPos pos)
	{
		HorizontalOffset offset = state.get(HORIZONTAL_OFFSET);
		if(offset==HorizontalOffset.NONE)
		{
			int dummy = state.get(POST_SLAVE);
			if(dummy==0)
				return VoxelShapes.empty();
			final double baseWidth = dummy==3?6./16: 4./16;
			VoxelShape ret = VoxelShapes.empty();
			for(Direction neighbor : Direction.BY_HORIZONTAL_INDEX)
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
						BlockPos neighborPos = pos.offset(neighbor);
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
					VoxelShape sideShape = VoxelShapes.create(horizontalBounds[0], minY, horizontalBounds[2],
							horizontalBounds[1], maxY, horizontalBounds[3]);
					ret = VoxelShapes.combine(ret, sideShape, IBooleanFunction.OR);
				}
			}
			return ret;
		}
		else
		{
			//TODO
			return VoxelShapes.empty();
		}
	}

	ThreadLocal<Boolean> recursionLock = new ThreadLocal<>();

	public boolean hasConnection(BlockState stateHere, Direction dir, IBlockReader world, BlockPos pos)
	{
		if(recursionLock.get()!=null&&recursionLock.get())
			return true;
		recursionLock.set(true);
		BlockPos neighborPos = pos.offset(dir);
		int dummy = stateHere.get(POST_SLAVE);
		boolean ret = false;
		if(dummy > 0&&dummy < 3)
		{
			BlockState neighborState = world.getBlockState(neighborPos);
			VoxelShape shape = neighborState.getShape(world, neighborPos);
			if(!shape.isEmpty())
			{
				AxisAlignedBB aabb = shape.getBoundingBox();
				boolean connect = dir==Direction.NORTH?aabb.maxZ==1: dir==Direction.SOUTH?aabb.minZ==0: dir==Direction.WEST?aabb.maxX==1: aabb.minX==0;
				ret = connect&&((dir.getAxis()==Axis.Z&&aabb.minX > 0&&aabb.maxX < 1)||(dir.getAxis()==Axis.X&&aabb.minZ > 0&&aabb.maxZ < 1));
			}
		}
		else if(dummy==3)
		{
			HorizontalOffset offset = stateHere.get(HORIZONTAL_OFFSET);
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
			return shape.getEnd(face.getAxis())==1;
		else
			return shape.getStart(face.getAxis())==0;
	}

	@Nonnull
	@Override
	public IModelData getModelData(@Nonnull IEnviromentBlockReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData)
	{
		ArrayList<String> visible = new ArrayList<>();
		visible.add("base");
		for(int i = 0; i <= 2; i++)
		{
			BlockPos upperPos = pos.up(1+i);
			BlockState upperState = world.getBlockState(upperPos);
			if(upperState.getBlock()==this)
			{
				for(Direction f : Direction.BY_HORIZONTAL_INDEX)
					if(hasConnection(upperState, f, world, upperPos))
					{
						String name = f.getOpposite().getName();
						if(i==2)//Arms
						{
							BlockPos armPos = upperPos.offset(f);
							boolean down = hasConnection(world.getBlockState(armPos), Direction.DOWN, world, armPos);
							if(down)
								visible.add("arm_"+name+"_down");
							else
								visible.add("arm_"+name+"_up");
						}
						else//Simple Connectors
							visible.add("con_"+i+"_"+name);
					}
			}
		}
		IEObjState modelState = new IEObjState(VisibilityList.show(visible));
		return new SinglePropertyModelData<>(modelState, Model.IE_OBJ_STATE);
	}

	@Override
	public boolean canConnectTransformer(IBlockReader world, BlockPos pos)
	{
		int offset = world.getBlockState(pos).get(POST_SLAVE);
		return offset > 0;
	}

	enum HorizontalOffset implements IStringSerializable
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
					return Direction.NORTH.getDirectionVec();
				case SOUTH:
					return Direction.SOUTH.getDirectionVec();
				case EAST:
					return Direction.EAST.getDirectionVec();
				case WEST:
					return Direction.WEST.getDirectionVec();
				case NONE:
					return BlockPos.NULL_VECTOR;
				default:
					throw new IllegalStateException();
			}
		}

		@Override
		public String getName()
		{
			return name().toLowerCase(Locale.ENGLISH);
		}
	}
}

/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.items.HammerItem;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class MetalLadderBlock extends LadderBlock
{
	private static final Map<Direction, VoxelShape> FRAMES = new EnumMap<>(Direction.class);

	static
	{
		for(Direction dir : DirectionUtils.BY_HORIZONTAL_INDEX)
		{
			VoxelShape forDir = VoxelShapes.empty();
			if(dir!=Direction.NORTH)
				forDir = merge(forDir, new AxisAlignedBB(0, 0, .9375, 1, 1, 1));
			if(dir!=Direction.EAST)
				forDir = merge(forDir, new AxisAlignedBB(0, 0, 0, .0625, 1, 1));
			if(dir!=Direction.SOUTH)
				forDir = merge(forDir, new AxisAlignedBB(0, 0, 0, 1, 1, .0625));
			if(dir!=Direction.WEST)
				forDir = merge(forDir, new AxisAlignedBB(.9375, 0, 0, 1, 1, 1));
			FRAMES.put(dir, forDir);
		}
	}

	private static VoxelShape merge(VoxelShape a, AxisAlignedBB b)
	{
		return VoxelShapes.combine(a, VoxelShapes.create(b), IBooleanFunction.OR);
	}

	private final CoverType type;

	public MetalLadderBlock(CoverType type)
	{
		super(
				Properties.create(Material.IRON)
						.sound(SoundType.METAL)
						.hardnessAndResistance(3, 15)
						.notSolid()
		);
		this.type = type;
		setRegistryName(new ResourceLocation(ImmersiveEngineering.MODID, "metal_ladder_"+type.name().toLowerCase(Locale.US)));

		IEContent.registeredIEBlocks.add(this);
		IEContent.registeredIEItems.add(new BlockItemIE(this));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext ctx)
	{
		VoxelShape base = super.getShape(state, world, pos, ctx);
		if(type==CoverType.NONE)
			return base;
		else
		{
			Direction ladderSide = state.get(LadderBlock.FACING);
			return VoxelShapes.combine(base, FRAMES.get(ladderSide), IBooleanFunction.OR);
		}
	}

	@Override
	public boolean isToolEffective(BlockState state, ToolType tool)
	{
		return super.isToolEffective(state, tool)||tool==HammerItem.HAMMER_TOOL;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx)
	{
		BlockState baseState = super.getStateForPlacement(ctx);
		if(type==CoverType.NONE||baseState==null)
			return baseState;
		else
			return baseState.with(LadderBlock.FACING, Direction.fromAngle(ctx.getPlacementYaw()).getOpposite());
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos)
	{
		if(type==CoverType.NONE)
			return super.isValidPosition(state, world, pos);
		else
			return true;
	}

	public enum CoverType
	{
		NONE,
		ALU,
		STEEL;
	}
}
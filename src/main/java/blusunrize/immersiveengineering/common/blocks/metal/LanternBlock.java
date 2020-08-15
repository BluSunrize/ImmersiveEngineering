/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasObjProperty;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.Map;

public class LanternBlock extends IEBaseBlock implements IHasObjProperty
{
	private static final IProperty<Direction> FACING = IEProperties.FACING_ALL;

	public LanternBlock(String name)
	{
		super(name, Properties.create(Material.IRON)
						.sound(SoundType.METAL)
						.hardnessAndResistance(3, 15)
						.lightValue(14),
				BlockItem::new,
				FACING, BlockStateProperties.WATERLOGGED);
		setNotNormalBlock();
	}

	private static final Map<Direction, VoxelShape> SHAPES = ImmutableMap.<Direction, VoxelShape>builder()
			.put(Direction.DOWN, VoxelShapes.create(0.25, 0.125, 0.25, 0.75, 1, 0.75))
			.put(Direction.UP, VoxelShapes.create(0.25, 0, 0.25, 0.75, 0.875, 0.75))
			.put(Direction.NORTH, VoxelShapes.create(0.25, 0.0625, 0.25, 0.75, 0.875, 1))
			.put(Direction.EAST, VoxelShapes.create(0, 0.0625, 0.25, 0.75, 0.875, 0.75))
			.put(Direction.SOUTH, VoxelShapes.create(0.25, 0.0625, 0, 0.75, 0.875, 0.75))
			.put(Direction.WEST, VoxelShapes.create(0.25, 0.0625, 0.25, 1, 0.875, 0.75))
			.build();

	private static final Map<Direction, VisibilityList> DISPLAY_LISTS = ImmutableMap.<Direction, VisibilityList>builder()
			.put(Direction.DOWN, VisibilityList.show("base", "attach_t"))
			.put(Direction.UP, VisibilityList.show("base", "attach_b"))
			.put(Direction.NORTH, VisibilityList.show("base", "attach_n"))
			.put(Direction.SOUTH, VisibilityList.show("base", "attach_s"))
			.put(Direction.WEST, VisibilityList.show("base", "attach_w"))
			.put(Direction.EAST, VisibilityList.show("base", "attach_e"))
			.build();

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPES.get(state.get(FACING));
	}

	@Override
	public VisibilityList compileDisplayList(BlockState state)
	{
		return DISPLAY_LISTS.get(state.get(FACING));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		return getDefaultState().with(FACING, context.getFace());
	}
}

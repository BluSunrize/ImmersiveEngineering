/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class ShaderBannerBlock extends IEEntityBlock<ShaderBannerBlockEntity>
{
	public static final Supplier<Properties> PROPERTIES = () -> Block.Properties.of()
			.mapColor(MapColor.WOOL)
			.ignitedByLava()
			.strength(1.0F)
			.sound(SoundType.WOOL)
			.noCollission()
			.noOcclusion();

	private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

	public ShaderBannerBlock(Properties props)
	{
		super(IEBlockEntities.SHADER_BANNER, props);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return Shapes.empty();
	}

	@Nonnull
	@Override
	public Item asItem()
	{
		return Items.WHITE_BANNER;
	}
}

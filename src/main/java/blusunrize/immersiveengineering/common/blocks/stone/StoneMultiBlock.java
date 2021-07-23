/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEMultiblockBlock;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class StoneMultiBlock<T extends MultiblockPartTileEntity<? super T>> extends IEMultiblockBlock
{
	public static Supplier<Properties> properties(boolean solid)
	{
		return () -> {
			Properties base = Properties.of(Material.STONE)
					.strength(2, 20);
			if(!solid)
				base.noOcclusion();
			return base;
		};
	}

	private final RegistryObject<BlockEntityType<T>> type;

	public StoneMultiBlock(Properties props, RegistryObject<BlockEntityType<T>> type)
	{
		super(props);
		this.type = type;
		lightOpacity = 0;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.ACTIVE);
	}

	@Nullable
	@Override
	public BlockEntity createTileEntity(@Nonnull BlockState state, @Nonnull BlockGetter world)
	{
		return type.get().create();
	}
}

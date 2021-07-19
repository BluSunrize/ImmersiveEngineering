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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Util;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class StoneMultiBlock<T extends MultiblockPartTileEntity<? super T>> extends IEMultiblockBlock
{
	public static Supplier<Properties> properties(boolean solid)
	{
		return () -> {
			Properties base = Properties.create(Material.ROCK)
					.hardnessAndResistance(2, 20);
			if(!solid)
				base.notSolid();
			return base;
		};
	}

	private final RegistryObject<TileEntityType<T>> type;

	public StoneMultiBlock(Properties props, RegistryObject<TileEntityType<T>> type)
	{
		super(props);
		this.type = type;
		lightOpacity = 0;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(IEProperties.ACTIVE);
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return type.get().create();
	}
}

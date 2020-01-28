/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.fluids;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.fluids.FluidAttributes;

import javax.annotation.Nonnull;

import static blusunrize.immersiveengineering.common.data.IEDataGenerator.rl;

public class PotionFluid extends Fluid
{
	public PotionFluid()
	{
		setRegistryName(ImmersiveEngineering.MODID, "potion");
		IEContent.registeredIEFluids.add(this);
	}

	@Nonnull
	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Nonnull
	@Override
	public Item getFilledBucket()
	{
		return Items.AIR;
	}

	@Override
	protected boolean canDisplace(@Nonnull IFluidState p_215665_1_, @Nonnull IBlockReader p_215665_2_,
								  @Nonnull BlockPos p_215665_3_, @Nonnull Fluid p_215665_4_, @Nonnull Direction p_215665_5_)
	{
		return true;
	}

	@Nonnull
	@Override
	protected Vec3d getFlow(@Nonnull IBlockReader p_215663_1_, @Nonnull BlockPos p_215663_2_, @Nonnull IFluidState p_215663_3_)
	{
		return Vec3d.ZERO;
	}

	@Override
	public int getTickRate(IWorldReader p_205569_1_)
	{
		return 0;
	}

	@Override
	protected float getExplosionResistance()
	{
		return 0;
	}

	@Override
	public float getActualHeight(@Nonnull IFluidState p_215662_1_, @Nonnull IBlockReader p_215662_2_, @Nonnull BlockPos p_215662_3_)
	{
		return 0;
	}

	@Override
	public float getHeight(@Nonnull IFluidState p_223407_1_)
	{
		return 0;
	}

	@Nonnull
	@Override
	protected BlockState getBlockState(@Nonnull IFluidState state)
	{
		return Blocks.AIR.getDefaultState();
	}

	@Override
	public boolean isSource(@Nonnull IFluidState state)
	{
		return true;
	}

	@Override
	public int getLevel(@Nonnull IFluidState p_207192_1_)
	{
		return 0;
	}

	@Nonnull
	@Override
	public VoxelShape func_215664_b(@Nonnull IFluidState p_215664_1_, @Nonnull IBlockReader p_215664_2_, @Nonnull BlockPos p_215664_3_)
	{
		return VoxelShapes.empty();
	}

	@Override
	protected FluidAttributes createAttributes()
	{
		return FluidAttributes.builder(rl("block/fluid/potion_still"), rl("block/fluid/potion_flowing"))
				.build(this);
	}
}

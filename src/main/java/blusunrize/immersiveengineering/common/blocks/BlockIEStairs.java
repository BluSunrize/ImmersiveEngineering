/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockIEStairs extends BlockStairs
{
	public boolean hasFlavour = false;
	public boolean isFlammable = false;
	public String name;
	float explosionResistance;
	BlockRenderLayer renderLayer = BlockRenderLayer.SOLID;

	public BlockIEStairs(String name, IBlockState state)
	{
		super(state);
		this.name = name;
		this.setTranslationKey(ImmersiveEngineering.MODID+"."+name);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
		this.useNeighborBrightness = true;
		this.explosionResistance = this.blockResistance/5f;
//		ImmersiveEngineering.registerBlock(this, ItemBlockIEStairs.class, name);
		IEContent.registeredIEBlocks.add(this);
		IEContent.registeredIEItems.add(new ItemBlockIEStairs(this));
	}

	public BlockIEStairs setFlammable(boolean b)
	{
		this.isFlammable = b;
		return this;
	}

	public BlockIEStairs setHasFlavour(boolean hasFlavour)
	{
		this.hasFlavour = hasFlavour;
		return this;
	}

	@Override
	public float getExplosionResistance(Entity exploder)
	{
		return explosionResistance;
	}

	public BlockIEStairs setExplosionResistance(float explosionResistance)
	{
		this.explosionResistance = explosionResistance;
		return this;
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
	{
		if(this.renderLayer!=BlockRenderLayer.SOLID)
			return false;
		return super.doesSideBlockRendering(state, world, pos, face);
	}

	public BlockIEStairs setRenderLayer(BlockRenderLayer layer)
	{
		this.renderLayer = layer;
		return this;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return renderLayer;
	}
}
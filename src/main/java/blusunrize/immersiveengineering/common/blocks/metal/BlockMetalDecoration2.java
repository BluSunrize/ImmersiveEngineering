/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IPostBlock;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.blocks.generic.TileEntityPost;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMetalDecoration2 extends BlockIETileProvider<BlockTypes_MetalDecoration2> implements IPostBlock
{
	public BlockMetalDecoration2()
	{
		super("metal_decoration2", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalDecoration2.class),
				ItemBlockIEBase.class, IEProperties.FACING_ALL, IEProperties.MULTIBLOCKSLAVE, IEProperties.INT_4,
				Properties.AnimationProperty, IOBJModelCallback.PROPERTY, IEProperties.CONNECTIONS);
		this.setHardness(3.0F);
		this.setResistance(15.0F);
		this.setAllNotNormalBlock();
		this.setMetaBlockLayer(BlockTypes_MetalDecoration2.RAZOR_WIRE.getMeta(), BlockRenderLayer.CUTOUT, BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		this.setMetaBlockLayer(BlockTypes_MetalDecoration2.STEEL_SLOPE.getMeta(), BlockRenderLayer.CUTOUT_MIPPED);
		this.setMetaBlockLayer(BlockTypes_MetalDecoration2.ALU_SLOPE.getMeta(), BlockRenderLayer.CUTOUT_MIPPED);
		lightOpacity = 0;
		this.setMetaMobilityFlag(BlockTypes_MetalDecoration2.STEEL_POST.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_MetalDecoration2.ALUMINUM_POST.getMeta(), EnumPushReaction.BLOCK);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		if(this.getMetaFromState(state)==BlockTypes_MetalDecoration2.ALUMINUM_POST.getMeta()||this.getMetaFromState(state)==BlockTypes_MetalDecoration2.STEEL_POST.getMeta())
			return;
		super.getDrops(drops, world, pos, state, fortune);
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityPost)
		{
			//TODO getFaceShape
			return ((TileEntityPost)te).dummy==0?side==EnumFacing.DOWN: ((TileEntityPost)te).dummy==3?side==EnumFacing.UP: ((TileEntityPost)te).dummy > 3?side.getAxis()==Axis.Y: side.getAxis()!=Axis.Y;
		}
		return super.isSideSolid(state, world, pos, side);
	}

	@Override
	public boolean canIEBlockBePlaced(IBlockState newState, BlockItemUseContext context)
	{
		if(stack.getItemDamage()==BlockTypes_MetalDecoration2.STEEL_POST.getMeta()||stack.getItemDamage()==BlockTypes_MetalDecoration2.ALUMINUM_POST.getMeta())
		{
			for(int hh = 1; hh <= 3; hh++)
			{
				BlockPos pos2 = pos.up(hh);
				if(world.isOutsideBuildHeight(pos2)||!world.getBlockState(pos2).getBlock().isReplaceable(world, pos2))
					return false;
			}
		}
		return true;
	}

	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity)
	{
		return (world.getTileEntity(pos) instanceof TileEntityPost);
	}

	@Override
	public TileEntity createBasicTE(World worldIn, BlockTypes_MetalDecoration2 type)
	{
		switch(type)
		{
			case STEEL_POST:
				return new TileEntityPost();
			case ALUMINUM_POST:
				return new TileEntityPost();
			case LANTERN:
				return new TileEntityLantern();
			case RAZOR_WIRE:
				return new TileEntityRazorWire();
			case TOOLBOX:
				return new TileEntityToolbox();
			case STEEL_SLOPE:
			case ALU_SLOPE:
				return new TileEntityStructuralArm();
		}
		return null;
	}

	@Override
	public boolean canConnectTransformer(IBlockAccess world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		BlockTypes_MetalDecoration2 type = state.getValue(property);
		boolean slave = state.getValue(IEProperties.MULTIBLOCKSLAVE);
		return slave&&(type==BlockTypes_MetalDecoration2.STEEL_POST||type==BlockTypes_MetalDecoration2.ALUMINUM_POST);
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}

	@Override
	public boolean allowWirecutterHarvest(IBlockState state)
	{
		return getMetaFromState(state)==BlockTypes_MetalDecoration2.RAZOR_WIRE.getMeta();
	}
}
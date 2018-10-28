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
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWallmount;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenPost;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;

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
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof TileEntityWoodenPost)
		{
			if(!((TileEntityWoodenPost)tileEntity).isDummy()&&!world.isRemote&&world.getGameRules().getBoolean("doTileDrops")&&!world.restoringBlockSnapshots)
				world.spawnEntity(new EntityItem(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, new ItemStack(this, 1, this.getMetaFromState(state))));
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing)
	{
		int meta = this.getMetaFromState(world.getBlockState(pos));
		if(meta==BlockTypes_MetalDecoration2.STEEL_WALLMOUNT.getMeta()||meta==BlockTypes_MetalDecoration2.ALUMINUM_WALLMOUNT.getMeta())
		{
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof TileEntityWallmount)
			{
				if(facing==EnumFacing.UP)
					return ((TileEntityWallmount)te).orientation==0||((TileEntityWallmount)te).orientation==2;
				else if(facing==EnumFacing.DOWN)
					return ((TileEntityWallmount)te).orientation==1||((TileEntityWallmount)te).orientation==3;
				else
					return facing==(((TileEntityWallmount)te).orientation > 1?((TileEntityWallmount)te).facing.getOpposite(): ((TileEntityWallmount)te).facing);
			}
		}
		return super.canBeConnectedTo(world, pos, facing);
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityWoodenPost)
		{
			return ((TileEntityWoodenPost)te).dummy==0?side==EnumFacing.DOWN: ((TileEntityWoodenPost)te).dummy==3?side==EnumFacing.UP: ((TileEntityWoodenPost)te).dummy > 3?side.getAxis()==Axis.Y: side.getAxis()!=Axis.Y;
		}
		if(te instanceof TileEntityWallmount)
		{
			if(side==EnumFacing.UP)
				return ((TileEntityWallmount)te).orientation==0||((TileEntityWallmount)te).orientation==2;
			else if(side==EnumFacing.DOWN)
				return ((TileEntityWallmount)te).orientation==1||((TileEntityWallmount)te).orientation==3;
			else
				return side==(((TileEntityWallmount)te).orientation > 1?((TileEntityWallmount)te).facing.getOpposite(): ((TileEntityWallmount)te).facing);
		}
		return super.isSideSolid(state, world, pos, side);
	}


	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side)
	{
		int meta = this.getMetaFromState(state);
		if(meta==BlockTypes_MetalDecoration2.STEEL_WALLMOUNT.getMeta()||meta==BlockTypes_MetalDecoration2.ALUMINUM_WALLMOUNT.getMeta())
		{
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof TileEntityWallmount)
			{
				if(side==EnumFacing.UP)
					return ((TileEntityWallmount)te).orientation==0||((TileEntityWallmount)te).orientation==2?BlockFaceShape.CENTER: BlockFaceShape.UNDEFINED;
				else if(side==EnumFacing.DOWN)
					return ((TileEntityWallmount)te).orientation==1||((TileEntityWallmount)te).orientation==3?BlockFaceShape.CENTER: BlockFaceShape.UNDEFINED;
				else
					return side==(((TileEntityWallmount)te).orientation > 1?((TileEntityWallmount)te).facing.getOpposite(): ((TileEntityWallmount)te).facing)?BlockFaceShape.CENTER: BlockFaceShape.UNDEFINED;
			}
		}
		return BlockFaceShape.SOLID;
	}

	@Override
	public boolean canIEBlockBePlaced(World world, BlockPos pos, IBlockState newState, EnumFacing side, float hitX, float hitY, float hitZ, EntityPlayer player, ItemStack stack)
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
		return (world.getTileEntity(pos) instanceof TileEntityWoodenPost);
	}

	@Override
	public TileEntity createBasicTE(World worldIn, BlockTypes_MetalDecoration2 type)
	{
		switch(type)
		{
			case STEEL_POST:
				return new TileEntityWoodenPost();
			case STEEL_WALLMOUNT:
				return new TileEntityWallmount();
			case ALUMINUM_POST:
				return new TileEntityWoodenPost();
			case ALUMINUM_WALLMOUNT:
				return new TileEntityWallmount();
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
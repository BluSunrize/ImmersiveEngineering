/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIEMultiblock;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMetalMultiblocks extends BlockIEMultiblock<BlockTypes_MetalMultiblock>
{
	public BlockMetalMultiblocks()
	{
		super("metal_multiblock", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalMultiblock.class), ItemBlockIEBase.class, IEProperties.DYNAMICRENDER, IEProperties.BOOLEANS[0], Properties.AnimationProperty, IEProperties.OBJ_TEXTURE_REMAP);
		setHardness(3.0F);
		setResistance(15.0F);
		this.setMetaBlockLayer(BlockTypes_MetalMultiblock.TANK.getMeta(), BlockRenderLayer.CUTOUT);
		this.setMetaBlockLayer(BlockTypes_MetalMultiblock.DIESEL_GENERATOR.getMeta(), BlockRenderLayer.CUTOUT);
		this.setMetaBlockLayer(BlockTypes_MetalMultiblock.BOTTLING_MACHINE.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		this.setAllNotNormalBlock();
		lightOpacity = 0;
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}

	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		if(BlockTypes_MetalMultiblock.values()[meta].needsCustomState())
			return BlockTypes_MetalMultiblock.values()[meta].getCustomState();
		return null;
	}

	@Override
	public EnumPushReaction getPushReaction(IBlockState state)
	{
		return EnumPushReaction.BLOCK;
	}

	@Override
	public TileEntity createBasicTE(World world, BlockTypes_MetalMultiblock type)
	{
		switch(type)
		{
			case METAL_PRESS:
				return new TileEntityMetalPress();
			case CRUSHER:
				return new TileEntityCrusher();
			case TANK:
				return new TileEntitySheetmetalTank();
			case SILO:
				return new TileEntitySilo();
			case ASSEMBLER:
				return new TileEntityAssembler();
			case AUTO_WORKBENCH:
				return new TileEntityAutoWorkbench();
			case BOTTLING_MACHINE:
				return new TileEntityBottlingMachine();
			case SQUEEZER:
				return new TileEntitySqueezer();
			case FERMENTER:
				return new TileEntityFermenter();
			case REFINERY:
				return new TileEntityRefinery();
			case DIESEL_GENERATOR:
				return new TileEntityDieselGenerator();
			case EXCAVATOR:
				return new TileEntityExcavator();
			case BUCKET_WHEEL:
				return new TileEntityBucketWheel();
			case ARC_FURNACE:
				return new TileEntityArcFurnace();
			case LIGHTNINGROD:
				return new TileEntityLightningrod();
			case MIXER:
				return new TileEntityMixer();
		}
		return null;
	}


	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityMultiblockPart)
		{
			TileEntityMultiblockPart tile = (TileEntityMultiblockPart)te;
			if(tile instanceof TileEntityMultiblockMetal&&((TileEntityMultiblockMetal)tile).isRedstonePos())
				return true;
			if(te instanceof TileEntityMetalPress)
			{
				return tile.pos < 3||(tile.pos==7&&side==EnumFacing.UP);
			}
			else if(te instanceof TileEntityCrusher)
			{
				return tile.pos%5==0||tile.pos==2||tile.pos==9||(tile.pos==19&&side.getOpposite()==tile.facing);
			}
			else if(te instanceof TileEntitySheetmetalTank)
			{
				return tile.pos==4||tile.pos==40||(tile.pos >= 18&&tile.pos < 36);
			}
			else if(te instanceof TileEntitySilo)
			{
				return tile.pos==4||tile.pos==58||(tile.pos >= 18&&tile.pos < 54);
			}
			else if(te instanceof TileEntitySqueezer||te instanceof TileEntityFermenter)
			{
				return tile.pos==0||tile.pos==9||tile.pos==5||(tile.pos==11&&side.getOpposite()==tile.facing);
			}
			else if(te instanceof TileEntityRefinery)
			{
				return tile.pos==2||tile.pos==5||tile.pos==9||(tile.pos==19&&side.getOpposite()==tile.facing)||(tile.pos==27&&side==tile.facing);
			}
			else if(te instanceof TileEntityDieselGenerator)
			{
				if(tile.pos==0||tile.pos==2)
					return side.getAxis()==tile.facing.rotateY().getAxis();
				else if(tile.pos >= 15&&tile.pos <= 17)
					return side==EnumFacing.UP;
				else if(tile.pos==23)
					return side==(tile.mirrored?tile.facing.rotateYCCW(): tile.facing.rotateY());
			}
			else if(te instanceof TileEntityExcavator)
			{
				if(tile.pos%18 < 9||(tile.pos >= 18&&tile.pos < 36))
					return true;
			}
			else if(te instanceof TileEntityArcFurnace)
			{
				if(tile.pos==2||tile.pos==25||tile.pos==52)
					return side.getOpposite()==tile.facing||(tile.pos==52&&side==EnumFacing.UP);
				if(tile.pos==82||tile.pos==86||tile.pos==88||tile.pos==112)
					return side==EnumFacing.UP;
				if((tile.pos >= 21&&tile.pos <= 23)||(tile.pos >= 46&&tile.pos <= 48)||(tile.pos >= 71&&tile.pos <= 73))
					return side==tile.facing;
			}
		}
		return super.isSideSolid(state, world, pos, side);
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}
}
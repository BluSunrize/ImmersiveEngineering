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
import blusunrize.immersiveengineering.common.blocks.generic.TileEntityMultiblockPart;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
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
	public PushReaction getPushReaction(BlockState state)
	{
		return PushReaction.BLOCK;
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
	public boolean isSideSolid(BlockState state, IBlockAccess world, BlockPos pos, Direction side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityMultiblockPart)
		{
			TileEntityMultiblockPart tile = (TileEntityMultiblockPart)te;
			if(tile instanceof TileEntityMultiblockMetal&&((TileEntityMultiblockMetal)tile).isRedstonePos())
				return true;
			if(te instanceof TileEntityMetalPress)
			{
				return tile.posInMultiblock < 3||(tile.posInMultiblock==7&&side==Direction.UP);
			}
			else if(te instanceof TileEntityCrusher)
			{
				return tile.posInMultiblock%5==0||tile.posInMultiblock==2||tile.posInMultiblock==9||(tile.posInMultiblock==19&&side.getOpposite()==tile.facing);
			}
			else if(te instanceof TileEntitySheetmetalTank)
			{
				return tile.posInMultiblock==4||tile.posInMultiblock==40||(tile.posInMultiblock >= 18&&tile.posInMultiblock < 36);
			}
			else if(te instanceof TileEntitySilo)
			{
				return tile.posInMultiblock==4||tile.posInMultiblock==58||(tile.posInMultiblock >= 18&&tile.posInMultiblock < 54);
			}
			else if(te instanceof TileEntitySqueezer||te instanceof TileEntityFermenter)
			{
				return tile.posInMultiblock==0||tile.posInMultiblock==9||tile.posInMultiblock==5||(tile.posInMultiblock==11&&side.getOpposite()==tile.facing);
			}
			else if(te instanceof TileEntityRefinery)
			{
				return tile.posInMultiblock==2||tile.posInMultiblock==5||tile.posInMultiblock==9||(tile.posInMultiblock==19&&side.getOpposite()==tile.facing)||(tile.posInMultiblock==27&&side==tile.facing);
			}
			else if(te instanceof TileEntityDieselGenerator)
			{
				if(tile.posInMultiblock==0||tile.posInMultiblock==2)
					return side.getAxis()==tile.facing.rotateY().getAxis();
				else if(tile.posInMultiblock >= 15&&tile.posInMultiblock <= 17)
					return side==Direction.UP;
				else if(tile.posInMultiblock==23)
					return side==(tile.mirrored?tile.facing.rotateYCCW(): tile.facing.rotateY());
			}
			else if(te instanceof TileEntityExcavator)
			{
				if(tile.posInMultiblock%18 < 9||(tile.posInMultiblock >= 18&&tile.posInMultiblock < 36))
					return true;
			}
			else if(te instanceof TileEntityArcFurnace)
			{
				if(tile.posInMultiblock==2||tile.posInMultiblock==25||tile.posInMultiblock==52)
					return side.getOpposite()==tile.facing||(tile.posInMultiblock==52&&side==Direction.UP);
				if(tile.posInMultiblock==82||tile.posInMultiblock==86||tile.posInMultiblock==88||tile.posInMultiblock==112)
					return side==Direction.UP;
				if((tile.posInMultiblock >= 21&&tile.posInMultiblock <= 23)||(tile.posInMultiblock >= 46&&tile.posInMultiblock <= 48)||(tile.posInMultiblock >= 71&&tile.posInMultiblock <= 73))
					return side==tile.facing;
			}
		}
		return super.isSideSolid(state, world, pos, side);
	}

	@Override
	public boolean allowHammerHarvest(BlockState state)
	{
		return true;
	}
}
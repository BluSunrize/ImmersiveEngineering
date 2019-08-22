/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.metal.SiloTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MultiblockSilo implements IMultiblock
{
	public static MultiblockSilo instance = new MultiblockSilo();

	static ItemStack[][][] structure = new ItemStack[7][3][3];

	static
	{
		for(int h = 0; h < 7; h++)
			for(int l = 0; l < 3; l++)
				for(int w = 0; w < 3; w++)
				{
					if(h==0)
					{
						if((l==0||l==2)&&(w==0||w==2))
							structure[h][l][w] = new ItemStack(IEContent.blockWoodenDecoration, 1, BlockTypes_WoodenDecoration.FENCE.getMeta());
						else if(l==1&&w==1)
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, EnumMetals.IRON.getMeta());
					}
					else if(h < 1||h > 5||w!=1||l!=1)
						structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, EnumMetals.IRON.getMeta());
				}
	}

	@Override
	public ItemStack[][][] getStructureManual()
	{
		return structure;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
	{
		return false;
	}

	@Override
	public float getManualScale()
	{
		return 10;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	//@OnlyIn(Dist.CLIENT)
	static ItemStack renderStack = ItemStack.EMPTY;

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack.isEmpty())
			renderStack = new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.SILO.getMeta());
		GlStateManager.translate(2, 2.5, 1);
		GlStateManager.rotate(-45, 0, 1, 0);
		GlStateManager.rotate(-20, 1, 0, 0);
		GlStateManager.scale(8, 8, 8);
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
	}

	@Override
	public String getUniqueName()
	{
		return "IE:Silo";
	}

	@Override
	public boolean isBlockTrigger(BlockState state)
	{
		return Utils.isInTag(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)), "blockSheetmetalIron");
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, Direction side, PlayerEntity player)
	{
		Direction f = Direction.fromAngle(player.rotationYaw);
		pos = pos.offset(f);
		if(!(Utils.isOreBlockAt(world, pos.offset(f, -1).offset(f.rotateY()), "fenceTreatedWood")&&Utils.isOreBlockAt(world, pos.offset(f, -1).offset(f.rotateYCCW()), "fenceTreatedWood")))
			for(int i = 0; i < 6; i++)
				if(Utils.isOreBlockAt(world, pos.add(0, -i, 0).offset(f, -1).offset(f.rotateY()), "fenceTreatedWood")&&Utils.isOreBlockAt(world, pos.add(0, -i, 0).offset(f, -1).offset(f.rotateYCCW()), "fenceTreatedWood"))
				{
					pos = pos.add(0, -i, 0);
					break;
				}

		for(int h = 0; h <= 6; h++)
			for(int xx = -1; xx <= 1; xx++)
				for(int zz = -1; zz <= 1; zz++)
					if(h==0)
					{
						if(Math.abs(xx)==1&&Math.abs(zz)==1)
						{
							if(!Utils.isOreBlockAt(world, pos.add(xx, h, zz), "fenceTreatedWood"))
								return false;
						}
						else if(xx==0&&zz==0)
							if(!Utils.isOreBlockAt(world, pos.add(xx, h, zz), "blockSheetmetalIron"))
								return false;
					}
					else
					{
						if(h < 6&&xx==0&&zz==0)
						{
							if(!world.isAirBlock(pos.add(xx, h, zz)))
								return false;
						}
						else if(!Utils.isOreBlockAt(world, pos.add(xx, h, zz), "blockSheetmetalIron"))
							return false;
					}

		BlockState state = IEContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.SILO.getMeta());
		state = state.with(IEProperties.FACING_HORIZONTAL, f.getOpposite());
		for(int h = 0; h <= 6; h++)
			for(int l = -1; l <= 1; l++)
				for(int w = -1; w <= 1; w++)
				{
					if(h==0&&!((l==0&&w==0)||(Math.abs(l)==1&&Math.abs(w)==1)))
						continue;
					if(h > 0&&h < 6&&l==0&&w==0)
						continue;

					int xx = f==Direction.EAST?l: f==Direction.WEST?-l: f==Direction.NORTH?-w: w;
					int zz = f==Direction.NORTH?l: f==Direction.SOUTH?-l: f==Direction.EAST?w: -w;

					world.setBlockState(pos.add(xx, h, zz), state);
					BlockPos pos2 = pos.add(xx, h, zz);
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof SiloTileEntity)
					{
						SiloTileEntity currTank = (SiloTileEntity)curr;
						currTank.offset = new int[]{xx, h, zz};
						currTank.posInMultiblock = h*9+(l+1)*3+(w+1);
						currTank.formed = true;
						currTank.offset = new int[]{xx, h, zz};
						currTank.markDirty();
						world.addBlockEvent(pos2, IEContent.blockMetalMultiblock, 255, 0);
					}
				}
		return true;
	}

	static final IngredientStack[] materials = new IngredientStack[]{new IngredientStack("fenceTreatedWood", 4), new IngredientStack("blockSheetmetalIron", 50)};

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}
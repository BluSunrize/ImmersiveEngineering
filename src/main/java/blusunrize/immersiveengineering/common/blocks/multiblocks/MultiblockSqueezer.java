package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDevice0;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MultiblockSqueezer implements IMultiblock
{
	public static MultiblockSqueezer instance = new MultiblockSqueezer();

	static ItemStack[][][] structure = new ItemStack[3][3][3];
	static{
		for(int h=0;h<3;h++)
			for(int l=0;l<3;l++)
				for(int w=0;w<3;w++)
					if(h==0)
					{
						if(l==0&&w==0)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
						else if(l==1 && w>0)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
						else
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
					}
					else if(h==1)
					{
						if(l==0&&w==0)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
						else if(l==0&&w==2)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
						else if(l>0 && w<2)
							structure[h][l][w] = new ItemStack(IEContent.blockWoodenDevice0,1, BlockTypes_WoodenDevice0.BARREL.getMeta());
					}
					else if(h==2)
					{
						if(l==1&&w==1)
							structure[h][l][w] = new ItemStack(Blocks.PISTON);
						else if(l>0&&w<2)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta());
					}
	}

	@Override
	public ItemStack[][][] getStructureManual()
	{
		return structure;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
	{
		if(iterator==3)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("002200");
			return true;
		}
		if(iterator==4)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("001010");
			return true;
		}

		return false;
	}
	@Override
	public float getManualScale()
	{
		return 13;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}
	@SideOnly(Side.CLIENT)
	static ItemStack renderStack;
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack==null)
			renderStack = new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.SQUEEZER.getMeta());
		GlStateManager.scale(-4, 4, 4);
		GlStateManager.translate(0, .125, 0);
		GlStateManager.rotate(225, 0, 1, 0);
		GlStateManager.rotate(-20, 1, 0, 0);
		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}

	@Override
	public String getUniqueName()
	{
		return "IE:Squeezer";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return state.getBlock()==IEContent.blockWoodenDevice0 && (state.getBlock().getMetaFromState(state)==BlockTypes_WoodenDevice0.BARREL.getMeta());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		side = side.getOpposite();
		if(side==EnumFacing.UP||side==EnumFacing.DOWN)
			side = EnumFacing.fromAngle(player.rotationYaw);

		boolean mirror = false;
		boolean b = this.structureCheck(world, pos, side, mirror);
		if(!b)
		{
			mirror = true;
			b = structureCheck(world, pos, side, mirror);
		}
		if(!b)
			return false;

		for(int h=-1;h<=1;h++)
			for(int l=-1;l<=1;l++)
				for(int w=-1;w<=1;w++)
				{
					if((h==0&&w==0&&l==-1)||(h==0&&w==1&&l>-1)||(h==1&&(l<0||w>0)))
						continue;

					int ww = mirror?-w:w;
					BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);

					world.setBlockState(pos2, IEContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.SQUEEZER.getMeta()));
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntitySqueezer)
					{
						TileEntitySqueezer tile = (TileEntitySqueezer)curr;
						tile.facing=side;
						tile.formed=true;
						tile.pos = (h+1)*9 + (l+1)*3 + (w+1);
						tile.offset = new int[]{(side==EnumFacing.WEST?-l: side==EnumFacing.EAST?l: side==EnumFacing.NORTH?ww: -ww),h,(side==EnumFacing.NORTH?-l: side==EnumFacing.SOUTH?l: side==EnumFacing.EAST?ww : -ww)};
						tile.mirrored=mirror;
						tile.markDirty();
						world.addBlockEvent(pos2, IEContent.blockMetalMultiblock, 255, 0);
					}
				}
		return true;
	}

	boolean structureCheck(World world, BlockPos startPos, EnumFacing dir, boolean mirror)
	{
		for(int h=-1;h<=1;h++)
			for(int l=-1;l<=1;l++)
				for(int w=-1;w<=1;w++)
				{
					if((h==0&&w==0&&l==-1)||(h==0&&w==1&&l>-1)||(h==1&&(l<0||w>0)))
						continue;

					int ww = mirror?-w:w;
					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), ww).add(0, h, 0);

					if(h==-1)
					{
						if(l==-1&&w==-1)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
								return false;
						}
						else if(l==0 && w>-1)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDevice1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()))
								return false;
						}
						else
						{
							if(!Utils.isOreBlockAt(world, pos, "scaffoldingSteel"))
								return false;
						}
					}
					else if(h==0)
					{
						if(l==-1&&w==-1)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
								return false;
						}
						else if(l==-1&&w==1)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()))
								return false;
						}
						else if(l>-1 && w<1)
						{
							if(!Utils.isBlockAt(world, pos, IEContent.blockWoodenDevice0, BlockTypes_WoodenDevice0.BARREL.getMeta()))
								return false;
						}
					}
					else if(h==1)
					{
						if(l==0&&w==0)
						{
							if(!Utils.isBlockAt(world, pos, Blocks.PISTON, -1))
								return false;
						}
						else if(l>-1&&w<1)
						{
							if(!Utils.isOreBlockAt(world, pos, "fenceSteel"))
								return false;
						}
					}
				}
		return true;
	}

	static final ItemStack[] materials = new ItemStack[]{
			new ItemStack(IEContent.blockMetalDecoration1,6,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()),
			new ItemStack(IEContent.blockMetalDevice1,2,BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()),
			new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()),
			new ItemStack(IEContent.blockMetalDecoration0,2,BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()),
			new ItemStack(IEContent.blockWoodenDevice0,4,BlockTypes_WoodenDevice0.BARREL.getMeta()),
			new ItemStack(Blocks.PISTON),
			new ItemStack(IEContent.blockMetalDecoration1,3,BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta())};
	@Override
	public ItemStack[] getTotalMaterials()
	{
		return materials;
	}
}
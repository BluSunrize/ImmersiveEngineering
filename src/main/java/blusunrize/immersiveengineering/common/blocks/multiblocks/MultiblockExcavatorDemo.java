package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MultiblockExcavatorDemo implements IMultiblock
{
	public static MultiblockExcavatorDemo instance = new MultiblockExcavatorDemo();
	static ItemStack[][][] structure = new ItemStack[7][8][3];
	static{
		for(int h=0;h<3;h++)
			for(int l=0;l<6;l++)
				for(int w=0;w<3;w++)
				{
					if(l>0&&w==1)
						continue;
					if(l==0)
					{
						if(w==0&&h==1)
							structure[h+2][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
						else if((w==1&&h==1)||(w==0&&h==2))
							structure[h+2][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
						else if(w==0&&h==0)
							structure[h+2][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.RADIATOR.getMeta());
						else
							structure[h+2][l][w] = new ItemStack(IEContent.blockSheetmetal,1,BlockTypes_MetalsAll.STEEL.getMeta());
					}
					else if(w==0)
					{
						if(l<3 && h==2)
							structure[h+2][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.RADIATOR.getMeta());
						else if(l<3)
							structure[h+2][l][w] = new ItemStack(IEContent.blockSheetmetal,1,BlockTypes_MetalsAll.STEEL.getMeta());
						else if(h==0)
							structure[h+2][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
						else
							structure[h+2][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
					}
					else if(w==2)
					{
						if(l==1)
							structure[h+2][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
						else if(l==2)
							structure[h+2][l][w] = new ItemStack(IEContent.blockSheetmetal,1,BlockTypes_MetalsAll.STEEL.getMeta());
						else if(h==0)
							structure[h+2][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
						else if(h==1)
							structure[h+2][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
						else
							structure[h+2][l][w] = new ItemStack(IEContent.blockSheetmetal,1,BlockTypes_MetalsAll.STEEL.getMeta());
					}
				}
		for(int h=0;h<7;h++)
			for(int l=0;l<7;l++)
			{
				if((h==0||h==6) && l!=3)
					continue;
				if((l==0||l==6) && h!=3)
					continue;
				if(l==0||h==0||l==6||h==6 || ((l==1||l==5) && (h==1||h==5)) || (h==3&&l==3))
					structure[h][l+1][1]= new ItemStack(IEContent.blockStorage,1,BlockTypes_MetalsIE.STEEL.getMeta());
				else
					structure[h][l+1][1]= new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
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
		return false;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}
	@SideOnly(Side.CLIENT)
	static ItemStack renderStack;
	@SideOnly(Side.CLIENT)
	static ItemStack renderStack2;
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack==null)
			renderStack = new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.EXCAVATOR.getMeta());
		if(renderStack2==null)
			renderStack2 = new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.BUCKET_WHEEL.getMeta());
		GlStateManager.scale(-8,8,8);
		GlStateManager.rotate(180, 0, 1, 0);
		GlStateManager.translate(0, .0625, .25);
		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.translate(0, 0, -.5);
		ClientUtils.mc().getRenderItem().renderItem(renderStack2, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}
	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public String getUniqueName()
	{
		return "IE:ExcavatorDemo";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		return false;
	}

	static final ItemStack[] materials = new ItemStack[]{
			new ItemStack(IEContent.blockMetalDecoration1,26,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()),
			new ItemStack(IEContent.blockSheetmetal,15,BlockTypes_MetalsAll.STEEL.getMeta()),
			new ItemStack(IEContent.blockStorage,9,BlockTypes_MetalsIE.STEEL.getMeta()),
			new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()),
			new ItemStack(IEContent.blockMetalDecoration0,9,BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()),
			new ItemStack(IEContent.blockMetalDecoration0,5,BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()),
			new ItemStack(IEContent.blockMetalDecoration0,3,BlockTypes_MetalDecoration0.RADIATOR.getMeta())};
	@Override
	public ItemStack[] getTotalMaterials()
	{
		return materials;
	}
}
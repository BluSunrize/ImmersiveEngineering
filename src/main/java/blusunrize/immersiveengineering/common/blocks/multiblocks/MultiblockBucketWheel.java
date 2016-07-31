package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBucketWheel;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MultiblockBucketWheel implements IMultiblock
{
	public static MultiblockBucketWheel instance = new MultiblockBucketWheel();
	static ItemStack[][][] structure = new ItemStack[7][7][1];
	static{
		for(int h=0;h<7;h++)
			for(int l=0;l<7;l++)
			{
				if((h==0||h==6) && l!=3)
					continue;
				if((l==0||l==6) && h!=3)
					continue;
				if(l==0||h==0||l==6||h==6 || ((l==1||l==5) && (h==1||h==5)) || (h==3&&l==3))
					structure[h][l][0]= new ItemStack(IEContent.blockStorage,1,BlockTypes_MetalsIE.STEEL.getMeta());
				else
					structure[h][l][0]= new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
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
	public float getManualScale()
	{
		return 12;
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
			renderStack = new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.BUCKET_WHEEL.getMeta());
		GlStateManager.scale(-8,8,8);
		GlStateManager.rotate(180, 0, 1, 0);
		GlStateManager.translate(0, .0625, 0);
		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}

	@Override
	public String getUniqueName()
	{
		return "IE:BucketWheel";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return Utils.compareToOreName(new ItemStack(state.getBlock(),1,state.getBlock().getMetaFromState(state)), "blockSteel");
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		if(side==EnumFacing.UP||side==EnumFacing.DOWN)
			side = EnumFacing.fromAngle(player.rotationYaw);

		for(int h=-3;h<=3;h++)
			for(int w=-3;w<=3;w++)
			{
				BlockPos pos2 = pos.add((side==EnumFacing.NORTH?w: side==EnumFacing.SOUTH?-w: 0), h, (side==EnumFacing.WEST?w: side==EnumFacing.EAST?-w: 0));

				if((h==-3||h==3) && w!=0)
					continue;
				if((w==-3||w==3) && h!=0)
					continue;
				if(w==-3||h==-3||w==3||h==3 || ((w==-2||w==2) && (h==-2||h==2)) || (h==0&&w==0))
				{
					if(!Utils.isOreBlockAt(world, pos2, "blockSteel"))
						return false;
				}
				else
				{
					if(!Utils.isOreBlockAt(world, pos2, "scaffoldingSteel"))
						return false;
				}
			}

		for(int h=-3;h<=3;h++)
			for(int w=-3;w<=3;w++)
			{
				BlockPos pos2 = pos.add((side==EnumFacing.NORTH?w: side==EnumFacing.SOUTH?-w: 0), h, (side==EnumFacing.WEST?w: side==EnumFacing.EAST?-w: 0));

				if((h==-3||h==3) && w!=0)
					continue;
				if((w==-3||w==3) && h!=0)
					continue;

				world.setBlockState(pos2, IEContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.BUCKET_WHEEL.getMeta()));
				TileEntity curr = world.getTileEntity(pos2);
				if(curr instanceof TileEntityBucketWheel)
				{
					TileEntityBucketWheel tile = (TileEntityBucketWheel) curr;
					tile.facing=side;
					tile.formed=true;
					tile.pos = (w+3) + (h+3)*7;

					tile.offset = new int[]{(side==EnumFacing.NORTH?w: side==EnumFacing.SOUTH?-w: 0),h,(side==EnumFacing.WEST?w: side==EnumFacing.EAST?-w: 0)};
					tile.markDirty();
					world.addBlockEvent(pos2, IEContent.blockMetalMultiblock, 255, 0);
				}
			}
		return true;
	}

	static final ItemStack[] materials = new ItemStack[]{
			new ItemStack(IEContent.blockStorage,9,BlockTypes_MetalsIE.STEEL.getMeta()),
			new ItemStack(IEContent.blockMetalDecoration1,20,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta())};
	@Override
	public ItemStack[] getTotalMaterials()
	{
		return materials;
	}
}
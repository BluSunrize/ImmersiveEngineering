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
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.WireApi;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Connector;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFeedthrough;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Set;

public class MultiblockFeedthrough implements IMultiblock
{
	public static MultiblockFeedthrough instance = new MultiblockFeedthrough();
	static ItemStack[][][] structure = new ItemStack[1][1][3];
	static{
		structure[0][0][0] = new ItemStack(IEContent.blockConnectors, 1);
		structure[0][0][1] = new ItemStack(Blocks.BOOKSHELF, 1).setTranslatableName("tile.immersiveengineering.arb_solid.name");
		structure[0][0][2] = new ItemStack(IEContent.blockConnectors, 1);
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

	private ItemStack renderStack;
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack==null||renderStack.isEmpty())
			renderStack = new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.FEEDTHROUGH.getMeta());

		GlStateManager.translate(.5, .5, 1.5);
		GlStateManager.rotate(-45, 0, 1, 0);
		GlStateManager.rotate(-30, 1, 0, 0);
		GlStateManager.scale(1.75, 1.75, 1.75);

		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}

	@Override
	public IBlockState getBlockstateFromStack(int index, ItemStack stack)
	{
		IBlockState ret = IMultiblock.super.getBlockstateFromStack(index, stack);
		if (stack==structure[0][0][0])
			return ret.withProperty(IEProperties.FACING_ALL, EnumFacing.SOUTH);
		return ret;
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public String getUniqueName()
	{
		return "IE:Feedthrough";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return WireApi.getWireType(state)!=null;
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		side = side.getOpposite();
		IBlockState here = world.getBlockState(pos).getActualState(world, pos);
		if (here.getValue(IEProperties.FACING_ALL)!=side)
			return false;
		Set<ImmersiveNetHandler.Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
		if (conns!=null&&!conns.isEmpty())
			return false;
		WireType wire = WireApi.getWireType(here);
		if (wire==null)//This shouldn't ever happen
			return false;
		BlockPos tmp = pos.offset(side);
		IBlockState middle = world.getBlockState(tmp).getActualState(world, tmp);
		if (!middle.isFullCube()||middle.getBlock().hasTileEntity(middle)||middle.getRenderType()!= EnumBlockRenderType.MODEL)
			return false;
		tmp = pos.offset(side, 2);
		IBlockState otherConn = world.getBlockState(tmp).getActualState(world, tmp);
		if (WireApi.getWireType(otherConn)!=wire)
			return false;
		if (otherConn.getValue(IEProperties.FACING_ALL)!=side.getOpposite())
			return false;
		conns = ImmersiveNetHandler.INSTANCE.getConnections(world, tmp);
		if (conns!=null&&!conns.isEmpty())
			return false;
		IBlockState state = IEContent.blockConnectors.getDefaultState().withProperty(IEContent.blockConnectors.property,
				BlockTypes_Connector.FEEDTHROUGH).withProperty(IEProperties.FACING_ALL, side);
		for (int i = 0;i<=2;i++)
		{
			tmp = pos.offset(side, i);
			world.setBlockState(tmp, state);
			TileEntity te = world.getTileEntity(tmp);
			if (te instanceof TileEntityFeedthrough)
			{
				((TileEntityFeedthrough) te).reference = wire;
				((TileEntityFeedthrough) te).stateForMiddle = middle;
				((TileEntityFeedthrough) te).offset = i-1;
				world.checkLight(tmp);
			}

		}
		return true;
	}

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return new IngredientStack[]{
				new IngredientStack(new ItemStack(IEContent.blockConnectors, 2)),
				new IngredientStack(new ItemStack(Blocks.BOOKSHELF, 1).setTranslatableName("Arbitrary solid block"))
		};
	}
}
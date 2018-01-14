/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MultiblockFeedthrough implements IMultiblock
{
	public static MultiblockFeedthrough instance = new MultiblockFeedthrough();
	static ItemStack[][][] structure = new ItemStack[1][1][3];
	static{
		structure[0][0][0] = new ItemStack(IEContent.blockConnectors, 1);
		//TODO translation?
		structure[0][0][1] = new ItemStack(Blocks.STONE, 1).setTranslatableName("Arbitrary solid block");
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
		//if(iterator==10||iterator==16)
		//	return ImmersiveEngineering.proxy.drawConveyorInGui("immersiveengineering:conveyor", EnumFacing.EAST);
		return false;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	private IBlockState state;
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		if (state==null)
			state = IEContent.blockConnectors.getDefaultState().withProperty(IEContent.blockConnectors.property,
					BlockTypes_Connector.FEEDTHROUGH);
		blockRenderer.renderBlockBrightness(state, 1);
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
		return TileEntityFeedthrough.getWireType(state)!=null;
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
		WireType wire = TileEntityFeedthrough.getWireType(here);
		if (wire==null)//This shouldn't ever happen
			return false;
		BlockPos tmp = pos.offset(side);
		IBlockState middle = world.getBlockState(tmp).getActualState(world, tmp);
		if (!middle.isOpaqueCube()||!middle.isFullBlock()||!middle.isFullCube())
			return false;
		tmp = pos.offset(side, 2);
		IBlockState otherConn = world.getBlockState(tmp).getActualState(world, tmp);
		if (TileEntityFeedthrough.getWireType(otherConn)!=wire)
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
			}

		}
		return true;
	}

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return Arrays.stream(structure[0][0]).map(IngredientStack::new).collect(Collectors.toList()).toArray(new IngredientStack[3]);
	}
}
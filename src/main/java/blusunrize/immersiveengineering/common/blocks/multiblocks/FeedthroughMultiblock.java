/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.blocks.metal.FeedthroughTileEntity;
import blusunrize.immersiveengineering.dummy.GlStateManager;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static blusunrize.immersiveengineering.api.IEProperties.FACING_ALL;

public class FeedthroughMultiblock implements IMultiblock
{
	private static final ITextComponent ARBITRARY_SOLID = new TranslationTextComponent("tile.immersiveengineering.arb_solid.name");
	public static FeedthroughMultiblock instance = new FeedthroughMultiblock();
	static List<BlockInfo> structure = new ArrayList<>();

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	private ItemStack renderStack;

	private Block getDemoConnector()
	{
		return Connectors.getEnergyConnector(WireType.LV_CATEGORY, false);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack==null||renderStack.isEmpty())
			renderStack = new ItemStack(Connectors.feedthrough);

		GlStateManager.translated(.5, .5, 1.5);
		GlStateManager.rotated(-45, 0, 1, 0);
		GlStateManager.rotated(-30, 1, 0, 0);
		GlStateManager.scaled(1.75, 1.75, 1.75);

		GlStateManager.disableCull();
		ClientUtils.mc().getItemRenderer().renderItemIntoGUI(renderStack, 0, 0);
		GlStateManager.enableCull();
	}

	@Override
	public Vec3i getSize()
	{
		return new Vec3i(3, 1, 1);
	}

	@Override
	public void disassemble(World world, BlockPos startPos, boolean mirrored, Direction clickDirectionAtCreation)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public BlockPos getTriggerOffset()
	{
		return new BlockPos(-1, 0, 0);
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public ResourceLocation getUniqueName()
	{
		return new ResourceLocation(ImmersiveEngineering.MODID, "feedthrough");
	}

	@Override
	public boolean isBlockTrigger(BlockState state)
	{
		return WireApi.getWireType(state)!=null;
	}

	@Nullable
	WireType checkValidConnector(World w, BlockPos pos, GlobalWireNetwork globalNet, Direction expectedDirection)
	{
		LocalWireNetwork localNet = globalNet.getNullableLocalNet(pos);
		if(localNet==null)
			return null;
		Collection<Connection> connsHere = localNet.getConnections(pos);
		IImmersiveConnectable connHere = localNet.getConnector(pos);
		if(connsHere==null)
			connsHere = ImmutableSet.of();
		if(connsHere.size() > 1||connHere.getConnectionPoints().size()!=1)
			return null;
		BlockState state = w.getBlockState(pos);
		if(!state.has(FACING_ALL)||state.get(FACING_ALL)!=expectedDirection)
			return null;
		return WireApi.getWireType(state);
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, Direction side, PlayerEntity player)
	{
		GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(world);
		//Check
		BlockState stateHere = world.getBlockState(pos);
		if(stateHere.getProperties().contains(FACING_ALL))
			side = stateHere.get(FACING_ALL);
		WireType wire = checkValidConnector(world, pos, globalNet, side);
		if(wire==null)
			return false;
		BlockPos middlePos = pos.offset(side);
		BlockState middle = world.getBlockState(middlePos);
		if(!middle.getShape(world, pos).equals(VoxelShapes.fullCube())
				||middle.getBlock().hasTileEntity(middle)
				||middle.getRenderType()!=BlockRenderType.MODEL)
			return false;
		BlockPos otherPos = pos.offset(side, 2);
		WireType otherWire = checkValidConnector(world, otherPos, globalNet, side.getOpposite());
		if(otherWire!=wire)
			return false;
		LocalWireNetwork localHere = globalNet.getLocalNet(pos);
		ConnectionPoint cpHere = localHere.getConnector(pos).getConnectionPoints().iterator().next();
		Collection<Connection> connsHere = localHere.getConnections(cpHere);
		LocalWireNetwork localOther = globalNet.getLocalNet(otherPos);
		ConnectionPoint cpOther = localOther.getConnector(otherPos).getConnectionPoints().iterator().next();
		Collection<Connection> connsOther = localOther.getConnections(cpOther);
		if(connsOther.stream().anyMatch(c -> c.isEnd(cpHere)))
			return false;
		for(Connection c : connsOther)
		{
			ConnectionPoint otherEnd = c.getOtherEnd(cpOther);
			if(connsHere.stream().anyMatch(c2 -> c2.isEnd(otherEnd)))
				return false;
		}
		//Form
		if(!world.isRemote)
		{
			BlockState state = Connectors.feedthrough.getDefaultState().with(FACING_ALL, side);
			BlockPos masterPos = pos.offset(side);
			FeedthroughTileEntity master = setBlock(world, masterPos, state, wire, middle, 0);
			if(master!=null)
			{
				moveConnectionsToMaster(connsOther, cpOther, world, master.getPositivePoint());
				moveConnectionsToMaster(connsHere, cpHere, world, master.getNegativePoint());
			}
			setBlock(world, pos, state, wire, middle, -1);
			setBlock(world, pos.offset(side, 2), state, wire, middle, 1);
		}
		return true;
	}

	@Override
	public List<BlockInfo> getStructure()
	{
		if(structure.isEmpty())
		{
			//Along x axis
			structure.add(new BlockInfo(
					BlockPos.ZERO,
					getDemoConnector().getDefaultState().with(FACING_ALL, Direction.EAST),
					null
			));
			structure.add(new BlockInfo(
					new BlockPos(1, 0, 0),
					Blocks.BOOKSHELF.getDefaultState(),
					null
			));
			structure.add(new BlockInfo(
					new BlockPos(2, 0, 0),
					getDemoConnector().getDefaultState().with(FACING_ALL, Direction.WEST),
					null
			));
		}
		return structure;
	}

	private void moveConnectionsToMaster(Collection<Connection> conns, ConnectionPoint oldCommon, World world,
										 ConnectionPoint newCommon)
	{
		for(Connection c : ImmutableSet.copyOf(conns))
			ApiUtils.moveConnectionEnd(c, oldCommon, newCommon, world);
	}

	@Nullable
	private FeedthroughTileEntity setBlock(World world, BlockPos here, BlockState newState, WireType wire, BlockState middle,
										   int offset)
	{
		world.setBlockState(here, newState);
		TileEntity te = world.getTileEntity(here);
		if(te instanceof FeedthroughTileEntity)
		{
			FeedthroughTileEntity feedthrough = (FeedthroughTileEntity)te;
			feedthrough.reference = wire;
			feedthrough.stateForMiddle = middle;
			feedthrough.offset = offset;
			world.addBlockEvent(here, feedthrough.getBlockState().getBlock(), 253, 0);
			return feedthrough;
		}
		return null;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{
				new ItemStack(getDemoConnector(), 2),
				new ItemStack(Blocks.BOOKSHELF, 1).setDisplayName(ARBITRARY_SOLID)
		};
	}

	@Override
	public boolean overwriteBlockRender(BlockState state, int iterator)
	{
		return false;
	}
}
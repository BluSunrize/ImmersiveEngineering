/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks.MultiblockManualData;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import blusunrize.immersiveengineering.client.utils.FeedthroughManualData;
import blusunrize.immersiveengineering.common.blocks.metal.FeedthroughBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks.Connectors;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.shapes.Shapes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.api.IEProperties.FACING_ALL;
public class FeedthroughMultiblock implements IMultiblock
{
	public static FeedthroughMultiblock instance = new FeedthroughMultiblock();
	static List<StructureBlockInfo> structure = new ArrayList<>();

	public static Block getDemoConnector()
	{
		return Connectors.getEnergyConnector(WireType.LV_CATEGORY, false).get();
	}

	@Override
	public Vec3i getSize(@Nullable Level world)
	{
		return new Vec3i(3, 1, 1);
	}

	@Override
	public void disassemble(Level world, BlockPos startPos, boolean mirrored, Direction clickDirectionAtCreation)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public BlockPos getTriggerOffset()
	{
		return new BlockPos(-1, 0, 0);
	}

	@Override
	public void initializeClient(Consumer<MultiblockManualData> consumer)
	{
		consumer.accept(new FeedthroughManualData());
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public ResourceLocation getUniqueName()
	{
		return IEApi.ieLoc("feedthrough");
	}

	@Override
	public boolean isBlockTrigger(BlockState state, Direction side, @Nullable Level world)
	{
		return WireApi.getWireType(state)!=null;
	}

	@Nullable
	WireType checkValidConnector(Level w, BlockPos pos, GlobalWireNetwork globalNet, Direction expectedDirection)
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
		if(!state.hasProperty(FACING_ALL)||state.getValue(FACING_ALL)!=expectedDirection)
			return null;
		return WireApi.getWireType(state);
	}

	@Override
	public boolean createStructure(Level world, BlockPos pos, Direction side, Player player)
	{
		GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(world);
		//Check
		BlockState stateHere = world.getBlockState(pos);
		if(stateHere.hasProperty(FACING_ALL))
			side = stateHere.getValue(FACING_ALL);
		WireType wire = checkValidConnector(world, pos, globalNet, side);
		if(wire==null)
			return false;
		BlockPos middlePos = pos.relative(side);
		BlockState middle = world.getBlockState(middlePos);
		if(!middle.getShape(world, pos).equals(Shapes.block())
				||middle.getBlock() instanceof EntityBlock
				||middle.getRenderShape()!=RenderShape.MODEL)
			return false;
		BlockPos otherPos = pos.relative(side, 2);
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
		if(!world.isClientSide)
		{
			BlockState state = Connectors.FEEDTHROUGH.defaultBlockState().setValue(FACING_ALL, side);
			BlockPos masterPos = pos.relative(side);
			FeedthroughBlockEntity master = setBlock(world, masterPos, state, wire, middle, 0);
			if(master!=null)
			{
				moveConnectionsToMaster(connsOther, cpOther, world, master.getPositivePoint());
				moveConnectionsToMaster(connsHere, cpHere, world, master.getNegativePoint());
			}
			setBlock(world, pos, state, wire, middle, -1);
			setBlock(world, pos.relative(side, 2), state, wire, middle, 1);
		}
		return true;
	}

	@Override
	public List<StructureBlockInfo> getStructure(@Nullable Level world)
	{
		if(structure.isEmpty())
		{
			//Along x axis
			structure.add(new StructureBlockInfo(
					BlockPos.ZERO,
					getDemoConnector().defaultBlockState().setValue(FACING_ALL, Direction.EAST),
					null
			));
			structure.add(new StructureBlockInfo(
					new BlockPos(1, 0, 0),
					Blocks.BOOKSHELF.defaultBlockState(),
					null
			));
			structure.add(new StructureBlockInfo(
					new BlockPos(2, 0, 0),
					getDemoConnector().defaultBlockState().setValue(FACING_ALL, Direction.WEST),
					null
			));
		}
		return structure;
	}

	private void moveConnectionsToMaster(Collection<Connection> conns, ConnectionPoint oldCommon, Level world,
										 ConnectionPoint newCommon)
	{
		for(Connection c : ImmutableSet.copyOf(conns))
			WireUtils.moveConnectionEnd(c, oldCommon, newCommon, world);
	}

	@Nullable
	private FeedthroughBlockEntity setBlock(Level world, BlockPos here, BlockState newState, WireType wire, BlockState middle,
											int offset)
	{
		world.setBlockAndUpdate(here, newState);
		BlockEntity te = world.getBlockEntity(here);
		if(te instanceof FeedthroughBlockEntity feedthrough)
		{
			feedthrough.reference = wire;
			feedthrough.stateForMiddle = middle;
			feedthrough.offset = offset;
			world.blockEvent(here, feedthrough.getBlockState().getBlock(), 253, 0);
			return feedthrough;
		}
		return null;
	}

	@Override
	public Component getDisplayName()
	{
		return Connectors.FEEDTHROUGH.get().getName();
	}
}
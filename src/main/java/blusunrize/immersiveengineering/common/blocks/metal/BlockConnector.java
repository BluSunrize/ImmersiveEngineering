/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.Connection;
import blusunrize.immersiveengineering.api.energy.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.energy.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public abstract class BlockConnector extends BlockIETileProvider
{

	public BlockConnector(String name)
	{
		super(name, Block.Properties.create(Material.IRON).
						hardnessAndResistance(3.0F, 15.0F),
				ItemBlockIEBase.class, IEProperties.FACING_ALL);
		lightOpacity = 0;
		setBlockLayer(BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		setNotNormalBlock();
		/*setMetaBlockLayer(BlockTypes_Connector.CONNECTOR_PROBE.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.CUTOUT, BlockRenderLayer.TRANSLUCENT);
		setMetaBlockLayer(BlockTypes_Connector.FEEDTHROUGH.getMeta(), BlockRenderLayer.SOLID,
				BlockRenderLayer.CUTOUT, BlockRenderLayer.CUTOUT_MIPPED, BlockRenderLayer.TRANSLUCENT);
		setMetaMobilityFlag(BlockTypes_Connector.TRANSFORMER.getMeta(), EnumPushReaction.BLOCK);
		setMetaMobilityFlag(BlockTypes_Connector.TRANSFORMER_HV.getMeta(), EnumPushReaction.BLOCK);
		setMetaMobilityFlag(BlockTypes_Connector.ENERGY_METER.getMeta(), EnumPushReaction.BLOCK);
		setMetaMobilityFlag(BlockTypes_Connector.FEEDTHROUGH.getMeta(), EnumPushReaction.BLOCK);*/
	}

		/*
		TODO when unlisted properties are back
	@Override
	protected void fillStateContainer(Builder<Block, IBlockState> builder)
	{
		IUnlistedProperty[] unlisted = (base instanceof ExtendedBlockState)?((ExtendedBlockState)base).getUnlistedProperties().toArray(new IUnlistedProperty[0]): new IUnlistedProperty[0];
		unlisted = Arrays.copyOf(unlisted, unlisted.length+1);
		unlisted[unlisted.length-1] = IEProperties.CONNECTIONS;
		return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), unlisted);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getExtendedState(state, world, pos);
		if(state instanceof IExtendedBlockState)
		{
			IExtendedBlockState ext = (IExtendedBlockState)state;
			TileEntity te = world.getTileEntity(pos);
			if(!(te instanceof TileEntityImmersiveConnectable))
				return state;
			state = ext.with(IEProperties.CONNECTIONS, ((TileEntityImmersiveConnectable)te).genConnBlockstate());
		}
		return state;
	}
	*/

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos)
	{
		super.neighborChanged(state, world, pos, blockIn, fromPos);
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityConnector)
		{
			TileEntityConnector connector = (TileEntityConnector)te;
			if(world.isAirBlock(pos.offset(connector.facing)))
			{
				spawnAsEntity(world, pos, new ItemStack(this));
				connector.getWorld().removeBlock(pos);
			}
		}
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, EntityPlayer player)
	{
		//Select the wire if the player is sneaking
		//TODO alternative to world instaceof World
		if(player!=null&&player.isSneaking()&&world instanceof World)
		{
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof TileEntityImmersiveConnectable)
			{
				TargetingInfo subTarget = null;
				if(target.hitVec!=null)
					subTarget = new TargetingInfo(target.sideHit, (float)target.hitVec.x-pos.getX(), (float)target.hitVec.y-pos.getY(), (float)target.hitVec.z-pos.getZ());
				else
					subTarget = new TargetingInfo(target.sideHit, 0, 0, 0);
				BlockPos masterPos = ((TileEntityImmersiveConnectable)te).getConnectionMaster(null, subTarget);
				if(masterPos!=pos)
					te = world.getTileEntity(masterPos);
				if(te instanceof TileEntityImmersiveConnectable)
				{
					ConnectionPoint cp = ((TileEntityImmersiveConnectable)te).getTargetedPoint(subTarget, masterPos.subtract(pos));
					if(cp!=null)
						for(Connection c : GlobalWireNetwork.getNetwork((World)world).getLocalNet(cp).getConnections(cp))
							if(!c.isInternal())
								return c.type.getWireCoil();
				}
			}
		}
		return super.getPickBlock(state, target, world, pos, player);
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}
}
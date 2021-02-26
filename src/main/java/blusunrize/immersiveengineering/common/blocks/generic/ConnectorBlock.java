/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import blusunrize.immersiveengineering.common.blocks.metal.EnergyConnectorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public abstract class ConnectorBlock extends IETileProviderBlock
{
	public ConnectorBlock(String name, BiFunction<Block, Item.Properties, Item> item, Property... additional)
	{
		super(name, Block.Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(3.0F, 15.0F).notSolid(),
				item, additional);
		lightOpacity = 0;
		setMobility(PushReaction.BLOCK);
	}

	public ConnectorBlock(String name, Property... additional)
	{
		this(name, BlockItemIE::new, additional);
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
	{
		super.neighborChanged(state, world, pos, block, fromPos, isMoving);
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof EnergyConnectorTileEntity)
		{
			EnergyConnectorTileEntity connector = (EnergyConnectorTileEntity)te;
			if(world.isAirBlock(pos.offset(connector.getFacing())))
			{
				spawnAsEntity(world, pos, new ItemStack(this));
				connector.getWorldNonnull().removeBlock(pos, false);
			}
		}
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult targetIn, IBlockReader world, BlockPos pos, PlayerEntity player)
	{
		//Select the wire if the player is sneaking
		//TODO alternative to world instaceof World
		if(player!=null&&player.isSneaking()&&world instanceof World&&targetIn instanceof BlockRayTraceResult)
		{
			BlockRayTraceResult target = (BlockRayTraceResult)targetIn;
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof IImmersiveConnectable)
			{
				TargetingInfo subTarget = new TargetingInfo(target.getFace(), (float)target.getHitVec().x-pos.getX(),
						(float)target.getHitVec().y-pos.getY(), (float)target.getHitVec().z-pos.getZ());
				BlockPos masterPos = ((IImmersiveConnectable)te).getConnectionMaster(null, subTarget);
				if(masterPos!=pos)
					te = world.getTileEntity(masterPos);
				if(te instanceof IImmersiveConnectable)
				{
					ConnectionPoint cp = ((IImmersiveConnectable)te).getTargetedPoint(subTarget, masterPos.subtract(pos));
					if(cp!=null)
						for(Connection c : GlobalWireNetwork.getNetwork((World)world).getLocalNet(cp).getConnections(cp))
							if(!c.isInternal())
								return c.type.getWireCoil(c);
				}
			}
		}
		return super.getPickBlock(state, targetIn, world, pos, player);
	}

	@Override
	public boolean allowHammerHarvest(BlockState state)
	{
		return true;
	}
}
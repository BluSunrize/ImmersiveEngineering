/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import blusunrize.immersiveengineering.common.blocks.metal.EnergyConnectorTileEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fml.RegistryObject;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ConnectorBlock<T extends BlockEntity & IImmersiveConnectable> extends IETileProviderBlock<T>
{
	public static final EnumProperty<Direction> DEFAULT_FACING_PROP = IEProperties.FACING_ALL;

	public ConnectorBlock(
			String name, Supplier<BlockEntityType<T>> tileType, BiFunction<Block, Item.Properties, Item> item, Consumer<BlockBehaviour.Properties> extraSetup
	)
	{
		super(name, tileType, Util.make(
				Block.Properties.of(Material.METAL)
						.sound(SoundType.METAL)
						.strength(3.0F, 15.0F)
						.noOcclusion(),
				extraSetup), item);
		lightOpacity = 0;
		setMobility(PushReaction.BLOCK);
	}

	public ConnectorBlock(String name, RegistryObject<BlockEntityType<T>> tileType)
	{
		this(name, tileType, BlockItemIE::new);
	}

	public ConnectorBlock(String name, Consumer<Properties> extraSetup, RegistryObject<BlockEntityType<T>> tileType)
	{
		this(name, tileType, BlockItemIE::new, extraSetup);
	}

	public ConnectorBlock(String name, RegistryObject<BlockEntityType<T>> tileType, BiFunction<Block, Item.Properties, Item> itemClass)
	{
		this(name, tileType, itemClass, $ -> {});
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
	{
		super.neighborChanged(state, world, pos, block, fromPos, isMoving);
		BlockEntity te = world.getBlockEntity(pos);
		if(te instanceof EnergyConnectorTileEntity)
		{
			EnergyConnectorTileEntity connector = (EnergyConnectorTileEntity)te;
			if(world.isEmptyBlock(pos.relative(connector.getFacing())))
			{
				popResource(world, pos, new ItemStack(this));
				connector.getWorldNonnull().removeBlock(pos, false);
			}
		}
	}

	@Override
	public ItemStack getPickBlock(BlockState state, HitResult targetIn, BlockGetter world, BlockPos pos, Player player)
	{
		//Select the wire if the player is sneaking
		//TODO alternative to world instaceof World
		if(player!=null&&player.isShiftKeyDown()&&world instanceof Level&&targetIn instanceof BlockHitResult)
		{
			BlockHitResult target = (BlockHitResult)targetIn;
			BlockEntity te = world.getBlockEntity(pos);
			if(te instanceof IImmersiveConnectable)
			{
				TargetingInfo subTarget = new TargetingInfo(target.getDirection(), (float)target.getLocation().x-pos.getX(),
						(float)target.getLocation().y-pos.getY(), (float)target.getLocation().z-pos.getZ());
				BlockPos masterPos = ((IImmersiveConnectable)te).getConnectionMaster(null, subTarget);
				if(masterPos!=pos)
					te = world.getBlockEntity(masterPos);
				if(te instanceof IImmersiveConnectable)
				{
					ConnectionPoint cp = ((IImmersiveConnectable)te).getTargetedPoint(subTarget, masterPos.subtract(pos));
					if(cp!=null)
						for(Connection c : GlobalWireNetwork.getNetwork((Level)world).getLocalNet(cp).getConnections(cp))
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
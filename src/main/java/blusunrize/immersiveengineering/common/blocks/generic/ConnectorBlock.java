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
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.blocks.metal.EnergyConnectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class ConnectorBlock<T extends BlockEntity & IImmersiveConnectable> extends IEEntityBlock<T>
{
	public static final Supplier<Properties> PROPERTIES = () -> Block.Properties.of()
			.mapColor(MapColor.METAL)
			.sound(SoundType.METAL)
			.strength(3.0F, 15.0F)
			.noOcclusion()
			.dynamicShape();
	public static final EnumProperty<Direction> DEFAULT_FACING_PROP = IEProperties.FACING_ALL;

	public ConnectorBlock(Properties props, RegistryObject<BlockEntityType<T>> entityType)
	{
		super(entityType, props.pushReaction(PushReaction.BLOCK));
		lightOpacity = 0;
	}

	public ConnectorBlock(Properties props, BiFunction<BlockPos, BlockState, T> entityType)
	{
		super(entityType, props.pushReaction(PushReaction.BLOCK));
		lightOpacity = 0;
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
	{
		super.neighborChanged(state, world, pos, block, fromPos, isMoving);
		BlockEntity te = world.getBlockEntity(pos);
		if(te instanceof EnergyConnectorBlockEntity)
		{
			EnergyConnectorBlockEntity connector = (EnergyConnectorBlockEntity)te;
			if(world.isEmptyBlock(pos.relative(connector.getFacing())))
			{
				popResource(world, pos, new ItemStack(this));
				connector.getLevelNonnull().removeBlock(pos, false);
			}
		}
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult targetIn, BlockGetter world, BlockPos pos, Player player)
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
		return super.getCloneItemStack(state, targetIn, world, pos, player);
	}
}
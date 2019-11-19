/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.localhandlers;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.WireCollisionData.CollisionInfo;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.IEnergyWire;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class WireDamageHandler extends LocalNetworkHandler implements ICollisionHandler
{
	public static final ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "wire_damage");

	private static final double KNOCKBACK_PER_DAMAGE = 10;

	public WireDamageHandler(LocalWireNetwork net)
	{
		super(net);
	}

	@Override
	public void onCollided(LivingEntity e, BlockPos pos, CollisionInfo info)
	{
		WireType wType = info.conn.type;
		if(!(wType instanceof IShockingWire))
			return;
		IShockingWire shockWire = (IShockingWire)wType;
		double extra = shockWire.getDamageRadius();
		AxisAlignedBB eAabb = e.getBoundingBox();
		AxisAlignedBB includingExtra = eAabb.grow(extra).offset(-pos.getX(), -pos.getY(), -pos.getZ());
		boolean endpointsInEntity = includingExtra.contains(info.intersectA)||includingExtra.contains(info.intersectB);
		Optional<Vec3d> rayRes;
		if(endpointsInEntity)
			rayRes = Optional.empty();
		else
			rayRes = includingExtra.rayTrace(info.intersectA, info.intersectB);
		if(endpointsInEntity||rayRes.isPresent())
		{
			//TODO proper damage calculations
			float damage = 5;
			if(damage!=0)
			{
				IEDamageSources.ElectricDamageSource dmg = IEDamageSources.causeWireDamage(damage, shockWire.getElectricSource());
				if(dmg.apply(e))
				{
					damage = dmg.dmg;
					Vec3d v = e.getLookVec();
					ApiUtils.knockbackNoSource(e, damage/KNOCKBACK_PER_DAMAGE, v.x, v.z);
				}
			}
		}
	}

	@Override
	public LocalNetworkHandler merge(LocalNetworkHandler other)
	{
		return this;
	}

	@Override
	public void onConnectorLoaded(ConnectionPoint p, IImmersiveConnectable iic)
	{

	}

	@Override
	public void onConnectorUnloaded(BlockPos p, IImmersiveConnectable iic)
	{

	}

	@Override
	public void onConnectorRemoved(BlockPos p, IImmersiveConnectable iic)
	{

	}

	@Override
	public void onConnectionAdded(Connection c)
	{

	}

	@Override
	public void onConnectionRemoved(Connection c)
	{

	}

	public interface IShockingWire extends IEnergyWire
	{
		/**
		 * @return The radius around this wire where entities should be damaged if it is enabled in the config. Must be
		 * less that DELTA_NEAR in blusunrize.immersiveengineering.api.ApiUtils.handleVec (currently .3)
		 */
		double getDamageRadius();

		IElectricEquipment.ElectricSource getElectricSource();

		//TODO use and change parameters to be useful
		default float getDamageAmount(Entity e, Connection c)
		{
			return 0;
		}
	}
}

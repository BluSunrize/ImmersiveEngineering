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
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.IEnergyWire;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.Path;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
		EnergyTransferHandler energyHandler = getEnergyHandler();
		if(energyHandler==null)
			return;
		IShockingWire shockWire = (IShockingWire)wType;
		double extra = shockWire.getDamageRadius();
		AxisAlignedBB eAabb = e.getBoundingBox();
		AxisAlignedBB includingExtra = eAabb.grow(extra).offset(-pos.getX(), -pos.getY(), -pos.getZ());
		boolean endpointsInEntity = includingExtra.contains(info.intersectA)||includingExtra.contains(info.intersectB);
		Optional<Vector3d> rayRes;
		if(endpointsInEntity)
			rayRes = Optional.empty();
		else
			rayRes = includingExtra.rayTrace(info.intersectA, info.intersectB);
		Map<ConnectionPoint, EnergyConnector> sources = energyHandler.getSources();
		if(!sources.isEmpty()&&(endpointsInEntity||rayRes.isPresent()))
		{
			Object2IntMap<ConnectionPoint> available = getAvailableEnergy(sources);
			Map<ConnectionPoint, Path> paths = new HashMap<>();
			int totalAvailable = 0;
			ConnectionPoint target = info.conn.getEndA();//TODO less random choice?
			for(Object2IntMap.Entry<ConnectionPoint> entry : available.object2IntEntrySet())
			{
				Path path = energyHandler.getPath(entry.getKey(), target);
				if(path!=null)
				{
					totalAvailable += entry.getIntValue()*(1-path.loss);
					paths.put(entry.getKey(), path);
				}
			}
			totalAvailable = Math.min(totalAvailable, shockWire.getTransferRate());

			final float maxPossibleDamage = shockWire.getDamageAmount(e, info.conn, totalAvailable);
			if(maxPossibleDamage > 0)
			{
				IEDamageSources.ElectricDamageSource dmg =
						IEDamageSources.causeWireDamage(maxPossibleDamage, shockWire.getElectricSource());
				if(dmg.apply(e))
				{
					final float actualDamage = dmg.dmg;
					Vector3d v = e.getLookVec();
					ApiUtils.knockbackNoSource(e, actualDamage/KNOCKBACK_PER_DAMAGE, v.x, v.z);
					//Consume energy
					double factor = actualDamage/maxPossibleDamage;
					Object2DoubleMap<Connection> transferred = energyHandler.getTransferredNextTick();
					for(Object2IntMap.Entry<ConnectionPoint> entry : available.object2IntEntrySet())
					{
						Path path = paths.get(entry.getKey());
						int availableFromSource = entry.getIntValue();
						double energyFromSource = availableFromSource*factor;
						EnergyConnector source = sources.get(entry.getKey());
						source.extractEnergy(MathHelper.ceil(energyFromSource));
						for(Connection c : path.conns)
							transferred.mergeDouble(c, energyFromSource, Double::sum);
					}
				}
			}
		}
	}

	private Object2IntMap<ConnectionPoint> getAvailableEnergy(Map<ConnectionPoint, EnergyConnector> sources)
	{
		Object2IntMap<ConnectionPoint> ret = new Object2IntOpenHashMap<>();
		for(Entry<ConnectionPoint, EnergyConnector> c : sources.entrySet())
		{
			int energy = c.getValue().getAvailableEnergy();
			if(energy > 0)
				ret.put(c.getKey(), energy);
		}
		return ret;
	}

	private EnergyTransferHandler getEnergyHandler()
	{
		return net.getHandler(EnergyTransferHandler.ID, EnergyTransferHandler.class);
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

		default float getDamageAmount(Entity e, Connection c, int energy)
		{
			return 0;
		}
	}
}

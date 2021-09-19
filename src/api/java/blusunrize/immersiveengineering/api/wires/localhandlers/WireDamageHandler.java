/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.localhandlers;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment.ElectricSource;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.WireCollisionData.CollisionInfo;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.IEnergyWire;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.Path;
import blusunrize.immersiveengineering.api.wires.utils.IElectricDamageSource;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

public class WireDamageHandler extends LocalNetworkHandler implements ICollisionHandler
{
	public static final ResourceLocation ID = new ResourceLocation(Lib.MODID, "wire_damage");
	public static final SetRestrictedField<BiFunction<Float, ElectricSource, IElectricDamageSource>> GET_WIRE_DAMAGE
			= SetRestrictedField.common();

	private static final double KNOCKBACK_PER_DAMAGE = 10;

	public WireDamageHandler(LocalWireNetwork net, GlobalWireNetwork global)
	{
		super(net, global);
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
		AABB eAabb = e.getBoundingBox();
		AABB includingExtra = eAabb.inflate(extra).move(-pos.getX(), -pos.getY(), -pos.getZ());
		boolean collides = includingExtra.contains(info.intersectA)||includingExtra.contains(info.intersectB);
		if(!collides&&!includingExtra.clip(info.intersectA, info.intersectB).isPresent())
			return;
		final ConnectionPoint target = info.conn.getEndA();//TODO less random choice?
		final List<SourceData> available = getAvailableEnergy(energyHandler, target);
		if(available.isEmpty())
			return;
		int totalAvailable = 0;
		for(SourceData source : available)
			totalAvailable += source.amountAvailable*(1-source.pathToSource.loss);
		totalAvailable = Math.min(totalAvailable, shockWire.getTransferRate());

		final float maxPossibleDamage = shockWire.getDamageAmount(e, info.conn, totalAvailable);
		if(maxPossibleDamage <= 0)
			return;
		IElectricDamageSource dmg = GET_WIRE_DAMAGE.getValue()
				.apply(maxPossibleDamage, shockWire.getElectricSource());
		if(!dmg.apply(e))
			return;
		final float actualDamage = dmg.getDamage();
		Vec3 v = e.getLookAngle();
		ApiUtils.knockbackNoSource(e, actualDamage/KNOCKBACK_PER_DAMAGE, v.x, v.z);
		//Consume energy
		final double factor = actualDamage/maxPossibleDamage;
		Object2DoubleMap<Connection> transferred = energyHandler.getTransferredNextTick();
		for(SourceData source : available)
		{
			final double energyFromSource = source.amountAvailable*factor;
			source.source.extractEnergy(Mth.ceil(energyFromSource));
			for(Connection c : source.pathToSource.conns)
				transferred.mergeDouble(c, energyFromSource, Double::sum);
		}
	}

	private List<SourceData> getAvailableEnergy(EnergyTransferHandler energyHandler, ConnectionPoint target)
	{
		List<SourceData> ret = new ArrayList<>();
		Map<ConnectionPoint, Path> paths = null;
		for(Entry<ConnectionPoint, EnergyConnector> c : energyHandler.getSources().entrySet())
		{
			final int energy = c.getValue().getAvailableEnergy();
			if(energy <= 0)
				continue;
			if(paths==null)
				paths = energyHandler.getPathsFromSource(target);
			final Path path = paths.get(c.getKey());
			if(path!=null)
				ret.add(new SourceData(energy, path, c.getValue(), c.getKey()));
		}
		return ret;
	}

	private static class SourceData
	{
		private final int amountAvailable;
		private final Path pathToSource;
		private final EnergyConnector source;
		private final ConnectionPoint point;

		public SourceData(int amount, Path path, EnergyConnector source, ConnectionPoint point)
		{
			this.amountAvailable = amount;
			this.pathToSource = path;
			this.source = source;
			this.point = point;
		}
	}

	private EnergyTransferHandler getEnergyHandler()
	{
		return localNet.getHandler(EnergyTransferHandler.ID, EnergyTransferHandler.class);
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

/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.wires;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.WireApi;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.IEnergyWire;
import blusunrize.immersiveengineering.api.wires.localhandlers.WireDamageHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.WireDamageHandler.IShockingWire;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Wires.EnergyWireConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Wires.WireConfig;
import blusunrize.immersiveengineering.common.register.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.api.wires.WireApi.registerFeedthroughForWiretype;
import static blusunrize.immersiveengineering.api.wires.WireType.*;
import static blusunrize.immersiveengineering.common.config.IEServerConfig.WIRES;

public class IEWireTypes
{
	public static double[] renderDiameter = {.03125, .03125, .0625, .0625, .0625, .03125};
	public static ShockingWire COPPER;
	public static ShockingWire ELECTRUM;
	public static ShockingWire STEEL;
	public static WireType STRUCTURE_ROPE;
	public static WireType STRUCTURE_STEEL;
	public static WireType REDSTONE;
	public static EnergyWire COPPER_INSULATED;
	public static EnergyWire ELECTRUM_INSULATED;
	public static InternalConnection INTERNAL_CONNECTION;

	public static void modConstruction()
	{
		WireType.COPPER = COPPER = new ShockingWire(IEWireType.COPPER);
		WireType.ELECTRUM = ELECTRUM = new ShockingWire(IEWireType.ELECTRUM);
		WireType.STEEL = STEEL = new ShockingWire(IEWireType.STEEL);
		WireType.STRUCTURE_ROPE = STRUCTURE_ROPE = new BasicWire(IEWireType.STRUCTURE_ROPE);
		WireType.STRUCTURE_STEEL = STRUCTURE_STEEL = new BasicWire(IEWireType.STRUCTURE_STEEL);
		WireType.REDSTONE = REDSTONE = new BasicWire(IEWireType.REDSTONE);
		WireType.COPPER_INSULATED = COPPER_INSULATED = new EnergyWire(IEWireType.COPPER_INSULATED);
		WireType.ELECTRUM_INSULATED = ELECTRUM_INSULATED = new EnergyWire(IEWireType.ELECTRUM_INSULATED);
		WireType.INTERNAL_CONNECTION = INTERNAL_CONNECTION = new InternalConnection();
	}

	public static void setup()
	{
		registerFeedthroughForWiretype(COPPER, IEApi.ieLoc("block/connector/connector_lv"),
				new double[]{0, 4, 8, 12}, .5,
				Connectors.getEnergyConnector(LV_CATEGORY, false).defaultBlockState());
		registerFeedthroughForWiretype(ELECTRUM, IEApi.ieLoc("block/connector/connector_mv"),
				new double[]{0, 4, 8, 12}, .5625,
				Connectors.getEnergyConnector(MV_CATEGORY, false).defaultBlockState());
		registerFeedthroughForWiretype(STEEL, IEApi.ieLoc("block/connector/connector_hv"),
				new double[]{0, 4, 8, 12}, .75,
				Connectors.getEnergyConnector(HV_CATEGORY, false).defaultBlockState());
		registerFeedthroughForWiretype(REDSTONE, IEApi.ieLoc("block/connector/connector_redstone"),
				new double[]{3, 8, 11, 16}, .5625, .5,
				Connectors.CONNECTOR_REDSTONE.defaultBlockState()
		);
	}

	public enum IEWireType
	{
		COPPER("COPPER", null),
		ELECTRUM("ELECTRUM", null),
		STEEL("STEEL", null),
		STRUCTURE_ROPE("STRUCTURE_ROPE", null),
		STRUCTURE_STEEL("STRUCTURE_STEEL", null),
		REDSTONE("REDSTONE", null),
		COPPER_INSULATED("COPPER_INS", COPPER),
		ELECTRUM_INSULATED("ELECTRUM_INS", ELECTRUM);
		public final String uniqueName;
		public final IEWireType energyBaseType;

		IEWireType(String name, @Nullable IEWireType base)
		{
			this.uniqueName = name;
			if(base!=null)
				this.energyBaseType = base;
			else
				this.energyBaseType = this;
		}
	}

	private static class BasicWire extends WireType
	{
		final IEWireType type;
		final WireConfig config;
		final IntValue color;

		public BasicWire(IEWireType type)
		{
			super();
			this.type = type;
			WireApi.registerWireType(this);
			this.config = WIRES.wireConfigs.get(type);
			this.color = IEClientConfig.wireColors.get(type);
		}

		@Override
		public int getColour(Connection connection)
		{
			return color.get();
		}

		@Override
		public double getSlack()
		{
			return 1.005;
		}

		@Override
		public int getMaxLength()
		{
			return config.maxLength.get();
		}

		@Override
		public ItemStack getWireCoil(Connection con)
		{
			return new ItemStack(Misc.WIRE_COILS.get(this), 1);
		}

		@Override
		public String getUniqueName()
		{
			return type.uniqueName;
		}

		@Override
		public double getRenderDiameter()
		{
			return renderDiameter[type.ordinal()%6];
		}

		@Nonnull
		@Override
		public String getCategory()
		{
			return switch(type)
					{
						case COPPER, COPPER_INSULATED -> LV_CATEGORY;
						case ELECTRUM, ELECTRUM_INSULATED -> MV_CATEGORY;
						case STEEL -> HV_CATEGORY;
						case STRUCTURE_ROPE, STRUCTURE_STEEL -> STRUCTURE_CATEGORY;
						case REDSTONE -> REDSTONE_CATEGORY;
					};
		}

		@Override
		public Collection<ResourceLocation> getRequestedHandlers()
		{
			return ImmutableList.of();
		}
	}

	private static class EnergyWire extends BasicWire implements IEnergyWire
	{
		private final EnergyWireConfig config;

		public EnergyWire(IEWireType type)
		{
			super(type);
			this.config = WIRES.energyWireConfigs.get(type.energyBaseType);
		}

		public double getLossRatio()
		{
			return config.lossRatio.get();
		}

		@Override
		public int getTransferRate()
		{
			return config.transferRate.get();
		}

		@Override
		public double getBasicLossRate(Connection c)
		{
			return getLossRatio()*c.getLength()/getMaxLength();
		}

		@Override
		public double getLossRate(Connection c, int transferred)
		{
			return 0;
		}
	}

	private static class ShockingWire extends EnergyWire implements IShockingWire
	{
		private final IElectricEquipment.ElectricSource eSource;

		public ShockingWire(IEWireType type)
		{
			super(type);
			if(getDamageRadius() > 0)
				eSource = new IElectricEquipment.ElectricSource(.5F*(1+type.ordinal()));
			else
				eSource = new IElectricEquipment.ElectricSource(-1);
		}

		@Override
		public double getDamageRadius()
		{
			return switch(type)
					{
						case COPPER -> .05;
						case ELECTRUM -> .1;
						case STEEL -> .3;
						default -> 0;
					};
		}

		@Override
		public IElectricEquipment.ElectricSource getElectricSource()
		{
			return eSource;
		}

		@Override
		public float getDamageAmount(Entity e, Connection c, int energy)
		{
			float factor;
			if(this==COPPER)
				factor = 2F;
			else if(this==ELECTRUM)
				factor = 5F;
			else if(this==STEEL)
				factor = 15F;
			else
				throw new UnsupportedOperationException("");
			return factor*energy/getTransferRate()*8;
		}

		@Override
		public Collection<ResourceLocation> getRequestedHandlers()
		{
			return ImmutableList.of(WireDamageHandler.ID);
		}
	}

	private static class InternalConnection extends WireType
	{

		@Override
		public String getUniqueName()
		{
			return "INTERNAL";
		}

		@Override
		public int getColour(Connection connection)
		{
			return 0;
		}

		@Override
		public double getSlack()
		{
			return 1.001;
		}

		@Override
		public int getMaxLength()
		{
			return 0;
		}

		@Override
		public ItemStack getWireCoil(Connection con)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public double getRenderDiameter()
		{
			return 0;
		}

		@Nonnull
		@Override
		public String getCategory()
		{
			return "INTERNAL";
		}
	}
}

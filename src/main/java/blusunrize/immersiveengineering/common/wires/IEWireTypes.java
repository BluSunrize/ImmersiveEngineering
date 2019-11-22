/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.wires;

import blusunrize.immersiveengineering.api.tool.IElectricEquipment;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.WireApi;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.IEnergyWire;
import blusunrize.immersiveengineering.api.wires.localhandlers.WireDamageHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.WireDamageHandler.IShockingWire;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Collection;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.api.wires.WireApi.registerFeedthroughForWiretype;
import static blusunrize.immersiveengineering.api.wires.WireType.*;
import static blusunrize.immersiveengineering.common.IEConfig.WIRES;

public class IEWireTypes
{
	public static String[] uniqueNames = {"COPPER", "ELECTRUM", "STEEL", "STRUCTURE_ROPE", "STRUCTURE_STEEL", "REDSTONE",
			"COPPER_INS", "ELECTRUM_INS"};
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
		WireType.COPPER = COPPER = new ShockingWire(0);
		WireType.ELECTRUM = ELECTRUM = new ShockingWire(1);
		WireType.STEEL = STEEL = new ShockingWire(2);
		WireType.STRUCTURE_ROPE = STRUCTURE_ROPE = new BasicWire(3);
		WireType.STRUCTURE_STEEL = STRUCTURE_STEEL = new BasicWire(4);
		WireType.REDSTONE = REDSTONE = new BasicWire(5);
		WireType.COPPER_INSULATED = COPPER_INSULATED = new EnergyWire(6);
		WireType.ELECTRUM_INSULATED = ELECTRUM_INSULATED = new EnergyWire(7);
		WireType.INTERNAL_CONNECTION = INTERNAL_CONNECTION = new InternalConnection();
	}

	public static void setup()
	{
		registerFeedthroughForWiretype(COPPER, new ResourceLocation(MODID, "block/connector/connector_lv.obj"),
				new ResourceLocation(MODID, "blocks/connector_connector_lv"), new float[]{0, 4, 8, 12},
				.5, Connectors.getEnergyConnector(LV_CATEGORY, false).getDefaultState());
		registerFeedthroughForWiretype(ELECTRUM, new ResourceLocation(MODID, "block/connector/connector_mv.obj"),
				new ResourceLocation(MODID, "blocks/connector_connector_mv"), new float[]{0, 4, 8, 12},
				.5625, Connectors.getEnergyConnector(MV_CATEGORY, false).getDefaultState());
		registerFeedthroughForWiretype(STEEL, new ResourceLocation(MODID, "block/connector/connector_hv.obj"),
				new ResourceLocation(MODID, "blocks/connector_connector_hv"), new float[]{0, 4, 8, 12},
				.75, Connectors.getEnergyConnector(HV_CATEGORY, false).getDefaultState());
		registerFeedthroughForWiretype(REDSTONE, new ResourceLocation(MODID, "block/connector/connector_redstone.obj.ie"),
				ImmutableMap.of(), new ResourceLocation(MODID, "blocks/connector_connector_redstone"), new float[]{3, 8, 11, 16},
				.5625, .5, Connectors.connectorRedstone.getDefaultState()
		);
	}

	private static class BasicWire extends WireType
	{
		final int ordinal;

		public BasicWire(int ordinal)
		{
			super();
			this.ordinal = ordinal;
			WireApi.registerWireType(this);
		}

		@Override
		public int getColour(Connection connection)
		{
			return WIRES.wireColouration.get().get(ordinal);
		}

		@Override
		public double getSlack()
		{
			return 1.005;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public TextureAtlasSprite getIcon(Connection connection)
		{
			return iconDefaultWire;
		}

		@Override
		public int getMaxLength()
		{
			return WIRES.wireLength.get().get(ordinal%6);
		}

		@Override
		public ItemStack getWireCoil(Connection con)
		{
			return new ItemStack(Misc.wireCoils.get(this), 1);
		}

		@Override
		public String getUniqueName()
		{
			return uniqueNames[ordinal];
		}

		@Override
		public double getRenderDiameter()
		{
			return renderDiameter[ordinal%6];
		}

		@Nullable
		@Override
		public String getCategory()
		{
			switch(ordinal)
			{
				case 0:
				case 6:
					return LV_CATEGORY;
				case 1:
				case 7:
					return MV_CATEGORY;
				case 2:
					return HV_CATEGORY;
				case 3:
				case 4:
					return STRUCTURE_CATEGORY;
				case 5:
					return REDSTONE_CATEGORY;
				default:
					return null;
			}
		}

		@Override
		public Collection<ResourceLocation> getRequestedHandlers()
		{
			if(ordinal < 3)
				return ImmutableList.of(WireDamageHandler.ID);
			else
				return ImmutableList.of();
		}
	}

	private static class EnergyWire extends BasicWire implements IEnergyWire
	{

		public EnergyWire(int ordinal)
		{
			super(ordinal);
		}

		public double getLossRatio()
		{
			return Math.abs(WIRES.wireLossRatio.get().get(ordinal%6));
		}

		@Override
		public int getTransferRate()
		{
			return Math.abs(WIRES.wireTransferRate.get().get(ordinal%6));
		}

		@Override
		public double getBasicLossRate(Connection c)
		{
			double length = Math.sqrt(c.getEndA().getPosition().distanceSq(new Vec3d(c.getEndB().getPosition()), false));
			return getLossRatio()*length/getMaxLength();
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

		public ShockingWire(int ordinal)
		{
			super(ordinal);
			if(getDamageRadius() > 0)
				eSource = new IElectricEquipment.ElectricSource(.5F*(1+ordinal));
			else
				eSource = new IElectricEquipment.ElectricSource(-1);
		}

		@Override
		public double getDamageRadius()
		{
			switch(ordinal)
			{
				case 0://LV
					return .05;
				case 1://MV
					return .1;
				case 2://HV
					return .3;
			}
			return 0;
		}

		@Override
		public IElectricEquipment.ElectricSource getElectricSource()
		{
			return eSource;
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
			return 0;
		}

		@Override
		public TextureAtlasSprite getIcon(Connection connection)
		{
			return null;
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
	}
}

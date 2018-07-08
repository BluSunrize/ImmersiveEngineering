/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment;
import blusunrize.immersiveengineering.common.IEContent;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.api.energy.wires.WireApi.registerFeedthroughForWiretype;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Connector.*;

/**
 * @author BluSunrize - 08.03.2015<br>
 * Rewritten: 26.06.2015
 * <br>
 * The WireTypes of IE. Extend this to make your own
 */
public abstract class WireType
{
	public static final String LV_CATEGORY = "LV";
	public static final String MV_CATEGORY = "MV";
	public static final String HV_CATEGORY = "HV";
	public static final String STRUCTURE_CATEGORY = "STRUCTURE";
	public static final String REDSTONE_CATEGORY = "REDSTONE";
	private static LinkedHashSet<WireType> values = new LinkedHashSet<WireType>();

	public static LinkedHashSet<WireType> getValues()
	{
		return values;
	}

	public static WireType getValue(String name)
	{
		for(WireType type : values)
			if(type!=null&&type.getUniqueName().equals(name))
				return type;
		return COPPER;
	}

	public WireType()
	{
		values.add(this);
	}

	public abstract String getUniqueName();

	public abstract double getLossRatio();

	public abstract int getTransferRate();

	/**
	 * Try not to get to complex with determining colour here
	 */
	public abstract int getColour(Connection connection);

	/**
	 * Determines how saggy the wire is
	 */
	public abstract double getSlack();

	@SideOnly(Side.CLIENT)
	public abstract TextureAtlasSprite getIcon(Connection connection);

	public abstract int getMaxLength();

	public abstract ItemStack getWireCoil();

	public ItemStack getWireCoil(Connection c)
	{
		return getWireCoil();
	}

	public abstract double getRenderDiameter();

	public abstract boolean isEnergyWire();

	public boolean canCauseDamage()
	{
		return false;
	}

	/**
	 * Used to determine which other wire types can be on the same connector as this wire (obviously does not apply to transformers)
	 * Returning null will cause the wire to be incompatible with all other wires
	 */
	@Nullable
	public String getCategory()
	{
		return null;
	}

	/**
	 * @return The radius around this wire where entities should be damaged if it is enabled in the config. Must be
	 * less that DELTA_NEAR in blusunrize.immersiveengineering.api.ApiUtils.handleVec (currently .3)
	 */
	public double getDamageRadius()
	{
		return 0;//Don't shock people unless it is explicitely enabled for this wire type
	}

	//THESE VALUES ARE FOR IE's OWN WIRES!
	public static String[] uniqueNames = {"COPPER", "ELECTRUM", "STEEL", "STRUCTURE_ROPE", "STRUCTURE_STEEL", "REDSTONE",
			"COPPER_INS", "ELECTRUM_INS"};
	public static double[] wireLossRatio;
	public static int[] wireTransferRate;
	public static int[] wireColouration;
	public static int[] wireLength;
	public static Item ieWireCoil;
	public static double[] renderDiameter = {.03125, .03125, .0625, .0625, .0625, .03125};
	@SideOnly(Side.CLIENT)
	public static TextureAtlasSprite iconDefaultWire;

	public static WireType COPPER;
	public static WireType ELECTRUM;
	public static WireType STEEL;
	public static WireType STRUCTURE_ROPE;
	public static WireType STRUCTURE_STEEL;
	public static WireType REDSTONE;
	public static WireType COPPER_INSULATED;
	public static WireType ELECTRUM_INSULATED;

	public static void init()
	{
		COPPER = new IEBASE(0);
		ELECTRUM = new IEBASE(1);
		STEEL = new IEBASE(2);
		STRUCTURE_ROPE = new IEBASE(3);
		STRUCTURE_STEEL = new IEBASE(4);
		REDSTONE = new IEBASE(5);
		COPPER_INSULATED = new IEBASE(6);
		ELECTRUM_INSULATED = new IEBASE(7);
		registerFeedthroughForWiretype(COPPER, new ResourceLocation(MODID, "block/connector/connector_lv.obj"),
				new ResourceLocation(MODID, "blocks/connector_connector_lv"), new float[]{0, 4, 8, 12},
				.5, IEContent.blockConnectors.getStateFromMeta(CONNECTOR_LV.getMeta()),
				8*2F/COPPER.getTransferRate(), 2, (f) -> f);
		registerFeedthroughForWiretype(ELECTRUM, new ResourceLocation(MODID, "block/connector/connector_mv.obj"),
				new ResourceLocation(MODID, "blocks/connector_connector_mv"), new float[]{0, 4, 8, 12},
				.5625, IEContent.blockConnectors.getStateFromMeta(CONNECTOR_MV.getMeta()),
				8*5F/ELECTRUM.getTransferRate(), 5, (f) -> f);
		registerFeedthroughForWiretype(STEEL, new ResourceLocation(MODID, "block/connector/connector_hv.obj"),
				new ResourceLocation(MODID, "blocks/connector_connector_hv"), new float[]{0, 4, 8, 12},
				.75, IEContent.blockConnectors.getStateFromMeta(CONNECTOR_HV.getMeta()),
				8*15F/STEEL.getTransferRate(), 15, (f) -> f);
		registerFeedthroughForWiretype(REDSTONE, new ResourceLocation(MODID, "block/connector/connector_redstone.obj.ie"),
				ImmutableMap.of(), new ResourceLocation(MODID, "blocks/connector_connector_redstone"), new float[]{3, 8, 11, 16},
				.5625, .5, IEContent.blockConnectors.getStateFromMeta(CONNECTOR_REDSTONE.getMeta()),
				0, 0, (f) -> f);
	}

	public IElectricEquipment.ElectricSource getElectricSource()
	{
		return COPPER.getElectricSource();
	}

	/**
	 * DO NOT SUBCLASS THIS.
	 * This is a core implementation as a base for IE's default wires
	 * DO NOT SUBCLASS THIS.
	 */
	private static class IEBASE extends WireType
	{
		final int ordinal;
		private final IElectricEquipment.ElectricSource eSource;

		public IEBASE(int ordinal)
		{
			super();
			this.ordinal = ordinal;
			WireApi.registerWireType(this);
			if(canCauseDamage())
				eSource = new IElectricEquipment.ElectricSource(.5F*(1+ordinal));
			else
				eSource = new IElectricEquipment.ElectricSource(-1);
		}

		@Override
		public double getLossRatio()
		{
			return Math.abs(wireLossRatio[ordinal%6]);
		}

		@Override
		public int getTransferRate()
		{
			return Math.abs(wireTransferRate[ordinal%6]);
		}

		@Override
		public int getColour(Connection connection)
		{
			return wireColouration[ordinal];
		}

		@Override
		public double getSlack()
		{
			return 1.005;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public TextureAtlasSprite getIcon(Connection connection)
		{
			return iconDefaultWire;
		}

		@Override
		public int getMaxLength()
		{
			return wireLength[ordinal%6];
		}

		@Override
		public ItemStack getWireCoil()
		{
			return new ItemStack(ieWireCoil, 1, ordinal);
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

		@Override
		public boolean isEnergyWire()
		{
			return ordinal%6 < 3;
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
		public boolean canCauseDamage()
		{
			return ordinal < 3;
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
		public IElectricEquipment.ElectricSource getElectricSource()
		{
			return eSource;
		}
	}
}

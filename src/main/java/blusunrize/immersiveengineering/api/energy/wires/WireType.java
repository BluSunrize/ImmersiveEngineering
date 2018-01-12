/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author BluSunrize - 08.03.2015<br>
 * Rewritten: 26.06.2015
 * <br>
 * The WireTypes of IE. Extend this to make your own
 */
public abstract class WireType
{
	private static LinkedHashSet<WireType> values = new LinkedHashSet<WireType>();
	public static LinkedHashSet<WireType> getValues()
	{
		return values;
	}
	public static WireType getValue(String name)
	{
		for(WireType type: values)
			if(type!=null && type.getUniqueName().equals(name))
				return type;
		return COPPER;
	}

	public WireType()
	{
		values.add(this);
	}
	public static Set<Set<WireType>> matching = new HashSet<>();

	public static boolean canMix(WireType a, WireType b)
	{
		for (Set<WireType> s:matching)
			if (s.contains(a))
				return s.contains(b);
		return false;
	}

	public abstract String getUniqueName();
	public abstract double getLossRatio();
	public abstract int getTransferRate();
	/**Try not to get to complex with determining colour here*/
	public abstract int getColour(Connection connection);
	/**Determines how saggy the wire is*/
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
	public boolean canCauseDamage() {
		return false;
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

	public static WireType COPPER = new IEBASE(0);
	public static WireType ELECTRUM = new IEBASE(1);
	public static WireType STEEL = new IEBASE(2);
	public static WireType STRUCTURE_ROPE = new IEBASE(3);
	public static WireType STRUCTURE_STEEL = new IEBASE(4);
	public static WireType REDSTONE = new IEBASE(5);
	public static WireType COPPER_INSULATED = new IEBASE(6);
	public static WireType ELECTRUM_INSULATED = new IEBASE(7);
	static
	{
		Set<WireType> matching = new HashSet<>();
		matching.add(COPPER);
		matching.add(COPPER_INSULATED);
		WireType.matching.add(matching);
		matching = new HashSet<>();
		matching.add(ELECTRUM);
		matching.add(ELECTRUM_INSULATED);
		WireType.matching.add(matching);
		matching = new HashSet<>();
		matching.add(STEEL);
		WireType.matching.add(matching);
		matching = new HashSet<>();
		matching.add(STRUCTURE_STEEL);
		matching.add(STRUCTURE_ROPE);
		WireType.matching.add(matching);
		matching = new HashSet<>();
		matching.add(REDSTONE);
		WireType.matching.add(matching);
	}

	/**
	 * DO NOT SUBCLASS THIS.
	 * This is a core implementation as a base for IE's default wires
	 * DO NOT SUBCLASS THIS.
	 */
	private static class IEBASE extends WireType
	{
		final int ordinal;
		public IEBASE(int ordinal)
		{
			super();
			this.ordinal = ordinal;
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
			return new ItemStack(ieWireCoil,1,ordinal);
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
			return ordinal%6<3;
		}

		@Override
		public double getDamageRadius()
		{
			switch (ordinal)
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
			return ordinal<3;
		}
	}
}

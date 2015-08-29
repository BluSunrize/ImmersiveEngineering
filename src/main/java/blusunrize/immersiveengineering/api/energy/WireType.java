package blusunrize.immersiveengineering.api.energy;

import java.util.LinkedHashSet;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;

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

	public abstract String getUniqueName();
	public abstract double getLossRatio();
	public abstract int getTransferRate();
	/**Try not to get to complex with determining colour here*/
	public abstract int getColour(Connection connection);
	/**Determines how saggy the wire is*/
	public abstract double getSlack();
	public abstract IIcon getIcon(Connection connection);
	public abstract int getMaxLength();
	public abstract ItemStack getWireCoil();
	public abstract double getRenderDiameter();
	public abstract boolean isEnergyWire();

	//THESE VALUES ARE FOR IE's OWN CABLES!
	public static String[] uniqueNames = {"COPPER","ELECTRUM","STEEL","STRUCTURE_ROPE","STRUCTURE_STEEL"};
	public static double[] cableLossRatio;
	public static int[] cableTransferRate;
	public static int[] cableColouration;
	public static int[] cableLength;
	public static Item ieWireCoil;
	public static double[] renderDiameter = {.03125,.03125, .0625,.0625,.0625};
	public static IIcon iconDefaultWire;
	
	public static WireType COPPER = new WireType.IEBASE(0);
	public static WireType ELECTRUM = new WireType.IEBASE(1);
	public static WireType STEEL = new WireType.IEBASE(2);
	public static WireType STRUCTURE_ROPE = new WireType.IEBASE(3);
	public static WireType STRUCTURE_STEEL = new WireType.IEBASE(4);

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
			return Math.abs(cableLossRatio[ordinal]);
		}
		@Override
		public int getTransferRate()
		{
			return Math.abs(cableTransferRate[ordinal]);
		}
		@Override
		public int getColour(Connection connection)
		{
			return cableColouration[ordinal];
		}
		@Override
		public double getSlack()
		{
			return 1.005;
		}
		@Override
		public IIcon getIcon(Connection connection)
		{
			return iconDefaultWire;
		}
		@Override
		public int getMaxLength()
		{
			return cableLength[ordinal];
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
			return renderDiameter[ordinal];
		}
		@Override
		public boolean isEnergyWire()
		{
			return ordinal<3;
		}
	}
}

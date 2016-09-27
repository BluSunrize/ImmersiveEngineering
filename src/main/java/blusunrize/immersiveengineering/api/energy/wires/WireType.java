package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedHashSet;

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

	//THESE VALUES ARE FOR IE's OWN WIRES!
	public static String[] uniqueNames = {"COPPER", "ELECTRUM", "STEEL", "STRUCTURE_ROPE", "STRUCTURE_STEEL", "REDSTONE"};
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
			return Math.abs(wireLossRatio[ordinal]);
		}
		@Override
		public int getTransferRate()
		{
			return Math.abs(wireTransferRate[ordinal]);
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
			return wireLength[ordinal];
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

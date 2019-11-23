/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerProvider;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * @author BluSunrize - 08.03.2015<br>
 * Rewritten: 26.06.2015
 * <br>
 * The WireTypes of IE. Extend this to make your own
 */
public abstract class WireType implements ILocalHandlerProvider
{
	public static final String LV_CATEGORY = "LV";
	public static final String MV_CATEGORY = "MV";
	public static final String HV_CATEGORY = "HV";
	public static final String STRUCTURE_CATEGORY = "STRUCTURE";
	public static final String REDSTONE_CATEGORY = "REDSTONE";
	private static LinkedHashSet<WireType> values = new LinkedHashSet<>();

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

	/**
	 * Try not to get to complex with determining colour here
	 */
	public abstract int getColour(Connection connection);

	/**
	 * Determines how saggy the wire is
	 */
	public abstract double getSlack();

	@OnlyIn(Dist.CLIENT)
	public abstract TextureAtlasSprite getIcon(Connection connection);

	public abstract int getMaxLength();

	public abstract ItemStack getWireCoil(Connection con);

	public abstract double getRenderDiameter();

	/**
	 * Used to determine which other wire types can be on the same connector as this wire (obviously does not apply to transformers)
	 * Returning null will cause the wire to be incompatible with all other wires
	 */
	@Nullable
	public String getCategory()
	{
		return null;
	}


	@OnlyIn(Dist.CLIENT)
	public static TextureAtlasSprite iconDefaultWire;

	public static WireType COPPER;
	public static WireType ELECTRUM;
	public static WireType STEEL;
	public static WireType STRUCTURE_ROPE;
	public static WireType STRUCTURE_STEEL;
	public static WireType REDSTONE;
	public static WireType COPPER_INSULATED;
	public static WireType ELECTRUM_INSULATED;
	public static WireType INTERNAL_CONNECTION;

	public static Collection<WireType> getIEWireTypes()
	{
		return ImmutableList.of(
				COPPER,
				ELECTRUM,
				STEEL,
				STRUCTURE_ROPE,
				STRUCTURE_STEEL,
				REDSTONE,
				COPPER_INSULATED,
				ELECTRUM_INSULATED
		);
	}
}

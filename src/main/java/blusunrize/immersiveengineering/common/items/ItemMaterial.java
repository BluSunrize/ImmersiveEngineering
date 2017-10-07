/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;


public class ItemMaterial extends ItemIEBase
{
	public ItemMaterial()
	{
		super("material", 64,
				"stick_treated", "stick_iron", "stick_steel", "stick_aluminum",
				"hemp_fiber", "hemp_fabric",
				"coal_coke", "slag",
				"component_iron", "component_steel",
				"waterwheel_segment", "windmill_blade", "windmill_sail",
				"wooden_grip", "gunpart_barrel", "gunpart_drum", "gunpart_hammer",
				"dust_coke", "dust_hop_graphite", "ingot_hop_graphite",
				"wire_copper", "wire_electrum", "wire_aluminum", "wire_steel",
				"dust_saltpeter","dust_sulfur", "electron_tube", "circuit_board");
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
	}
}
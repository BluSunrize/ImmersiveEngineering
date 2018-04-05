/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class DataFixer1213 implements IFixableData
{
	//Map to meta=0 variant first to prevent warnings, then map the meta values
	//TODO add block state remapping when the FixType for that is available (1.13)
	private static Map<ResourceLocation, ResourceLocation> RAW_MAPPER = new HashMap<>();
	private static Map<ResourceLocation, ResourceLocation[]> META_MAPPER = new HashMap<>();

	static
	{
		addMapping("material",
				"stick_treated", "stick_iron", "stick_steel", "stick_aluminum", "hemp_fiber", "hemp_fabric",
				"coal_coke", "slag", "component_iron", "component_steel", "waterwheel_segment", "windmill_blade",
				"windmill_sail", "wooden_grip", "gunpart_barrel", "gunpart_drum", "gunpart_hammer", "dust_coke",
				"dust_hop_graphite", "ingot_hop_graphite", "wire_copper", "wire_electrum", "wire_aluminum", "wire_steel",
				"dust_saltpeter", "dust_sulfur", "electron_tube", "circuit_board"
		);
		addMapping("metal",
				"ingot_copper", "ingot_aluminum", "ingot_lead", "ingot_silver", "ingot_nickel", "ingot_uranium",
				"ingot_constantan", "ingot_electrum", "ingot_steel", "dust_copper", "dust_aluminum", "dust_lead",
				"dust_silver", "dust_nickel", "dust_uranium", "dust_constantan", "dust_electrum", "dust_steel",
				"dust_iron", "dust_gold", "nugget_copper", "nugget_aluminum", "nugget_lead", "nugget_silver",
				"nugget_nickel", "nugget_uranium", "nugget_constantan", "nugget_electrum", "nugget_steel", "nugget_iron",
				"plate_copper", "plate_aluminum", "plate_lead", "plate_silver", "plate_nickel", "plate_uranium",
				"plate_constantan", "plate_electrum", "plate_steel", "plate_iron", "plate_gold"
		);
		addMapping("drillhead", "drillhead_iron", "drillhead_steel");
		addMapping("seed", "hempseed");
		addMapping("fake_icon", "birthday", "lucky");
		addMapping("tool",
				ItemHammer.NAME, ItemCutter.NAME, ItemVoltmeter.NAME, ItemManual.NAME);
		addMapping("toolupgrade",
				"toolupgrade_drill_waterproof",
				"toolupgrade_drill_lube",
				"toolupgrade_drill_damage",
				"toolupgrade_drill_capacity",
				"toolupgrade_revolver_bayonet",
				"toolupgrade_revolver_magazine",
				"toolupgrade_revolver_electro",
				"toolupgrade_chemthrower_focus",
				"toolupgrade_railgun_scope",
				"toolupgrade_railgun_capacitors",
				"toolupgrade_shield_flash",
				"toolupgrade_shield_shock",
				"toolupgrade_shield_magnet",
				"toolupgrade_chemthrower_multitank");
		addMapping("mold", "mold_plate", "mold_gear", "mold_rod", "mold_bullet_casing",
				"mold_wire", "mold_packing4", "mold_packing9", "mold_unpacking");
		addMapping("wirecoil", "wirecoil_copper", "wirecoil_electrum", "wirecoil_hv",
				"wirecoil_rope", "wirecoil_structural", "wirecoil_redstone", "wirecoil_insulated_copper",
				"wirecoil_insulated_electrum");
	}

	private static void addMapping(String orig, String... now)
	{
		RAW_MAPPER.put(newLoc(orig), newLoc(now[0]));
		if (now.length > 1)
		{
			ResourceLocation[] mapTo = new ResourceLocation[now.length - 1];
			for (int i = 0; i < now.length - 1; i++)
				mapTo[i] = newLoc(now[i + 1]);
			META_MAPPER.put(newLoc(now[0]), mapTo);
		}
	}

	private static ResourceLocation newLoc(String path)
	{
		return new ResourceLocation(ImmersiveEngineering.MODID, path);
	}

	@Override
	public int getFixVersion()
	{
		return 0;
	}

	@Nonnull
	@Override
	public NBTTagCompound fixTagCompound(@Nonnull NBTTagCompound compound)
	{
		ResourceLocation oldLoc = new ResourceLocation(compound.getString("id"));
		int meta = compound.getInteger("Damage");
		ResourceLocation[] mapTo = META_MAPPER.get(oldLoc);
		if (mapTo != null && meta >= 0 && meta < mapTo.length)
		{
			compound.removeTag("Damage");
			compound.setString("id", mapTo[meta].toString());
		}
		return compound;
	}

	@SubscribeEvent
	public static void mapRaw(RegistryEvent.MissingMappings<Item> ev)
	{
		for (RegistryEvent.MissingMappings.Mapping<Item> m : ev.getMappings())
		{
			if (RAW_MAPPER.containsKey(m.key))
			{
				ResourceLocation newLoc = RAW_MAPPER.get(m.key);
				Item newItem = Item.REGISTRY.getObject(newLoc);
				if (newItem != null)
					m.remap(newItem);
			}
		}
	}
}
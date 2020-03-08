/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.data.models.LoadedModelBuilder;
import blusunrize.immersiveengineering.common.data.models.LoadedModelProvider;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Molds;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.items.IEItems.Weapons;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;

import static blusunrize.immersiveengineering.common.data.IEDataGenerator.rl;

public class ItemModels extends LoadedModelProvider
{
	BlockStates blockStates;

	public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper, BlockStates blockStates)
	{
		super(generator, ImmersiveEngineering.MODID, "item", existingFileHelper);
		this.blockStates = blockStates;
	}

	private ResourceLocation forgeLoc(String s)
	{
		return new ResourceLocation("forge", s);
	}

	@Override
	protected void registerModels()
	{
		for(Entry<Block, ModelFile> model : blockStates.itemModels.entrySet())
			getBuilder(model.getKey())
					.parent(model.getValue());

		cubeBottomTop(name(WoodenDevices.woodenBarrel),
				rl("block/wooden_device/barrel_side"),
				rl("block/wooden_device/barrel_up_none"),
				rl("block/wooden_device/barrel_up_none"));

		cubeBottomTop(name(MetalDevices.barrel),
				rl("block/metal_device/barrel_side"),
				rl("block/metal_device/barrel_up_none"),
				rl("block/metal_device/barrel_up_none"));

		obj(MetalDecoration.steelPost, modLoc("block/wooden_device/wooden_post_inv.obj"))
				.texture("post", modLoc("block/metal_decoration/steel_post"))
				.transforms(modLoc("item/post"));
		obj(MetalDecoration.aluPost, modLoc("block/wooden_device/wooden_post_inv.obj"))
				.texture("post", modLoc("block/metal_decoration/aluminum_post"))
				.transforms(modLoc("item/post"));
		obj(WoodenDecoration.treatedPost, modLoc("block/wooden_device/wooden_post_inv.obj"))
				.texture("post", modLoc("block/wooden_decoration/post"))
				.transforms(modLoc("item/post"));
		for(EnumMetals m : EnumMetals.values())
			createMetalModels(m);
		addItemModels("metal_", IEItems.Metals.ingots.values().stream().filter(i -> ImmersiveEngineering.MODID.equals(i.getRegistryName().getNamespace())).toArray(Item[]::new));
		addItemModels("metal_", IEItems.Metals.nuggets.values().stream().filter(i -> ImmersiveEngineering.MODID.equals(i.getRegistryName().getNamespace())).toArray(Item[]::new));
		addItemModels("metal_", IEItems.Metals.dusts.values().toArray(new Item[IEItems.Metals.ingots.size()]));
		addItemModels("metal_", IEItems.Metals.plates.values().toArray(new Item[IEItems.Metals.ingots.size()]));
		for(Item bag : IEItems.Misc.shaderBag.values())
			addItemModel("shader_bag", bag);

		addItemModels("material_", Ingredients.stickTreated, Ingredients.stickIron, Ingredients.stickSteel, Ingredients.stickAluminum,
				Ingredients.hempFiber, Ingredients.hempFabric, Ingredients.coalCoke, Ingredients.slag,
				Ingredients.componentIron, Ingredients.componentSteel, Ingredients.waterwheelSegment, Ingredients.windmillBlade, Ingredients.windmillSail,
				Ingredients.woodenGrip, Ingredients.gunpartBarrel, Ingredients.gunpartDrum, Ingredients.gunpartHammer,
				Ingredients.dustCoke, Ingredients.dustHopGraphite, Ingredients.ingotHopGraphite,
				Ingredients.wireCopper, Ingredients.wireElectrum, Ingredients.wireAluminum, Ingredients.wireSteel,
				Ingredients.dustSaltpeter, Ingredients.dustSulfur, Ingredients.electronTube, Ingredients.circuitBoard);

		addItemModels("tool_", Tools.hammer, Tools.wirecutter, Tools.manual, Tools.steelPick, Tools.steelShovel, Tools.steelAxe, Tools.steelSword);
		addItemModels("", IEItems.Misc.wireCoils.values().toArray(new Item[0]));
		addItemModels("", IEItems.Misc.graphiteElectrode);
		addItemModels("", IEItems.Misc.toolUpgrades.values().toArray(new Item[0]));
		addItemModels("", Molds.moldPlate, Molds.moldGear, Molds.moldRod, Molds.moldBulletCasing, Molds.moldWire, Molds.moldPacking4, Molds.moldPacking9, Molds.moldUnpacking);
		addItemModels("bullet_", Ingredients.emptyCasing, Ingredients.emptyShell);
		addItemModels("bullet_", Weapons.bullets.values());
		addItemModels("", IEItems.Misc.faradaySuit.values());
		addItemModel("blueprint", IEItems.Misc.blueprint);
		addItemModel("seed_hemp", IEItems.Misc.hempSeeds);
		addItemModel("drillhead_iron", Tools.drillheadIron);
		addItemModel("drillhead_steel", Tools.drillheadSteel);
	}

	private LoadedModelBuilder obj(IItemProvider item, ResourceLocation model)
	{
		Preconditions.checkArgument(existingFileHelper.exists(model, ResourcePackType.CLIENT_RESOURCES, "", "models"));
		return getBuilder(item)
				.loader(forgeLoc("obj"))
				.additional("model", new ResourceLocation(model.getNamespace(), "models/"+model.getPath()))
				.additional("flip-v", true);
	}

	private LoadedModelBuilder getBuilder(IItemProvider item)
	{
		return getBuilder(name(item));
	}

	private String name(IItemProvider item)
	{
		return item.asItem().getRegistryName().getPath();
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "Item models";
	}

	private void createMetalModels(EnumMetals metal)
	{
		String name = metal.tagName();
		if(metal.shouldAddOre())
			cubeAll(name(Metals.ores.get(metal)), rl("block/metal/ore_"+name));
		if(!metal.isVanillaMetal())
		{
			ResourceLocation defaultName = rl("block/metal/storage_"+name);
			if(metal==EnumMetals.URANIUM)
			{
				ResourceLocation side = rl("block/metal/storage_"+name+"_side");
				ResourceLocation top = rl("block/metal/storage_"+name+"_top");
				cubeBottomTop(name(Metals.storage.get(metal)), side, top, top);
			}
			else
				cubeAll(name(Metals.storage.get(metal)), defaultName);
		}
		ResourceLocation sheetmetalName = rl("block/metal/sheetmetal_"+name);
		cubeAll(name(Metals.sheetmetal.get(metal)), sheetmetalName);

	}

	private void addItemModels(String texturePrefix, Item... items)
	{
		addItemModels(texturePrefix, Arrays.asList(items));
	}

	private void addItemModels(String texturePrefix, Collection<Item> items)
	{
		for(Item item : items)
			addItemModel(texturePrefix==null?null: (texturePrefix+item.getRegistryName().getPath()), item);
	}

	private void addItemModel(String texture, Item item)
	{
		String path = name(item);
		String textureLoc = texture==null?path: ("item/"+texture);
		withExistingParent(path, mcLoc("item/generated"))
				.texture("layer0", modLoc(textureLoc));
	}
}

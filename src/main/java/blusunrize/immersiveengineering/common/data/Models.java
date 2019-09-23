/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.data.model.ModelFile.GeneratedModelFile;
import blusunrize.immersiveengineering.common.data.model.ModelGenerator;
import blusunrize.immersiveengineering.common.data.model.ModelHelper;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.common.data.IEDataGenerator.rl;

public class Models extends ModelGenerator
{
	final Map<EnumMetals, MetalModels> metalModels = new HashMap<>();
	final GeneratedModelFile treatedFencePost = ModelHelper.createFencePost(rl("block/treated_wood_horizontal"),
			rl("block/treated_fence_post"));
	final GeneratedModelFile steelFencePost = ModelHelper.createFencePost(rl("block/storage_steel"),
			rl("block/steel_fence_post"));
	final GeneratedModelFile aluFencePost = ModelHelper.createFencePost(rl("block/storage_aluminum"),
			rl("block/alu_fence_post"));
	final GeneratedModelFile treatedFenceSide = ModelHelper.createFenceSide(rl("block/treated_wood_horizontal"),
			rl("block/treated_fence_side"));
	final GeneratedModelFile steelFenceSide = ModelHelper.createFenceSide(rl("block/storage_steel"),
			rl("block/steel_fence_side"));
	final GeneratedModelFile aluFenceSide = ModelHelper.createFenceSide(rl("block/storage_aluminum"),
			rl("block/alu_fence_side"));

	public Models(DataGenerator gen)
	{
		super(gen);
		for(EnumMetals m : EnumMetals.values())
			metalModels.put(m, new MetalModels(m));
	}

	@Override
	protected void registerModels(Consumer<GeneratedModelFile> out)
	{
		for(MetalModels mm : metalModels.values())
			mm.register(out);
		out.accept(treatedFencePost);
		out.accept(treatedFenceSide);
		out.accept(steelFencePost);
		out.accept(steelFenceSide);
		out.accept(aluFencePost);
		out.accept(aluFenceSide);
	}

	public static class MetalModels
	{
		GeneratedModelFile ore;
		GeneratedModelFile storage;
		GeneratedModelFile sheetmetal;

		public MetalModels(EnumMetals metal)
		{
			String name = metal.tagName();
			if(metal.shouldAddOre())
				ore = ModelHelper.createBasicCube(rl("block/ore_"+name));
			if(!metal.isVanillaMetal())
			{
				ResourceLocation defaultName = rl("block/storage_"+name);
				if(metal==EnumMetals.URANIUM)
				{
					ResourceLocation side = rl("block/storage_"+name+"_side");
					ResourceLocation top = rl("block/storage_"+name+"_top");
					storage = ModelHelper.createBasicCube(side, top, top, defaultName);
				}
				else
					storage = ModelHelper.createBasicCube(defaultName);
			}
			sheetmetal = ModelHelper.createBasicCube(rl("block/sheetmetal_"+name));
		}

		void register(Consumer<GeneratedModelFile> out)
		{
			if(ore!=null)
				out.accept(ore);
			if(storage!=null)
				out.accept(storage);
			out.accept(sheetmetal);
		}
	}
}

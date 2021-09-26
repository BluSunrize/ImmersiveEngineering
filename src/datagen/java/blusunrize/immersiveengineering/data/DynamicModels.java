package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.models.obj.callback.DynamicSubmodelCallbacks;
import blusunrize.immersiveengineering.client.render.conveyor.RedstoneConveyorRender;
import blusunrize.immersiveengineering.client.render.entity.SawbladeRenderer;
import blusunrize.immersiveengineering.client.render.tile.*;
import blusunrize.immersiveengineering.data.DynamicModels.SimpleModelBuilder;
import blusunrize.immersiveengineering.data.blockstates.MultiblockStates;
import blusunrize.immersiveengineering.data.models.IEOBJBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.client.model.generators.loaders.OBJLoaderBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.Map.Entry;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class DynamicModels extends ModelProvider<SimpleModelBuilder>
{
	private final MultiblockStates multiblocks;

	public DynamicModels(MultiblockStates multiblocks, DataGenerator generator, ExistingFileHelper existingFileHelper)
	{
		super(generator, Lib.MODID, "dynamic", rl -> new SimpleModelBuilder(rl, existingFileHelper), existingFileHelper);
		this.multiblocks = multiblocks;
	}

	@Override
	protected void registerModels()
	{
		getBuilder(ArcFurnaceRenderer.NAME)
				.customLoader(IEOBJBuilder::begin)
				.modelLocation(rl("models/block/metal_multiblock/arc_furnace_electrodes.obj.ie"))
				.callback(DynamicSubmodelCallbacks.INSTANCE)
				.flipV(true)
				.end();
		getBuilder(AutoWorkbenchRenderer.NAME)
				.customLoader(IEOBJBuilder::begin)
				.modelLocation(rl("models/block/metal_multiblock/auto_workbench_animated.obj.ie"))
				.callback(DynamicSubmodelCallbacks.INSTANCE)
				.flipV(true)
				.end();
		getBuilder(BottlingMachineRenderer.NAME)
				.customLoader(IEOBJBuilder::begin)
				.modelLocation(rl("models/block/metal_multiblock/bottling_machine_animated.obj.ie"))
				.callback(DynamicSubmodelCallbacks.INSTANCE)
				.flipV(true)
				.end();
		getBuilder(BucketWheelRenderer.NAME)
				.customLoader(IEOBJBuilder::begin)
				.modelLocation(rl("models/block/metal_multiblock/bucket_wheel.obj.ie"))
				.callback(DynamicSubmodelCallbacks.INSTANCE)
				.flipV(true)
				.end();
		getBuilder(CrusherRenderer.NAME)
				.customLoader(OBJLoaderBuilder::begin)
				.modelLocation(rl("models/block/metal_multiblock/crusher_drum.obj"))
				.flipV(true)
				.end();
		getBuilder(SawmillRenderer.NAME)
				.customLoader(OBJLoaderBuilder::begin)
				.modelLocation(rl("models/block/metal_multiblock/sawmill_animated.obj"))
				.flipV(true)
				.end();
		getBuilder(DieselGeneratorRenderer.NAME)
				.customLoader(OBJLoaderBuilder::begin)
				.modelLocation(rl("models/block/metal_multiblock/diesel_generator_fan.obj"))
				.flipV(true)
				.end();
		getBuilder(MetalPressRenderer.NAME)
				.customLoader(OBJLoaderBuilder::begin)
				.modelLocation(rl("models/block/metal_multiblock/metal_press_piston.obj"))
				.flipV(true)
				.end();
		getBuilder(MixerRenderer.NAME)
				.customLoader(OBJLoaderBuilder::begin)
				.modelLocation(rl("models/block/metal_multiblock/mixer_agitator.obj"))
				.flipV(true)
				.end();
		getBuilder(SampleDrillRenderer.NAME)
				.customLoader(OBJLoaderBuilder::begin)
				.modelLocation(rl("models/block/metal_device/core_drill_center.obj"))
				.flipV(true)
				.end();
		getBuilder(SqueezerRenderer.NAME)
				.customLoader(OBJLoaderBuilder::begin)
				.modelLocation(rl("models/block/metal_multiblock/squeezer_piston.obj"))
				.flipV(true)
				.end();
		getBuilder(WatermillRenderer.NAME)
				.customLoader(IEOBJBuilder::begin)
				.modelLocation(rl("models/block/wooden_device/watermill.obj.ie"))
				.callback(DynamicSubmodelCallbacks.INSTANCE)
				.flipV(true)
				.end();
		getBuilder(WindmillRenderer.NAME)
				.customLoader(IEOBJBuilder::begin)
				.modelLocation(rl("models/block/wooden_device/windmill.obj.ie"))
				.callback(DynamicSubmodelCallbacks.INSTANCE)
				.flipV(true)
				.end();
		getBuilder(RedstoneConveyorRender.MODEL_NAME)
				.customLoader(IEOBJBuilder::begin)
				.modelLocation(rl("models/block/conveyor_redstone.obj.ie"))
				.callback(DynamicSubmodelCallbacks.INSTANCE)
				.flipV(true)
				.end();
		getBuilder(SawbladeRenderer.NAME)
				.customLoader(IEOBJBuilder::begin)
				.modelLocation(rl("models/item/buzzsaw_diesel.obj.ie"))
				.callback(DynamicSubmodelCallbacks.INSTANCE)
				.flipV(true)
				.end();
		TurretRenderer.MODEL_FILE_BY_BLOCK.forEach((block, file) -> {
			getBuilder(TurretRenderer.MODEL_NAME_BY_BLOCK.get(block))
					.customLoader(IEOBJBuilder::begin)
					.modelLocation(rl("models/"+file))
					.callback(DynamicSubmodelCallbacks.INSTANCE)
					.flipV(true)
					.end();
		});
		for(Entry<Block, ModelFile> multiblock : multiblocks.unsplitModels.entrySet())
			withExistingParent(multiblock.getKey().getRegistryName().getPath(), multiblock.getValue().getLocation());
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "Dynamic models";
	}

	public static class SimpleModelBuilder extends ModelBuilder<SimpleModelBuilder>
	{

		public SimpleModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper)
		{
			super(outputLocation, existingFileHelper);
		}
	}
}

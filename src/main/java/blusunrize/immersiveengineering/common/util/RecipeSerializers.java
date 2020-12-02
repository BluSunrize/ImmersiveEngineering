/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.common.crafting.*;
import blusunrize.immersiveengineering.common.crafting.fluidaware.BasicShapedRecipe;
import blusunrize.immersiveengineering.common.crafting.fluidaware.ShapelessFluidAwareRecipe;
import blusunrize.immersiveengineering.common.crafting.serializers.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;
import java.util.function.Supplier;

public class RecipeSerializers
{
	public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(
			ForgeRegistries.RECIPE_SERIALIZERS, ImmersiveEngineering.MODID
	);

	public static final RegistryObject<SpecialRecipeSerializer<SpeedloaderLoadRecipe>> SPEEDLOADER_LOAD = RECIPE_SERIALIZERS.register(
			"crafting_special_speedloader_load", special(SpeedloaderLoadRecipe::new)
	);
	public static final RegistryObject<SpecialRecipeSerializer<FlareBulletColorRecipe>> FLARE_BULLET_COLOR = RECIPE_SERIALIZERS.register(
			"crafting_special_flare_bullet_color", special(FlareBulletColorRecipe::new)
	);
	public static final RegistryObject<SpecialRecipeSerializer<PotionBulletFillRecipe>> POTION_BULLET_FILL = RECIPE_SERIALIZERS.register(
			"crafting_special_potion_bullet_fill", special(PotionBulletFillRecipe::new)
	);
	public static final RegistryObject<SpecialRecipeSerializer<JerrycanRefillRecipe>> JERRYCAN_REFILL = RECIPE_SERIALIZERS.register(
			"crafting_special_jerrycan_refill", special(JerrycanRefillRecipe::new)
	);
	public static final RegistryObject<SpecialRecipeSerializer<PowerpackRecipe>> POWERPACK_SERIALIZER = RECIPE_SERIALIZERS.register(
			"powerpack", special(PowerpackRecipe::new)
	);
	public static final RegistryObject<HammerCrushingRecipeSerializer> HAMMER_CRUSHING_SERIALIZER = RECIPE_SERIALIZERS.register(
			"hammer_crushing", HammerCrushingRecipeSerializer::new
	);
	public static final RegistryObject<SpecialRecipeSerializer<EarmuffsRecipe>> EARMUFF_SERIALIZER = RECIPE_SERIALIZERS.register(
			"earmuffs", special(EarmuffsRecipe::new)
	);
	public static final RegistryObject<RGBRecipeSerializer> RGB_SERIALIZER = RECIPE_SERIALIZERS.register(
			"rgb", RGBRecipeSerializer::new
	);
	public static final RegistryObject<TurnAndCopyRecipeSerializer> TURN_AND_COPY_SERIALIZER = RECIPE_SERIALIZERS.register(
			"turn_and_copy", TurnAndCopyRecipeSerializer::new
	);
	public static final RegistryObject<RevolverAssemblyRecipeSerializer> REVOLVER_ASSEMBLY_SERIALIZER = RECIPE_SERIALIZERS.register(
			"revolver_assembly", RevolverAssemblyRecipeSerializer::new
	);
	public static final RegistryObject<SpecialRecipeSerializer<RevolverCycleRecipe>> REVOLVER_CYCLE_SERIALIZER = RECIPE_SERIALIZERS.register(
			"revolver_cycle", special(RevolverCycleRecipe::new)
	);
	public static final RegistryObject<SpecialRecipeSerializer<IERepairItemRecipe>> IE_REPAIR_SERIALIZER = RECIPE_SERIALIZERS.register(
			"ie_item_repair", special(IERepairItemRecipe::new)
	);
	public static final RegistryObject<SpecialRecipeSerializer<ShaderBagRecipe>> SHADER_BAG_SERIALIZER = RECIPE_SERIALIZERS.register(
			"shader_bag", special(ShaderBagRecipe::new)
	);
	public static final RegistryObject<DamageToolRecipeSerializer> DAMAGE_TOOL_SERIALIZER = RECIPE_SERIALIZERS.register(
			"damage_tool", DamageToolRecipeSerializer::new
	);
	public static final RegistryObject<WrappingRecipeSerializer<BasicShapedRecipe, ?>> IE_SHAPED_SERIALIZER = RECIPE_SERIALIZERS.register(
			"shaped_fluid", () -> new WrappingRecipeSerializer<>(
					IRecipeSerializer.CRAFTING_SHAPED, BasicShapedRecipe::toVanilla, BasicShapedRecipe::new
			)
	);
	public static final RegistryObject<WrappingRecipeSerializer<ShapelessFluidAwareRecipe, ?>> IE_SHAPELESS_SERIALIZER = RECIPE_SERIALIZERS.register(
			"shapeless_fluid", () -> new WrappingRecipeSerializer<>(
					IRecipeSerializer.CRAFTING_SHAPELESS, ShapelessFluidAwareRecipe::toVanilla, ShapelessFluidAwareRecipe::new
			)
	);

	static
	{
		AlloyRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"alloy", AlloyRecipeSerializer::new
		);
		BlastFurnaceRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"blast_furnace", BlastFurnaceRecipeSerializer::new
		);
		BlastFurnaceFuel.SERIALIZER = RECIPE_SERIALIZERS.register(
				"blast_furnace_fuel", BlastFurnaceFuelSerializer::new
		);
		CokeOvenRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"coke_oven", CokeOvenRecipeSerializer::new
		);
		ClocheRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"cloche", ClocheRecipeSerializer::new
		);
		ClocheFertilizer.SERIALIZER = RECIPE_SERIALIZERS.register(
				"fertilizer", ClocheFertilizerSerializer::new
		);
		BlueprintCraftingRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"blueprint", BlueprintCraftingRecipeSerializer::new
		);
		MetalPressRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"metal_press", MetalPressRecipeSerializer::new
		);
		ArcFurnaceRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"arc_furnace", ArcFurnaceRecipeSerializer::new
		);
		BottlingMachineRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"bottling_machine", BottlingMachineRecipeSerializer::new
		);
		CrusherRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"crusher", CrusherRecipeSerializer::new
		);
		SawmillRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"sawmill", SawmillRecipeSerializer::new
		);
		FermenterRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"fermenter", FermenterRecipeSerializer::new
		);
		SqueezerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"squeezer", SqueezerRecipeSerializer::new
		);
		RefineryRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"refinery", RefineryRecipeSerializer::new
		);
		MixerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"mixer", MixerRecipeSerializer::new
		);
		MineralMix.SERIALIZER = RECIPE_SERIALIZERS.register(
				"mineral_mix", MineralMixSerializer::new
		);
		GeneratedListRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"generated_list", GeneratedListSerializer::new
		);
	}

	private static <T extends IRecipe<?>> Supplier<SpecialRecipeSerializer<T>> special(Function<ResourceLocation, T> create)
	{
		return () -> new SpecialRecipeSerializer<>(create);
	}
}

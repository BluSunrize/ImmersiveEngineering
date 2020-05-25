package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.common.crafting.*;
import blusunrize.immersiveengineering.common.crafting.serializers.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeSerializers
{
	public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = new DeferredRegister<>(ForgeRegistries.RECIPE_SERIALIZERS, ImmersiveEngineering.MODID);

	public static final RegistryObject<SpecialRecipeSerializer<SpeedloaderLoadRecipe>> SPEEDLOADER_LOAD = RECIPE_SERIALIZERS.register(
			"crafting_special_speedloader_load", () -> new SpecialRecipeSerializer<>(SpeedloaderLoadRecipe::new)
	);
	public static final RegistryObject<SpecialRecipeSerializer<FlareBulletColorRecipe>> FLARE_BULLET_COLOR = RECIPE_SERIALIZERS.register(
			"crafting_special_flare_bullet_color", () -> new SpecialRecipeSerializer<>(FlareBulletColorRecipe::new)
	);
	public static final RegistryObject<SpecialRecipeSerializer<PotionBulletFillRecipe>> POTION_BULLET_FILL = RECIPE_SERIALIZERS.register(
			"crafting_special_potion_bullet_fill", () -> new SpecialRecipeSerializer<>(PotionBulletFillRecipe::new)
	);
	public static final RegistryObject<SpecialRecipeSerializer<JerrycanRefillRecipe>> JERRYCAN_REFILL = RECIPE_SERIALIZERS.register(
			"crafting_special_jerrycan_refill", () -> new SpecialRecipeSerializer<>(JerrycanRefillRecipe::new)
	);
	public static final RegistryObject<SpecialRecipeSerializer<PowerpackRecipe>> POWERPACK_SERIALIZER = RECIPE_SERIALIZERS.register(
			"powerpack", () -> new SpecialRecipeSerializer<>(PowerpackRecipe::new)
	);
	public static final RegistryObject<HammerCrushingRecipeSerializer> HAMMER_CRUSHING_SERIALIZER = RECIPE_SERIALIZERS.register(
			"hammer_crushing", HammerCrushingRecipeSerializer::new
	);
	public static final RegistryObject<SpecialRecipeSerializer<EarmuffsRecipe>> EARMUFF_SERIALIZER = RECIPE_SERIALIZERS.register(
			"earmuffs", () -> new SpecialRecipeSerializer<>(EarmuffsRecipe::new)
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

		CraftingHelper.register(new IEConfigConditionSerializer());
	}
}

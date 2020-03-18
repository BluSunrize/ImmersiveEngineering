package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.crafting.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
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
	public static final RegistryObject<SpecialRecipeSerializer<OreCrushingRecipe>> ORE_CRUSHING_SERIALIZER = RECIPE_SERIALIZERS.register(
			"ore_crushing", () -> new SpecialRecipeSerializer<>(OreCrushingRecipe::new)
	);
	public static final RegistryObject<SpecialRecipeSerializer<EarmuffsRecipe>> EARMUFF_SERIALIZER = RECIPE_SERIALIZERS.register(
			"earmuffs", () -> new SpecialRecipeSerializer<>(EarmuffsRecipe::new)
	);
	public static final RegistryObject<RGBRecipeSerializer> RGB_SERIALIZER = RECIPE_SERIALIZERS.register(
			"rgb", RGBRecipeSerializer::new
	);
}

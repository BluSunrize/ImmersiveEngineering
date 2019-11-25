package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.crafting.FlareBulletRecipe;
import blusunrize.immersiveengineering.common.crafting.PotionBulletRecipe;
import blusunrize.immersiveengineering.common.crafting.SpeedloaderRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mod.EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RecipeSerializers {
	public static final SpecialRecipeSerializer<SpeedloaderRecipe> SPEEDLOADER_LOAD = IRecipeSerializer.register(ImmersiveEngineering.MODID + ":crafting_special_speedloader_load", new SpecialRecipeSerializer<>(SpeedloaderRecipe::new));
	public static final SpecialRecipeSerializer<PotionBulletRecipe> POTION_BULLET_FILL = IRecipeSerializer.register(ImmersiveEngineering.MODID + ":crafting_special_potion_bullet_fill", new SpecialRecipeSerializer<>(PotionBulletRecipe::new));
	public static final SpecialRecipeSerializer<FlareBulletRecipe> FLARE_BULLET_COLOR = IRecipeSerializer.register(ImmersiveEngineering.MODID + ":crafting_special_flare_bullet_color", new SpecialRecipeSerializer<>(FlareBulletRecipe::new));

	@SubscribeEvent
	public static void register(Register<IRecipeSerializer<?>> event) {
		Field[] fields = RecipeSerializers.class.getDeclaredFields();
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers()) && field.getType().isAssignableFrom(SpecialRecipeSerializer.class)) {
				try {
					event.getRegistry().register((IRecipeSerializer<?>) field.get(null));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.crafting.SpeedloaderRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RecipeSerializers {
	public static final SpecialRecipeSerializer<SpeedloaderRecipe> SPEEDLOADER_LOAD = IRecipeSerializer.register(ImmersiveEngineering.MODID + ":crafting_special_speedloader_load", new SpecialRecipeSerializer<>(SpeedloaderRecipe::new));

	@SubscribeEvent
	public static void register(Register<IRecipeSerializer<?>> event) {
		event.getRegistry().register(SPEEDLOADER_LOAD);
	}
}

/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class IEKeybinds
{
	public static KeyBinding keybind_magnetEquip = new KeyBinding("key.immersiveengineering.magnetEquip", GLFW.GLFW_KEY_S, "key.categories.immersiveengineering");
	public static KeyBinding keybind_chemthrowerSwitch = new KeyBinding("key.immersiveengineering.chemthrowerSwitch", -1, "key.categories.immersiveengineering");
	public static KeyBinding keybind_railgunZoom = new KeyBinding("key.immersiveengineering.railgunZoom", InputMappings.Type.MOUSE, 2, "key.categories.immersiveengineering");

	public static void register()
	{
		IKeyConflictContext noKeyConflict = new IKeyConflictContext()
		{
			@Override
			public boolean isActive()
			{
				return mc().currentScreen==null;
			}

			@Override
			public boolean conflicts(IKeyConflictContext other)
			{
				return false;
			}
		};
		keybind_magnetEquip.setKeyConflictContext(noKeyConflict);
		ClientRegistry.registerKeyBinding(keybind_magnetEquip);

		keybind_railgunZoom.setKeyConflictContext(new ItemKeybindConflictContext(
				(stack, player) -> stack.getItem() instanceof IZoomTool&&((IZoomTool)stack.getItem()).canZoom(stack, player))
		);
		ClientRegistry.registerKeyBinding(keybind_railgunZoom);

		keybind_chemthrowerSwitch.setKeyConflictContext(KeyConflictContext.IN_GAME);
		ClientRegistry.registerKeyBinding(keybind_chemthrowerSwitch);
	}
}

/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class IEKeybinds
{
	public static KeyMapping keybind_magnetEquip = new KeyMapping("key.immersiveengineering.magnetEquip", GLFW.GLFW_KEY_S, "key.categories.immersiveengineering");
	public static KeyMapping keybind_chemthrowerSwitch = new KeyMapping("key.immersiveengineering.chemthrowerSwitch", -1, "key.categories.immersiveengineering");
	public static KeyMapping keybind_railgunZoom = new KeyMapping("key.immersiveengineering.railgunZoom", InputConstants.Type.MOUSE, 2, "key.categories.immersiveengineering");

	public static void register()
	{
		IKeyConflictContext noKeyConflict = new IKeyConflictContext()
		{
			@Override
			public boolean isActive()
			{
				return mc().screen==null;
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

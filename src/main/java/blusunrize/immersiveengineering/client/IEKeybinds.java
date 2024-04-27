/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.lwjgl.glfw.GLFW;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class IEKeybinds
{
	public static KeyMapping keybind_magnetEquip = new KeyMapping("key.immersiveengineering.magnetEquip", GLFW.GLFW_KEY_S, "key.categories.immersiveengineering");
	public static KeyMapping keybind_chemthrowerSwitch = new KeyMapping("key.immersiveengineering.chemthrowerSwitch", -1, "key.categories.immersiveengineering");
	public static KeyMapping keybind_railgunZoom = new KeyMapping("key.immersiveengineering.railgunZoom", InputConstants.Type.MOUSE, 2, "key.categories.immersiveengineering");

	@SubscribeEvent
	public static void registerKeybinds(RegisterKeyMappingsEvent ev)
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
		ev.register(keybind_magnetEquip);

		keybind_railgunZoom.setKeyConflictContext(new ItemKeybindConflictContext(
				(stack, player) -> stack.getItem() instanceof IZoomTool&&((IZoomTool)stack.getItem()).canZoom(stack, player))
		);
		ev.register(keybind_railgunZoom);

		keybind_chemthrowerSwitch.setKeyConflictContext(KeyConflictContext.IN_GAME);
		ev.register(keybind_chemthrowerSwitch);
	}
}

/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.lib.manual.ManualEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record ManualUnlockToast(Optional<AdvancementToast> originalToast, List<ManualEntry> entries) implements Toast
{
	private static final ResourceLocation BACKGROUND_SPRITE = IEApi.ieLoc("hud/toast_manual");

	private static final Component EUREKA = Component.translatable(Lib.GUI+"toast.eureka");
	private static final Component HEADLINE = Component.translatable(Lib.GUI+"toast.manual_unlocked");

	@Override
	public int height()
	{
		return 48+originalToast.map(Toast::height).orElse(0);
	}

	@Override
	public Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible)
	{
		guiGraphics.pose().pushPose();
		originalToast.ifPresent(toast -> {
			toast.render(guiGraphics, toastComponent, timeSinceLastVisible);
			guiGraphics.pose().translate(0, toast.height(), 0);
		});
		guiGraphics.blitSprite(BACKGROUND_SPRITE, 0, 0, this.width(), 48);
		guiGraphics.renderFakeItem(Tools.MANUAL.asItem().getDefaultInstance(), 7, 8);
		if(timeSinceLastVisible < 1000)
		{
			guiGraphics.pose().scale(2,2,2);
			guiGraphics.drawString(toastComponent.getMinecraft().font, EUREKA, 16, 4, 0xf78034, false);
		}
		else
		{
			guiGraphics.drawString(toastComponent.getMinecraft().font, HEADLINE, 32, 6, 0xf78034, false);
			if(!this.entries.isEmpty())
			{
				int iEntry = (int)((timeSinceLastVisible/1000)%this.entries.size());
				guiGraphics.drawString(toastComponent.getMinecraft().font, entries.get(iEntry).getTitle(), 32, 18, 0x555555, false);
			}
		}
		guiGraphics.pose().popPose();
		if(timeSinceLastVisible >= AdvancementToast.DISPLAY_TIME*toastComponent.getNotificationDisplayTimeMultiplier())
			return Visibility.HIDE;
		else
			return Visibility.SHOW;
	}
}

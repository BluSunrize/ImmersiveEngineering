/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.accessors;

import net.minecraft.client.gui.NewChatGui;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NewChatGui.class)
public interface NewChatGuiAccess
{
	@Invoker
	void callDeleteChatLine(int id);

	@Invoker
	void callPrintChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId);
}

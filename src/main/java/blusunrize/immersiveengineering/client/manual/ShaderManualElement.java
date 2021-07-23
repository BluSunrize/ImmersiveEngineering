/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.manual;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.utils.IngredientUtils;
import blusunrize.immersiveengineering.common.network.MessageShaderManual;
import blusunrize.immersiveengineering.common.network.MessageShaderManual.MessageType;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.SpecialManualElements;
import blusunrize.lib.manual.gui.GuiButtonManual;
import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import blusunrize.lib.manual.gui.ManualScreen;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static blusunrize.lib.manual.ManualUtils.mc;

/**
 * @author BluSunrize - 11.11.2016
 */
public class ShaderManualElement extends SpecialManualElements
{
	final ShaderRegistry.ShaderRegistryEntry shader;

	ItemStack shaderItem;
	ItemStack[] exampleItems;
	IngredientWithSize replicationCost;
	int example = 0;
	boolean unlocked;

	MutableComponent name, text;

	public ShaderManualElement(ManualInstance manual, ShaderRegistry.ShaderRegistryEntry shader)
	{
		super(manual);
		this.shader = shader;
	}

	@Override
	public int getPixelsTaken()
	{
		return 47;
	}

	@Override
	public void onOpened(ManualScreen gui, int x, int y, List<Button> buttons)
	{
		Player player = mc().player;
		UUID uuid = player.getUUID();
		unlocked = ShaderRegistry.receivedShaders.get(uuid).contains(shader.getName());

		shaderItem = new ItemStack(ShaderRegistry.itemShader);
		shaderItem.getOrCreateTag().putString("shader_name", shader.getName().toString());
		replicationCost = shader.replicationCost.get();

		if(unlocked)
		{
			ArrayList<ItemStack> list = new ArrayList<>();
			for(ItemStack is : ShaderRegistry.itemExamples)
			{
				ItemStack s = is.copy();
				s.getCapability(CapabilityShader.SHADER_CAPABILITY)
						.ifPresent(wrapper -> {
							wrapper.setShaderItem(shaderItem);
							list.add(s);
						});
			}
			exampleItems = list.toArray(new ItemStack[0]);
			example = 0;
			if(exampleItems.length > 1)
			{
				buttons.add(new GuiButtonManualNavigation(gui, x+50, y, 10, 16, 0, btn -> {
					example--;
					if(example < 0)
						example = this.exampleItems.length-1;
				}));
				buttons.add(new GuiButtonManualNavigation(gui, x+100, y, 10, 16, 1,
						btn -> example = (example+1)%exampleItems.length
				));
			}
		}
		else
			exampleItems = null;

		this.name = TextUtils.applyFormat(shaderItem.getHoverName(), ChatFormatting.BOLD);
		MutableComponent textAssembly = new TextComponent("");
		textAssembly.append(TextUtils.applyFormat(new TranslatableComponent("desc.immersiveengineering.info.shader.level"), ChatFormatting.BOLD));
		textAssembly.append(new TranslatableComponent("desc.immersiveengineering.info.shader.rarity."+shader.rarity.name().toLowerCase(Locale.US)));
		if(unlocked)
		{
			String set = shader.info_set==null||shader.info_set.isEmpty()?null: ManualUtils.attemptStringTranslation(Lib.DESC_INFO+"shader.set.%s", shader.info_set);
			String reference = shader.info_reference==null||shader.info_reference.isEmpty()?null: ManualUtils.attemptStringTranslation(Lib.DESC_INFO+"shader.reference.%s", shader.info_reference);
			String details = shader.info_details==null||shader.info_details.isEmpty()?null: ManualUtils.attemptStringTranslation(Lib.DESC_INFO+"shader.details.%s", shader.info_details);

			if(set!=null)
				textAssembly.append("\n")
						.append(TextUtils.applyFormat(new TranslatableComponent("desc.immersiveengineering.info.shader.set"), ChatFormatting.BOLD))
						.append(" "+set);
			if(reference!=null)
				textAssembly.append("\n")
						.append(TextUtils.applyFormat(new TranslatableComponent("desc.immersiveengineering.info.shader.reference"), ChatFormatting.BOLD))
						.append("\n"+reference);
			if(details!=null)
				textAssembly.append("\n")
						.append(TextUtils.applyFormat(new TranslatableComponent("desc.immersiveengineering.info.shader.details"), ChatFormatting.BOLD))
						.append("\n"+details);

			String cost = Integer.toString(replicationCost.getCount());
			if(!IngredientUtils.hasPlayerIngredient(mc().player, replicationCost)&&!mc().player.abilities.instabuild)
				cost = ChatFormatting.RED+cost;
			buttons.add(new GuiButtonManual(gui, x+50, y+120, 70, 12,
					new TextComponent(I18n.get("ie.manual.entry.shaderList.order")+" "+cost+"x   ").withStyle(ChatFormatting.BOLD),
					btn -> {
						if(IngredientUtils.hasPlayerIngredient(mc().player, replicationCost)||mc().player.abilities.instabuild)
							ImmersiveEngineering.packetHandler.sendToServer(new MessageShaderManual(MessageType.SPAWN, shader.getName()));
						gui.fullInit();
					})
					.setTextColour(gui.getManual().getTextColour(), gui.getManual().getHighlightColour()));
		}
		else
		{
			textAssembly.append("\n\n").append(new TranslatableComponent("ie.manual.entry.shaderList.noInfo"));
			if(player.abilities.instabuild)
				buttons.add(new GuiButtonManual(gui, x+10, y+120, 100, 16,
						new TranslatableComponent("ie.manual.entry.shaderList.unlock"),
						btn -> {
							UUID playerId = mc().player.getUUID();
							ImmersiveEngineering.packetHandler.sendToServer(new MessageShaderManual(MessageType.UNLOCK, shader.getName()));
							ShaderRegistry.receivedShaders.put(playerId, shader.getName());
							gui.fullInit();
						})
						.setTextColour(gui.getManual().getTextColour(), gui.getManual().getHighlightColour())
				);
		}
		this.text = textAssembly;
	}

	@Override
	public void render(PoseStack transform, ManualScreen gui, int x, int y, int mouseX, int mouseY)
	{
		Lighting.turnBackOn();
		float scale = 2;
		transform.pushPose();
		transform.translate(x, y, 0);
		transform.scale(scale, scale, scale);
		boolean examples = exampleItems!=null&&exampleItems.length > 0;

		ManualUtils.renderItemStack(transform, shaderItem, (int)((10+(examples?0: 34))/scale), (int)((-8)/scale), false);
		if(examples&&example >= 0&&example < exampleItems.length)
			ManualUtils.renderItemStack(transform, exampleItems[example], (int)((63)/scale), (int)((-8)/scale), false);

		transform.scale(1/scale, 1/scale, 1/scale);
		if(unlocked)
			ManualUtils.renderItemStack(transform, replicationCost.getRandomizedExampleStack(mc().player.tickCount), 102, 118, false);

		Lighting.turnOff();

		int w = manual.fontRenderer().width(this.name.getString());
		drawWrappedWithTransform(transform, this.name, 60-w/2, 24);
		if(this.text!=null&&!this.text.getString().isEmpty())
			drawWrappedWithTransform(transform, this.text, 0, 38);

		transform.popPose();
	}

	private void drawWrappedWithTransform(
			PoseStack transform, FormattedText text, int x, int y
	)
	{
		for(FormattedCharSequence line : manual.fontRenderer().split(text, 120))
		{
			manual.fontRenderer().draw(transform, line, (float)x, (float)y, manual.getTextColour());
			y += manual.fontRenderer().lineHeight;
		}
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		return false;
	}

	@Override
	public void recalculateCraftingRecipes()
	{
	}
}
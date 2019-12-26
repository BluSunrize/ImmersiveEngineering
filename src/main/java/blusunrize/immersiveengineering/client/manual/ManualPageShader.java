/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.manual;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.common.network.MessageShaderManual;
import blusunrize.immersiveengineering.common.network.MessageShaderManual.MessageType;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.SpecialManualElements;
import blusunrize.lib.manual.gui.GuiButtonManual;
import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import blusunrize.lib.manual.gui.ManualScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static blusunrize.lib.manual.ManualUtils.mc;

/**
 * @author BluSunrize - 11.11.2016
 */
//TODO fix and clean up. Only changed to compile
public class ManualPageShader extends SpecialManualElements
{
	final ShaderRegistry.ShaderRegistryEntry shader;

	ItemStack shaderItem;
	ItemStack[] exampleItems;
	IngredientStack replicationCost;
	int example = 0;
	boolean unlocked;

	String text, localizedText;

	public ManualPageShader(ManualInstance manual, ShaderRegistry.ShaderRegistryEntry shader)
	{
		super(manual);
		this.shader = shader;
	}

	@Override
	public int getPixelsTaken()
	{
		return 0;//TODO
	}

	@Override
	public void onOpened(ManualScreen gui, int x, int y, List<Button> buttons)
	{
		PlayerEntity player = mc().player;
		UUID uuid = player.getUniqueID();
		unlocked = ShaderRegistry.receivedShaders.get(uuid).contains(shader.getName());

		shaderItem = new ItemStack(ShaderRegistry.itemShader);
		shaderItem.getOrCreateTag().putString("shader_name", shader.getName().toString());
		replicationCost = shader.replicationCost;

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

		this.text = "§lRarity: §r"+" "+shader.rarity.name();
		if(unlocked)
		{
			String set = shader.info_set==null||shader.info_set.isEmpty()?null: ManualUtils.attemptStringTranslation(Lib.DESC_INFO+"shader.set.%s", shader.info_set);
			String reference = shader.info_reference==null||shader.info_reference.isEmpty()?null: ManualUtils.attemptStringTranslation(Lib.DESC_INFO+"shader.reference.%s", shader.info_reference);
			String details = shader.info_details==null||shader.info_details.isEmpty()?null: ManualUtils.attemptStringTranslation(Lib.DESC_INFO+"shader.details.%s", shader.info_details);

			if(set!=null)
				this.text += "<br><br>"+I18n.format("desc.immersiveengineering.info.shader.set")+" "+set;
			if(reference!=null)
				this.text += "<br><br>"+I18n.format("desc.immersiveengineering.info.shader.reference")+"<br>"+reference;
			if(details!=null)
				this.text += "<br><br>"+I18n.format("desc.immersiveengineering.info.shader.details")+"<br>"+details;

			String cost = Integer.toString(replicationCost.inputSize);
			if(!ApiUtils.hasPlayerIngredient(mc().player, replicationCost)&&!mc().player.abilities.isCreativeMode)
				cost = TextFormatting.RED+cost;
			buttons.add(new GuiButtonManual(gui, x+50, y+138, 70, 12,
					TextFormatting.BOLD+I18n.format("ie.manual.entry.shaderList.order")+" "+cost+"x   ",
					btn -> {
						if(ApiUtils.hasPlayerIngredient(mc().player, replicationCost)||mc().player.abilities.isCreativeMode)
							ImmersiveEngineering.packetHandler.sendToServer(new MessageShaderManual(MessageType.SPAWN, shader.getName()));
						gui.fullInit();
					})
					.setTextColour(gui.getManual().getTextColour(), gui.getManual().getHighlightColour()));
		}
		else
		{
			this.text += "<br><br>"+I18n.format("ie.manual.entry.shaderList.noInfo");
			if(player.abilities.isCreativeMode)
				buttons.add(new GuiButtonManual(gui, x+10, y+80, 100, 16,
						I18n.format("ie.manual.entry.shaderList.unlock"),
						btn -> {
							UUID playerId = mc().player.getUniqueID();
							ImmersiveEngineering.packetHandler.sendToServer(new MessageShaderManual(MessageType.UNLOCK, shader.getName()));
							ShaderRegistry.receivedShaders.put(playerId, shader.getName());
							gui.fullInit();
						})
						.setTextColour(gui.getManual().getTextColour(), gui.getManual().getHighlightColour())
				);
		}
	}

	@Override
	public void render(ManualScreen gui, int x, int y, int mouseX, int mouseY)
	{
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.enableGUIStandardItemLighting();
		float scale = 2;
		GL11.glScalef(scale, scale, scale);
		boolean examples = exampleItems!=null&&exampleItems.length > 0;

		ManualUtils.renderItem().renderItemAndEffectIntoGUI(shaderItem, (int)((x+10+(examples?0: 34))/scale), (int)((y-8)/scale));
		if(examples&&example >= 0&&example < exampleItems.length)
			ManualUtils.renderItem().renderItemAndEffectIntoGUI(exampleItems[example], (int)((x+63)/scale), (int)((y-8)/scale));

		GL11.glScalef(1/scale, 1/scale, 1/scale);

		if(unlocked)
			ManualUtils.renderItem().renderItemAndEffectIntoGUI(replicationCost.getRandomizedExampleStack(mc().player.ticksExisted), x+102, y+136);

		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);

		String name = "§l"+shader.getName();
		int w = manual.fontRenderer().getStringWidth(name);
		manual.fontRenderer().drawString(name, x+60-w/2, y+24, manual.getTextColour());
		if(localizedText!=null&&!localizedText.isEmpty())
			manual.fontRenderer().drawSplitString(localizedText, x, y+38, 120, manual.getTextColour());

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
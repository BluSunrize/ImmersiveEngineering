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
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.utils.IngredientUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.network.MessageShaderManual;
import blusunrize.immersiveengineering.common.network.MessageShaderManual.MessageType;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.SpecialManualElements;
import blusunrize.lib.manual.gui.GuiButtonManual;
import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import blusunrize.lib.manual.gui.ManualScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

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

	String name, text;

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
		PlayerEntity player = mc().player;
		UUID uuid = player.getUniqueID();
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

		this.name = ClientUtils.applyFormat(shaderItem.getDisplayName(), TextFormatting.BOLD).getString();
		IFormattableTextComponent textAssembly = new StringTextComponent("");
		textAssembly.func_230529_a_(ClientUtils.applyFormat(new TranslationTextComponent("desc.immersiveengineering.info.shader.level"), TextFormatting.BOLD));
		textAssembly.func_230529_a_(new TranslationTextComponent("desc.immersiveengineering.info.shader.rarity."+shader.rarity.name().toLowerCase(Locale.US)));
		if(unlocked)
		{
			String set = shader.info_set==null||shader.info_set.isEmpty()?null: ManualUtils.attemptStringTranslation(Lib.DESC_INFO+"shader.set.%s", shader.info_set);
			String reference = shader.info_reference==null||shader.info_reference.isEmpty()?null: ManualUtils.attemptStringTranslation(Lib.DESC_INFO+"shader.reference.%s", shader.info_reference);
			String details = shader.info_details==null||shader.info_details.isEmpty()?null: ManualUtils.attemptStringTranslation(Lib.DESC_INFO+"shader.details.%s", shader.info_details);

			if(set!=null)
				textAssembly.func_240702_b_("\n")
						.func_230529_a_(ClientUtils.applyFormat(new TranslationTextComponent("desc.immersiveengineering.info.shader.set"), TextFormatting.BOLD))
						.func_240702_b_(" "+set);
			if(reference!=null)
				textAssembly.func_240702_b_("\n")
						.func_230529_a_(ClientUtils.applyFormat(new TranslationTextComponent("desc.immersiveengineering.info.shader.reference"), TextFormatting.BOLD))
						.func_240702_b_("\n"+reference);
			if(details!=null)
				textAssembly.func_240702_b_("\n")
						.func_230529_a_(ClientUtils.applyFormat(new TranslationTextComponent("desc.immersiveengineering.info.shader.details"), TextFormatting.BOLD))
						.func_240702_b_("\n"+details);

			String cost = Integer.toString(replicationCost.getCount());
			if(!IngredientUtils.hasPlayerIngredient(mc().player, replicationCost)&&!mc().player.abilities.isCreativeMode)
				cost = TextFormatting.RED+cost;
			buttons.add(new GuiButtonManual(gui, x+50, y+120, 70, 12,
					new StringTextComponent(I18n.format("ie.manual.entry.shaderList.order")+" "+cost+"x   ").func_240699_a_(TextFormatting.BOLD),
					btn -> {
						if(IngredientUtils.hasPlayerIngredient(mc().player, replicationCost)||mc().player.abilities.isCreativeMode)
							ImmersiveEngineering.packetHandler.sendToServer(new MessageShaderManual(MessageType.SPAWN, shader.getName()));
						gui.fullInit();
					})
					.setTextColour(gui.getManual().getTextColour(), gui.getManual().getHighlightColour()));
		}
		else
		{
			textAssembly.func_240702_b_("\n\n").func_230529_a_(new TranslationTextComponent("ie.manual.entry.shaderList.noInfo"));
			if(player.abilities.isCreativeMode)
				buttons.add(new GuiButtonManual(gui, x+10, y+120, 100, 16,
						new TranslationTextComponent("ie.manual.entry.shaderList.unlock"),
						btn -> {
							UUID playerId = mc().player.getUniqueID();
							ImmersiveEngineering.packetHandler.sendToServer(new MessageShaderManual(MessageType.UNLOCK, shader.getName()));
							ShaderRegistry.receivedShaders.put(playerId, shader.getName());
							gui.fullInit();
						})
						.setTextColour(gui.getManual().getTextColour(), gui.getManual().getHighlightColour())
				);
		}
		this.text = textAssembly.getString();
	}

	@Override
	public void render(MatrixStack transform, ManualScreen gui, int x, int y, int mouseX, int mouseY)
	{
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.enableStandardItemLighting();
		float scale = 2;
		GL11.glScalef(scale, scale, scale);
		boolean examples = exampleItems!=null&&exampleItems.length > 0;

		ManualUtils.renderItem().renderItemAndEffectIntoGUI(shaderItem, (int)((x+10+(examples?0: 34))/scale), (int)((y-8)/scale));
		if(examples&&example >= 0&&example < exampleItems.length)
			ManualUtils.renderItem().renderItemAndEffectIntoGUI(exampleItems[example], (int)((x+63)/scale), (int)((y-8)/scale));

		GL11.glScalef(1/scale, 1/scale, 1/scale);

		if(unlocked)
			ManualUtils.renderItem().renderItemAndEffectIntoGUI(replicationCost.getRandomizedExampleStack(mc().player.ticksExisted), x+102, y+118);

		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);

		int w = manual.fontRenderer().getStringWidth(this.name);
		manual.fontRenderer().drawString(transform, this.name, x+60-w/2, y+24, manual.getTextColour());
		if(this.text!=null&&!this.text.isEmpty())
			manual.fontRenderer().func_238418_a_(ITextComponent.func_241827_a_(this.text), x, y+38, 120, manual.getTextColour());

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
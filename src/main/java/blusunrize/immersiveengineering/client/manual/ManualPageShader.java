package blusunrize.immersiveengineering.client.manual;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualPages;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import blusunrize.lib.manual.gui.GuiManual;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.List;

/**
 * @author BluSunrize - 11.11.2016
 */
public class ManualPageShader extends ManualPages
{
	final ShaderRegistry.ShaderRegistryEntry shader;

	ItemStack shaderItem;
	ItemStack[] exampleItems;
	int example = 0;

	public ManualPageShader(ManualInstance manual, ShaderRegistry.ShaderRegistryEntry shader)
	{
		super(manual, "");
		this.shader = shader;
	}

	@Override
	public void initPage(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
	{
		shaderItem = new ItemStack(ShaderRegistry.itemShader);
		shaderItem.setTagCompound(new NBTTagCompound());
		shaderItem.getTagCompound().setString("shader_name", shader.getName());

		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		for(ItemStack is : ShaderRegistry.itemExamples)
			if(is!=null && is.hasCapability(CapabilityShader.SHADER_CAPABILITY,null))
			{
				ItemStack s = is.copy();
				ShaderWrapper wrapper = s.getCapability(CapabilityShader.SHADER_CAPABILITY,null);
				if(wrapper!=null)
				{
					wrapper.setShaderItem(shaderItem);
					list.add(s);
				}
			}
		exampleItems = list.toArray(new ItemStack[list.size()]);
		example = 0;
		if(exampleItems.length>1)
		{
			pageButtons.add(new GuiButtonManualNavigation(gui, 100, x+50, y+11, 10,16, 0));
			pageButtons.add(new GuiButtonManualNavigation(gui, 101, x+100, y+11, 10,16, 1));
		}

		String set = shader.info_set==null||shader.info_set.isEmpty()?null: ManualUtils.attemptStringTranslation(Lib.DESC_INFO+"shader.set.%s", shader.info_set);
		String reference = shader.info_reference==null||shader.info_reference.isEmpty()?null: ManualUtils.attemptStringTranslation(Lib.DESC_INFO+"shader.reference.%s", shader.info_reference);
		String details = shader.info_details==null||shader.info_details.isEmpty()?null: ManualUtils.attemptStringTranslation(Lib.DESC_INFO+"shader.details.%s", shader.info_details);
		this.text = "§lRarity: §r"+" "+shader.rarity.rarityName;
//		I18n.format("desc.immersiveengineering.info.shader.rarity")
		if(set!=null)
			this.text += "<br><br>" + I18n.format("desc.immersiveengineering.info.shader.set")+" "+set;
		if(reference!=null)
			this.text += "<br><br>" + I18n.format("desc.immersiveengineering.info.shader.reference")+"<br>"+reference;
		if(details!=null)
			this.text += "<br><br>" + I18n.format("desc.immersiveengineering.info.shader.details")+"<br>"+details;
		super.initPage(gui, x, y, pageButtons);
	}

	@Override
	public void renderPage(GuiManual gui, int x, int y, int mx, int my)
	{
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.enableGUIStandardItemLighting();
		float scale = 2;
		GL11.glScalef(scale,scale,scale);
		ManualUtils.renderItem().renderItemAndEffectIntoGUI(shaderItem, (int)((x+10)/scale),(int)((y+3)/scale));
		if(example>=0&&example<exampleItems.length)
			ManualUtils.renderItem().renderItemAndEffectIntoGUI(exampleItems[example], (int)((x+63)/scale),(int)((y+3)/scale));
		GL11.glScalef(1/scale,1/scale,1/scale);
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);

		manual.fontRenderer.setUnicodeFlag(true);
		String name = "§l"+shader.getName();
		int w = manual.fontRenderer.getStringWidth(name);
		manual.fontRenderer.drawString(name,x+60-w/2,y+35,manual.getTextColour(),false);
		if(localizedText != null && !localizedText.isEmpty())
			manual.fontRenderer.drawSplitString(localizedText, x, y+50, 120, manual.getTextColour());
	}

	@Override
	public void mouseDragged(int x, int y, int clickX, int clickY, int mx, int my, int lastX, int lastY, int button)
	{
	}

	@Override
	public void buttonPressed(GuiManual gui, GuiButton button)
	{
		if(button.id==100)
			example = (example-1)%exampleItems.length;
		else if(button.id==101)
			example = (example+1)%exampleItems.length;
		super.buttonPressed(gui, button);
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		return false;
	}
}
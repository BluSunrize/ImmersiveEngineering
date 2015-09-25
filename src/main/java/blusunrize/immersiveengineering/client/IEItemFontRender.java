package blusunrize.immersiveengineering.client;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;

public class IEItemFontRender extends FontRenderer {

	public IEItemFontRender(GameSettings gameSettings, ResourceLocation resource, TextureManager textureManager, boolean b)
	{
		super(gameSettings, resource, textureManager, b);
	}

}
